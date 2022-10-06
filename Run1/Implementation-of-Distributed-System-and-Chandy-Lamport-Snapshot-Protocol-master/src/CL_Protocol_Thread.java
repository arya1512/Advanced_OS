
//Thread to start chandy lamport protocol
public class CL_Protocol_Thread extends Thread{

	Network_map map_obj;
	public CL_Protocol_Thread(Network_map map_obj){
		this.map_obj = map_obj;
	}
	public void run(){
		if(map_obj.isFirstSnapshot){
			map_obj.isFirstSnapshot = false;
		}
		else{
			try {
				Thread.sleep(map_obj.snapshotDelay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		CL_Protocol.beginCLProtocol(map_obj);
	}
}
