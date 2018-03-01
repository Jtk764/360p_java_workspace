// thread safe book class

public class Book {
	private int id;
	private String name;
	private boolean isCheckedOut;
	private String owner;
	
	public Book(String s){
		name=s;
	};
	
	
	public synchronized int setAndGetId (int c){
		id=c;
		return id;
		}
	
	public synchronized int getId (){
		return id;
		}
	
	
	public synchronized String setName (String s){
		name=s;
		return name;
		}
	
	public synchronized String getName (){
		return name;
		}
	
	public synchronized boolean setStatus (boolean b){
		isCheckedOut=b;
		return isCheckedOut;
		}
	
	public synchronized boolean getStatus (){
		return isCheckedOut;
		}
	
	
	public synchronized String setOwner (String s){
		owner=s;
		return name;
		}
	
	public synchronized String getOwner (){
		return owner;
		}
	
	
}
