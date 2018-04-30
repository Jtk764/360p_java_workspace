package kvpaxos;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.atomic.AtomicInteger;


public class Client {
    String[] servers;
    int[] ports;

    // Your data here
    static AtomicInteger id;
    int client_id;
    

    public Client(String[] servers, int[] ports){
        this.servers = servers;
        this.ports = ports;
        // Your initialization code here
        if(id == null) {
        	id = new AtomicInteger(0);
        	client_id = 0;
        }
        else client_id = id.incrementAndGet();
    
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
        KVPaxosRMI stub;
        try{
            Registry registry= LocateRegistry.getRegistry(this.ports[id]);
            stub=(KVPaxosRMI) registry.lookup("KVPaxos");
            if(rmi.equals("Get"))
                callReply = stub.Get(req);
            else if(rmi.equals("Put")){
                callReply = stub.Put(req);}
            else
                System.out.println("Wrong parameters!");
        } catch(Exception e){
            return null;
        }
        return callReply;
    }

    // RMI handlers
    public Integer Get(String key){
        // Your code here
    	Op op = new Op("Get", client_id, key, null);
    	Request req = new Request(op);
    	
    	Response res = null;
    	int index = 0;
    	while(res == null) {
    		res = Call("Get", req, index);
    		index = (index+1)%ports.length;
    	}
    	if(res.success) return res.got.value;
    	else return null;

    }

    public boolean Put(String key, Integer value){
        // Your code here
    	Op op = new Op("Put", client_id, key, value);
    	Request req = new Request(op);
    	
    	Response res = null;
    	int index = 0;
    	while(res == null) {
    		res = Call("Put", req, index);
    		index = (index+1)%ports.length;
    	}
    	return res.success;
    }

}
