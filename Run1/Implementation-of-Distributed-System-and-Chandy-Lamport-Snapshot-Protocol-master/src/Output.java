import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

//Print the globalSnapshots to the output File
public class Output {
	Network_map map_obj;

	public Output(Network_map map_obj) {
		this.map_obj = map_obj;
	}


	public void storeSnapshotsToFile() {
		String file_name = Network_map.outFile + "-" + map_obj.id + ".out";
		synchronized(map_obj.globalSnapshots){
			try {
				File file = new File(file_name);
				FileWriter fW;
				if(file.exists()){
					fW = new FileWriter(file,true);
				}
				else
				{
					fW = new FileWriter(file);
				}
				BufferedWriter bW = new BufferedWriter(fW);

   
				for(int i=0;i<map_obj.globalSnapshots.size();i++){
					for(int j:map_obj.globalSnapshots.get(i)){
						bW.write(j + " ");
						
					}
					if(i<(map_obj.globalSnapshots.size()-1)){
						bW.write("\n");
					}
				}			
				map_obj.globalSnapshots.clear();
				bW.close();
			}
			catch(IOException ex) {
				System.out.println("Error writing to file '" + file_name + "'");
			}
		}
	}

}

