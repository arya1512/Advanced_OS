import java.io.IOException;

public class Main {
	public static void main(String[] args) throws IOException, InterruptedException {
		
		final int NODE_ZERO = 0;
		//Parse through config.txt file
		Network_map map_obj = Initial_config.initial_config(args[1]);
		// Get the node number of the current Node
		map_obj.id = Integer.parseInt(args[0]);
		int current_node = map_obj.id;
		//Get the configuration file name from command read_line
		map_obj.configFileName = args[1];
		Network_map.outFile = map_obj.configFileName.substring(0, map_obj.configFileName.lastIndexOf('.'));
		//Build converge cast spanning tree
		ConvergeCast.constructNodeTree(map_obj.Mtx);
		// Transfer the collection of nodes from ArrayList to hash map nodes
		for(int i=0;i<map_obj.nodes.size();i++){
			map_obj.nodeInfo.put(map_obj.nodes.get(i).nodeId, map_obj.nodes.get(i));
		}
	
		//Create a server socket 
		Start_server server = new Start_server(map_obj);
		
		//Create channels and keep it till the end
		new Client(map_obj, current_node);

		map_obj.vector = new int[map_obj.no_of_node];

		//Initialize all data structures
		map_obj.initialize(map_obj);

		//Initially node 0 is active therefore if this node is 0 then it should be active
		if(current_node == NODE_ZERO){
			map_obj.active = true;		
			//Call Chandy Lamport protocol if it is node 0
			new CL_Protocol_Thread(map_obj).start();		
			new SendMessageThread(map_obj).start();
		}
		
		server.listenforinput(); //Listen for client connections
		
	}
}
