package paxos;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is the main class you need to implement paxos instances.
 */
public class Paxos implements PaxosRMI, Runnable{

    ReentrantLock mutex;
    String[] peers; // hostname
    int[] ports; // host port
    int me; // index into peers[]
    
    

    
    Registry registry;
    PaxosRMI stub;

    AtomicBoolean dead;// for testing
    AtomicBoolean unreliable;// for testing

    // Your data here
    AtomicInteger _max;
	min _min;
												//needs lock
    HashMap<Integer, Response> seqResponseMap; 	//keeps track of latest response for each sequence
    ReentrantLock responseLock;
    
    HashMap<Integer, Request> preparedMap;
    HashMap<Integer, Request> acceptedMap;
    HashMap<Integer, retStatus> seqStatus;
    ReentrantLock preparedLock; //for prepared
    ReentrantLock acceptedLock; //for accepted
    ReentrantLock statusLock; 	//for status
    
    Semaphore proposerLock; 	//used for proposer creation
    Object tmp; 				//used for proposer creation
    int tmpseq; 				//used for proposer creation
    					
    					//needs lock
    ReentrantLock peerLock;
    int[] done_peer; 	//keep track of the this instances knowledge of other peers mins, 
    
    
    ReentrantLock doneLock;
    boolean[] sentList;
    AtomicBoolean needdmsg;       //used when sending request or responses for doen message info

    /**
     * Call the constructor to create a Paxos peer.
     * The hostnames of all the Paxos peers (including this one)
     * are in peers[]. The ports are in ports[].
     */
    public Paxos(int me, String[] peers, int[] ports){

        this.me = me;
        this.peers = peers;
        this.ports = ports;
        this.mutex = new ReentrantLock();
        this.dead = new AtomicBoolean(false);
        this.unreliable = new AtomicBoolean(false);

        this.seqResponseMap = new HashMap<Integer, Response>();
        seqStatus=new HashMap<Integer, retStatus>();
        _max = new AtomicInteger(-1);
        _min=  new min(-1,-1);
        preparedMap=new HashMap<Integer, Request>();
        acceptedMap=new HashMap<Integer, Request>();
        proposerLock = new Semaphore(1);
        done_peer = new int[peers.length];
        for ( int i =0 ;i < done_peer.length ; i++) done_peer[i]=-1;
        statusLock=new ReentrantLock();
        preparedLock=new ReentrantLock();
        acceptedLock= new ReentrantLock();
        responseLock = new ReentrantLock();
        peerLock = new ReentrantLock();
        needdmsg = new AtomicBoolean(false);
        doneLock = new ReentrantLock();
        
        // register peers, do not modify this part
        try{
            System.setProperty("java.rmi.server.hostname", this.peers[this.me]);
            registry = LocateRegistry.createRegistry(this.ports[this.me]);
            stub = (PaxosRMI) UnicastRemoteObject.exportObject(this, this.ports[this.me]);
            registry.rebind("Paxos", stub);
        } catch(Exception e){
            e.printStackTrace();
        }
    }


    /**
     * Call() sends an RMI to the RMI handler on server with
     * arguments rmi name, request message, and server id. It
     * waits for the reply and return a response message if
     * the server responded, and return null if Call() was not
     * be able to contact the server.
     *
     * You should assume that Call() will time out and return
     * null after a while if it doesn't get a reply from the server.
     *
     * Please use Call() to send all RMIs and please don't change
     * this function.
     */
    public Response Call(String rmi, Request req, int id){
        Response callReply = null;

        PaxosRMI stub;
        try{
            Registry registry=LocateRegistry.getRegistry(this.ports[id]);
            stub=(PaxosRMI) registry.lookup("Paxos");
            if(rmi.equals("Prepare"))
                callReply = stub.Prepare(req);
            else if(rmi.equals("Accept"))
                callReply = stub.Accept(req);
            else if(rmi.equals("Decide"))
                callReply = stub.Decide(req);
            else
                System.out.println("Wrong parameters!");
        } catch(Exception e){
            return null;
        }
        return callReply;
    }


