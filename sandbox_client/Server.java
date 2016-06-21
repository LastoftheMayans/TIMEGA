package sandbox_client;

/**
 * Models the server connection. Handles all server I/O.
 */

import server.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Server implements Runnable {
	
	private Socket _socket;
	private PrintWriter _out;
	private BufferedReader _in;
	
	private Client _client;
	
	public Server(Socket socket, Client client, ControlsTab tab, String name) {
		_client = client;

		// set up a connection
		try {
			_out = new PrintWriter(socket.getOutputStream(), true);
			_in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			// initialize handshake
			_out.write(Protocol.HELLO);
			_out.flush();
			
			if(_in.read() == Protocol.WELCOME) {
				// if handshake successful, try to connect
				this.sendName(name);
				Database.name(name);
			}
						
		} catch (IOException e) {
			// e.printStackTrace();
			_client.disconnection();
		}
		
	}
	
	// disconnect the server
	public void cleanup() {
		try {
			if(_socket != null)
				_socket.close();
			_out.close();
			_in.close();
			_client.disconnection();
		} catch (IOException e) {
			// e.printStackTrace();
		}
	}

	// loop to handle tcp input
	@Override
	public void run() {
		while(true) {
			try {
				
				int command = _in.read();
				if(command == -1) {
					break;
				}
				
				this.handle(command);
				
			} catch(IOException e) {
				e.printStackTrace();
				break;
			}
		}
		
		this.cleanup();
	}
	
	// read and respond to a message
	private synchronized void handle(int message) throws IOException {
		
		if(message == Protocol.NEW_PLAYER) {
			
			Player p = new Player();
			
			p.name = _in.readLine();
			p.race = _in.readLine();
			p.red = Integer.parseInt(_in.readLine());
			p.green = Integer.parseInt(_in.readLine());
			p.blue = Integer.parseInt(_in.readLine());			
			
			Database.addPlayer(p);
			
		} else if(message == Protocol.MAP) {
			String name = _in.readLine();
			_client.nameMap(name);
			int length = _in.read();
			if (length == 0) {
				return;
			}
			
			int[] mapdata = new int[length];
			for(int i=0; i<length; i++) {
				mapdata[i] = _in.read();
			}
			_client.writeMap(mapdata);
			
			// sets up the last stuff that needs to be done
			_client.databaseFinished();

		} else if(message == Protocol.EN_MAP) {
			_client.setEnabledMap(true);
		} else if(message == Protocol.DIS_MAP) {
			_client.setEnabledMap(false);
		} else if(message == Protocol.EN_PLANETS) {
			_client.setEnabledPlanets(true);
		} else if(message == Protocol.DIS_PLANETS) {
			_client.setEnabledPlanets(false);
		} else if(message == Protocol.EN_RESEARCH) {
			_client.setEnabledResearch(true);
		} else if(message == Protocol.DIS_RESEARCH) {
			_client.setEnabledResearch(false);
		} else if(message == Protocol.EN_PERSONNEL) {
			_client.setEnabledPersonnel(true);
		} else if(message == Protocol.DIS_PERSONNEL) {
			_client.setEnabledPersonnel(false);
		} else if(message == Protocol.EN_EMPIRE) {
			_client.setEnabledEmpire(true);
		} else if(message == Protocol.DIS_EMPIRE) {
			_client.setEnabledEmpire(false);
		} else if(message == Protocol.EN_STATUS) {
			_client.setEnabledStatus(true);
		} else if(message == Protocol.DIS_STATUS) {
			_client.setEnabledStatus(false);
		} else if(message == Protocol.EN_COUNCIL) {
			_client.setEnabledCouncil(true);
		} else if(message == Protocol.DIS_COUNCIL) {
			_client.setEnabledCouncil(false);
		} else if(message == Protocol.EN_COMBAT) {
			_client.setEnabledCombat(true);
		} else if(message == Protocol.DIS_COMBAT) {
			_client.setEnabledCombat(false);
		} else if(message == Protocol.PLANET_CHOWN) {
			String planetName = _in.readLine();
			String newOwner = _in.readLine();
			String oldOwner = Database.ownerOf(planetName);
			Database.updatePlanet(planetName, newOwner);
			_client.notifyChown(planetName, newOwner, oldOwner);

		} else if(message == Protocol.NEW_SDOCK) {
			String planetName = _in.readLine();
			Database.updateSD(planetName, true);
			_client.notifySD(planetName, true);

		} else if(message == Protocol.REMOVE_SDOCK) {
			String planetName = _in.readLine();
			Database.updateSD(planetName, false);
			_client.notifySD(planetName, false);

		} else if(message == Protocol.END_ROUND) {
			_client.endRoundStart();
			
			for(String tech : Database.getTechQueue()) {
				this.write(Protocol.SEND_TECH, _client.getName() + "\n" + tech);
			}
			
			for(String person : Database.getPersonnelQueue()) {
				if(Database.hasPerson(_client.getName(), person)) {
					this.write(Protocol.REMOVE_PERSON, _client.getName() + "\n" + person);
				} else {
					this.write(Protocol.SEND_PERSON, _client.getName() + "\n" + person);
				}
			}
			
			this.write(Protocol.END_ROUND);
			
			if(Database.isAdvancing()) {
				this.write(Protocol.ADVANCE, _client.getName() + "\n" + Integer.toString(_client.getColor()));
			}
						
			Database.clearTechQueue();
			Database.clearPersonnelQueue();
			Database.setAdvancing(false);
			
		} else if(message == Protocol.SEND_TECH) {
			String player = _in.readLine();
			String tech = _in.readLine();
			
			Database.research(player, tech);
			
			_client.research(player, tech);
			
		} else if(message == Protocol.REMOVE_TECH) {
			String player = _in.readLine();
			String tech = _in.readLine();
			
			Database.forget(player, tech);
			
			_client.forget(player, tech);
			
		} else if(message == Protocol.SEND_PERSON) {
			String player = _in.readLine();
			String person = _in.readLine();
			
			Database.hire(player, person);
			
			_client.hire(player, person);
			
		} else if(message == Protocol.REMOVE_PERSON) {
			String player = _in.readLine();
			String person = _in.readLine();
			
			Database.release(player, person);
			
			_client.release(player, person);
		}
		
		else if(message == Protocol.ADVANCE) {
			String player = _in.readLine();
			String color = _in.readLine();
			
			Database.advancePlayer(player, color);
			_client.advancePlayer(player);
			
		} else if(message == Protocol.ROUND_OK) {
			_client.endRoundFinish();
		} else if(message == Protocol.SEND_RESOLUTION) {
			String resolution1 = _in.readLine();
			String resolution2 = _in.readLine();
			
			_client.resolution(resolution1, resolution2);
		}
	}
	
	public synchronized void sendName(String name) {
		_out.write(Protocol.NAME);
		_out.write(name);
		_out.write("\n");
		_out.flush();
		
		int message = 0;
		try {
			message = _in.read();
		} catch (IOException e) {
			_client.disconnection();
		}
		
		if(message == Protocol.VALID) {
			_client.validName(true);
			// request map
			_out.write(Protocol.MAP);
			_out.flush();
			Thread t = new Thread(this);
			t.setDaemon(true);
			t.start();
			return;
		} else if(message == Protocol.INVALID) {
			_client.validName(false);
			return;
		}
		
		_client.disconnection();
		
	}
	
	public synchronized void write(int protocol, String text) {
		_out.write(protocol);
		_out.write(text + "\n");
		_out.flush();
	}
	
	public synchronized void write(int protocol) {
		_out.write(protocol);
		_out.flush();
	}
	

}
