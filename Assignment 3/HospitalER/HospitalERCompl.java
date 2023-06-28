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
import java.io.*;
import java.awt.Color;

/**
 * Simulation of a Hospital ER
 * 
 * The hospital has a collection of Departments, including the ER department, each of which has
 *  and a treatment room.
 * 
 * When patients arrive at the hospital, they are immediately assessed by the
 *  triage team who determine the priority of the patient and (unrealistically) a sequence of treatments 
 *  that the patient will need.
 *
 * The simulation should move patients through the departments for each of the required treatments,
 * finally discharging patients when they have completed their final treatment.
 *
 *  READ THE ASSIGNMENT PAGE!
 */

public class HospitalERCompl{
    // Fields for recording the patients waiting in the waiting room and being treated in the treatment room
    private static final int MAX_PATIENTS = 5;   // max number of patients currently being treated
    Map<String, Department> departments = new HashMap<String, Department>(); //all departments, key = name
    
    // fields for the statistics
    private int numTreated = 0;
    private double totalWait = 0;
    private int pri1NumTreated = 0;
    private double pri1TotalWait = 0;
    private int pri2NumTreated = 0;
    private int pri3NumTreated = 0;
    Map<String, List<Integer>> depTreatmentTimes = new HashMap<String, List<Integer>>(); //value = list, key = department name
    Map<String, List<Integer>> priWaitTimes = new HashMap<String, List<Integer>>(); //value = list, key = priority
    
    // Fields for the simulation
    private boolean running = false;
    private int time = 0; // The simulated time - the current "tick"
    private int delay = 300;  // milliseconds of real time for each tick

    // fields controlling the probabilities.
    private int arrivalInterval = 5;   // new patient every 5 ticks, on average
    private double probPri1 = 0.1; // 10% priority 1 patients
    private double probPri2 = 0.2; // 20% priority 2 patients
    private Random random = new Random();  // The random number generator.

    /**
     * Construct a new HospitalERCompl object, setting up the GUI, and resetting
     */
    public static void main(String[] arguments){
        HospitalERCompl er = new HospitalERCompl();
        er.setupGUI();
        er.reset(false);   // initialise with an ordinary queue.
    }        

    /**
     * Set up the GUI: buttons to control simulation and sliders for setting parameters
     */
    public void setupGUI(){
        UI.addButton("Reset (Queue)", () -> {this.reset(false); });
        UI.addButton("Reset (Pri Queue)", () -> {this.reset(true);});
        UI.addButton("Start", ()->{if (!running){ run(); }});   //don't start if already running!
        UI.addButton("Pause & Report", ()->{running=false;});
        UI.addSlider("Speed", 1, 400, (401-delay),
            (double val)-> {delay = (int)(401-val);});
        UI.addSlider("Av arrival interval", 1, 50, arrivalInterval,
            (double val)-> {arrivalInterval = (int)val;});
        UI.addSlider("Prob of Pri 1", 1, 100, probPri1*100,
            (double val)-> {probPri1 = val/100;});
        UI.addSlider("Prob of Pri 2", 1, 100, probPri2*100,
            (double val)-> {probPri2 = Math.min(val/100,1-probPri1);});
        UI.addButton("Quit", UI::quit);
        UI.setWindowSize(1000,600);
        UI.setDivider(0.5);
    }

