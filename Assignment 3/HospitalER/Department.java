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

/**
 * A treatment Department (Surgery, X-ray room,  ER, Ultrasound, etc)
 * Each department will need
 * - A name,
 * - A maximum number of patients that can be treated at the same time
 * - A Set of Patients that are currently being treated
 * - A Queue of Patients waiting to be treated.
 *    (ordinary queue, or priority queue, depending on argument to constructor)
 */

public class Department{

    private String name;
    private int maxPatients;
    private int patientsTreated = 0;
    Set<Patient> treatmentRoom = new HashSet<Patient>();
    Queue<Patient> waitingRoom = new ArrayDeque<Patient>();
    
    /**
     * Construct a new Department object
     * parameters are the arrival time and the priority.
     */
    public Department(String depName, int maxP, boolean usePriorityQueue){
        name = depName;
        maxPatients = maxP;
        if (usePriorityQueue == true)    {waitingRoom = new PriorityQueue<Patient>();}
        else                            {waitingRoom = new ArrayDeque<Patient>();}
    }
    
    /** Add a patient into the waiting room queue **/
    public void addWaitPatient(Patient p){
        waitingRoom.offer(p);
    }
    
    /** Add a patient into the treatmentRoom **/
    public void addTreatPatient(Patient p){
        treatmentRoom.add(p);
    }

    /** Return the max patients **/
    public int getMaxPatients(){
        return maxPatients;
    }
    
    /** Return the department name **/
    public String getName(){
        return name;
    }
    

    /**
     * Draw the department: the patients being treated and the patients waiting
     * You may need to change the names if your fields had different names
     */
    public void redraw(double y){
        UI.setFontSize(14);
        UI.drawString(name, 0, y-35);
        double x = 10;
        UI.drawRect(x-5, y-30, maxPatients*10, 30);  // box to show max number of patients
        for(Patient p : treatmentRoom){
            p.redraw(x, y);
            x += 10;
        }
        x = 200;
        for(Patient p : waitingRoom){
            p.redraw(x, y);
            x += 10;
        }
    }

}
