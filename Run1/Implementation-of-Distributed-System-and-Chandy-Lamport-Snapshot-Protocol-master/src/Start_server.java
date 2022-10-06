import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

public class Start_server{

	ServerSocket port_listener = null;
	Socket socket = null;
	int port_server;
	private Network_map map_obj;
	
	public Start_server(Network_map map_obj) {
		
		this.map_obj = map_obj; //Global map_obj
		// port number on which this node should listen 
		port_server = map_obj.nodes.get(map_obj.id).port;
		try {
			port_listener = new ServerSocket(port_server);
		} 
		catch(BindException e) {
			System.out.println("Node " + map_obj.id + " : " + e.getMessage() + ", Port : " + port_server);
			System.exit(1);
		}
		catch (IOException e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void listenforinput(){
		//Listen and accept for any client connections
		try {
			while (true) {
				try {
					socket = port_listener.accept();
				} catch (IOException e1) {
					System.out.println("Connection error,try again!!");
					System.exit(1);
				}
				// For every client request start a new thread 
				new ReceiveThread(socket,map_obj).start();
			}
		}
		finally {
			try {
				port_listener.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}