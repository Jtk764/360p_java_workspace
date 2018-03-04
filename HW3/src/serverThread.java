import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.SocketException;

public class serverThread implements Runnable{
	
	private int mode;
	private ServerSocket tcpSocket;
	private DatagramSocket udpSocket;
	private int port;
	
	 public serverThread() {
		try {
			udpSocket = new DatagramSocket();
			port = udpSocket.getLocalPort();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		mode=0;
	}

	@Override
	public void run() {
		
		
	}

}
