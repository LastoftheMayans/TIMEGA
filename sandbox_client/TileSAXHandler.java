package sandbox_client;

/**
 * Reads information from the planets.xml file and sends the information back to the tile class.
 * 
 * @author dmayans
 */

import java.util.LinkedList;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class TileSAXHandler extends DefaultHandler {
	
	private String _string;
	private LinkedList<Tile> _tiles;
	private Tile _currTile;
	private int _currPlanetIndex;
	
	@Override
	public void startDocument() {
		_string = "";
		_tiles = new LinkedList<Tile>();
		_currTile = new Tile();
		_currTile.setTitle("Erase");
		_tiles.addFirst(_currTile);
		_currPlanetIndex = -1;
	}
	
	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
		if(qName.equals("tile")) {
			_currTile = new Tile();
			_currTile.setPath(new String(atts.getValue("path")));
			_currPlanetIndex = -1;
		} else if(qName.equals("planet")) {
			_currTile.addPlanet();
			_currPlanetIndex++;
		} else if(qName.equals("red")) {
			_currTile.addTechSpec(_currPlanetIndex, Database.RED);
		} else if(qName.equals("blue")) {
			_currTile.addTechSpec(_currPlanetIndex, Database.BLUE);
		} else if(qName.equals("green")) {
			_currTile.addTechSpec(_currPlanetIndex, Database.GREEN);
		} else if(qName.equals("yellow")) {
			_currTile.addTechSpec(_currPlanetIndex, Database.YELLOW);
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) {
		if(qName.equals("title")) {
			_currTile.setTitle(new String(_string));
		} else if(qName.equals("name")) {
			_currTile.setPlanetName(_currPlanetIndex, new String(_string));
		} else if(qName.equals("resources")) {
			_currTile.setPlanetResources(_currPlanetIndex, new String(_string));
		} else if(qName.equals("influence")) {
			_currTile.setPlanetInfluence(_currPlanetIndex, new String(_string));
		} else if(qName.equals("tile")) {
			_tiles.addLast(_currTile);
		} 
		_string = "";
	}
	
	@Override
	public void characters(char[] ch, int start, int length) {
		_string += new String(ch, start, length).trim();
	}
	
	@Override
	public void endDocument() {
		Tile.placeTiles(_tiles);
	}
	
}
