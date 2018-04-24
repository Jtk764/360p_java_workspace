package paxos;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
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
    AtomicInteger _min;
	
												//needs lock
    HashMap<Integer, Response> seqResponseMap; 	//keeps track of latest response for each sequence
    Request prepared;
    Request accepted;
    HashMap<Integer, retStatus> seqStatus;
    ReentrantLock preparedLock; 	//for prepared
    ReentrantLock acceptedLock; 	//for accepted
    ReentrantLock statusLock; 	//for status
    Semaphore proposerLock; 		//used for proposer creation
    Object tmp; 		//used for proposer creation
    int tmpseq; 		//used for proposer creation
    					
    					//needs lock
    int[] peer_min; 	//keep track of the this instances knowledge of other peers mins, 
    					
    AtomicInteger dmsgcnt;        //keep track of number of dmsg
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
        _min = new AtomicInteger(-1);
        prepared=null;
        accepted=null;
        proposerLock = new Semaphore(1);
        peer_min = new int[peers.length];
        statusLock=new ReentrantLock();
        preparedLock=new ReentrantLock();
        acceptedLock= new ReentrantLock();
        
        
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
    	
    	int proposal=0;
    	int count=0;
    	for (int i = 0; i < ports.length; i++){
    		Request r = new Request(seq, -1, me);
    		r.proposal=true;
    		Response resp=Call("Decide", r, i);
    		if (resp != null && resp.pvalue > proposal){
    			proposal = resp.pvalue+1;
    		}	
    	}
    	for (int i = 0; i < ports.length; i++){ // send and handle the prepare
    		Request r = new Request(seq, proposal, me);
    		Response resp=Call("Prepare", r, i);
    		if (resp != null && resp.ack){
    			count++;
	    		if ( resp.n_a > seqResponseMap.get(Integer.valueOf(seq)).n_a){  //needs lock
	    			seqResponseMap.put(Integer.valueOf(seq), resp);
	    		}
	    		if (resp.dmsg){
	    			peer_min[i] = resp.dvalue;
	    		}
    		}
    	}
    	if ( count >= (ports.length/2)+1){
    		count=0;
    		for (int i = 0; i < ports.length; i++){ // send and handle the accept
    			if (seqResponseMap.get(seq) != null ) //needs lock
    				value = seqResponseMap.get(seq).v_a; // use value from memory
        		Request r = new Request(seq, proposal, me, value);
        		Response resp=Call("Prepare", r, i);
        		if (resp != null && resp.ack){
        			count++;
    	    		if (resp.dmsg){
    	    			peer_min[i] = resp.dvalue;
    	    		}
        		}
        	}
    	}
    	
    }

    // RMI handler
    public Response Prepare(Request req){
    	assert req.value == null;
    	if (req.dmsg){
			peer_min[req.pid] = req.dvalue;
		}
    	if ( prepared != null && req.l < prepared.l ) return new Response(false);
    	else {
			prepared = req;
			acceptedLock.lock();
    		if (accepted == null){
    			acceptedLock.unlock();
    			return new Response(true);
    		}
    		else{
    			return new Response(true, accepted.seq , accepted.l , accepted.value);	
    		}
    	}

    }

    public Response Accept(Request req){
    	assert req.value != null;
    	if (req.dmsg){
			peer_min[req.pid] = req.dvalue;
		}
    	preparedLock.lock();
    	if ( prepared != null && req.l < prepared.l ) return new Response(false);
    	else {
    			preparedLock.unlock();
    			acceptedLock.lock();
    			accepted = req;
    			acceptedLock.unlock();
    			return new Response(true);
    	}
    }

    public Response Decide(Request req){
    	if (req.dmsg){
			peer_min[req.pid] = req.dvalue;
		}
    	return new Response(true);
    }

    /**
     * The application on this machine is done with
     * all instances <= seq.
     *
     * see the comments for Min() for more explanation.
     */
    public void Done(int seq) {
        // Your code here
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
        return _min.get();
    }



    /**
     * the application wants to know whether this
     * peer thinks an instance has been decided,
     * and if so what the agreed value is. Status()
     * should just inspect the local peer state;
     * it should not contact other Paxos peers.
     */
    public retStatus Status(int seq){
    	return seqStatus.get(Integer.valueOf(seq));
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


}
