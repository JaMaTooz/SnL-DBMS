package matuszak;

import java.sql.*;
import javafx.beans.property.*;

public class ResultTuple {

	private SimpleStringProperty ISBN;
	private SimpleStringProperty title;
	private SimpleStringProperty author;
	private SimpleStringProperty publisher;
	private boolean add;
	
	public ResultTuple(String ISBN, String title, String author, String publisher) {
		this.ISBN = new SimpleStringProperty(ISBN);
		this.title = new SimpleStringProperty(title);
		this.author = new SimpleStringProperty(author);
		this.publisher = new SimpleStringProperty(publisher);
		add = false;
	}
	
	public String getISBN() {
		return ISBN.get();
	}
	
	public void setISBN(String ISBN) {
		this.ISBN.set(ISBN);
	}
	
	public String getTitle() {
		return title.get();
	}
	
	public void setTitle(String title) {
		this.title.set(title);
	}
	
	public String getAuthor() {
		return author.get();
	}
	
	public void setAuthor(String author) {
		this.author.set(author);
	}
	
	public String getPublisher() {
		return publisher.get();
	}
	
	public void setPublisher(String publisher) {
		this.publisher.set(publisher);
	}
	
	public boolean getAdd() {
		return add;
	}
	
	public void setAdd(boolean in) {
		add = in;
	}
}
