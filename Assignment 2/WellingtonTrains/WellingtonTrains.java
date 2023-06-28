// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2022T2, Assignment 2
 * Name: Leory Xue
 * Username: xueleor    
 * ID: 300607821
 */

import ecs100.*;
import java.util.*;
import java.util.Map.Entry;
import java.io.*;
import java.nio.file.*;

/**
 * WellingtonTrains
 * A program to answer queries about Wellington train lines and timetables for
 *  the train services on those train lines.
 *
 * See the assignment page for a description of the program and what you have to do.
 */

public class WellingtonTrains{
    //Fields to store the collections of Stations and Lines
    Map<String, Station> stations = new HashMap<String, Station>(); //all stations, key = name
    Map<String, TrainLine> trainLines = new HashMap<String, TrainLine>(); //all stations, key = name

    // Fields for the suggested GUI.
    private String stationName;        // station to get info about, or to start journey from
    private String lineName;           // train line to get info about.
    private String destinationName;
    private int startTime = 0;         // time for enquiring about

    private static boolean loadedData = false;  // used to ensure that the program is called from main.

    /**
     * main method:  load the data and set up the user interface
     */
    public static void main(String[] args){
        WellingtonTrains wel = new WellingtonTrains();
        wel.loadData();   // load all the data
        wel.setupGUI();   // set up the interface
    }

    /**
     * Load data files
     */
    public void loadData(){
        loadStationData();
        UI.println("Loaded Stations");
        loadTrainLineData();
        UI.println("Loaded Train Lines");
        // The following is only needed for the Completion and Challenge
        loadTrainServicesData();
        UI.println("Loaded Train Services");
        loadedData = true;
        UI.println("Please note that this program is case sensitive :)");
    }

    /**
     * User interface has buttons for the queries and text fields to enter stations and train line
     * You will need to implement the methods here.
     */
    public void setupGUI(){
        UI.addButton("All Stations",        this::listAllStations);
        UI.addButton("Stations by name",    this::listStationsByName);
        UI.addButton("All Lines",           this::listAllTrainLines);
        UI.addTextField("Station",          (String name) -> {this.stationName=name;});
        UI.addTextField("Train Line",       (String name) -> {this.lineName=name;});
        UI.addTextField("Destination",      (String name) -> {this.destinationName=name;});
        UI.addTextField("Time (24hr)",      (String time) ->
            {try{this.startTime=Integer.parseInt(time);}catch(Exception e){UI.println("Enter four digits");}});
        UI.addButton("Lines of Station",    () -> {listLinesOfStation(this.stationName);});
        UI.addButton("Stations on Line",    () -> {listStationsOnLine(this.lineName);});
        UI.addButton("Stations connected?", () -> {checkConnected(this.stationName, this.destinationName);});
        UI.addButton("Next Services",       () -> {findNextServices(this.stationName, this.startTime);});
        UI.addButton("Find Trip",           () -> {findTrip(this.stationName, this.destinationName, this.startTime);});

        UI.addButton("Quit", UI::quit);
        UI.setMouseListener(this::doMouse);

        UI.setWindowSize(900, 400);
        UI.setDivider(0.2);
        // this is just to remind you to start the program using main!
        if (! loadedData){
            UI.setFontSize(36);
            UI.drawString("Start the program from main", 2, 36);
            UI.drawString("in order to load the data", 2, 80);
            UI.sleep(2000);
            UI.quit();
        }
        else {
            UI.drawImage("data/geographic-map.png", 0, 0);
            UI.drawString("Click to list closest stations", 2, 12);
        }
    }

    /** doMouse method which finds 10 closest stations to where user releases mouse **/
    public void doMouse(String action, double x, double y){
        if (action.equals("released")){
            UI.clearText();
            List<Station> allStations = new ArrayList<>(stations.values());         //copy map values to arraylist
            Collections.sort(allStations,
                            (Station s1, Station s2) -> {
                            double dist1 = Math.sqrt((s1.getXCoord() - x)*(s1.getXCoord()-x)+(s1.getYCoord()-y)*(s1.getYCoord()-y));
                            double dist2 = Math.sqrt((s2.getXCoord() - x)*(s2.getXCoord()-x)+(s2.getYCoord()-y)*(s2.getYCoord()-y));
                            if (dist1 < dist2)      {return -1; }   //s1 should be earlier in list 
                            else if (dist1 > dist2 ){return 1; }    //s2 should be earlier in list
                            else                    {return 0; }    //same position
                            });
            for (int i = 0; i < 10; i++){
                double dist = Math.sqrt((allStations.get(i).getXCoord() - x)*(allStations.get(i).getXCoord()-x)+(allStations.get(i).getYCoord()-y)*(allStations.get(i).getYCoord()-y));
                UI.printf("%.02fkm: %s \n", dist, allStations.get(i).getName());
            }
        }
    }

