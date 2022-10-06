import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Random;

public class SendMessageThread extends Thread{
	Network_map map_obj;
	public SendMessageThread(Network_map map_obj) {
		this.map_obj = map_obj;
	}
	void sendMessages() throws InterruptedException{

		// get a random number between minPerActive to maxPerActive to send that many messages
		int randMessages = 1;
		int minSendDelay = 0;
		synchronized(map_obj){
			randMessages = this.getRandomNumber(map_obj.minPerActive,map_obj.maxPerActive);
			// If random number is 0
			if(randMessages == 0){
				randMessages = this.getRandomNumber(map_obj.minPerActive + 1,map_obj.maxPerActive);
			}
			minSendDelay = map_obj.minSendDelay;
		}

		//Send the messages to random nbr each time and add minSendDelay between each send
		for(int i=0;i<randMessages;i++){
			synchronized(map_obj){
				//get a random neigbour
				int randNeighborNode = this.getRandomNumber(0,map_obj.nbr.size()-1);
				int curNeighbor = map_obj.nbr.get(randNeighborNode);

				if(map_obj.active == true){
					//send application message
					AppMessage m = new AppMessage(); 
					// Implementing Vector clock protocol
					map_obj.vector[map_obj.id]++;
					m.vector = new int[map_obj.vector.length];
					System.arraycopy( map_obj.vector, 0, m.vector, 0, map_obj.vector.length );
					m.nodeId = map_obj.id;
			
					//Send object data to the neighbor
					try {
						ObjectOutputStream output_stream = map_obj.oStream.get(curNeighbor);
						output_stream.writeObject(m);	
						output_stream.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}	
					//increment msgSentCount
					map_obj.msgSentCount++;
				}
			}
			// Wait for minimum sending delay before sending another message
			try{
				Thread.sleep(minSendDelay);
			}
			catch (InterruptedException e) {
				System.out.println("Error in SendMessages");
				e.printStackTrace();
			}
		}
		synchronized(map_obj){
			// After sending minPerActive to maxPerActive number of messages node should be passive
			map_obj.active = false;
		}


	}
	public void run(){
		try {
			this.sendMessages();
		} catch (InterruptedException e) {
			System.out.println("Error in SendMessages");
			e.printStackTrace();
		}
	}
	// Function to generate random number in a given range
	int getRandomNumber(int min,int max){
		Random rand = new Random();
		int randomNum = rand.nextInt((max - min) + 1) + min;
		return randomNum;
	}
}
