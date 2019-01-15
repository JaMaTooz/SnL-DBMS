package matuszak;

import java.sql.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.util.Callback;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.*;
import javafx.scene.control.CheckBox;


// This class contains a pane and information about results from a search to be displayed
public class SearchResult {
	
	private ResultSet rs;
	private Scene scene;
	private boolean empty;
	private TableView<ResultTuple> table;
	private String sql;
	private Database db;
	private String uID;
	
	public SearchResult(String uID) {
		this.uID = uID;
	}
	
	public void initiallize(String in, String text) {
		if (in.matches("Title")) { sql = "select ISBN, title from Book where title like '" + text + "%';"; }
		else if (in.matches("Author")) { sql = "select b.ISBN, b.title from Book b, Wrote w where w.aName like '" + text + "%' and w.ISBN like b.ISBN;"; }
		else if (in.matches("ISBN")) { sql = "select ISBN, title from Book where ISBN like '" + text + "';"; }
		else if (in.matches("Publisher")) { sql = "select b.ISBN, b.title from Book b, Published p where p.pName like '" + text + "%' and p.ISBN like b.ISBN;"; }
		System.out.println(sql);
		db = new Database();
		try {
			rs = db.query(sql);
			if (!rs.next()) {
				empty = true;
			} else {
				empty = false;
				table = createTable(0);
			}
		} catch (SQLException ex) {
			System.out.println(ex);
		} finally {
			db.closeDB();
		}
	}
	
	private TableView<ResultTuple> createTable(int startIndex) {
		TableView<ResultTuple> tb = new TableView<ResultTuple>();
		tb.setMinHeight(500);
		TableColumn<ResultTuple, String> cOne = new TableColumn<ResultTuple, String>("Title");
		cOne.setMinWidth(250);
		cOne.setCellValueFactory(new PropertyValueFactory<ResultTuple, String>("title"));
		TableColumn<ResultTuple, String> cTwo = new TableColumn<ResultTuple, String>("ISBN");
		cTwo.setMinWidth(100);
		cTwo.setCellValueFactory(new PropertyValueFactory<ResultTuple, String>("ISBN"));
		TableColumn<ResultTuple, String> cThr = new TableColumn<ResultTuple, String>("Author");
		cThr.setMinWidth(115);
		cThr.setCellValueFactory(new PropertyValueFactory<ResultTuple, String>("author"));
		TableColumn<ResultTuple, String> cFou = new TableColumn<ResultTuple, String>("Publisher");
		cFou.setMinWidth(115);
		cFou.setCellValueFactory(new PropertyValueFactory<ResultTuple, String>("publisher"));
		tb.getColumns().addAll(cOne, cTwo, cThr, cFou);
		try {
			rs.beforeFirst();
			ObservableList<ResultTuple> ol = FXCollections.observableArrayList();
			while (rs.next()) {
				ol.add(add());
			}
			tb.setItems(ol);
			
			TableColumn editCol = new TableColumn("Add");
			editCol.setStyle("-fx-alignment: CENTER;");
            
            // Create a new cellFactory to allow buttons to be created in a column
            Callback<TableColumn<ResultTuple, String>, TableCell<ResultTuple, String>> cellFactory = new Callback<TableColumn<ResultTuple, String>, TableCell<ResultTuple, String>>() {
            	
            	// Call tells column what should be placed in the column, in this case just a button
            	@Override
            	public TableCell<ResultTuple, String> call(final TableColumn param) {
            		final TableCell<ResultTuple, String> cell = new TableCell<ResultTuple, String>() {
            			private final CheckBox check = new CheckBox();
            			{
            				// Creates an action to open a new instance of the BowlerController with inserted Values
            				EventHandler<ActionEvent> select = e -> {
            					ResultTuple tempID = getTableView().getItems().get(getIndex());
            					tempID.setAdd(!tempID.getAdd());
            				};
            				check.setOnAction(select);
            			}
            			
            			@Override
            			public void updateItem(String item, boolean empty) {
            				super.updateItem(item, empty);
            				if (empty) {
            					setGraphic(null);
            				} else {
            					setGraphic(check);
            				}
            			}
            		};
            		return cell;
            	};
            };
            
            editCol.setCellFactory(cellFactory);
            editCol.setMinWidth(60);
            editCol.setMaxWidth(60);
            
            tb.getColumns().add(editCol);
            
		} catch (SQLException ex) {
			System.out.println(ex);
		}
		return tb;
	}
	
	// Adds entry at current result set index to the table;
	public ResultTuple add() {
		ResultTuple rt = new ResultTuple("", "", "", "");
		try {
			rt.setISBN(rs.getString(1));
			rt.setTitle(rs.getString(2));
			ResultSet rOne = db.query("select aName from Wrote where ISBN = '" + rs.getString(1) + "';");
			rOne.next();
			rt.setAuthor(rOne.getString(1));
			ResultSet rTwo = db.query("select pName from Published where ISBN = '" + rs.getString(1) + "';");
			rTwo.next();
			rt.setPublisher(rTwo.getString(1));
		} catch (SQLException ex) {
			System.out.println(ex);
		}
		return rt;
	}
	
	public Scene getScene() {
		if (empty) {
			StackPane pane = new StackPane();
			pane.getChildren().add(new Label("No Results Found."));
			scene = new Scene(pane);
		} else {
			VBox pane = new VBox();
			pane.setAlignment(Pos.TOP_CENTER);
			Button addAll = new Button("Add selected to personal collection");
			
			// When button is pressed search table for all checked tuples
			EventHandler<ActionEvent> add = a -> {
				int count = 0;
				for (int x = 0; x < table.getItems().size(); x++) {
					if (table.getItems().get(x).getAdd()) {
						addMyBooks(table.getItems().get(x));
						count++;
					}
				}
				StackPane donePane = new StackPane(new Label("Added all " + count + " selected books."));
				Scene doneScene = new Scene(donePane);
				Stage doneStage = new Stage();
				doneStage.setScene(doneScene);
				doneStage.setTitle("Search Results");
				doneStage.setWidth(300);
				doneStage.setHeight(100);
				doneStage.show();
				
				Stage stage = (Stage) addAll.getScene().getWindow();
				stage.close();
			};
			addAll.setOnAction(add);
			
			pane.getChildren().addAll(table, addAll);
			scene = new Scene(pane);
		}
		return scene;
	}
	
	private void addMyBooks(ResultTuple rt) {
		if (uID != null && rt.getISBN() != null) {
			db = new Database();
			ResultSet own = db.query("select * from Owns where uID = '" + uID + "' and ISBN = '" + rt.getISBN() + "';");
			try {
				if (!own.next()) {
					sql = "insert into Owns(uID, ISBN) values ('" + uID + "', '" + rt.getISBN() + "');";
					db.exSQL(sql);
				} else {
					System.out.println("User already owns book " + rt.getISBN());
				}
			} catch (SQLException ex) {
				System.out.println(ex);
			} finally {
				db.closeDB();
			}
		} else {
			System.out.println("Did not add entry due to null values; User: " + uID + " ISB: " + rt.getISBN());
		}
	}
	
	public boolean isEmpty() {
		return empty;
	}
}
