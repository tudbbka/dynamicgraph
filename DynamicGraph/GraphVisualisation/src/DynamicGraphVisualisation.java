/*
 	This program takes the set of JSON files generated from the Python program that generated the 
 	dynamic graph and generates a set of images that can be used to generate a movie.
 	Copyright (C) 2014  Anuja Meetoo Appavoo & Paramasiven Appavoo @ NUS 
 	
    This program is free software: you can redistribute it and/or modify it under the terms of the GNU 
    General Public License as published by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
    the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU 
    General Public License for more details.

    You should have received a copy of the GNU General Public License along with this program.  If not, see 
    <http://www.gnu.org/licenses/>.
*/

import java.io.File;      
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator; 
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSinkImages;
import org.graphstream.stream.file.FileSinkImages.LayoutPolicy;
import org.graphstream.stream.file.FileSinkImages.OutputPolicy;
import org.graphstream.stream.file.FileSinkImages.OutputType;
import org.graphstream.stream.file.FileSinkImages.Quality;
import org.graphstream.stream.file.FileSinkImages.Resolution;
import org.graphstream.stream.file.FileSinkImages.Resolutions;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class DynamicGraphVisualisation {
	public static void main(String[] args)  {
		String filePath = "../GraphGeneration/networkEvolution/";
		
		// create a graph
		Graph graph = new SingleGraph("Dynamic Graph");
		
		// set attributes of the graph
		graph.addAttribute("ui.stylesheet", "url(file:CSS/graphStyle.css)");
    	graph.addAttribute("ui.antialias");
    	graph.addAttribute("ui.quality");
    	
		// set parameters for FileSinkImages
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");   	
		OutputPolicy outputPolicy = OutputPolicy.BY_EVENT;
		String prefix = "../movie/images/image";
		OutputType type = OutputType.PNG;
		Resolution resolution = Resolutions.HD720;
		
		FileSinkImages fsi = new FileSinkImages( type, resolution );
		fsi.setOutputPolicy( outputPolicy );
		fsi.setLayoutPolicy( LayoutPolicy.COMPUTED_ONCE_AT_NEW_IMAGE);
		fsi.setQuality(Quality.HIGH);
		fsi.addLogo( "logo/soclogo.jpg", 10, 10 );
		fsi.setStyleSheet("url(file:CSS/graphStyle.css)");
		
		graph.addSink(fsi);
		
		try{
			fsi.begin(prefix);
	    	
			String JSONfileName = filePath + "network" ;
			int numJSONfiles = getNumJSONfileNames(filePath);
			
	    	// build initial graph
			GraphData prevGraphData; 
			GraphData graphData = getGraphData(JSONfileName+"0.json");	// get the details from JSON file as a GraphData object 
			displayGraph(graph, graphData, prefix, fsi);		// display initial graph
		
			String fileName;
			
			for(int i = 1; i <=20; i++) {		// iterate over all JSON file names
			//for(int i = 1; i < numJSONfiles; i++) {		// iterate over all JSON file names
				fileName = JSONfileName + i + ".json";
				
				prevGraphData = new GraphData();		
				createCopy(prevGraphData, graphData);	// create a copy of the graph of previous time step in prevGraphData
				Thread.sleep(1000);
				graphData = getGraphData(fileName);	// get the details from JSON file as a GraphData object
				displayGraph(graph, graphData, prevGraphData, prefix, fsi);	// display the graph
			}
			fsi.end();
		}
		catch(Exception e){ e.printStackTrace();}
	} // end main

	/*
	 *description: Gets list of names of all JSON files in the working directory 
	 */
	public static int getNumJSONfileNames(String filePath){
		List<String> JSONfileNames = new LinkedList<String>(); 
		File folder = new File(filePath); // refer to working directory
		File[] listOfFiles = folder.listFiles(); // get all files in working directory
		
		// store all files ending with '.json' in JSONfileNames  
		for (int i = 0; i < listOfFiles.length; i++) {
			if(listOfFiles[i].getName().endsWith(".json"))
				JSONfileNames.add(listOfFiles[i].getName());
		}
		return JSONfileNames.size();
	} // end getJSONfileNames
	
	/*
	 * description: Creates a copy of a GraphData object, graphData, to graphDataCopy
	 */
	public static void createCopy(GraphData graphDataCopy, GraphData graphData){
		Set<String> nodes = graphData.getNodes();
		Iterator<String> nodeIt = nodes.iterator();
		while(nodeIt.hasNext()){
			String nodeName = nodeIt.next();
			graphDataCopy.addNode(nodeName);
		}
		Set<Edge> edges = graphData.getEdges();
		Iterator<Edge> edgeIt = edges.iterator();
		while(edgeIt.hasNext()){
			Edge edge = edgeIt.next();
			graphDataCopy.addEdge(edge.getNodeFrom(), edge.getNodeTo());
		}
	} // end createCopy
	
	/*
	 * description: Extract details of a graph from a JSON file and store them in a GraphData object
	 */
	public static GraphData getGraphData(String fileName){
		System.out.println(fileName);
    	GraphData graphData = new GraphData();
    	
    	JSONParser parser = new JSONParser();
	 	try {
	 		Object obj = parser.parse(new FileReader(fileName));
	  		JSONObject jsonObject = (JSONObject) obj;
	  		String name = "", source = "", target = "";
	 		
	 		JSONArray nodes = (JSONArray) jsonObject.get("nodes");
	 		Iterator<JSONObject> nodesIterator = nodes.iterator();
	 		while (nodesIterator.hasNext()) {
	 			name = (String) nodesIterator.next().get("name");
	 			graphData.addNode(name);
	 		}
	 		JSONArray links = (JSONArray) jsonObject.get("links");
	 		Iterator<JSONObject> linksIterator = links.iterator();
	 		while (linksIterator.hasNext()) {
	 			JSONObject edge = linksIterator.next();
	 			source = String.valueOf(edge.get("source"));
	 			target = String.valueOf(edge.get("target"));
	 			graphData.addEdge(source, target);
	 		}
	 	} catch (Exception e) {
	 		e.printStackTrace();
	 	}
	 	return graphData;
    } // end getGraphData
    
	/*
	 * description: Displays the details of an initial graph
	 */
    public static void displayGraph(Graph graph, GraphData graphData, String prefix, FileSinkImages fsi) {
    	Iterator<String> nodeIt = graphData.getNodes().iterator();
    	Set<String> addedNodes = new HashSet<String>();
    	Set<Edge> allEdges = graphData.getEdges();
    			
    	String nodeName;
    	try{   	
	    	if(nodeIt.hasNext()){
	    		nodeName = nodeIt.next();
	    		graph.addNode(nodeName);
	    		graph.getNode(nodeName).addAttribute("ui.class", "new");
	    		Thread.sleep(500);
			    graph.getNode(nodeName).removeAttribute("ui.class");
			     addedNodes.add(nodeName);
	    	}
	    	while(nodeIt.hasNext()){
	    		nodeName = nodeIt.next();
	    		graph.addNode(nodeName);
	    		graph.getNode(nodeName).addAttribute("ui.class", "new");
			    Thread.sleep(500);
			    addedNodes.add(nodeName);
			    
			    // add all edges from the node to already added nodes
			    Iterator<Edge> edgeIt = allEdges.iterator();
			    List<Edge> removeEdgeList = new LinkedList<Edge>();
			    while(edgeIt.hasNext()){
			    	Edge edge = edgeIt.next();
			    	
			    	if((edge.getNodeFrom().equalsIgnoreCase(nodeName)&& addedNodes.contains(edge.getNodeTo()))
			    			|| (edge.getNodeTo().equalsIgnoreCase(nodeName) && addedNodes.contains(edge.getNodeFrom())) ){
			    		
			    		graph.addEdge(edge.getNodeFrom()+ "-" + edge.getNodeTo(), edge.getNodeFrom(), edge.getNodeTo());
			    		
			    		graph.getEdge(edge.getNodeFrom()+ "-" + edge.getNodeTo()).addAttribute("ui.class", "new");
					    Thread.sleep(500);
					    graph.getEdge(edge.getNodeFrom()+ "-" + edge.getNodeTo()).removeAttribute("ui.class");
					    removeEdgeList.add(edge);
			    	}
			    }
			    Iterator<Edge> delEdgeIt = removeEdgeList.iterator(); 
			    while(delEdgeIt.hasNext()){
			    	allEdges.remove(delEdgeIt.next());
			    }
			    graph.getNode(nodeName).removeAttribute("ui.class");
	    	}
    	}
    	catch(Exception e){e.printStackTrace();}
    } // end displayGraph
    
    /*
     * description:  Displays new nodes and edges added in the current time step 
     */
    public static void displayGraph(Graph graph, GraphData graphData, GraphData prevGraphData, String prefix, FileSinkImages fsi) {
    	Set<String> newNodes = new HashSet<String>();
    	Iterator<String> nodeIt = graphData.getNodes().iterator();
    	
    	while(nodeIt.hasNext()){
    		newNodes.add(nodeIt.next()); 
    	}
    	newNodes.removeAll(prevGraphData.getNodes()); // new nodes to be added
    	   	
    	nodeIt = newNodes.iterator();
    	String nodeName;
    	try{
	    	while(nodeIt.hasNext()){
	    		nodeName = nodeIt.next();
	    		graph.addNode(nodeName);
	    		graph.getNode(nodeName).addAttribute("ui.class", "new");
			    Thread.sleep(500);
			    
			    // display the edge added by the new node
			    Iterator<Edge> edgeIt = graphData.getEdges().iterator();
			    while(edgeIt.hasNext()){
			    	Edge edge = edgeIt.next();
			    	
			    	if((edge.getNodeFrom().equalsIgnoreCase(nodeName)) || (edge.getNodeTo().equalsIgnoreCase(nodeName))){
			    		System.out.println("need to ad edge " + edge.getNodeFrom()+ " to " + edge.getNodeTo()); ///////////////
			    		graph.addEdge(edge.getNodeFrom() + "-" + edge.getNodeTo(), edge.getNodeFrom(), edge.getNodeTo());
			    		graph.getEdge(edge.getNodeFrom()+ "-" + edge.getNodeTo()).addAttribute("ui.class", "new");
					    Thread.sleep(500);
					    graph.getEdge(edge.getNodeFrom()+ "-" + edge.getNodeTo()).removeAttribute("ui.class");
			    	}
			    }
			    graph.getNode(nodeName).removeAttribute("ui.class");
	    	}
    	}
    	catch(Exception e){ e.printStackTrace(); }
    } // end displayGraph
} // end class DynamicGraphVisualisation