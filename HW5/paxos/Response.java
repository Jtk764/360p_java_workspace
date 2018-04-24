package paxos;
import java.io.Serializable;

/**
 * Please fill in the data structure you use to represent the response message for each RMI call.
 * Hint: You may need a boolean variable to indicate ack of acceptors and also you may need proposal number and value.
 * Hint: Make it more generic such that you can use it for each RMI call.
 */
public class Response implements Serializable {
    static final long serialVersionUID=2L;
    // your data here
    boolean ack;
    boolean dmsg; //used to convey if done information needs to be carried
    int seq;
    int n_a;
    Object v_a;
    int dvalue; //used to convey the last done value this instance had
    int pvalue; //

    // Your constructor and methods here
    public Response(boolean ack) {
    	this.ack = ack;
    	this.seq = -1;
    	this.n_a = -1;
    	this.v_a = null;
    }
    
    public Response(boolean ack, int seq, int nA, Object vA) {
    	this.ack = ack;
    	this.seq = seq;
    	this.n_a = nA;
    	this.v_a = vA;
    }
    
    public Response(boolean ack, int seq) {
    	this.ack = ack;
    	this.seq = seq;
    	this.n_a = -1;
    	this.v_a = null;
    }
    
    public void setDone(int v){
    	dmsg=true;
    	dvalue=v;
    }
}