    // Methods for loading data and answering queries

    /** Load the station data from the file **/
    public void loadStationData(){
        stations.clear();
        try {
            List<String> lines = Files.readAllLines(Path.of("data/stations.data"));
            for (String line: lines){
                Scanner sc = new Scanner(line);
                String name = sc.next();
                int zone = sc.nextInt();
                double xpos = sc.nextDouble();
                double ypos = sc.nextDouble();
                Station station = new Station(name, zone,xpos,ypos);
                stations.put(name, station);
            }
        }
        catch(IOException e){UI.println("Failed to load station data: " + e);}
        
    }
    
    /** Load the TrainLine data from the file **/
    public void loadTrainLineData(){
        trainLines.clear();
        try {
            List<String> lines = Files.readAllLines(Path.of("data/train-lines.data"));
            for (String line: lines){
                Scanner sc = new Scanner(line);
                TrainLine trainL = new TrainLine(sc.next());
                trainLines.put(trainL.getName(), trainL);
            }
        }
        catch(IOException e){UI.println("Failed to load station data: " + e);}
        
        try {
            String filename;
            for (String lineName: trainLines.keySet()){
                filename = "data/"+lineName+"-stations.data";
                List<String> lines = Files.readAllLines(Path.of(filename));
                TrainLine currentTrainL = trainLines.get(lineName);
                for (String line : lines){
                    Scanner sc = new Scanner(line);
                    Station station = stations.get(sc.next());
                    if (station!=null && currentTrainL!=null){
                        station.addTrainLine(currentTrainL);
                    }
                    else {UI.println("unknown station "+station);}
                    currentTrainL.addStation(station);
                }
    
            }
        }
        catch(IOException e){UI.println("Failed to load train lines data: " + e);}
    }
    
    /** Load the Train services data from the file **/
    public void loadTrainServicesData(){
        try {
            String filename;
            for (String lineName: trainLines.keySet()){
                filename = "data/"+lineName+"-services.data";
                List<String> lines = Files.readAllLines(Path.of(filename));
                TrainLine currentTrainL = trainLines.get(lineName);
                for (String line : lines){
                    TrainService trainService = new TrainService(currentTrainL);
                    currentTrainL.addTrainService(trainService);
                    Scanner sc = new Scanner(line);
                    while (sc.hasNextInt()){
                        trainService.addTime(sc.nextInt());
                    }
                }
            }
        }
        catch(IOException e){UI.println("Failed to load train services data: " + e);}
    }
    
    
    /** List all stations from the stations map **/
    public void listAllStations(){
        UI.clearText();
        for (Station station : stations.values()){
            UI.println(station.toString());
        }
    }
    
    /** List the stations by natural ordering (alphabetical order) **/
    public void listStationsByName(){
        UI.clearText();
        List<Station> stationsByName = new ArrayList<>(stations.values());      //copy to an array and use natural sorting
        Collections.sort(stationsByName);       
        for (Station station : stationsByName){
            UI.println(station.toString());
        }
    }
    
    /** List all trainLines from the trainLines map **/
    public void listAllTrainLines(){
        UI.clearText();
        for (TrainLine trainLine : trainLines.values()){    
            UI.println(trainLine.toString());
        }
    }
    
    /** List train lines through a given station **/
    public void listLinesOfStation(String stationName){     
        UI.clearText();
        for (TrainLine trainLine : trainLines.values()){
            for (Station station : trainLine.getStations()){
                if (station.getName().equalsIgnoreCase(stationName)){               //if current station name equals textfield station name
                    UI.println(trainLine.toString());
                }
            }
        }   
    }
    
    /** List the stations on a given trainline **/
    public void listStationsOnLine(String lineName){
        UI.clearText();
        for (TrainLine trainLine : trainLines.values()){
            if (lineName != null && lineName.equalsIgnoreCase(trainLine.getName())){                      
                for (Station station : trainLine.getStations()){
                    UI.println(station.toString());
                }
            }
        }
    }   
    
    /** Find a trainline that contains a given station and destination station **/
    public void checkConnected(String stationName, String destinationName){
        UI.clearText();
        Station startStation = new Station("Temp", 0, 0, 0);        //initialising station objects to be used
        Station destStation = new Station("Temp", 0, 0, 0);
        for (String strStation : stations.keySet()){
            if (stationName != null && stationName.equalsIgnoreCase(strStation)) {startStation = stations.get(stationName);}
            if (destinationName != null && destinationName.equalsIgnoreCase(strStation)) {destStation = stations.get(destinationName);}
        }
        for (TrainLine trainLine : trainLines.values()){
            if (trainLine.getStations().contains(startStation) && trainLine.getStations().contains(destStation) && trainLine.getStations().indexOf(startStation) < trainLine.getStations().indexOf(destStation)){
                UI.println("The "+trainLine.getName()+" line goes from "+startStation.getName()+" to "+destStation.getName());
                UI.println("The trip goes through "+findZonesTravelled( startStation, destStation)+" fare zones.");
                break;
            }
        }
 
    }
    
