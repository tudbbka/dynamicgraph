'''
    This program takes an inital seed graph and generates a dynamic graph. It is an attempt to implement the 
    algorithm in research paper 'Microscopic Evolution of Social Networks'. The graph at each time step is then 
    saved in JSON format and a visualitsation of the graph is given using D3JS.
    Copyright (C) 2014  Anuja Meetoo Appavoo & Paramasiven Appavoo @ NUS 
     
    This program is free software: you can redistribute it and/or modify it under the terms of the GNU 
    General Public License as published by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
    the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU 
    General Public License for more details.

    You should have received a copy of the GNU General Public License along with this program.  If not, see 
    <http://www.gnu.org/licenses/>.    
'''
from networkx import *
import math
import random
import os # for file manipulation
import simplejson as json

'''
Algorithm:
1. Nodes arrive using the node arrival functionN(·).
2. Node u arrives and samples its lifetime a from the exponential distribution p(a)=λexp(−λa).
3. Node u adds the first edge to node v with probability proportional to its degree.
4. A node u with degree d samples a time gap δ from the distribution p g(δ|d;α, β)=(1/Z) * pow(δ, −α) * exp(−βdδ)and goes to sleep for δ time steps.
5. When a node wakes up, if its lifetime has not expired yet, it creates a two-hop edge using the random-random triangle closing model.
6. If a node’s lifetime has expired, then it stops adding edges; otherwise it repeats from step 4.
'''

# parameters
lambd = 0.0092
alpha = 0.84
beta = 0.0020

networkEvol = "networkEvolution/"
outputFile = "network"
visualizehtml = "visualizeHTML/"
tempNodes = []

def main():
    # create the graph
    graph = Graph()
    numTimeSteps = 30
    
    ## used to group new nodes in JSON
    global tempNodes
    
    # initialise graph from Benchmark data
    graph = initialiseGraph(graph);
    print("\nInitial graph:- ")
    displayGraph(graph, 0) 
       
    ## Save to JSON & HTML
    saveJSON(graph, networkEvol + outputFile + "0.json")
    saveHTML(outputFile, 0)
        
    for time in range(1, numTimeSteps + 1):
        print("\nGraph at time " + str(time) + ":-")
        
        ## used to change the group of new nodes in json output
        tempNodes = graph.nodes()
        
        # update graph for each timeStep
        graph = processGraph(graph, time)
        
        # build the list of degree and cumulative degree that will be used to get the destination node for an edge
        degreeList = [] # stores list of all degrees
        cummulativeDegreeList = []  # stores the cumulative degree of nodes
        total = 0   # total degree of all nodes
        for node in graph.nodes_iter():
            degreeList.append(graph.node[node]['degree'])
            total = total + graph.node[node]['degree']
            cummulativeDegreeList.append(total)
             
        # add nodes according to node arrival function N        
        graph = addNode(graph, time, cummulativeDegreeList)
        
        # display the updated graph
        displayGraph(graph, time) 
                
        ## Save to JSON & HTML
        saveJSON(graph, networkEvol + outputFile + str(time) + ".json")
        saveHTML(outputFile, time)

