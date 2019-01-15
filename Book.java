package matuszak;

import java.io.*;
import java.net.*;
import java.sql.*;

public class Book {
	
	// Split book based on input from http://pages.cs.wisc.edu/~anhai/data/784_data/books4/csv_files/barnes_and_noble.csv
	// Second URL http://pages.cs.wisc.edu/~anhai/data/784_data/books3/csv_files/barnes_and_noble.csv
	
	private Database db;
	
	private int totalLoaded;
	private ResultSet rs;
	
	private String ISBN;
	private String title;
	private String edition;
	private int length;
	private double price;
	private String language;
	private String author;
	private String publisher;
	
	private String sql;
	
	private int loop;
	
	// Default Constructor, just in case
	public Book() {
		db = new Database();
	}
	
	// Split for first URL
	public boolean split(String in, int x) {
		try {
			String[] splitIn = in.split(",");
			ISBN = splitIn[8];
			title = splitIn[1];
			
			// Edition not available from this dataset
			edition = null;
			
			// Approximately 8000 of the 10,000 entries had errors in the length column
			length = -1;
			
			// Makes sure the strings for numbers aren't empty and removes dollar signs
			if (!splitIn[3].isEmpty() && !splitIn[4].isEmpty()) {
				splitIn[3] = splitIn[3].substring(1);
				splitIn[4] = splitIn[4].substring(1);
				price = Math.max(Double.parseDouble(splitIn[3]), Double.parseDouble(splitIn[4]));
			} else if (!splitIn[3].isEmpty()) {
				splitIn[3] = splitIn[3].substring(1);
				price = Double.parseDouble(splitIn[3]);
			} else if (!splitIn[4].isEmpty()) {
				splitIn[4] = splitIn[4].substring(1);
				price = Double.parseDouble(splitIn[4]);
			} else {
				price = -1;
			}
			
			// Language not available from this dataset
			language = null;
			
			author = splitIn[2];
			publisher = splitIn[10];
			return true;
		} catch (Exception ex) {
			//System.out.println("Error split at input: " + x);
			//System.out.println(ex);
			return false;
		}
	}
	
	// Split for Second URL
	public boolean splitTwo(String in, int x) {
		try {
			String[] splitIn = in.split(",");
			ISBN = splitIn[4];
			title = splitIn[1];
			
			// Edition not available from this dataset
			edition = null;
			length = Integer.parseInt(splitIn[7]);
			
			// Makes sure the strings for numbers aren't empty and removes dollar signs
			if (!splitIn[2].isEmpty() && splitIn[2].contains("$")) {
				price = Double.parseDouble(splitIn[2].substring(1));
			} else {
				price = -1;
			}
			
			// Language not available from this dataset
			language = null;
			
			author = splitIn[3];
			publisher = splitIn[5];
			return true;
		} catch (Exception ex) {
			//System.out.println("Error split at input: " + x);
			//System.out.println(ex);
			return false;
		}
	}
	
	public void readURL() {
		try {
			for (loop = 0; loop <= 1; loop++) {
				URL url = new URL("http://pages.cs.wisc.edu/~anhai/data/784_data/books4/csv_files/barnes_and_noble.csv");
				if (loop == 1) {
					url = new URL("http://pages.cs.wisc.edu/~anhai/data/784_data/books3/csv_files/barnes_and_noble.csv");
					System.out.println("Second URL");
				}
				BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

				String line;
				int x = -1;
				totalLoaded = 0;
				while ((line = in.readLine()) != null) {
					if (x >= 0) {
						if (loop == 0) {
							if (!split(line, x)) {
								x++;
								continue;
							}
						} else {
							if (!splitTwo(line, x)) {
								x++;
								continue;
							}
						}
						totalLoaded++;
						if (!upload()) {
							System.out.println("Error at entry " + x + " in URL " + loop);
						}
					}
					x++;
				}
				System.out.println(x);
				System.out.println(totalLoaded);
				in.close();
			}
			db.closeDB();
		} catch (MalformedURLException ex) {
			System.out.println(ex);
		} catch (IOException ex) {
			System.out.println(ex);
		}
	}
	
	// Uploads values from split into tables Book, Author, Wrote, Publisher, Published
	public boolean upload() {
		// Check if ISBN is already in tables
		sql = "select ISBN from Book where ISBN = '" + ISBN + "';";
		rs = db.query(sql);
		try {
			// If ISBN exists
			if (rs.next()) {
				System.out.println("Book " + title + " already entered.");
				return true;
			} else {
				// Create new book if ISBN does not exist already
				sql = "insert into Book (ISBN, title, edition, length, price, language) values ('" + ISBN
						+ "', '" + title + "', '" + edition + "', " + length + ", " + price + ", '" + language + "');";
				db.exSQL(sql);
				
				// Check if author exists
				sql = "select aName from Author where aName = '" + author + "';";
				rs = db.query(sql);
				if (rs.next()) {
					sql = "update Author set titleCount = titleCount + 1 where aName = '" + author + "';";
					db.exSQL(sql);
				} else {
					sql = "insert into Author (aName, titleCount) values ('" + author + "', 1);";
					db.exSQL(sql);
				}
				// Add to wrote
				sql = "insert into Wrote (aName, ISBN) values ('" + author + "', '" + ISBN + "');";
				db.exSQL(sql);
				
				// Check if publisher exists
				sql = "select pName from Publisher where pName = '" + publisher + "';";
				rs = db.query(sql);
				if (rs.next()) {
					sql = "update Publisher set pCount = pCount + 1 where pName = '" + publisher + "';";
					db.exSQL(sql);
				} else {
					sql = "insert into Publisher (pName, pCount) values ('" + publisher + "', 1);";
					db.exSQL(sql);
				}
				// Add to published
				sql = "insert into Published (pName, ISBN) values ('" + publisher + "', '" + ISBN + "');";
				db.exSQL(sql);
			}
		} catch (SQLException ex) {
			System.out.println(ex);
			return false;
		}
		return true;
	}
}