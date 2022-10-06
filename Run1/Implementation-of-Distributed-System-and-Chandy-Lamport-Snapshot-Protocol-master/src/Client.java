import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
	
	//Each node acts as a client to all its neighboring nodes
	public Client(Network_map map_obj, int current_node) {
		for(int i=0;i<map_obj.no_of_node;i++){
			if(map_obj.Mtx[current_node][i] == 1){
				String host_name = map_obj.nodeInfo.get(i).host;
				int port = map_obj.nodeInfo.get(i).port;
				InetAddress addr = null;
				try {
					addr = InetAddress.getByName(host_name);
				} catch (UnknownHostException e) {
					e.printStackTrace();
					System.exit(1);
				}
				Socket client = null;
				try {
					client = new Socket(addr,port);
				} catch (IOException e) {
					System.out.println("Connection error,try again!!");
					e.printStackTrace();
					System.exit(1);
				}
				//Send client request to all neighboring nodes
				map_obj.channels.put(i, client);
				map_obj.nbr.add(i);
				ObjectOutputStream output_stream = null;
				try {
					output_stream = new ObjectOutputStream(client.getOutputStream());
				} catch (IOException e) {
					
					e.printStackTrace();
				}
				map_obj.oStream.put(i, output_stream);	
			}
		}
	}
}