'''
Description: Initialises the graph from the data generated by Benchmark (file network.data), adding the nodes 
(each node having 3 int attributes, namely lifetime, degree and sleep) and edges
Input: Null graph
Output: Returns the graph with nodes and edges generated by Benchmark 
'''
def initialiseGraph(graph):
    # Read the nodes from the file generated by benchmark, one at a time and add to graph (with attributes)
    # Attributes: lifetime, degree
    # read list of edges from network.dat file generated by benchmark
    network = open('SeedGraph/network.dat', 'r')
    edge = network.readline()
    
    while(edge != ''):
        # process one line at a time
        edgePts = edge.split()
        sourceNode = eval(edgePts[0])
        destinationNode = eval(edgePts[1])
        
        # add the source and destination nodes
        # if node already in graph, need to increment its degree by 1, else add to node list as a new element 
        if(sourceNode in graph.nodes()):
            deg = graph.node[sourceNode]['degree'] + 1
            graph.add_node(sourceNode, lifetime = 0, degree=deg, sleep = 0)
        else:
            graph.add_node(sourceNode, lifetime = 0, degree=1, sleep = 0)
            
        # if node already in graph, need to increment its degree by 1, else add to node list as a new element
        if(destinationNode in graph.nodes()):
            deg = graph.node[destinationNode]['degree'] + 1
            life = graph.node[sourceNode]['lifetime']
            graph.add_node(destinationNode, lifetime = 0, degree=deg, sleep = 0)
        else:
            graph.add_node(destinationNode, lifetime = 0, degree=1, sleep = 0)
        
        # add the edge
        graph.add_edge(sourceNode, destinationNode)
        edge = network.readline()
        
        # add lifetime and time gap (sleep time)
        for node in graph.nodes_iter():
            deg = graph.node[node]['degree']
            life = sampleLifetime()
            slp = sampleTimeGap(deg, life)   # cannot sample as node being added above since it depends on degree of node
            graph.add_node(node, lifetime = life, degree = deg, sleep = slp)
        
    return graph

'''
Description: Displays the graph
Input: Graph, and time(for output filename)
Output: Displays the nodes and edges of the graph 
'''
def displayGraph(graph, time):  
    # prints list of nodes and edges in graph
    print("* Nodes: ")
    for node in graph.nodes_iter():
        print("\tNode ", node, " * Degree:", graph.node[node]['degree'], " * Lifetime:", graph.node[node]['lifetime'], " * Time gap (sleep):", graph.node[node]['sleep'])    
    print("* Edges: ", graph.edges())
    
'''
Description: Node arrival function that determines the number of new nodes for a particular time step
Input: Time step
Output: Number of new nodes for that time step 
'''
def N(time):
    return int(round(math.exp(0.25 * time)))

'''
Description: Add new nodes to the graph in a particular time step
Input: Graph and time step
Output: Returns the updated graph 
'''
def addNode(graph, time, cummulativeDegreeList):
    numNodes = N(time) # number of nodes determined by node arrival function N
    nodeID = max(graph.nodes()) + 1 # get the ID of the first node to be added
    
    for num in range(numNodes):
        # sample lifetime and time gap of node and add new node
        lifetime = sampleLifetime()
        sleep = sampleTimeGap(1, lifetime)
               
        # add first edge for node
        destinationNode = getDestinationForFirstEdge(graph, cummulativeDegreeList)
        graph.add_node(nodeID, lifetime = lifetime, degree = 1, sleep = sleep)
        graph.add_edge(nodeID, destinationNode)
      
        nodeID += 1 # increment ID for next node to be added
    print("\n* Number of nodes added: ", numNodes, "\n")
    return graph

'''
Description: Samples the lifetime of a node from the exponential distribution
Input: None
Output: Returns lifetime value 
'''
def sampleLifetime():
    # sample lifetime a of new node from the exponential distribution p(a)=λexp(−λa)
    lifetime = int(round(random.expovariate(lambd)))
    while(lifetime == 0):
        lifetime = int(round(random.expovariate(lambd)))
    return lifetime

'''
Description: Samples the time gap of a node from the distribution p(δ|d; α, β) = (1/Z)δ −α exp(−βdδ)
Input: Degree and lifetime of the node
Output: Returns the time gap value 
'''
def sampleTimeGap(degree, lifetime):
    timeGap = int(round((math.gamma(2 - alpha) * pow((beta * degree), -1))/math.gamma(1 - alpha)))
    
    while(timeGap == 0):
        timeGap = int(round((math.gamma(2 - alpha) * pow((beta * degree), -1))/math.gamma(1 - alpha)))
    if timeGap > lifetime:
        return lifetime
    return timeGap

