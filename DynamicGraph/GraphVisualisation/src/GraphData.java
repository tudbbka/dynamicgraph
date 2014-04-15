/*
 	This class is used to represent a graph as a set of nodes and edges. 
 	It is used by the DynamicGraphVisualisation class.
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

import java.util.Set; 
import java.util.HashSet;

/*
 * description
 */
public class GraphData {
	private Set<String> nodes;
	private Set<Edge> edges;
	
	public GraphData(){
		nodes = new HashSet<String>();
		edges = new HashSet<Edge>();
	}
	
	public Set<String> getNodes(){
		return nodes;
	}
	
	public Set<Edge> getEdges(){
		return edges;
	}
	
	public void addNode(String newNode){
		nodes.add(newNode);
	}
	
	public void removeNode(String delNode){
		nodes.remove(delNode);
	}
	
	public void addEdge(String nodeFrom, String nodeTo){
		edges.add(new Edge(nodeFrom, nodeTo));
	}
	
	public void removeEdge(String nodeFrom, String nodeTo){
		edges.remove(new Edge(nodeFrom, nodeTo));
	}
} // end class GraphData

class Edge{
	private String nodeFrom;
	private String nodeTo;
	
	public Edge(String nodeFrom, String nodeTo){
		this.nodeFrom = nodeFrom;
		this.nodeTo = nodeTo;
	}
	
	public String getNodeFrom(){
		return nodeFrom;
	}
	
	public String getNodeTo(){
		return nodeTo;
	}
	
	public Boolean equals(Edge cmp){
		if(cmp.getNodeFrom().equalsIgnoreCase(nodeFrom) && cmp.getNodeTo().equalsIgnoreCase(nodeTo))
			return true;
		return false;
	}
} // end class Edge
