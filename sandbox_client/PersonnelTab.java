package sandbox_client;

/**
 * Tab that tracks and updates personnel hiring by local player
 */

import java.util.HashMap;

import server.Player;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class PersonnelTab {
	
	private static int PANE_WIDTH = 140;
	private static int RECT_SIZE = 9;
	
	private Client _client;
	private int _index;
	
	protected final Tab _root = new Tab("Personnel");
	
	private GridPane _pane = new GridPane();
	private PersonnelButton _selected;
	private Button _hire;
	
	private static String[] RED = {"Fleet Control", "Moneylender", "<blank>", "Marauder",
		"Cultist", "<blank>", "Usurper", "Advisor", "<blank>", "Conqueror"};
	private static String[] BLUE = {"Astronomer", "placeholder_b2", "<blank>", "Cartographer",
		"Scavenger", "<blank>", "Admiral", "Technician", "<blank>", "Champion"};
	private static String[] GREEN = {"Envoy", "Salvager", "<blank", "Engineer",
		"Mechanic", "<blank>", "Chancellor", "Explorer", "<blank>", "Tactician"};
	
	// {red, blue, green, yellow}
	private static String[] BRIGHT = {"#C80000", "#000096", "#006400"};
	private static String[] GRAYED = {"#aaa", "#aaa", "#aaa"};
		
	private HashMap<String, PersonnelButton> _buttons;
	
	private Text _redTier;
	private Text _blueTier;
	private Text _greenTier;
	
	private Text _personnelTitle;
	private Label _personnelDescription;
	private PersonnelLight[] _boxes;
	private Text _tier;
	private HBox _hbox;

	
	public PersonnelTab(Client client) {
		_client = client;
		
		_root.setClosable(false);
		_root.setContent(_pane);
		_pane.setHgap(110);
		_pane.setVgap(12);
		_pane.setPadding(new Insets(30, 100, 20, 100));
		
		_pane.add(this.createTitlePane("Red"), 0, 0);
		_redTier = new Text("tier: 0");
		_pane.add(this.createTierPane(_redTier), 0, 1);
		
		_pane.add(this.createTitlePane("Blue"), 1, 0);
		_blueTier = new Text("tier: 0");
		_pane.add(this.createTierPane(_blueTier), 1, 1);
		
		_pane.add(this.createTitlePane("Green"), 2, 0);
		_greenTier = new Text("tier: 0");
		_pane.add(this.createTierPane(_greenTier), 2, 1);
				
		_buttons = new HashMap<String, PersonnelButton>();
		PersonnelButton b;
		
		for(int i=0; i<10; i++) {
			if(i == 2 || i == 5 || i == 8) {
				_pane.add(new Pane(), 0, i+2, 3, 1);
				
			} else {
				b = new PersonnelButton(RED[i], 0);
				_pane.add(b, 0, i+2);
				_buttons.put(RED[i], b);
				
				b = new PersonnelButton(BLUE[i], 1);
				_pane.add(b, 1, i+2);
				_buttons.put(BLUE[i], b);
				
				b = new PersonnelButton(GREEN[i], 2);
				_pane.add(b, 2, i+2);
				_buttons.put(GREEN[i], b);
			}
			
		}
				
		VBox v = new VBox(10);
		
		_hbox = new HBox(10);
		_personnelTitle = new Text();
		GridPane.setMargin(v, new Insets(10, 25, 25, 25));
		_personnelTitle.setStyle("-fx-font-weight:bold;-fx-font-size: 14px");
		_tier = new Text();
		_hbox.getChildren().addAll(_personnelTitle, _tier);
				
		_hire = new Button("Hire");
		_hire.setVisible(false);
		_hire.setOnAction(new HireHandler(_hire));
		_hire.setPrefWidth(100);
		
		_personnelDescription = new Label();
		_personnelDescription.setWrapText(true);
		_personnelDescription.setMaxWidth(600);
		v.getChildren().addAll(_hbox, _hire, _personnelDescription);
		
				
		_pane.add(v, 0, 12, 3, 1);
	}
	
	public void initialize() {
		_boxes = new PersonnelLight[Database.numPlayers()];
		for(int i=0; i<_boxes.length; i++) {
			Player p = Database.getPlayer(i);
			_boxes[i] = new PersonnelLight(Color.rgb(p.red, p.green, p.blue));
			_hbox.getChildren().add(_boxes[i]);
			_boxes[i].empty();
			_boxes[i].setVisible(false);
		}
		_index = Database.indexOf(_client.getName());
	}
	
	
	private Pane createTitlePane(String text) {
		Text t = new Text(text);
		t.setStyle("-fx-font-weight:bold;-fx-font-size: 14px");
		Pane output = new Pane();
		output.setMinWidth(PANE_WIDTH);
		output.setMaxWidth(PANE_WIDTH);
		output.getChildren().add(t);
		return output;
	}
	
	private Pane createTierPane(Text text) {
		Pane output = new Pane();
		output.setMinWidth(PANE_WIDTH-10);
		output.setMaxWidth(PANE_WIDTH-10);
		output.getChildren().add(text);
		GridPane.setMargin(output, new Insets(0, 0, 0, 10));
		return output;
	}
	
	public void updateSD() {
		_redTier.setText("tier: " + Database.personnelTier(_client.getName(), Database.RED));
		_blueTier.setText("tier: " + Database.personnelTier(_client.getName(), Database.BLUE));
		_greenTier.setText("tier: " + Database.personnelTier(_client.getName(), Database.GREEN));
	}
	
	public void hire(String player, String person) {
		if(player.equals(_client.getName())) {
			_buttons.get(person).brighten();
			if(_selected != null && _selected.getText().equals(person)) {
				_hire.setText("Release");
			}
		}
		
		if(_selected != null && _selected.getText().equals(person)) {
			this.paintPersonnelBoxes(person);
		}
		
	}
	
	public void release(String player, String person) {
		if(player.equals(_client.getName())) {
			_buttons.get(person).gray();
			if(_selected != null && _selected.getText().equals(person)) {
				_hire.setText("Hire");
			}
		}
		
		if(_selected != null && _selected.getText().equals(person)) {
			this.paintPersonnelBoxes(person);
		}
	}
	
	private void paintPersonnelBoxes(String person) {
		for(int i=0; i<_boxes.length; i++) {
			String player = Database.getPlayer(i).name;
			if(Database.hasPerson(player, person)) {
				_boxes[i].fill();
			} else {
				_boxes[i].empty();
			}
		}
	}
	
	private class PersonnelButton extends ToggleButton {
		
		private int _color;
		private String _name;
		private String[] _array;
		private String _status;
				
		public PersonnelButton(String text, int color) {
			super(text);
			_color = color;
			_name = text;
			_array = GRAYED;
			_status = "transparent";
			this.setOnAction(new PersonnelHandler(this));
			this.unhighlight();
			this.setWidth(PANE_WIDTH);
		}
				
		
		
		public void repaint() {
			this.setStyle("-fx-focus-color:transparent;-fx-background-color:" + _status + ";-fx-text-fill:" + _array[_color]);
		}
		
		public void highlight() {
			_status = "#ddd";
			this.repaint();
			_personnelTitle.setText(_name);
			_personnelDescription.setText(Database.descriptionOf(_name));
		}
		
		public void unhighlight() {
			_status = "transparent";
			this.repaint();
		}
		
		public void brighten() {
			_array = BRIGHT;
			this.repaint();
		}
		
		public void gray() {
			_array = GRAYED;
			this.repaint();
		}
				
	}
	
	private class PersonnelHandler implements EventHandler<ActionEvent> {

		private PersonnelButton _b;
		
		public PersonnelHandler(PersonnelButton b) {
			_b = b;
		}
		
		@Override
		public void handle(ActionEvent e) {
			if(_selected != null) {
				_selected.unhighlight();
			} else {
				_hire.setVisible(true);
				for(int i=0; i<_boxes.length; i++) {
					_boxes[i].setVisible(true);
				}
			}
			
			_selected = _b;
			_b.highlight();
			
			paintPersonnelBoxes(_b.getText());
			_tier.setText("(tier " + Database.tierOfPersonnel(_b.getText()) + ")");
			
			if(Database.localHasPerson(_client.getName(), _b.getText())) {
				_hire.setText("Release");
				_boxes[_index].fill();
			} else {
				_hire.setText("Hire");
				_boxes[_index].empty();
			}

			
		}
		
	}
	
	private class HireHandler implements EventHandler<ActionEvent> {
		
		private Button _b;
		
		public HireHandler(Button b) {
			_b = b;
		}

		@Override
		public void handle(ActionEvent e) {
			if(_b.getText().equals("Release")) {
				Database.localReleasePerson(_selected.getText());
				_b.setText("Hire");
				_buttons.get(_selected.getText()).gray();
				_boxes[_index].empty();
			} else {
				Database.localHirePerson(_selected.getText());
				_b.setText("Release");
				_buttons.get(_selected.getText()).brighten();
				_boxes[_index].fill();
			}
		}
		
	}
	
	
	private class PersonnelLight extends Rectangle {
		
		private Color _c;
		
		public PersonnelLight(Color c) {
			super(RECT_SIZE, RECT_SIZE);
			this._c = c;
			this.setStroke(c);
			HBox.setMargin(this, new Insets(4, 0, 0, 0));
		}
		
		public void fill() {
			this.setFill(_c);
		}
		
		public void empty() {
			this.setFill(Color.TRANSPARENT);
		}
		
	}
	
}