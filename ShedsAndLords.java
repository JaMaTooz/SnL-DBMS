package matuszak;

import javafx.application.Application;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.geometry.*;
import javafx.event.*;
import java.sql.*;

public class ShedsAndLords extends Application {
	
	// Makes primaryStage easily accessed through object
	private Stage activeStage;
	
	// Stores information about user to prevent excess database searches
	private String userID;
	private boolean userAdmin;
	
	// Stores the ideal sized of the stages
	private final int stageWidth = 600;
	private final int stageHeight = 600;
	
	// Images of logos to make creating them easier and faster
	private Image logo;
	private StackPane picPane;
	
	// Main method is only used when starting through eclipse, should be kept minimal
	public static void main(String[] args) {
		Application.launch(args);
	}
	
	// This class launches into the startup stage, and sets defaults for primaryStage
	@Override
	public void start(Stage primaryStage) {
		logo = new Image("file:images/logo.png");
		picPane = new StackPane();
		picPane.getChildren().add(new ImageView(logo));
		activeStage = primaryStage;
		activeStage.setResizable(false);
		loginStage();
	}
	
	// This is the default stage where user will login
	public void loginStage() {
		
		// Reset User Credentials
		userID = null;
		userAdmin = false;
		
		// Create the FlowPane for entering the user ID
		GridPane entryPane = new GridPane();
		entryPane.setHgap(10);
		entryPane.setVgap(10);
		entryPane.setAlignment(Pos.CENTER);
		TextField idEntry = new TextField();
		Button enter = new Button("Enter");
		Button newUser = new Button("Create New User");
		entryPane.add(new Label("Please enter your User ID"), 0, 0);
		entryPane.add(idEntry, 0, 1);
		entryPane.add(enter, 0, 2);
		entryPane.setHalignment(enter, HPos.CENTER);
		entryPane.setHalignment(newUser, HPos.CENTER);
		
		EventHandler<ActionEvent> login = e -> {
			boolean exists = attemptLogin(idEntry.getText());
			if (!exists) {
				entryPane.add(newUser, 0, 3);
			}
		};
		
		// Creates the button pressed event
		enter.setOnAction(login);
		idEntry.setOnAction(enter.getOnAction());
		newUser.setOnAction(e -> addUser(idEntry.getText()));
		
		// Creates a BorderPane to hold the 2 previous panes
		BorderPane pane = new BorderPane();
		pane.setPadding(new Insets(20, 20, 20, 20));
		pane.setTop(picPane);
		pane.setCenter(entryPane);
		
		// Ties together the panes and shows scene
		Scene scene = new Scene(pane, stageWidth, stageHeight);
		activeStage.setTitle("Sheds & Lords Database");
		activeStage.setScene(scene);
		activeStage.show();
	}
	
	private void addUser(String input) {
		Database db = new Database();
		try {
			ResultSet rs = db.query("select * from User where uID = '" + input + "';");
			if (rs.next()) {
				userID = rs.getString(1);
				userAdmin = rs.getBoolean(2);
				selectStage();
			} else {
				db.exSQL("insert into User (uID, uAdmin) values ('" + input + "', false);");
				userID = input;
				userAdmin = false;
				selectStage();
			}
		} catch (SQLException ex) {
			System.out.println(ex);
		} finally {
			db.closeDB();
		}
	}
	
	// This method will check the input string at the login stage for a user ID
	private boolean attemptLogin(String input) {
		Database db = new Database();
		userID = input;
		userAdmin = true;
		// Search DB for ID, set userAdmin
		// SELECT admin FROM Users;
		// If null then createNewUser(userID) else userAdmin = return
		ResultSet rs = db.query("select * from User where uID = '" + input + "';");
		try {
			if (rs.next()) {
				userID = rs.getString(1);
				userAdmin = rs.getBoolean(2);
				selectStage();
			} else if (userID == null) {
				return false;
			}
		} catch (SQLException ex) {
			System.out.print(ex);
		} finally {
			db.closeDB();
		}
		return false;
	}
	
