package kvpaxos;
import paxos.Paxos;
import paxos.State;
// You are allowed to call Paxos.Status to check if agreement was made.

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class Server implements KVPaxosRMI {

    ReentrantLock mutex;
    Registry registry;
    Paxos px;
    int me;

    String[] servers;
    int[] ports;
    KVPaxosRMI stub;

    // Your definitions here
    HashMap<String, Integer> store;		//key value store this replica knows so far
    ReentrantLock storeLock;
    
    HashMap<Integer, Paxos.retStatus> log;		//log of paxos agreements 
    ReentrantLock logLock;
    
    AtomicInteger max;	//for knowing next sequence number to try



    public Server(String[] servers, int[] ports, int me){
        this.me = me;
        this.servers = servers;
        this.ports = ports;
        this.mutex = new ReentrantLock();
        this.px = new Paxos(me, servers, ports);
        // Your initialization code here
        store = new HashMap<String, Integer>();
        storeLock = new ReentrantLock();
        
        log = new HashMap<Integer, Paxos.retStatus>();
        logLock = new ReentrantLock();
        

        try{
            System.setProperty("java.rmi.server.hostname", this.servers[this.me]);
            registry = LocateRegistry.getRegistry(this.ports[this.me]);
            stub = (KVPaxosRMI) UnicastRemoteObject.exportObject(this, this.ports[this.me]);
            registry.rebind("KVPaxos", stub);
        } catch(Exception e){
            e.printStackTrace();
        }
    }


    // RMI handlers
    public Response Get(Request req){
        // Your code here

    }

    public Response Put(Request req){
        // Your code here
    	//Find a sequence number for this req
    	//Learn about new DECIDED sequences and enter their Op values into log
    	//Learn about new Pending sequences? and do something with them, maybe store as null in the map
    	//idk about forgotten
    	//start a new instance with a new Op for the request and enter it into log
    	//Wait for agreement, while status == pending
    	//When decided, send back true if decided Op matches your Op, else false
    	Op op = req.op;
    	
    	int seq = max.get();
    	Paxos.retStatus stat = px.Status(seq);
    	while(stat != null) {
    		//Need to check for duplicate requests here I think
    		//using stat.v.clientSeq vs op.clientSeq
    		
    		
    		if(stat.state == State.Decided) {
    			log.put(seq, stat);
    			seq = max.incrementAndGet();
    			stat = px.Status(seq);
    		}
    		else {
    			log.put(seq, null);
    			seq = max.decrementAndGet();
    			stat = px.Status(seq);
    		}
    	}
    	

    	px.Start(seq, op);
    	
    	Op got = wait(seq);
    	log.put(seq, px.Status(seq));
    	
    	return new Response(true);

    }
    
    public Op wait(int seq) {
    	int to = 10;
    	while(true) {
    		Paxos.retStatus ret = px.Status(seq);
    		if(ret.state == State.Decided) {
    			return (Op)ret.v;
    		}
    		try {
    			Thread.sleep(to);
    		} catch(Exception e) {
    			e.printStackTrace();
    		}
    		if(to < 1000) {
    			to = to * 2;
    		}
    	}
    }


}