    /**
     * The application wants Paxos to start agreement on instance seq,
     * with proposed value v. Start() should start a new thread to run
     * Paxos on instance seq. Multiple instances can be run concurrently.
     *
     * Hint: You may start a thread using the runnable interface of
     * Paxos object. One Paxos object may have multiple instances, each
     * instance corresponds to one proposed value/command. Java does not
     * support passing arguments to a thread, so you may reset seq and v
     * in Paxos object before starting a new thread. There is one issue
     * that variable may change before the new thread actually reads it.
     * Test won't fail in this case.
     *
     * Start() just starts a new thread to initialize the agreement.
     * The application will call Status() to find out if/when agreement
     * is reached.
     */
    public void Start(int seq, Object value){
    	if(seq < Min()) {
    		return;
    	}
    	statusLock.lock();
    	for (int i =Min(); i <= seq; i++ ) seqStatus.putIfAbsent(Integer.valueOf(i), new retStatus(State.Pending, null));
    	statusLock.unlock();
    	try {
			proposerLock.acquire(); 	// lock this paxos instance variables so that the new thread c
										// can be created first
		} catch (InterruptedException e) {
										// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	tmp = value;
    	tmpseq=seq;
    	new Thread(this).start();
    }

    @Override
    public void run(){
        //Your code here
    	//this is the proposer
    	Object value = tmp;
    	int seq = tmpseq;
    	proposerLock.release();
    	boolean decided = false;
    	
    	int proposal=0;
    	int next_proposal = 0;
    	int count=0;

    	while(!decided) {
    		if (this.dead.get()) return;
	    	for (int i = 0; i < ports.length; i++){ // send and handle the prepare
	    		Request r = new Request(seq, proposal, me);
	    		doneLock.lock();
	    		if(needdmsg.get() && !sentList[i]) {
	    			r.dmsg = true;
	    			peerLock.lock();
	    			r.dvalue = done_peer[me];
	    			peerLock.unlock();
	    			sentList[i]=true;
	    		}
	    		doneLock.unlock();
	    		Response resp;
	    		if ( i == me) resp=Prepare(r);
	    		else resp=Call("Prepare", r, i);
	    		if (resp != null){
	    			if(resp.ack) {
		    			count++;
		    			responseLock.lock();
			    		if ( seqResponseMap.get(Integer.valueOf(seq)) != null && 
			    				resp.n_a > seqResponseMap.get(Integer.valueOf(seq)).n_a){  //needs lock
			    			seqResponseMap.put(Integer.valueOf(seq), resp);
			    		}
			    		else if (seqResponseMap.get(Integer.valueOf(seq)) == null 
			    				&& resp.n_a != -1){
			    			seqResponseMap.put(Integer.valueOf(seq), resp);
			    		}
			    		responseLock.unlock();
			    		if (resp.dmsg){
			    			peerLock.lock();
			    			if (i != me) done_peer[i] = resp.dvalue;
			    			Min();
			    			peerLock.unlock();
			    			//forget();
			    		}
	    			}
	    			else if(resp.pvalue >= next_proposal) {
	    				next_proposal = resp.pvalue+1;
	    			}
	    		}
	    	}
	    	if ( count >= (ports.length/2)+1){
	    		count=0;
	    		responseLock.lock();
    			if (seqResponseMap.get(seq) != null ) //needs lock
    				value = seqResponseMap.get(seq).v_a; // use value from memory
    			responseLock.unlock();
	    		for (int i = 0; i < ports.length; i++){ // send and handle the accept	    			
	        		Request r = new Request(seq, proposal, me, value);
		    		doneLock.lock();
		    		if(needdmsg.get() && !sentList[i]) {
		    			r.dmsg = true;
		    			peerLock.lock();
		    			r.dvalue = done_peer[me];
		    			peerLock.unlock();
		    			sentList[i]=true;
		    		}
		    		doneLock.unlock();
		    		Response resp;
		    		if ( i == me) resp=Accept(r);
		    		else resp=Call("Accept", r, i);
	        		if (resp != null && resp.ack){
	        			count++;
	    	    		if (resp.dmsg){
	    	    			peerLock.lock();
			    			done_peer[i] = resp.dvalue;
			    			Min();
			    			peerLock.unlock();
			    			//forget();
	    	    		}
	        		}
	        	}
	    		if(count >= (ports.length/2)+1) {
	    			decided = true;
	    			for (int i = 0; i < ports.length; i++){ // send and handle the done	    			
		        		Request r = new Request(seq, proposal, me, value);
			    		doneLock.lock();
			    		if(needdmsg.get() && !sentList[i]) {
			    			peerLock.lock();
			    			r.dvalue = done_peer[me];
			    			peerLock.unlock();
			    			sentList[i]=true;
			    		}
			    		doneLock.unlock();
		        		Response resp;
		        		if ( i == me) resp=Decide(r);
			    		else resp=Call("Decide", r, i);
		        		if (resp != null && resp.dmsg){
		        			peerLock.lock();
			    			done_peer[i] = resp.dvalue;
			    			Min();
			    			peerLock.unlock();
			    			//forget();
		        		}
	    			}
	    		}
	    	}
	    	else {
	    		proposal = next_proposal;
	    	}
    	}
    }

    // RMI handler
    public Response Prepare(Request req){
    	update(req, false);
    	if (req.dmsg){
			peerLock.lock();
			done_peer[req.pid] = req.dvalue;
			Min();
			peerLock.unlock();
			//forget();
		}
    	preparedLock.lock();
    	if ( preparedMap.get(Integer.valueOf(req.seq)) != null && 
    			req.l <= preparedMap.get(Integer.valueOf(req.seq)).l ) {
    		Response r = new Response(false);
    		r.pvalue = preparedMap.get(req.seq).l;
    		preparedLock.unlock();
        	doneLock.lock();
    		if(needdmsg.get() && !sentList[req.pid]) {
    			r.dmsg = true;
    			peerLock.lock();
    			r.dvalue = done_peer[me];
    			peerLock.unlock();
    			sentList[req.pid]=true;
    		}
    		doneLock.unlock();
    		return r;
    	}
    	else {
			preparedMap.put(req.seq, req);
			preparedLock.unlock();
			acceptedLock.lock();
    		if (acceptedMap.get(Integer.valueOf(req.seq)) == null){
    			acceptedLock.unlock();
    			Response r = new Response(true);
    	    	doneLock.lock();
    			if(needdmsg.get() && !sentList[req.pid]) {
    				r.dmsg = true;
    				peerLock.lock();
    				r.dvalue = done_peer[me];
    				peerLock.unlock();
    				sentList[req.pid]=true;
    			}
    			doneLock.unlock();
        		return r;
    		}
    		else{
    			Response r = new Response(true, req.seq , 
    					acceptedMap.get(Integer.valueOf(req.seq)).l , 
    					acceptedMap.get(Integer.valueOf(req.seq)).value);
    			acceptedLock.unlock();
	    		doneLock.lock();
	    		if(needdmsg.get() && !sentList[req.pid]) {
	    			r.dmsg = true;
	    			peerLock.lock();
	    			r.dvalue = done_peer[me];
	    			peerLock.unlock();
	    			sentList[req.pid]=true;
	    		}
	    		doneLock.unlock();
        		return r;
    		}
    	}

    }

    public Response Accept(Request req){
    	update(req, false);
    	if (req.dmsg){
			peerLock.lock();
			done_peer[req.pid] = req.dvalue;
			if ( _min.pid == req.pid) Min();
			peerLock.unlock();
			//forget();
		}
    	preparedLock.lock();
    	if ( preparedMap.get(Integer.valueOf(req.seq)) != null && 
    			req.l < preparedMap.get(Integer.valueOf(req.seq)).l ) {
    		preparedLock.unlock();
    		Response r = new Response(false);
        	doneLock.lock();
    		if(needdmsg.get() && !sentList[req.pid]) {
    			r.dmsg = true;
    			peerLock.lock();
    			r.dvalue = done_peer[me];
    			peerLock.unlock();
    			sentList[req.pid]=true;
    		}
    		doneLock.unlock();
    		return r;
    	}
    	else {
    			preparedLock.unlock();
    			acceptedLock.lock();
    			acceptedMap.put(req.seq, req);
    			acceptedLock.unlock();
    			Response r = new Response(true);
    	    	doneLock.lock();
    			if(needdmsg.get() && !sentList[req.pid]) {
    				r.dmsg = true;
    				peerLock.lock();
    				r.dvalue = done_peer[me];
    				peerLock.unlock();
    				sentList[req.pid]=true;
    			}
    			doneLock.unlock();
        		return r;
    	}
    }

    public Response Decide(Request req){
    	update(req, true);
    	if (req.dmsg){
    		peerLock.lock();
			done_peer[req.pid] = req.dvalue;
			peerLock.unlock();
		}
    	Response r = new Response(true);
    	doneLock.lock();
		if(needdmsg.get() && !sentList[req.pid]) {
			r.dmsg = true;
			peerLock.lock();
			r.dvalue = done_peer[me];
			peerLock.unlock();
			sentList[req.pid]=true;
		}
		doneLock.unlock();
    	return  r;
    }
    
    

    /**
     * The application on this machine is done with
     * all instances <= seq.
     *
     * see the comments for Min() for more explanation.
     */
    public void Done(int seq) {
        // Your code here
    	doneLock.lock();
    	sentList= new boolean[peers.length];
    	sentList[me]=true;
    	peerLock.lock();
    	done_peer[me] = seq;
    	peerLock.unlock();
    	statusLock.lock();
    	for (int i = Min(); i <= seq; i++){
    		if(seqStatus.containsKey(Integer.valueOf(i))){
    			seqStatus.get(Integer.valueOf(i)).state=State.Forgotten;
    		}
    	}
    	statusLock.unlock();
    	needdmsg.set(true);
    	doneLock.unlock();
    }
    
   

    /**
     * The application wants to know the
     * highest instance sequence known to
     * this peer.
     */
    public int Max(){
        return _max.get();
    }

    /**
     * Min() should return one more than the minimum among z_i,
     * where z_i is the highest number ever passed
     * to Done() on peer i. A peers z_i is -1 if it has
     * never called Done().

     * Paxos is required to have forgotten all information
     * about any instances it knows that are < Min().
     * The point is to free up memory in long-running
     * Paxos-based servers.

     * Paxos peers need to exchange their highest Done()
     * arguments in order to implement Min(). These
     * exchanges can be piggybacked on ordinary Paxos
     * agreement protocol messages, so it is OK if one
     * peers Min does not reflect another Peers Done()
     * until after the next instance is agreed to.

     * The fact that Min() is defined as a minimum over
     * all Paxos peers means that Min() cannot increase until
     * all peers have been heard from. So if a peer is dead
     * or unreachable, other peers Min()s will not increase
     * even if all reachable peers call Done. The reason for
     * this is that when the unreachable peer comes back to
     * life, it will need to catch up on instances that it
     * missed -- the other peers therefore cannot forget these
     * instances.
     */
    public int Min(){
        peerLock.lock();
    	_min.done= done_peer[me]+1;
    	_min.pid=me;
        for(int i = 0; i < done_peer.length; i++) {
        	if(done_peer[i] < _min.done) {
        		_min.done = done_peer[i]+1;
        		_min.pid = i;
        	}
        }
        int min=_min.done;
        peerLock.unlock();
        return min;
    }
    




    /**
     * the application wants to know whether this
     * peer thinks an instance has been decided,
     * and if so what the agreed value is. Status()
     * should just inspect the local peer state;
     * it should not contact other Paxos peers.
     */
    public retStatus Status(int seq){
    	statusLock.lock();
    	retStatus tmp=seqStatus.get(Integer.valueOf(seq));
    	statusLock.unlock();
    	return tmp;
    }

    /**
     * helper class for Status() return
     */
    public class retStatus{
        public State state;
        public Object v;

        public retStatus(State state, Object v){
            this.state = state;
            this.v = v;
        }
    }

    
    
    public class min{
        public int done;
        public int pid ;

        public min(int done, int pid){
            this.done = done;
            this.pid = pid;
        }
    }
    /**
     * Tell the peer to shut itself down.
     * For testing.
     * Please don't change these four functions.
     */
    public void Kill(){
        this.dead.getAndSet(true);
        if(this.registry != null){
            try {
                UnicastRemoteObject.unexportObject(this.registry, true);
            } catch(Exception e){
                System.out.println("None reference");
            }
        }
    }

    public boolean isDead(){
        return this.dead.get();
    }

    public void setUnreliable(){
        this.unreliable.getAndSet(true);
    }

    public boolean isunreliable(){
        return this.unreliable.get();
    }

    private void forget(){
    	statusLock.lock();
    	for (int i = 0; i < Min(); i++){
    		if(seqStatus.containsKey(Integer.valueOf(i))){
    			seqStatus.remove(i);
    		}
    	}
    	statusLock.unlock();
    }
    
    private void update(Request r, boolean decide){
    	
    	statusLock.lock();
    	if (r.seq > _max.get()){
    		_max.set(r.seq);
    	}
    	for (int i=Min(); i < r.seq; i++) seqStatus.putIfAbsent(Integer.valueOf(i), new retStatus(State.Pending, null));
    	if (!decide) {
    		seqStatus.putIfAbsent(Integer.valueOf(r.seq), new retStatus(State.Pending, null));
    	}
    	else seqStatus.put(Integer.valueOf(r.seq), new retStatus(State.Decided, r.value));
    	statusLock.unlock();
    }
}