	// This is the stage after login, where user can select from different option
	public void selectStage() {
		activeStage.setTitle("Sheds & Lords Database - Logged in as User: " + userID);
		
		// Create GridPane
		GridPane selectPane = new GridPane();
		selectPane.setHgap(20);
		selectPane.setAlignment(Pos.CENTER);
		selectPane.add(new Label("Welcome User " + userID), 1, 0);
		selectPane.add(new Label(""), 1, 1);
		Button modifyButton = new Button("Modify Database");
		Button searchButton = new Button("Search Database");
		Button myButton = new Button("My Books");
		
		// If user has admin access then show modify button in addition to others
		if (userAdmin) {
			selectPane.add(new Label("Modify the Database"), 0, 2);
			selectPane.add(modifyButton, 0, 3);
			selectPane.add(new Label("Search the Database"), 1, 2);
			selectPane.add(searchButton, 1, 3);
		} else {
			selectPane.add(new Label("Search the Database"), 0, 2);
			selectPane.add(searchButton, 0, 3);
		}
		selectPane.add(new Label("View your Books"), 2, 2);
		selectPane.add(myButton, 2, 3);
		
		// Stack pane to let user logout
		StackPane exitPane = new StackPane();
		Button exitButton = new Button("Logout");
		exitPane.getChildren().add(exitButton);
		
		modifyButton.setOnAction(e -> modifyStage());
		searchButton.setOnAction(e -> searchStage());
		myButton.setOnAction(e -> myBooks());
		exitButton.setOnAction(e -> loginStage());
		
		// Combine panes together
		BorderPane pane = new BorderPane();
		pane.setPadding(new Insets(20, 20, 20, 20));
		pane.setTop(picPane);
		pane.setCenter(selectPane);
		pane.setBottom(exitPane);
		
		// Show this scene
		Scene scene = new Scene(pane, stageWidth, stageHeight);
		activeStage.setScene(scene);
	}
	
	// This is the stage where the user can search the database, and add titles to their collection
	public void searchStage() {
		GridPane searchPane = new GridPane();
		searchPane.setAlignment(Pos.CENTER);
		searchPane.setHgap(10.0);
		searchPane.setVgap(10.0);
		

		// Pane for search elements - creates empty pane to fill space
		Pane enterPane = new HBox(20);
		enterPane.setPadding(new Insets(10, 10, 10, 10));

		enterPane.getChildren().clear();
		ComboBox<String> attributes = new ComboBox();
		attributes.getItems().addAll("Title", "ISBN", "Author", "Publisher");
		TextField aValue = new TextField();
		Button search = new Button("Search");

		EventHandler<ActionEvent> searchSubmit = s -> {

			// Create sql query for search in Book
			SearchResult sr = new SearchResult(userID);
			sr.initiallize(attributes.getValue(), aValue.getText());
			System.out.println(attributes.getValue() + " " + aValue.getText());

			// Create new window from search result
			Stage resultStage = new Stage();
			resultStage.setScene(sr.getScene());
			resultStage.setTitle("Search Results");
			if (sr.isEmpty()) {
				resultStage.setWidth(300);
				resultStage.setHeight(100);
			} else {
				resultStage.setWidth(stageWidth+60);
				resultStage.setHeight(stageHeight);
			}
			resultStage.show();
		};

		search.setOnAction(searchSubmit);

		enterPane.getChildren().addAll(attributes, new Label("="), aValue, search);

		Pane tableSearch = new HBox(10);
		tableSearch.getChildren().addAll(new Label("Search for a Book"));
		tableSearch.setPadding(new Insets(10, 10, 10, 10));
		
		searchPane.add(tableSearch, 0, 0);
		searchPane.add(enterPane, 0, 1);
		
		StackPane exitPane = new StackPane();
		Button exit = new Button("Back");
		exit.setOnAction(e -> selectStage());
		exitPane.getChildren().add(exit);
		
		BorderPane pane = new BorderPane();
		pane.setPadding(new Insets(20, 20, 20, 20));
		pane.setTop(picPane);
		pane.setCenter(searchPane);
		pane.setBottom(exitPane);
		
		
		Scene scene = new Scene(pane, stageWidth, stageHeight);
		activeStage.setScene(scene);
	}
	