'''
Description: Identifies the node to which the new node creates its first edge, where a node u 
adds the first edge to node v with probability proportional to its degree
Input: Graph
Output: Returns the destination node for the edge
'''
def getDestinationForFirstEdge(graph, cummulativeDegreeList):
    # identifies destination node for edge
    # Node u adds the first edge to node v with probability proportional to its degree. 
    randomNum = int(random.uniform(cummulativeDegreeList[0], cummulativeDegreeList[len(cummulativeDegreeList) - 1])) # generate a random number between the 2 numbers inclusive

    # identify node based on random number generated
    for index in range(len(cummulativeDegreeList)):
        if (randomNum <= cummulativeDegreeList[index]):
        ## if (randomNum < cummulativeDegreeList[index]):
            return graph.nodes()[index]
    return graph.nodes()[index]

'''
Description: Processes the graph at a time step, updating the lifetime, time gap and creating new edge of nodes waking up
Input: Graph and time (for filename)
Output: Returns the updated graph
'''
def processGraph(graph, time):   
    for node in graph.nodes_iter():
        life = graph.node[node]['lifetime'] - 1 # decrement the lifetime of each node
        if(life > 0):   # if lifetime of node has not yet expired
            deg = graph.node[node]['degree']
            slp = graph.node[node]['sleep'] - 1 # decrement time gap        
            graph.add_node(node, lifetime = life, degree = deg, sleep = slp)
            if(slp == 0): # node just woke up
                # add a two-hop edge
                oneHopNode = getReachableNode(graph, node, None)
                if(oneHopNode != -1):
                    twoHopNode = getReachableNode(graph, oneHopNode, node)
                    if(twoHopNode != -1):
                        graph.add_edge(node, twoHopNode)
                        print("* Node", node, "woke up and creates a two-hop edge to node", twoHopNode, "using the random-random triangle-closing model")
                        deg = deg + 1
                        # samples and add a new sleep
                        slp = sampleTimeGap(deg, life)
                        graph.add_node(node, lifetime = life, degree = deg, sleep = slp)
                        
                        # increment degree of twoHopNode
                        lifeDesNode = graph.node[twoHopNode]['lifetime']
                        degDesNode = graph.node[twoHopNode]['degree'] + 1
                        slpDesNode = graph.node[twoHopNode]['sleep']
                        graph.add_node(twoHopNode, lifetime = lifeDesNode, degree = degDesNode, sleep = slpDesNode)   
    return graph

'''
Description: Identifies a random reachable node from a specific node
Input: Graph and a node
Output: Returns a random reachable node from the input node
'''
def getReachableNode(graph, node, exclude):
    # get list of all reachable nodes from node
    
    reachableNodeList = graph.neighbors(node)
    if node in reachableNodeList:
        reachableNodeList.remove(node)
    if(exclude != None):
        reachableNodeList.remove(exclude)
    if(reachableNodeList == []):
        return -1
    # select a random node
    randomNum = int(random.uniform(0, (len(reachableNodeList) - 1))) # generates a random number between the 2 numbers inclusive
    return reachableNodeList[randomNum]

'''
Description: Saves a graph at a particular time step in JSON format
Input: Graph and name of JSON file
Output: JSON file with details of graph
'''
def saveJSON(G, fname):
    global tempNodes 
    json.dump(dict(nodes=[{"name":str(n-1), "group":1 if n in tempNodes else 2} for n in G.nodes()],
              links=[{"value ":1 ,  "target": v-1, "source": u-1} for u,v in G.edges()]),
              open(fname, 'w'), indent=0)
     
'''
Description: Creates an HTML page to visualise the graph using D3JS
Input: Name of a JSON file that contains graph details and a time step value
Output: HTML file to view the graph using D3JS
''' 
def saveHTML(jsonfile, time):
    global visualizehtml
    with open(visualizehtml + "visualize.part1.txt", 'r') as fp1:
        with open(visualizehtml + "visualize.part2.txt", 'r') as fp2:
            with open(networkEvol +  jsonfile + str(time) + ".json.html", 'w') as fp3:
                
                javascript = 'd3.json("' + jsonfile + str(time) + ".json" + '", function(error, graph) {'
                header = "Time: " + str(time) + " "
                header2= "<a href='"+ jsonfile + str(time+1) +".json.html'>Next</a><hr>"
                site = "Flickr - "
                footer ="<hr>"
                fp3.write(site + header + header2 +  fp1.read() + javascript + fp2.read() + footer)
                
main()