import java.util.LinkedList;

public class Student {
	private LinkedList<Book> inventory;
	private String name;
	
	public synchronized void addBook(Book b){
		inventory.add(b);
	}

}