    /** Find the closest index to specified target in a list **/
    public int findClosestIndex(List<Integer> times, int target){
        int closetIndex = -1;                                                 //With logic help from: https://stackoverflow.com/questions/14808485/find-largest-number-closest-to-given-number-in-an-array
        for (int index = 1; index < times.size(); index++) {
            if (times.get(index) > target) {
                if (closetIndex == -1){
                    closetIndex = index;        //outside of target area
                }
                else if (times.get(index) < times.get(closetIndex)){
                    closetIndex = index;
                }
            }
        }
        return closetIndex;
    }
    
    /** Finds the zones travelled between two stations **/
    public int findZonesTravelled(Station startStation, Station destStation){
        int zone = 0;
        if (startStation.getZone() > destStation.getZone()){                    //zone should be calculated using the larger zone - smaller zone + 1
            zone = startStation.getZone() - destStation.getZone() + 1;
        }
        else if (startStation.getZone() < destStation.getZone()){
            zone = destStation.getZone() - startStation.getZone() + 1;
        }
        return zone;
    }
    
    
    /** Find the next time for a service of a station **/
    public void findNextServices(String stationName, int startTime){
        UI.clearText();
        Station station = new Station("temp", 0, 0, 0);         //initialising station object to be used
        int indexOfStation = 0;                                 //index of the station used in the list of all times for that station
        List<Integer> times = new ArrayList<>();                //list for times of a specific station from a train line
        for (String strStation : stations.keySet()){
            if (stationName != null && stationName.equalsIgnoreCase(strStation)) {station = stations.get(stationName);}     //set station object to text field station
        }
        for (TrainLine trainLine : station.getTrainLines()){
            for (TrainService trainService : trainLine.getTrainServices()){
                indexOfStation = trainLine.getStations().indexOf(station);      //get index of the station from station list
                times.add(trainService.getTimes().get(indexOfStation));         //from the list of times get the use the index of the station and add it to the times list
            }
            int closestIndex = findClosestIndex(times, startTime);
            if (closestIndex == -1){
                UI.println("Sorry! Time may be too early or late for a service from this station");     //error handling
                break;
            }
            else {
                UI.println("Next service on "+trainLine.getName()+" from "+station.getName()+" is at "+trainLine.getTrainServices().get(closestIndex).getTimes().get(indexOfStation));
            }
            times.clear();
        }
    }
    
    /** Find a trip based on starting station, destination, and starting time **/
    public void findTrip(String stationName, String destinationName, int startTime){
        UI.clearText();
        Station startStation = new Station("Temp", 0, 0, 0);    
        Station destStation = new Station("Temp", 0, 0, 0);
        int indexOfStartStation = 0;
        int indexOfDestStation = 0;
        List<Integer> startTimes = new ArrayList<>();
        for (String strStation : stations.keySet()){
            if (stationName != null && stationName.equalsIgnoreCase(strStation)) {startStation = stations.get(stationName);}
            if (destinationName != null && destinationName.equalsIgnoreCase(strStation)) {destStation = stations.get(destinationName);}
        }
        for (TrainLine trainLine : trainLines.values()){
            if (trainLine.getStations().contains(startStation) && trainLine.getStations().contains(destStation) && trainLine.getStations().indexOf(startStation) < trainLine.getStations().indexOf(destStation)){
                for (TrainService trainService : trainLine.getTrainServices()){
                    indexOfStartStation = trainLine.getStations().indexOf(startStation);
                    indexOfDestStation = trainLine.getStations().indexOf(destStation);
                    startTimes.add(trainService.getTimes().get(indexOfStartStation));
                }
                int closestIndex = findClosestIndex(startTimes, startTime);
                if (closestIndex == -1){
                    UI.println("Sorry! Time may be too early or late for a service from this station");
                    break;
                }
                else {
                    int timeStart = trainLine.getTrainServices().get(closestIndex).getTimes().get(indexOfStartStation);
                    UI.println(trainLine.getTrainServices().get(closestIndex).toString()+" leaves "+startStation.getName()+" at "+timeStart+", arrives "+destStation.getName()+" at "+trainLine.getTrainServices().get(closestIndex).getTimes().get(indexOfDestStation));
                    UI.println("The trip goes through "+findZonesTravelled( startStation, destStation)+" fare zones.");
                    startTimes.clear();
                }
                break;
            }
        }
    }
}