    /**
     * Reset the simulation:
     *  stop any running simulation,
     *  reset the waiting and treatment rooms
     *  reset the statistics.
     */
    public void reset(boolean usePriorityQueue){
        running=false;
        UI.sleep(2*delay);  // to make sure that any running simulation has stopped
        time = 0;           // set the "tick" to zero.
        
        // reset the departments and the statistics.
        departments.clear();
        depTreatmentTimes.clear();
        numTreated = 0;
        totalWait = 0;
        
        
        departments.put("ER", new Department("ER", 5, usePriorityQueue));
        departments.put("Surgery", new Department("Surgery", 8, usePriorityQueue));
        departments.put("X-ray", new Department("X-ray", 2, usePriorityQueue));
        departments.put("MRI", new Department("MRI", 5, usePriorityQueue));
        departments.put("Ultrasound", new Department("Ultrasound", 1, usePriorityQueue));
        
        depTreatmentTimes.put("ER", new ArrayList<Integer>());
        depTreatmentTimes.put("Surgery", new ArrayList<Integer>());
        depTreatmentTimes.put("X-ray", new ArrayList<Integer>());
        depTreatmentTimes.put("MRI", new ArrayList<Integer>());
        depTreatmentTimes.put("Ultrasound", new ArrayList<Integer>());
        
        priWaitTimes.put("1", new ArrayList<Integer>());
        priWaitTimes.put("2", new ArrayList<Integer>());
        priWaitTimes.put("3", new ArrayList<Integer>());

        UI.clearGraphics();
        UI.clearText();
    }

    /**
     * Main loop of the simulation
     */
    public void run(){
        if (running) { return; } // don't start simulation if already running one!
        running = true;
        while (running){         // each time step, check whether the simulation should pause.
            time++;
            List<Patient> toRemove = new ArrayList<>();                                     //temp list to hold patients to be discharged     
            //each loop increments the time tick
            for (Department d : departments.values()){
                toRemove.clear();
                for (Patient p: d.treatmentRoom){
                    if (!p.completedCurrentTreatment() && !p.noMoreTreatments()){           // if patient hasn't completed current treatment and has more treatments
                        p.advanceTreatmentByTick();
                    }
                    else if (p.completedCurrentTreatment()){                            
                        toRemove.add(p);                         
                        if (p.getPriority() == 1)   {pri1NumTreated++;}
                        else if (p.getPriority() == 2)   {pri2NumTreated++;}
                        else if (p.getPriority() == 3)   {pri3NumTreated++;}
                        numTreated++;
                        depTreatmentTimes.get(p.getCurrentTreatment()).add(p.getTreatmentTime());           //statistic that adds the treatment time of a patient to respective list
                        
                        if (!p.noMoreTreatments()){                                         //if patient still has treatments continue
                            p.incrementTreatmentNumber();                   
                            if (!p.noMoreTreatments()){                                     //we must check again if the patient has any treatments before we continue
                                for (Department dep : departments.values()){                //iterate through the departments to find the department we want to move to
                                    if (p.getCurrentTreatment() == dep.getName()){
                                        dep.addWaitPatient(p);
                                    }
                                } 
                            }
                        }
                        if (p.noMoreTreatments()){                                          //if the patient has no more treatments discharge
                            UI.println(time+ ": Discharge: " + p);
                        }
                    }
                    
                }
                d.treatmentRoom.removeAll(toRemove); 
            }
                
            for (Department d : departments.values()){                                     //add a tick to every patient in a waiting room
                for (Patient p: d.waitingRoom){
                    p.waitForATick();
                }
            }
            
            for (Department d : departments.values()){
                if (d.treatmentRoom.size() < d.getMaxPatients() && !d.waitingRoom.isEmpty()){                               //if treatment room has space move a patient from waiting to treatment
                    Patient p = d.waitingRoom.peek();
                    d.treatmentRoom.add(p);
                    totalWait += p.getWaitingTime();
                    if (p.getPriority() == 1)   {pri1TotalWait += p.getWaitingTime();   priWaitTimes.get("1").add(p.getWaitingTime());}         //statistics
                    else if (p.getPriority() == 2)   {priWaitTimes.get("2").add(p.getWaitingTime());}
                    else if (p.getPriority() == 3)   {priWaitTimes.get("3").add(p.getWaitingTime());}
                    d.waitingRoom.remove();
                }
            }
            
            // Get any new patient that has arrived and add them to the waiting room
            if (time==1 || Math.random()<1.0/arrivalInterval){
                Patient newPatient = new Patient(time, randomPriority());
                UI.println(time+ ": Arrived: "+newPatient);
                String currentTreatment = newPatient.getCurrentTreatment();
                for (Department d : departments.values()){
                    if (currentTreatment == d.getName()){
                        d.addWaitPatient(newPatient);
                    }
                }
            }

            redraw();
            UI.sleep(delay);
        }
        // paused, so report current statistics
        reportStatistics();
    }

