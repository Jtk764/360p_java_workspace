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
    boolean acceptOK;
    
    Integer seq;
    Integer n_a;
    Object v_a;
    

    // Your constructor and methods here
    public Response(boolean accept, boolean ack) {
    	this.acceptOK = accept;
    	this.ack = ack;
    	this.seq = null;
    	this.n_a = null;
    	this.v_a = null;
    }
    
    public void prepare_ok(int seq, int nA, Object vA) {
    	this.seq = seq;
    	this.n_a = nA;
    	this.v_a = vA;
    }
    
    public void accept_ok(int seq) {
    	this.seq = seq;
    }
}