	// This is the stage where admin users can modify the database
	public void modifyStage() {
		activeStage.setTitle("Sheds & Lords Database - Logged in as User: " + userID);
		
		// Create GridPane
		GridPane selectPane = new GridPane();
		selectPane.setHgap(20);
		selectPane.setAlignment(Pos.CENTER);
		selectPane.add(new Label("Welcome Admin User " + userID), 1, 0);
		selectPane.add(new Label(""), 1, 1);
		Button updateButton = new Button("Update Tuples");
		Button insertButton = new Button("Insert Tuples");
		Button deleteButton = new Button("Delete Tuples");
		
		selectPane.add(new Label("Update tuples in tables"), 0, 2);
		selectPane.add(updateButton, 0, 3);
		selectPane.add(new Label("Insert into the tables"), 1, 2);
		selectPane.add(insertButton, 1, 3);
		selectPane.add(new Label("Delete from tables"), 2, 2);
		selectPane.add(deleteButton, 2, 3);
		
		// Stack pane to let user logout
		StackPane exitPane = new StackPane();
		Button exitButton = new Button("Back");
		exitPane.getChildren().add(exitButton);
		
		updateButton.setOnAction(e -> updateStage());
		insertButton.setOnAction(e -> insertStage());
		deleteButton.setOnAction(e -> deleteStage());
		exitButton.setOnAction(e -> selectStage());
		
		// Combine panes together
		BorderPane pane = new BorderPane();
		pane.setPadding(new Insets(20, 20, 20, 20));
		pane.setTop(picPane);
		pane.setCenter(selectPane);
		pane.setBottom(exitPane);
		
		// Show this scene
		Scene scene = new Scene(pane, stageWidth, stageHeight);
		activeStage.setScene(scene);
	}
	
