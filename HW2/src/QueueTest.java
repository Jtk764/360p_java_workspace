
public class QueueTest implements Runnable{
	
		final PriorityQueue q;
		String name;
		int priority;
		public QueueTest(PriorityQueue queue, String name, int priority) {
			q = queue;
			this.name = name;
			this.priority = priority;
		}
		public void run() {
			q.add(name, priority);
			System.out.println("adding " + name + " at: " + q.search(name));
		}
	
	public static void main(String[] args) {
		PriorityQueue q = new PriorityQueue(6);
		q.add("test1", 8);
		q.add("test2", 8);
		q.add("test3", 7);
		q.add("test4", 6);

		Thread t = new Thread(new SearchTest(q, "test1"));
		Thread t2 = new Thread(new QueueTest(q, "test6", 9));
		Thread t3 = new Thread(new PopTest(q));
//		Thread t4 = new Thread(new QueueTest(q, "test8", 8));
		t.start();
		t2.start();
		t3.start();
//		t4.start();
//		System.out.println(q.search("test5"));
		
		
	}

}

class SearchTest implements Runnable {
	
	final PriorityQueue q;
	String name;
	public SearchTest(PriorityQueue queue, String name) {
		q = queue;
		this.name = name;
	}
	public void run() {
		System.out.println("searching " + name + ": " + q.search(name));
	}
	
}

class PopTest implements Runnable {
	
	final PriorityQueue q;
	public PopTest(PriorityQueue queue) {
		q = queue;
	}
	public void run() {
		System.out.println("removing: " + q.getFirst());
	}
	
}
