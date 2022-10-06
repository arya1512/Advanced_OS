import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;


public class CL_Protocol { 
  
	public static void beginCLProtocol(Network_map map_obj) {
		synchronized(map_obj){
			map_obj.isRxdStateMsg[map_obj.id] = true;
			sendMarkerMessage(map_obj,map_obj.id);
		}
	}

	public static void sendMarkerMessage(Network_map map_obj, int channelNo){
		// Node which receives marker message turns red and sends
		// marker messages to all its neighboring channels.
		// Also save all the incoming application messages
		synchronized(map_obj){
			if(map_obj.color == Color.BLUE){
				map_obj.RxdMarker.put(channelNo, true);
				map_obj.color = Color.RED;
				map_obj.curState.active = map_obj.active;
				map_obj.curState.vector = map_obj.vector;
				map_obj.curState.nodeId = map_obj.id;
				//Record the vector timestamp when marker msg is received
				//and store it in globalSnapshots Arraylist
				int[] vectorCopy = new int[map_obj.curState.vector.length];
				for(int i=0;i<vectorCopy.length;i++){
					vectorCopy[i] = map_obj.curState.vector[i];  //Local Snapshot
				}
				map_obj.globalSnapshots.add(vectorCopy);

				//Save the channel state and application messages after it has become red
				map_obj.saveChannelMsg = 1;
				
				//Send marker messages to all its nbr
				for(int i : map_obj.nbr){
					MarkerMessage m = new MarkerMessage();
					m.nodeId = map_obj.id;
					ObjectOutputStream output_stream = map_obj.oStream.get(i);
					try {
						output_stream.writeObject(m);
						output_stream.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				
				//Edge case when only two nodes are there
				if((map_obj.nbr.size() == 1) && (map_obj.id!=0)){
					int parent = ConvergeCast.getParent(map_obj.id);	
					map_obj.curState.channel_state = map_obj.channel_state;
					map_obj.color = Color.BLUE;
					map_obj.saveChannelMsg = 0;
					// Send channel state to parent 
					ObjectOutputStream output_stream = map_obj.oStream.get(parent);
					try {
						output_stream.writeObject(map_obj.curState);
						output_stream.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
					map_obj.initialize(map_obj);
				}


			}
			//If color of the process is red and a marker message is received on this channel
			else if(map_obj.color == Color.RED){
				// Make note that marker msg was received on this channel
				map_obj.RxdMarker.put(channelNo, true);
				int channel=0;
				//Check if this node has received marker messages on all its incoming channels
				while(channel<map_obj.nbr.size() && map_obj.RxdMarker.get(map_obj.nbr.get(channel)) == true){
					channel++;
				}
				
				// If this node has received marker messages from all its incoming channels then 
				// send State Msg to node_0
				if(channel == map_obj.nbr.size() && map_obj.id != 0){
					int parent = ConvergeCast.getParent(map_obj.id);				
					// Record the channelState and StateMessage and which node is sending to node 0 as nodeId
					map_obj.curState.channel_state = map_obj.channel_state;
					map_obj.color = Color.BLUE;
					map_obj.saveChannelMsg = 0;
					ObjectOutputStream output_stream = map_obj.oStream.get(parent);
					try {
						output_stream.writeObject(map_obj.curState);
						output_stream.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
					map_obj.initialize(map_obj);
				}
				
				//If node_0 has receives all marker messages restart state of it
				if(channel == map_obj.nbr.size() &&  map_obj.id == 0){
					map_obj.curState.channel_state = map_obj.channel_state;
					map_obj.stateMsg.put(map_obj.id, map_obj.curState);
					map_obj.color = Color.BLUE;
					map_obj.saveChannelMsg = 0;
				}
			}
		}
	}

	// When node_0 receives state from all nodes
	public static boolean detectTermination(Network_map map_obj, StateMessage msg) throws InterruptedException {
		int channel=0,state=0,node=0;
		synchronized(map_obj){
			// Check if node_0 has received state message from all the nodes 
			while(node < map_obj.isRxdStateMsg.length && map_obj.isRxdStateMsg[node] == true){
				node++;
			}
			//If it has received all the state messages 
			if(node == map_obj.isRxdStateMsg.length){
				//Iterate each state message to check if any process is active
				for(state=0; state < map_obj.stateMsg.size(); state++){
					//If any process is active restart snapshot protocol
					if(map_obj.stateMsg.get(state).active == true){
						return true;
					}
				}
				
				//If all nodes are passive check for channel states
				if(state == map_obj.no_of_node){
					//Check if any channel is empty or not
					for(channel=0; channel < map_obj.no_of_node; channel++){
						//If channel id not empty restart snapshot protocol
						StateMessage value = map_obj.stateMsg.get(channel);
						for(ArrayList<AppMessage> cState : value.channel_state.values()){
							if(!cState.isEmpty()){
								return true;
							}
						}
					}
				}

				//If channels are empty and nodes are passive sendFinishMsg for termination
				if(channel == map_obj.no_of_node){
					sendFinishMsg(map_obj);
					return false;
				}
			}
		}
		return false;
	}


	// //When saveChannelMsg is enabled save all the application messages sent on each channel
	// //Array list holds the application messages received on each channel
	// public static void saveChannelMessages(int channelNo,AppMessage appmsg, Network_map map_obj) {
	// 	synchronized(map_obj){ 
	// 		if(map_obj.RxdMarker.get(channelNo) == false) {
	// 			// or create a list and add the message into it
	// 			if((map_obj.channel_state.get(channelNo).isEmpty())){
	// 				ArrayList<AppMessage> msgList = map_obj.channel_state.get(channelNo);
	// 				msgList.add(appmsg);
	// 				map_obj.channel_state.put(channelNo, msgList); // add to Hash map
	// 			}
	// 			// if the ArrayList is already there just add this message to it
	// 			else if(!(map_obj.channel_state.get(channelNo).isEmpty())){
	// 				map_obj.channel_state.get(channelNo).add(appmsg);
	// 			}
	// 		}
	// 	}
	// }

	// For all nodes other than node_0
	// forward StateMsg to converge cast tree towards node_0
	public static void sendToParent(Network_map map_obj, StateMessage stateMsg) {
		synchronized(map_obj){
			int parent = ConvergeCast.getParent(map_obj.id);
			// Send stateMsg to the parent
			ObjectOutputStream output_stream = map_obj.oStream.get(parent);
			try {
				output_stream.writeObject(stateMsg);
				output_stream.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	//Last
	//Method to send finish message to all the nbr of the current Node
	public static void sendFinishMsg(Network_map map_obj) {
		synchronized(map_obj){
			new Output(map_obj).storeSnapshotsToFile();
			for(int s : map_obj.nbr){
				FinishMessage m = new FinishMessage();
				ObjectOutputStream output_stream = map_obj.oStream.get(s);
				try {
					output_stream.writeObject(m);
					output_stream.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			System.out.println("Node : " + map_obj.id + " - Successfully written to output file");
			System.exit(0);
		}
	}
}