	// This stage lets an admin user insert into tables
	public void insertStage() {
		GridPane searchPane = new GridPane();
		searchPane.setAlignment(Pos.CENTER);
		searchPane.setHgap(10.0);
		searchPane.setVgap(10.0);
		
		ComboBox<String> table = new ComboBox();
		table.getItems().addAll("Book", "Author", "Publisher");
		table.setPromptText("Table to insert into");
		searchPane.add(table, 1, 0);
		
		EventHandler<ActionEvent> tableSelect = t -> {

			Pane tableDelete = new HBox(10);
			Pane enterPane = new HBox(20);
			Button insert = new Button("Insert");
			
			// If user selects book table to delete from
			if (table.getValue().matches("Book")) {
				tableDelete.getChildren().clear();
				enterPane.getChildren().clear();
				// Pane for search elements - creates empty pane to fill space
				enterPane.setPadding(new Insets(10, 10, 10, 10));
				TextField isbnValue = new TextField();
				isbnValue.setPromptText("EX: 123-4567890123");
				TextField titleValue = new TextField();
				TextField authorValue = new TextField();
				TextField publisherValue = new TextField();

				EventHandler<ActionEvent> searchSubmit = s -> {
					Database db = new Database();
					String isbn = isbnValue.getText();
					String title = titleValue.getText();
					String author = authorValue.getText();
					String publisher = authorValue.getText();
					ResultSet rs = db.query("select ISBN from Book where ISBN like '" + isbn + "';");
					try {
						if (rs.next() && !isbn.matches("")) {
							StackPane pane = new StackPane();
							pane.getChildren().add(new Label("Book titled " + title + " already exists."));
							Scene scene = new Scene(pane);
							Stage resultStage = new Stage();
							resultStage.setScene(scene);
							resultStage.setTitle("Insertion Results");
							resultStage.setWidth(300);
							resultStage.setHeight(100);
							resultStage.show();
						} else {
							db.exSQL("insert into Book(ISBN, title) values('" + isbn + "', '" + title + "');");
							db.exSQL("insert into Wrote(ISBN, aName) values('" + isbn + "', '" + author + "');");
							db.exSQL("update Author set titleCount = titleCount + 1 where aName like '" + author + "';");
							db.exSQL("insert into Published(ISBN, pName) values('" + isbn + "', '" + publisher + "');");
							db.exSQL("update Publisher set pCount = pCount + 1 where pName like '" + publisher + "';");
							
							StackPane pane = new StackPane();
							pane.getChildren().add(new Label("Book added."));
							Scene scene = new Scene(pane);
							Stage resultStage = new Stage();
							resultStage.setScene(scene);
							resultStage.setTitle("Insertion Results");
							resultStage.setWidth(300);
							resultStage.setHeight(100);
							resultStage.show();
						}
					} catch (SQLException ex) {
						System.out.println(ex);
					} finally {
						db.closeDB();
					}
				};

				insert.setOnAction(searchSubmit);
				HBox titleBox = new HBox();
				titleBox.setPadding(new Insets(10, 10, 10, 10));
				titleBox.getChildren().addAll(new Label("    Title ="), titleValue);
				HBox isbnBox = new HBox();
				isbnBox.setPadding(new Insets(10, 10, 10, 10));
				isbnBox.getChildren().addAll(new Label("     ISBN ="), isbnValue);
				HBox authorBox = new HBox();
				authorBox.setPadding(new Insets(10, 10, 10, 10));
				authorBox.getChildren().addAll(new Label("   Author ="), authorValue);
				HBox publisherBox = new HBox();
				publisherBox.setPadding(new Insets(10, 10, 10, 10));
				publisherBox.getChildren().addAll(new Label("Publisher ="), publisherValue);
				
				VBox allBox = new VBox();
				allBox.setPadding(new Insets(10, 10, 10, 10));
				allBox.getChildren().addAll(titleBox, isbnBox, authorBox, publisherBox);
				enterPane.getChildren().addAll(allBox, insert);
				
			} else if (table.getValue().matches("Author")) {
				tableDelete.getChildren().clear();
				enterPane.getChildren().clear();
				TextField aValue = new TextField();
				Button search = new Button("Insert");

				EventHandler<ActionEvent> searchSubmit = s -> {
					Database db = new Database();
					String author = aValue.getText();
					ResultSet rs = db.query("select aName from Author where aName like '" + author + "';");
					try {
						if (rs.next() && !author.matches("")) {
							StackPane pane = new StackPane();
							pane.getChildren().add(new Label("Author " + author + " already exists."));
							Scene scene = new Scene(pane);
							Stage resultStage = new Stage();
							resultStage.setScene(scene);
							resultStage.setTitle("Insertion Results");
							resultStage.setWidth(300);
							resultStage.setHeight(100);
							resultStage.show();
						} else {
							db.exSQL("insert into Author(aName, titleCount) values('" + author + "', 0);");
							StackPane pane = new StackPane();
							pane.getChildren().add(new Label("Author " + author + " added."));
							Scene scene = new Scene(pane);
							Stage resultStage = new Stage();
							resultStage.setScene(scene);
							resultStage.setTitle("Insertion Results");
							resultStage.setWidth(300);
							resultStage.setHeight(100);
							resultStage.show();
						}
					} catch (SQLException ex) {
						System.out.println(ex);
					} finally {
						db.closeDB();
					}
				};

				search.setOnAction(searchSubmit);

				enterPane.getChildren().addAll(new Label("Author Name"), new Label("="), aValue, search);
				
			} else if (table.getValue().matches("Publisher")) {
				tableDelete.getChildren().clear();
				enterPane.getChildren().clear();
				TextField aValue = new TextField();
				Button search = new Button("Insert");

				EventHandler<ActionEvent> searchSubmit = s -> {
					Database db = new Database();
					String publisher = aValue.getText();
					ResultSet rs = db.query("select pName from Publisher where pName like '" + publisher + "';");
					try {
						if (rs.next() && !publisher.matches("")) {
							StackPane pane = new StackPane();
							pane.getChildren().add(new Label("Publisher " + publisher + " already exists."));
							Scene scene = new Scene(pane);
							Stage resultStage = new Stage();
							resultStage.setScene(scene);
							resultStage.setTitle("Insertion Results");
							resultStage.setWidth(300);
							resultStage.setHeight(100);
							resultStage.show();
						} else {
							db.exSQL("insert into Publisher(pName, pCount) values('" + publisher + "', 0);");
							StackPane pane = new StackPane();
							pane.getChildren().add(new Label("Publisher " + publisher + " added."));
							Scene scene = new Scene(pane);
							Stage resultStage = new Stage();
							resultStage.setScene(scene);
							resultStage.setTitle("Insertion Results");
							resultStage.setWidth(300);
							resultStage.setHeight(100);
							resultStage.show();
						}
					} catch (SQLException ex) {
						System.out.println(ex);
					} finally {
						db.closeDB();
					}
				};

				search.setOnAction(searchSubmit);

				enterPane.getChildren().addAll(new Label("Publisher Name"), new Label("="), aValue, search);
				
			}
			
			tableDelete.setPadding(new Insets(10, 10, 10, 10));			
			searchPane.add(tableDelete, 1, 0);
			searchPane.add(enterPane, 1, 1);

		};
		table.setOnAction(tableSelect);
		
		StackPane exitPane = new StackPane();
		Button exit = new Button("Back");
		exit.setOnAction(e -> modifyStage());
		exitPane.getChildren().add(exit);
		
		BorderPane pane = new BorderPane();
		pane.setPadding(new Insets(20, 20, 20, 20));
		pane.setTop(picPane);
		pane.setCenter(searchPane);
		pane.setBottom(exitPane);
		
		Scene scene = new Scene(pane, stageWidth, stageHeight);
		activeStage.setScene(scene);
	}
	