    // Additional methods used by run() (You can define more of your own)

    /**
     * Report summary statistics about all the patients that have been discharged.
     * (Doesn't include information about the patients currently waiting or being treated)
     * The run method should have been recording various statistics during the simulation.          (INCLUDING CHALLENGE)
     */
    public void reportStatistics(){
        UI.printf("Discharged %d patients with average waiting time of %.02f minutes \n", numTreated, totalWait/numTreated);
        UI.printf("Discharged %d priority 1 patients with average waiting time of %.02f minutes", pri1NumTreated, pri1TotalWait/pri1NumTreated);
        UI.clearGraphics();
        UI.setColor(Color.black);
        UI.setFontSize(20);
        
        //TOTAL PATIENTS TREATED SORTED BY PRIORITY GRAPH
        UI.drawString("Total patients treated sorted by priority", 30, 30);
        UI.setColor(Color.green);
        UI.fillRect(30, 50, (double)pri3NumTreated/numTreated*400, 25);         //pri3 bar
        UI.setColor(Color.yellow);
        UI.fillRect(30, 80, (double)pri2NumTreated/numTreated*400, 25);         //pri2 bar
        UI.setColor(Color.red);
        UI.fillRect(30, 110, (double)pri1NumTreated/numTreated*400, 25);        //pri1 bar
        UI.setColor(Color.black);
        UI.drawLine(30, 50, 30, 145);                                           //y axis
        UI.drawLine(30, 145, 430, 145);                                         //x axis
        UI.setFontSize(11);
        UI.drawString(String.valueOf(pri3NumTreated), (double)pri3NumTreated/numTreated*400+50, 65);
        UI.drawString(String.valueOf(pri2NumTreated), (double)pri2NumTreated/numTreated*400+50, 95);
        UI.drawString(String.valueOf(pri1NumTreated), (double)pri1NumTreated/numTreated*400+50, 125);
        
        //Average treatment time per department GRAPH
        double ERtreatmentTime = depTreatmentTimes.get("ER").stream().mapToInt(val -> val).average().orElse(0.0);
        double SurgeryTreatmentTime = depTreatmentTimes.get("Surgery").stream().mapToInt(val -> val).average().orElse(0.0);
        double XrayTreatmentTime = depTreatmentTimes.get("X-ray").stream().mapToInt(val -> val).average().orElse(0.0);
        double MRItreatmentTime = depTreatmentTimes.get("MRI").stream().mapToInt(val -> val).average().orElse(0.0);
        double UltrasoundtreatmentTime = depTreatmentTimes.get("Ultrasound").stream().mapToInt(val -> val).average().orElse(0.0);
        UI.setFontSize(20);
        UI.drawString("Average treatment time per department in mins", 30, 170);
        UI.drawLine(100, 185, 100, 310);                                       //y axis
        UI.drawLine(100, 310, 400, 310);                                        //x axis
        UI.setFontSize(11);
        UI.drawString("ER", 10, 200);
        UI.drawString("Surgery", 10, 225);
        UI.drawString("X-ray", 10, 250);
        UI.drawString("MRI", 10, 275);
        UI.drawString("Ultrasound", 10, 300);
        UI.setLineWidth(5);
        UI.drawLine(100, 195, 100+ERtreatmentTime, 195);    //ER time line          printing avg from list help: https://stackoverflow.com/questions/10791568/calculating-average-of-an-array-list
        UI.drawString(String.valueOf(Math.round(ERtreatmentTime*100.0)/100.0), 125+ERtreatmentTime, 195);
        UI.drawLine(100, 220, 100+SurgeryTreatmentTime, 220);    //Surgery time line
        UI.drawString(String.valueOf(Math.round(SurgeryTreatmentTime*100.0)/100.0), 125+SurgeryTreatmentTime, 220);
        UI.drawLine(100, 245, 100+XrayTreatmentTime, 245);    //X-ray time line
        UI.drawString(String.valueOf(Math.round(XrayTreatmentTime*100.0)/100.0), 125+XrayTreatmentTime, 245);
        UI.drawLine(100, 270, 100+MRItreatmentTime, 270);    //MRI time line
        UI.drawString(String.valueOf(Math.round(MRItreatmentTime*100.0)/100.0), 125+MRItreatmentTime, 270);
        UI.drawLine(100, 295, 100+UltrasoundtreatmentTime, 295);    //Ultrasound time line
        UI.drawString(String.valueOf(Math.round(UltrasoundtreatmentTime*100.0)/100.0), 125+UltrasoundtreatmentTime, 296);
        
        //AVERAGE WAITING TIME BY PRIORIRTY GRAPH
        double pri1Time = priWaitTimes.get("1").stream().mapToInt(val -> val).average().orElse(0.0);
        double pri2Time = priWaitTimes.get("2").stream().mapToInt(val -> val).average().orElse(0.0);
        double pri3Time = priWaitTimes.get("3").stream().mapToInt(val -> val).average().orElse(0.0);
        UI.setFontSize(20);
        UI.setLineWidth(0);
        UI.drawString("Average waiting time by priority in mins", 30, 330);
        UI.drawLine(100, 345, 100, 470);
        UI.drawLine(100, 470, 400, 470);
        UI.setFontSize(11);
        UI.drawString("Priority 3", 10, 385);
        UI.drawString("Priority 2", 10, 410);
        UI.drawString("Priority 1", 10, 435);
        UI.setLineWidth(5);
        UI.setColor(Color.green);
        UI.drawLine(100, 380, 100+pri3Time, 380);    //Average wait time for priority 3
        UI.setColor(Color.yellow);
        UI.drawLine(100, 405, 100+pri2Time, 405);    //Average wait time for priority 2
        UI.setColor(Color.red);
        UI.drawLine(100, 430, 100+pri1Time, 430);    //Average wait time for priority 1
        UI.setColor(Color.black);
        UI.drawString(String.valueOf(Math.round(pri3Time*100.0)/100.0), 125+pri3Time, 380);
        UI.drawString(String.valueOf(Math.round(pri2Time*100.0)/100.0), 125+pri2Time, 405);
        UI.drawString(String.valueOf(Math.round(pri1Time*100.0)/100.0), 125+pri1Time, 430);
        
        
    }


    // HELPER METHODS FOR THE SIMULATION AND VISUALISATION
    /**
     * Redraws all the departments
     */
    public void redraw(){
        UI.clearGraphics();
        UI.setFontSize(14);
        UI.setLineWidth(0);
        UI.drawString("Treating Patients", 5, 15);
        UI.drawString("Waiting Queues", 200, 15);
        UI.drawLine(0,32,400, 32);
        double y = 80;
        
        // Draw the treatment room and the waiting room:
        for (Department d : departments.values()){
            d.redraw(y);
            y+=50;
        }
    }

    /** 
     * Returns a random priority 1 - 3
     * Probability of a priority 1 patient should be probPri1
     * Probability of a priority 2 patient should be probPri2
     * Probability of a priority 3 patient should be (1-probPri1-probPri2)
     */
    private int randomPriority(){
        double rnd = random.nextDouble();
        if (rnd < probPri1) {return 1;}
        if (rnd < (probPri1 + probPri2) ) {return 2;}
        return 3;
    }
}
