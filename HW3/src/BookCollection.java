import java.util.concurrent.atomic.AtomicInteger;

public class BookCollection {
	public String name;
	private AtomicInteger remaining;
	private Book[] collection;
	
	public BookCollection(String s, int c){
		remaining=new AtomicInteger(c);
		name=s;
		collection = new Book[c];
		for (int i=0; i<c; i++){
			collection[i]= new Book(s);
		}
	}
	
	public Book checkoutBook(int id){
		if (remaining.get() == 0) return null;
		Book tmp=null;
		int i =0;
		while (tmp == null && i < collection.length){
			if ( !collection[i].getStatus()){
				tmp = collection[i];
				tmp.setAndGetId(id);
				tmp.setStatus(true);
				remaining.decrementAndGet();
			}
			else i++;
		}
		return tmp;
		};
	
	public String returnBook(int id){
		for (int i=0; i < collection.length; i++){
			if ( id == collection[i].getId()){
				remaining.incrementAndGet();
				collection[i].setAndGetId(0);
				return collection[i].getOwner();
			}
		}
		return null;
	};
	
	@Override
    public String toString() {
        return String.format(name + " " +remaining.get());
    }
	
	
	
	
}
