package server;

/**
 * Not encapsulated. Grab the information you need from the public information, but make sure the locks are held.
 * 
 * @author dmayans
 */

import sandbox_client.Client;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServerDatabase {
		
	// Holds the array of all 60 tiles
	public static final ArrayList<Tile> TILES = new ArrayList<Tile>();
	
	// Maps planet names to their owners and space dock status
	public static final HashMap<String,String> PLANETS = new HashMap<String,String>();
	public static final Lock PLANETS_LOCK = new ReentrantLock();

	public static final HashMap<String,Boolean> SPACEDOCKS = new HashMap<String,Boolean>();
	public static final Lock SPACEDOCKS_LOCK = new ReentrantLock();
	
	public static Player[] PLAYERS;

	public static final HashMap<String,HashSet<String>> TECH = new HashMap<String,HashSet<String>>();
	public static final Lock TECH_LOCK = new ReentrantLock();
	
	public static final HashMap<String,HashSet<String>> PERSONNEL = new HashMap<String,HashSet<String>>();
	public static final Lock PERSONNEL_LOCK = new ReentrantLock();
	
	public static final HashMap<String,Integer> EMPIRE_STAGE = new HashMap<String,Integer>();
	public static final Lock EMPIRE_LOCK = new ReentrantLock();

	public static final HashMap<String,String> PAST_RESOLUTION = new HashMap<String,String>();
	public static final Lock RESOLUTION_LOCK = new ReentrantLock();
	
	
	public static final HashSet<String> TECH_SET = new HashSet<String>();
	public static final HashSet<String> PERSONNEL_SET = new HashSet<String>();
	public static final HashSet<String> RESOLUTION_SET = new HashSet<String>();

	//string = player, integer = for/against numbers, array = resolution
	@SuppressWarnings("unchecked")
	public static final HashMap<String,Integer[]> VOTES[] = new HashMap[]{new HashMap<String, Integer[]>(),new HashMap<String, Integer[]>()};
	public static final Lock VOTES_LOCK = new ReentrantLock();

	public static final HashMap<String, Integer> TOTAL_VOTES = new HashMap<String, Integer>();
	public static final Lock TOTAL_VOTES_LOCK = new ReentrantLock();

	public static final String[] CURRENT_RESOLUTIONS = {"",""};
	public static final Lock CURRENT_RESOLUTIONS_LOCK = new ReentrantLock();

	public static final HashMap<String, Integer[]> VOTES_BY_RESOLUTION = new HashMap<String, Integer[]>();
	public static final Lock VOTES_BY_RESOLUTION_LOCK = new ReentrantLock();


	public static final HashMap<String,String> TABS = new HashMap<String,String>();

	public static boolean hasName(String name) {
		for(Player n : ServerDatabase.PLAYERS) {
			if(name.equals(n.name)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static void initialize(List<Player> players) {
		PLAYERS = new Player[players.size()];
		int i=0;
		for(Player p : players) {
			PLAYERS[i++] = p;
			TECH.put(p.name, new HashSet<String>());
			PERSONNEL.put(p.name, new HashSet<String>());
			EMPIRE_STAGE.put(p.name, 0);
			TOTAL_VOTES.put(p.name,0);
			for(int k=0; k<2; k++){
				VOTES[k].put(p.name,new Integer[]{0,0});
			}
		}

		for(String tab : Client.TAB_NAMES) {
			if(tab.equals("Home")) continue;
			TABS.put(tab.toLowerCase(), tab);
		}
		
		// Since Mallice and Creuss don't actually exist on the map, we kind of have to add them in by hard code :(
		PLANETS.put("Mallice", "none");
		SPACEDOCKS.put("Mallice", false);
		// PLANETS.put("Creuss", "none");
		// SPACEDOCKS.put("Creuss", true);

	}
	
	public static final int CODE_TECH = 0;
	public static final int CODE_PERSONNEL = 1;
	public static final int CODE_RESOLUTIONS = 2;
	
	public static void placeNames(Collection<String> names, int code) {
		HashSet<String> set;
		if(code == CODE_TECH) {
			set = TECH_SET;
		} else if(code == CODE_PERSONNEL) {
			set = PERSONNEL_SET;
		} else if(code == CODE_RESOLUTIONS) {
			set = RESOLUTION_SET;
		} else {
			return;
		}
		
		for(String s : names) {
			set.add(s);
		}
	}	
	
}
