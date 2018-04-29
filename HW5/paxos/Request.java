package paxos;
import java.io.Serializable;

/**
 * Please fill in the data structure you use to represent the request message for each RMI call.
 * Hint: You may need the sequence number for each paxos instance and also you may need proposal number and value.
 * Hint: Make it more generic such that you can use it for each RMI call.
 * Hint: Easier to make each variable public
 */
public class Request implements Serializable {
    static final long serialVersionUID=1L;
    // Your data here
    boolean dmsg; //used to convey if done information needs to be carried
    boolean proposal; //used to indicate if the message if to get proposal number
    int seq;
    int pid; // peerid of the requester
    int l;
    Object value;
    int dvalue; //used to convey the last done value this instance had

    // Your constructor and methods here
    public Request(int seq, int n, int me) {
    	this.seq = seq;
    	l=n;
    	pid=me;
    	value = null;
    }
    
    public Request(int seq, int n, int me, Object v) {
    	this.seq = seq;
    	l=n;
    	pid=me;
    	value = v;
    }
    
}
