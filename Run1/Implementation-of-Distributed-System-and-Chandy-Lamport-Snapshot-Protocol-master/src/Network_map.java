import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

enum Color { RED,BLUE};
@SuppressWarnings("serial")
public class Network_map implements Serializable  {
	String configFileName;
	
	//Variables required for MAP Protocol
	int id;
	int[][] Mtx;
	ArrayList<Integer> nbr;
	int[] vector;
	boolean active;
	int msgSentCount;
	
	int no_of_node;
	int minPerActive;
	int maxPerActive;
	int minSendDelay;
	int snapshotDelay;
	int maxNumber;
	
	//Variables required for ChandyLamport Protocol
	Color color;	
	int saveChannelMsg;
	boolean isFirstSnapshot;
	static String outFile;

	//Mapping between process number as keys and <id,host,port> as value
	HashMap<Integer,Node> nodeInfo;
	
	//ArrayList which holds the total processes(nodes) 
	ArrayList<Node> nodes;
	
	// Create all the channels in the beginning and keep it open till the end
	// Mapping between each process as a server and its client connections
	HashMap<Integer,Socket> channels;
	
	// Mapping between ArrayList of messages for each process receiving incoming messages
	HashMap<Integer,ArrayList<AppMessage>> channel_state;
	
	// Create all the output streams associated with each socket 
	//Mapping between each sent message with object output stream
	HashMap<Integer,ObjectOutputStream> oStream;

	// Mapping between processes and StMsg which stores all state messages
	HashMap<Integer,StateMessage> stateMsg;	

	// Mapping between incoming channels and boolean received marker message
	HashMap<Integer,Boolean> RxdMarker;

	//State(Vector,ChannelStates and its id) of the each process in stored in this StateMessage Object
	StateMessage curState;
	
	//Check if state message has been received from all the processes in the system
	boolean[] isRxdStateMsg;
	
	//Final output vector snapshots
	ArrayList<int[]> globalSnapshots;
	
	//Constructor to initialize all variables
	public Network_map() {
		msgSentCount = 0;
		active=false;
		nbr = new ArrayList<>();
		color = Color.BLUE;
		saveChannelMsg=0;
		isFirstSnapshot = true;
		nodes = new ArrayList<Node>();
		nodeInfo = new HashMap<Integer,Node>();
		channels = new HashMap<Integer,Socket>();
		oStream = new HashMap<Integer,ObjectOutputStream>();
		globalSnapshots = new ArrayList<int[]>();
	}
	
	//Initialize again before taking another snapshot
	void initialize(Network_map map_obj){
		map_obj.channel_state = new HashMap<Integer,ArrayList<AppMessage>>();
		map_obj.RxdMarker = new HashMap<Integer,Boolean>();
		map_obj.stateMsg = new HashMap<Integer,StateMessage>();	

		Set<Integer> keys = map_obj.channels.keySet();
		
		for(Integer e : keys){
			ArrayList<AppMessage> arrList = new ArrayList<AppMessage>();
			map_obj.channel_state.put(e, arrList);
		}
		
		for(Integer e: map_obj.nbr){
			map_obj.RxdMarker.put(e,false);
		}
		map_obj.isRxdStateMsg = new boolean[map_obj.no_of_node];
		map_obj.curState = new StateMessage();
		map_obj.curState.vector = new int[map_obj.no_of_node];
	}	
}
