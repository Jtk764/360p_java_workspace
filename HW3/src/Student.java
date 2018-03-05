import java.util.ArrayList;
import java.util.LinkedList;

public class Student {
	private LinkedList<Book> inventory;
	public String name;
	
	public Student (String s){
		name=s;
		inventory = new LinkedList<Book>();
	}
	
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
	
	public synchronized Book getBook( int id){
		for (Book b : inventory){
			if (b.getId() == id){
				return b;
			}
		}
		return null;
	}
	
	public synchronized boolean isEmpty( ){
		return inventory.isEmpty();
	}
}