	// This stage lets an admin user delete tuples
	public void deleteStage() {
		GridPane searchPane = new GridPane();
		searchPane.setAlignment(Pos.CENTER);
		searchPane.setHgap(10.0);
		searchPane.setVgap(10.0);
		
		ComboBox<String> table = new ComboBox();
		table.getItems().addAll("Book", "Author", "Publisher");
		table.setPromptText("Table to delete from");
		searchPane.add(table, 1, 0);
		
		EventHandler<ActionEvent> tableSelect = t -> {

			Pane tableDelete = new HBox(10);
			Pane enterPane = new HBox(20);
			
			// If user selects book table to delete from
			if (table.getValue().matches("Book")) {
				tableDelete.getChildren().clear();
				enterPane.getChildren().clear();
				// Pane for search elements - creates empty pane to fill space
				enterPane.setPadding(new Insets(10, 10, 10, 10));
				ComboBox<String> attributes = new ComboBox();
				attributes.getItems().addAll("Title", "ISBN");
				TextField aValue = new TextField();
				Button search = new Button("Delete");

				EventHandler<ActionEvent> searchSubmit = s -> {
					Database db = new Database();
					String isbn = "";
					if (attributes.getValue().matches("Title")) {
						ResultSet rs = db.query("select ISBN from Book where title like '" + aValue.getText() + "';");
						try {
							rs.next();
							isbn = rs.getString(1);
						} catch (SQLException ex) {
							System.out.println(ex);
						}
					} else {
						isbn = aValue.getText();
					}
					
					ResultSet rs = db.query("select ISBN from Book where ISBN like '" + isbn + "';");
					try {
						if (rs.next() && !isbn.matches("")) {
							db.exSQL("delete from Wrote where ISBN like '" + isbn + "';");
							db.exSQL("delete from Published where ISBN like '" + isbn + "';");
							db.exSQL("delete from Owns where ISBN like '" + isbn + "';");
							db.exSQL("delete from Book where ISBN like '" + isbn + "';");
							StackPane pane = new StackPane();
							pane.getChildren().add(new Label("Deletion Succesful."));
							Scene scene = new Scene(pane);
							Stage resultStage = new Stage();
							resultStage.setScene(scene);
							resultStage.setTitle("Deletion Results");
							resultStage.setWidth(300);
							resultStage.setHeight(100);
							resultStage.show();
						} else {
							StackPane pane = new StackPane();
							pane.getChildren().add(new Label("No Book found with that " + attributes.getValue() + "."));
							Scene scene = new Scene(pane);
							Stage resultStage = new Stage();
							resultStage.setScene(scene);
							resultStage.setTitle("Deletion Results");
							resultStage.setWidth(300);
							resultStage.setHeight(100);
							resultStage.show();
						}
					} catch (SQLException ex) {
						System.out.println(ex);
					} finally {
						db.closeDB();
					}
				};

				search.setOnAction(searchSubmit);

				enterPane.getChildren().addAll(attributes, new Label("="), aValue, search);
				
			} else if (table.getValue().matches("Author")) {
				tableDelete.getChildren().clear();
				enterPane.getChildren().clear();
				TextField aValue = new TextField();
				Button search = new Button("Delete");

				EventHandler<ActionEvent> searchSubmit = s -> {
					Database db = new Database();
					String author = aValue.getText();
					ResultSet rs = db.query("select aName from Author where aName like '" + author + "';");
					try {
						if (rs.next() && !author.matches("")) {
							db.exSQL("delete from Wrote where aName like '" + author + "';");
							db.exSQL("delete from Author where aName like '" + author + "';");
							StackPane pane = new StackPane();
							pane.getChildren().add(new Label("Deletion Succesful."));
							Scene scene = new Scene(pane);
							Stage resultStage = new Stage();
							resultStage.setScene(scene);
							resultStage.setTitle("Deletion Results");
							resultStage.setWidth(300);
							resultStage.setHeight(100);
							resultStage.show();
						} else {
							StackPane pane = new StackPane();
							pane.getChildren().add(new Label("No Author found with the name " + author + "."));
							Scene scene = new Scene(pane);
							Stage resultStage = new Stage();
							resultStage.setScene(scene);
							resultStage.setTitle("Deletion Results");
							resultStage.setWidth(300);
							resultStage.setHeight(100);
							resultStage.show();
						}
					} catch (SQLException ex) {
						System.out.println(ex);
					} finally {
						db.closeDB();
					}
				};

				search.setOnAction(searchSubmit);

				enterPane.getChildren().addAll(new Label("Author Name"), new Label("="), aValue, search);
				
			} else if (table.getValue().matches("Publisher")) {
				tableDelete.getChildren().clear();
				enterPane.getChildren().clear();
				TextField aValue = new TextField();
				Button search = new Button("Delete");

				EventHandler<ActionEvent> searchSubmit = s -> {
					Database db = new Database();
					String publisher = aValue.getText();
					ResultSet rs = db.query("select aName from Publisher where pName like '" + publisher + "';");
					try {
						if (rs.next() && !publisher.matches("")) {
							db.exSQL("delete from Published where pName like '" + publisher + "';");
							db.exSQL("delete from Publisher where pName like '" + publisher + "';");
							StackPane pane = new StackPane();
							pane.getChildren().add(new Label("Deletion Succesful."));
							Scene scene = new Scene(pane);
							Stage resultStage = new Stage();
							resultStage.setScene(scene);
							resultStage.setTitle("Deletion Results");
							resultStage.setWidth(300);
							resultStage.setHeight(100);
							resultStage.show();
						} else {
							StackPane pane = new StackPane();
							pane.getChildren().add(new Label("No Publisher found with the name " + publisher + "."));
							Scene scene = new Scene(pane);
							Stage resultStage = new Stage();
							resultStage.setScene(scene);
							resultStage.setTitle("Deletion Results");
							resultStage.setWidth(300);
							resultStage.setHeight(100);
							resultStage.show();
						}
					} catch (SQLException ex) {
						System.out.println(ex);
					} finally {
						db.closeDB();
					}
				};

				search.setOnAction(searchSubmit);

				enterPane.getChildren().addAll(new Label("Publisher Name"), new Label("="), aValue, search);
				
			}
			
			tableDelete.setPadding(new Insets(10, 10, 10, 10));			
			searchPane.add(tableDelete, 1, 0);
			searchPane.add(enterPane, 1, 1);

		};
		table.setOnAction(tableSelect);
		
		StackPane exitPane = new StackPane();
		Button exit = new Button("Back");
		exit.setOnAction(e -> modifyStage());
		exitPane.getChildren().add(exit);
		
		BorderPane pane = new BorderPane();
		pane.setPadding(new Insets(20, 20, 20, 20));
		pane.setTop(picPane);
		pane.setCenter(searchPane);
		pane.setBottom(exitPane);
		
		
		Scene scene = new Scene(pane, stageWidth, stageHeight);
		activeStage.setScene(scene);
	}
	
