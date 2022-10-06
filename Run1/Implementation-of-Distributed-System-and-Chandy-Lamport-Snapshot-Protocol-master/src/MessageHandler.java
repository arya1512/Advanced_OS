import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;


public class MessageHandler { 
  	//When saveChannelMsg is enabled save all the application messages sent on each channel
	//Array list holds the application messages received on each channel
	public static void saveChannelMessages(int channelNo,AppMessage appmsg, Network_map map_obj) {
		synchronized(map_obj){ 
			if(map_obj.RxdMarker.get(channelNo) == false) {
				// or create a list and add the message into it
				if((map_obj.channel_state.get(channelNo).isEmpty())){
					ArrayList<AppMessage> msgList = map_obj.channel_state.get(channelNo);
					msgList.add(appmsg);
					map_obj.channel_state.put(channelNo, msgList); // add to Hash map
				}
				// if the ArrayList is already there just add this message to it
				else if(!(map_obj.channel_state.get(channelNo).isEmpty())){
					map_obj.channel_state.get(channelNo).add(appmsg);
				}
			}
		}
	}

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

}

