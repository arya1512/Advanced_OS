import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Initial_config {

	public static Network_map initial_config(String name) throws IOException{
		Network_map map_file = new Network_map();
		int node_count = 0,next = 0;
		// Keeps track of current node
		int current_node = 0;
		
		String file_name = System.getProperty("user.dir") + "/" + name;
		
		String read_line = null;
		try {
			BufferedReader buf = new BufferedReader(new FileReader(file_name));
			
			while((read_line = buf.readLine()) != null) {
				if(read_line.length() == 0 || read_line.startsWith("#"))
					continue;
				// Ignore comments and consider only those lines which are not comments
				String[] config_input;
				if(read_line.contains("#")){
					String[] config_input_comment = read_line.split("#.*$"); //Ignore text after # symbol
					config_input = config_input_comment[0].split("\\s+");
				}
				else {
					config_input = read_line.split("\\s+");
				}

				if(next == 0 && config_input.length == 6){
					map_file.no_of_node = Integer.parseInt(config_input[0]);
					map_file.minPerActive = Integer.parseInt(config_input[1]);
					map_file.maxPerActive = Integer.parseInt(config_input[2]);
					map_file.minSendDelay = Integer.parseInt(config_input[3]);
					map_file.snapshotDelay = Integer.parseInt(config_input[4]);
					map_file.maxNumber = Integer.parseInt(config_input[5]);
					map_file.Mtx = new int[map_file.no_of_node][map_file.no_of_node];
					next++;
				}
				else if(next == 1 && node_count < map_file.no_of_node)
				{							
					map_file.nodes.add(new Node(Integer.parseInt(config_input[0]),config_input[1],Integer.parseInt(config_input[2])));
					node_count++;
					if(node_count == map_file.no_of_node){
						next = 2;
					}
				}
				else if(next == 2) {
					for(String i : config_input){
						if(current_node != Integer.parseInt(i)) {
							map_file.Mtx[current_node][Integer.parseInt(i)] = 1;
							map_file.Mtx[Integer.parseInt(i)][current_node] = 1;
						}
					}
					current_node++;
				}
			}
			buf.close();  
		}
		catch(FileNotFoundException ex) {
			System.out.println("Unable to open file '" + file_name + "'");                
		}
		catch(IOException ex) {
			System.out.println("Error reading file '" + file_name + "'");                  
		}
		return map_file;
	}

//	public static void main(String[] args) throws IOException {
//		Network_map m = Initial_config.initial_config("config.txt");
//		
//		for(Node n : m.nodes) {
//			System.out.println(n.host + " " + n.nodeId + " " + n.port);
//		}
//		System.out.println(m.no_of_node);
//		System.out.println(m.minPerActive);
//		System.out.println(m.maxPerActive);
//		System.out.println(m.minSendDelay);
//		System.out.println(m.snapshotDelay);
//		System.out.println(m.maxNumber);
//		
//		for(int i=0;i<m.no_of_node;i++){
//			for(int j=0;j<m.no_of_node;j++){
//				System.out.print(m.Mtx[i][j]+"  ");
//			}
//			System.out.println();
//		}
//
//	}
}