	// This stage lets an admin user update tables
	public void updateStage() {
		GridPane searchPane = new GridPane();
		searchPane.setAlignment(Pos.CENTER);
		searchPane.setHgap(10.0);
		searchPane.setVgap(10.0);
		

		// Pane for search elements - creates empty pane to fill space
		Pane enterPane = new HBox(20);
		enterPane.setPadding(new Insets(10, 10, 10, 10));
		Pane isbnPane = new HBox(20);
		isbnPane.setPadding(new Insets(10, 10, 10, 10));

		enterPane.getChildren().clear();
		Label attributes = new Label("Title");
		TextField aValue = new TextField();
		TextField isbnValue = new TextField();
		Button submit = new Button("Submit");

		EventHandler<ActionEvent> updateSubmit = s -> {
			String isbn = isbnValue.getText();
			Database db = new Database();
			ResultSet rs;
			try {
				rs = db.query("select ISBN from Book where ISBN like '" + isbn + "';");
				if (rs.next()) {
					db.exSQL("update Book set title = '" + aValue.getText() + "' where ISBN like '" + isbn + "';");
					Stage resultStage = new Stage();
					StackPane pane = new StackPane();
					pane.getChildren().add(new Label("Title changed to " + aValue.getText() + "."));
					Scene scene = new Scene(pane);
					resultStage.setScene(scene);
					resultStage.setTitle("Update Results");
					resultStage.setWidth(300);
					resultStage.setHeight(100);
					resultStage.show();
				} else {
					Stage resultStage = new Stage();
					StackPane pane = new StackPane();
					pane.getChildren().add(new Label("Book with ISBN " + isbn + " was not found."));
					Scene scene = new Scene(pane);
					resultStage.setScene(scene);
					resultStage.setTitle("Update Results");
					resultStage.setWidth(300);
					resultStage.setHeight(100);
					resultStage.show();
				}
			} catch (SQLException ex) {
				System.out.println(ex);
			} finally {
				db.closeDB();
			}
		};

		submit.setOnAction(updateSubmit);

		enterPane.getChildren().addAll(attributes, new Label("="), aValue, submit);
		isbnPane.getChildren().addAll(new Label("ISBN = "), isbnValue);

		Pane tableSearch = new HBox(10);
		tableSearch.getChildren().addAll(new Label("Update values for Book"));
		tableSearch.setPadding(new Insets(10, 10, 10, 10));
		
		searchPane.add(tableSearch, 0, 0);
		searchPane.add(enterPane, 0, 2);
		searchPane.add(isbnPane, 0, 1);
		
		StackPane exitPane = new StackPane();
		Button exit = new Button("Back");
		exit.setOnAction(e -> modifyStage());
		exitPane.getChildren().add(exit);
		
		BorderPane pane = new BorderPane();
		pane.setPadding(new Insets(20, 20, 20, 20));
		pane.setTop(picPane);
		pane.setCenter(searchPane);
		pane.setBottom(exitPane);
		
		
		Scene scene = new Scene(pane, stageWidth, stageHeight);
		activeStage.setScene(scene);
	}
	
	// This is the stage where the user can view their own books easily
	public void myBooks() {
		MyBooks mb = new MyBooks(userID);
		mb.initiallize();

		// Create new window from MyBooks
		Stage resultStage = new Stage();
		resultStage.setScene(mb.getScene());
		resultStage.setTitle("My Books");
		if (mb.isEmpty()) {
			resultStage.setWidth(300);
			resultStage.setHeight(100);
		} else {
			resultStage.setWidth(stageWidth+60);
			resultStage.setHeight(stageHeight);
		}
		resultStage.show();
	}
}
