import java.util.ArrayList;
import java.util.LinkedList;

public class Student {
	private LinkedList<Book> inventory;
	private String name;
	
	public synchronized void addBook(Book b){
		inventory.add(b);
	}
	
	public synchronized void removeBook(Book b){
		inventory.remove(b);
	}

	public synchronized ArrayList<String> list(){
		if (inventory.isEmpty()) return null;
		ArrayList<String> tmp = new ArrayList<String>(inventory.size());
		for ( Book b : inventory){
			tmp.add(String.format(b.getId()+" "+b.getName()));
		}
		return tmp;
	}
}
