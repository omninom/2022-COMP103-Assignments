// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2022T2, Assignment 6
 * Name: Leory Xue
 * Username: xueleor
 * ID: 300607821
 */

import ecs100.*;
import java.io.*;
import java.util.*;
import java.nio.file.*;

public class BusNetworks {

    /** Map of towns, indexed by their names */
    private Map<String,Town> busNetwork = new HashMap<String,Town>();

    /** CORE
     * Loads a network of towns from a file.
     * Constructs a Set of Town objects in the busNetwork field
     * Each town has a name and a set of neighbouring towns
     * First line of file contains the names of all the towns.
     * Remaining lines have pairs of names of towns that are connected.
     */
    public void loadNetwork(String filename) {
        try {
            busNetwork.clear();
            UI.clearText();
            List<String> lines = Files.readAllLines(Path.of(filename));
            String firstLine = lines.remove(0);
            String[] towns = firstLine.split(" ");
            for (String town : towns) {
                busNetwork.put(town, new Town(town));
            }
            for (String line : lines) {
                String[] pair = line.split(" ");
                Town t1 = busNetwork.get(pair[0]);
                Town t2 = busNetwork.get(pair[1]);
                t1.addNeighbour(t2);
                t2.addNeighbour(t1);
            }


            UI.println("Loaded " + busNetwork.size() + " towns:");

        } catch (IOException e) {throw new RuntimeException("Loading data.txt failed" + e);}
    }

    /**  CORE
     * Print all the towns and their neighbours:
     * Each line starts with the name of the town, followed by
     *  the names of all its immediate neighbours,
     */
    public void printNetwork() {
        UI.println("The current network: \n====================");
        for (Town t : busNetwork.values()) {
            ArrayList <String> neighbours = new ArrayList<>();
            for (Town n  : t.getNeighbours()) {
                neighbours.add(n.getName());
            }
            UI.println(t.getName() + " -> " + neighbours);
        }
    }

    /** COMPLETION
     * Return a set of all the nodes that are connected to the given node.
     * Traverse the network from this node in the standard way, using a
     * visited set, and then return the visited set
     */
    public Set<Town> findAllConnected(Town town) {
        Set<Town> visited = new HashSet<Town>();
        findAllConnected(town, visited);
        return visited;

    }
    /** Helper recursive method that traverses the network from the given node
     *  and adds all the nodes it visits to the visited set.
     */
    public void findAllConnected(Town town, Set<Town> visited){
        visited.add(town);
        for (Town n : town.getNeighbours()) {
            if (!visited.contains(n)) {
                findAllConnected(n, visited);
            }
        }
    }

    /**  COMPLETION
     * Print all the towns that are reachable through the network from
     * the town with the given name.
     * Note, do not include the town itself in the list.
     */
    public void printReachable(String name){
        Town town = busNetwork.get(name);
        if (town==null){
            UI.println(name+" is not a recognised town");
        }
        else {
            UI.println("\nFrom "+town.getName()+" you can get to:");
            for (Town t : findAllConnected(town)){
                if (!t.equals(town)){
                    UI.println("  "+t.getName());
                }
            }
        }

    }

    /**  COMPLETION
     * Print all the connected sets of towns in the busNetwork
     * Each line of the output should be the names of the towns in a connected set
     * Works through busNetwork, using findAllConnected on each town that hasn't
     * yet been printed out.
     */
    public void printConnectedGroups() {
        UI.println("Groups of Connected Towns: \n================");
        int groupNum = 1;
        Set<Town> printed = new HashSet<>();
        for (Town t : busNetwork.values()) {
            Set<Town> visited = new HashSet<>();
            findAllConnected(t, visited);
            if (visited.size() > 0 && !printed.contains(t)) {
                UI.println("Group " + groupNum + ":");
                for (Town town : visited) {
                    UI.println("  " + town.getName());
                    printed.add(town);
                }
                groupNum++;
            }
        }

    }

    /**
     * Set up the GUI (buttons and mouse)
     */
    public void setupGUI() {
        UI.addButton("Load", ()->{loadNetwork(UIFileChooser.open());});
        UI.addButton("Print Network", this::printNetwork);
        UI.addTextField("Reachable from", this::printReachable);
        UI.addButton("All Connected Groups", this::printConnectedGroups);
        UI.addButton("Clear", UI::clearText);
        UI.addButton("Quit", UI::quit);
        UI.setWindowSize(1100, 500);
        UI.setDivider(1.0);
        loadNetwork("BusNetworks/data-small.txt");
    }

    // Main
    public static void main(String[] arguments) {
        BusNetworks bnw = new BusNetworks();
        bnw.setupGUI();
    }

}
