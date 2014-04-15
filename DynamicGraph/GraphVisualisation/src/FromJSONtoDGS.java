/*
 	This program takes the set of JSON files generated from the Python program 
 	that generated the dynamic graph that is used with the D3JS force
 	directed graph. It outputs a file in the DGS format, the GraphStream 
 	format to represent network evolution. As input, you need to specify the
 	number of time steps computed.
    Copyright (C) 2014  Paramasiven Appavoo & Anuja Meetoo Appavoo @ NUS 

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Iterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class FromJSONtoDGS {

	public static void main(String[] args) throws Exception {
		
		// Specify the number of snapshots stored as JSON files below
		int numOfJSONFiles = 10;
		
		// Relative folder and and prefix of the JSON files
		String filePrefix = "networkEvolution/network";
		String fileSuffix = ".json";
		String filejson="";
		String dgsString = "";
		String newnode="";
		String newedge="";
		
		// Relative output path of the DGS file and its name
		FileWriter fwdgs = new FileWriter("DGS/Examples/network.dgs");
		JSONParser parser = new JSONParser();
		int countEvent=1;
		dgsString += "DGS004\n";
		dgsString += "\"NetworkEvolution\" 0 0\n";
		
		// Processing the JSON files
		for(int i = 0; i < numOfJSONFiles; i++){	
			filejson = filePrefix + String.valueOf(i) + fileSuffix;
			System.out.println("Processing file: " + filejson);
			Object obj = parser.parse(new FileReader(filejson));
			JSONObject jsonObject = (JSONObject) obj;
			String name = "", source = "", target = "";
			
			// Processing each node in one JSON file
			JSONArray nodes = (JSONArray) jsonObject.get("nodes");
			Iterator<JSONObject> nodesIterator = nodes.iterator();
			while (nodesIterator.hasNext()) {
				name = (String) nodesIterator.next().get("name");
				newnode = "an \"" + name + "\"\n";
				if(!dgsString.contains(newnode)){
						dgsString += newnode;
						dgsString += "st " + String.valueOf(countEvent++) + "\n";
				}
			}
			
			// Processing each edge in one JSON file
			JSONArray links = (JSONArray) jsonObject.get("links");
			Iterator<JSONObject> linksIterator = links.iterator();
			while (linksIterator.hasNext()) {
				JSONObject edge = linksIterator.next();
				source = String.valueOf(edge.get("source"));
				target = String.valueOf(edge.get("target"));
				newedge = "ae \"" + source + "-" + target + "\" \"" +  source + "\" \"" + target + "\"\n";
				if(!dgsString.contains(newedge)){
					dgsString +=  newedge;
					dgsString += "st " + String.valueOf(countEvent++) + "\n";
				}
			}
		}
		
		// Writing all the events in the dgs file
		fwdgs.write(dgsString);
		fwdgs.close();
	}
}
