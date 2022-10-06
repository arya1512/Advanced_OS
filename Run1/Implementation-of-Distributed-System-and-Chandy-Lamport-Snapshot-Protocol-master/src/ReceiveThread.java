import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.net.Socket;

//Read object data sent by neighboring clients
public class ReceiveThread extends Thread {
	Socket socket;
	Network_map map_obj;

	public ReceiveThread(Socket csocket,Network_map map_obj) {
		this.socket = csocket;
		this.map_obj = map_obj;
	}

	public void run() {
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		while(true){
			try {
				StreamMessage msg;
				msg = (StreamMessage) ois.readObject();
				// Synchronizing map_obj so that multiple threads access map_obj in a synchronized way
				synchronized(map_obj){
					boolean isNotTerminated = true;
					//If MarkerMessage send marker messages to all neighboring nodes
					if(msg instanceof MarkerMessage){
						int channelNo = ((MarkerMessage) msg).nodeId;
						CL_Protocol.sendMarkerMessage(map_obj,channelNo);
					}	

					//If AppMsg and node is passive becomes active only if
					//it has sent fewer than maxNumber messages
					else if((msg instanceof AppMessage) && 
							(map_obj.active == false) && 
							(map_obj.msgSentCount < map_obj.maxNumber) && 
							(map_obj.saveChannelMsg == 0))
					{
						map_obj.active = true; 
						new SendMessageThread(map_obj).start();
					}
					
					//If AppMsg and saveChannelMsg = 1 then save it
					else if((msg instanceof AppMessage) && 
							(map_obj.active == false) && 
							(map_obj.saveChannelMsg == 1))
					{
						//Save the channel No from where AppMsg was sent
						int channelNo = ((AppMessage) msg).nodeId;
						//Log the application message since saveChannelMsg is enabled
						MessageHandler.saveChannelMessages(channelNo,((AppMessage) msg) ,map_obj);
					}
					
					//If StateMessage then and nodeId is 0 check for termination
					//else forward it to the parent on converge cast tree towards node_0
					else if(msg instanceof StateMessage){
						if(map_obj.id == 0){
							//Message received at node_0 from nodeId
							map_obj.stateMsg.put(((StateMessage)msg).nodeId,((StateMessage)msg));
							map_obj.isRxdStateMsg[((StateMessage) msg).nodeId] = true;
							if(map_obj.stateMsg.size() == map_obj.no_of_node){
								//Check for termination or take next snapshot
								isNotTerminated = CL_Protocol.detectTermination(map_obj,((StateMessage)msg));
								if(isNotTerminated){
									map_obj.initialize(map_obj);
									//Call thread again to take new snapshot
									new CL_Protocol_Thread(map_obj).start();	
								}								
							}
						}
						else{
							MessageHandler.sendToParent(map_obj,((StateMessage)msg));
						}
					}
					
					//If finishMsg send to all nbr
					else if(msg instanceof FinishMessage){	
						CL_Protocol.sendFinishMsg(map_obj);
					}

					if(msg instanceof AppMessage){
						//Implementing vector protocol on receiver side
						for(int i=0;i<map_obj.no_of_node;i++){
							map_obj.vector[i] = Math.max(map_obj.vector[i], ((AppMessage) msg).vector[i]);
						}
						map_obj.vector[map_obj.id]++;
					}
				}
			}
			catch(StreamCorruptedException e) {
				e.printStackTrace();
				System.exit(2);
			}
			catch (IOException e) {
				e.printStackTrace();
				System.exit(2);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				System.exit(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(2);
			}
		}
	}
}
