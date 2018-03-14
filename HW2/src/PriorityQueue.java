import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PriorityQueue {
	
	class node {
		String name;
		int priority;
		Lock lock = new ReentrantLock();
		node next;
		node prev;
		
		public node(String n, int p) {
			name = n;
			priority = p;
		}
	}
	HashSet<String> list = new HashSet<String>();
	int capacity;
	node head;
	Lock addLock = new ReentrantLock();
	Condition notFull = addLock.newCondition();
	Lock popLock = new ReentrantLock();
	Condition notEmpty = popLock.newCondition();
	AtomicInteger size;
	
	public PriorityQueue(int maxSize) {
        // Creates a Priority queue with maximum allowed size as capacity
		capacity = maxSize;
		size = new AtomicInteger(0);
		head = null;
	}

	public int add(String name, int priority) {
        // Adds the name with its priority to this queue.
        // Returns the current position in the list where the name was inserted;
        // otherwise, returns -1 if the name is already present in the list.
        // This method blocks when the list is full.
		if(list.contains(name)) return -1;
		
		boolean wakePop = false;
		addLock.lock();
		try {
			while (size.get() == capacity) {
				try {
					notFull.await();
				}catch(InterruptedException e) {}
			}
			node e = new node(name, priority);
			int index = size.getAndIncrement();
			if(index == 0) {
				head = e;
				list.add(name);
				wakePop = true;
			}
			else if (index == 1) {
				node h = head;
				head.lock.lock();
				try {
					if(head.name.equals(name)) {
						return -1;
					}
					else if(head.priority < priority) {
						head.prev = e;
						e.next = head;
						head = e;
						list.add(name);
						return 0;
					}
					else {
						head.next = e;
						e.prev = head;
						list.add(name);
						return 1;
					} 
				} finally {h.lock.unlock();}
			}
			else {
				index = 0;
				node cur = head;
				cur.lock.lock();
				if(cur.name.equals(name)) {
					cur.lock.unlock();
					return -1;
				}
				else if(cur.priority < priority) {
					try {
						head.prev = e;
						e.next = head;
						head = e;
						list.add(name);
						return 0;
					} finally { cur.lock.unlock(); }
				}
				else {
					boolean added = false;
					try {
						while(cur.next != null) {
							if(cur.name.equals(name)) {
								cur.lock.unlock();
								return -1;
							}
							else {
								cur.next.lock.lock();
								boolean need = true;
								try {
									if(cur.next.name.equals(name)) {
										cur.next.lock.unlock();
										need = false;
										return -1;
									}
									else if(cur.next.priority < priority) {
										
										e.next = cur.next;
										e.prev = cur;
										cur.next = e;
										e.next.prev = e;
										added = true;
										cur = cur.next;
										list.add(name);
										return index + 1;
									}
									else {
										index++;
										cur = cur.next;
									}
								} finally { 
									if(need) cur.prev.lock.unlock();
									else cur.lock.unlock(); 
								}
							}	
						}
						if(!added) {
							cur.next = e;
							e.prev = cur;
							cur.lock.unlock();
							list.add(name);
							return index+1;
						}
					}finally { 
						if(added) cur.next.lock.unlock(); 
					}
					}
				}
		}finally { addLock.unlock(); }
		if(wakePop) {
			popLock.lock();
			try {
				notEmpty.signalAll();
			} finally { popLock.unlock(); }
		}
		return 0;
	}

	public int search(String name) {
        // Returns the position of the name in the list;
        // otherwise, returns -1 if the name is not found.
		boolean found = false;
		if(size.get() == 0) return -1;
		else {
			int index = 0;
			node cur = head;
			cur.lock.lock();
			try {
				if(cur.name.equals(name)) {
					found = true;
					return 0;
				}
				while(cur.next != null) {
					if(cur.name.equals(name)) return index;
					else {
						cur.next.lock.lock();
						try {
							cur = cur.next;
							index++;
						}finally { cur.prev.lock.unlock(); }
					}
				}
				if(!found) {
					if(cur.name.equals(name)) return index;
					else return -1;
				}
			} finally { cur.lock.unlock(); }
		}
		return -1;
	}

	public String getFirst() {
        // Retrieves and removes the name with the highest priority in the list,
        // or blocks the thread if the list is empty.
		String result;
		boolean wakePush = false;
		popLock.lock();
		try {
			while(size.get() == capacity) {
				try {
					notEmpty.await();
				} catch (InterruptedException e) {}
			}
			node oldHead = head;
			oldHead.lock.lock();
			try {
				result = head.name;
				head = head.next;
				if(size.getAndDecrement() == capacity) {
					wakePush = true;
				}
				list.remove(oldHead.name);
			}finally { oldHead.lock.unlock(); }
		}finally { popLock.unlock(); }
		
		if(wakePush) {
			addLock.lock();
			try {
				notFull.signalAll();
			}finally { addLock.unlock(); }
		}
		
		return result;
	}
}