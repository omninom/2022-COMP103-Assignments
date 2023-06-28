// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2022T2, Assignment 5
 * Name:
 * Username:
 * ID:
 */

import ecs100.*;
import java.awt.Color;
import java.util.*;
import java.io.*;
import java.nio.file.*;

/** <description of class OrganisationChart>
 */

public class OrganisationChart {

    // Fields
    private Position organisation;             // the root of the current organisational chart 
    private Position pressedPosition = null;   // the position on which the mouse was pressed
    private Position selectedPosition = null;  // the selected position on which we can modify
                                               //  the attributes.
    private boolean newPosition = false;       // adding a new Position to tree
                                               //  the user entered

    private String newRole = null;

    // constants for the layout
    public static final double NEW_LEFT = 10;  // left of the new position Icon
    public static final double NEW_TOP = 10;   // top of the new position Icon

    public static final double ICON_X = 40;    // location and size of the remove icon
    public static final double ICON_Y = 100;   
    public static final double ICON_RAD = 20;   


    /**
     * Set up the GUI (buttons and mouse)
     */
    public void setupGUI(){
        UI.setMouseMotionListener( this::doMouse );
        UI.addTextField("Change Role", this::setRole);
        UI.addButton("Load test tree",  this::makeTestTree);
        UI.addButton("Save tree",  this::saveFile);
        UI.addButton("Load tree",  this::loadFile);
        UI.addButton("Quit", UI::quit);
        UI.setWindowSize(1100,500);
        UI.setDivider(0);
    }

    /**
     * initialise the root of the organisation
     */
    public void initialiseChart() {
        organisation = new Position("CEO");   // Set the root node of the organisation
        redraw();
    }
    /** If a Position has been selected, update the name of the role of this position */
    public void setRole (String v){
        if (selectedPosition != null) {
            selectedPosition.setRole(v);
        }
        redraw();
    }

    /**
     * Most of the work is initiated by the mouse.
     * 
     * The action depends on where the mouse is pressed:
     *   on the new icon,
     *   a Position in the tree, or
     * and where it is released:
     *   on the same Position,
     *   another Position in the tree,
     *   on the delete Icon, or
     *   empty space
     * 
     * See the table in the assignment description.
     * The method follows the structure of the table.
     */
    public void doMouse(String action, double x, double y){

        if (action.equals("pressed")){
            //initialise
            newPosition = false;
            pressedPosition = null;

            if (onNewIcon(x, y)) {// adding a new vacant Position to tree
                newPosition = true;
            }
            else { // acting on an existing Position
                pressedPosition = findPosition(x, y, organisation);
                if (pressedPosition != null) {
                    pressedPosition.draw(false, true);
                }
            }
        }
        else if (action.equals("dragged")){     //Challenge: while dragged redraw
            if (pressedPosition != null) {
                pressedPosition.moveOffset(x);
                redraw();
            }
        }
        else if (action.equals("released")){
            Position targetPosition = findPosition(x, y, organisation);

            // pressed on "new" icon, released on a target
            if (newPosition && targetPosition != null){
                Position newP = new Position();
                addNewPosition(newP, targetPosition);     // Method to complete!
            }
            // pressed and released on a Position 
            else if (pressedPosition != null && targetPosition == pressedPosition) {
                // selecting a position
                selectedPosition = targetPosition;
            }
            // pressed on a Position and released on empty space
            else if (pressedPosition != null && targetPosition == null && ! onRemoveIcon(x, y)){
                // move the Position to left or right
                pressedPosition.moveOffset(x);
                }
            // pressed on a Position 
            else if (pressedPosition != null){
                if (targetPosition != null ) {
                    // moving pressed position to target
                    movePosition(pressedPosition, targetPosition);  // Method to complete!
                }
                else if (onRemoveIcon(x, y) ){
                    // removing Position from tree
                    removePosition(pressedPosition);                // Method to complete!
                }
            }
            this.redraw();
        }
    }


    //  METHODS FOR YOU TO COMPLETE ===============================
    
    // Drawing the tree  =========================================
    /** [STEP 1:]
     *  Recursive method to draw all nodes in a subtree, given the root node.
     *  (The provided code just draws the root node;
     *  you need to make it draw all the nodes.)
     */
    private void drawTree(Position pos) {
        if (pos==selectedPosition){
            pos.drawHighlighted();
        }
        else {
            pos.draw();
        }
        //draw the nodes under pos
        for (Position teamM : pos.getTeam()){
            drawTree(teamM);
        }
    }

    /** 
     * Find and return a Position that is currently placed over the point (x,y). 
     * Must do a recursive search of the subtree whose root is the given Position.
     * [STEP 2:] 
     *    Returns a Position if it finds one,
     *    Returns null if it doesn't.
     * If (x,y) is under two Positions, it should return the top one.
     */
    private Position findPosition(double x, double y, Position pos){
        Position ans = null;
        if (pos==null) { return null; }
        if (pos.on(x,y)) { ans = pos; }
        for (Position teamM : pos.getTeam()){
            Position found = findPosition(x,y,teamM);
            if (found!=null) { ans = found; }
        }
        return ans;
    }

    /** [STEP 2:] 
     * Add the new position to the target's team.
     * Check the arguments are valid first.
     */
    public void addNewPosition(Position newPos, Position target){
        if ((newPos == null) || (target == null)){return;}      //invalid arguments
        target.addToTeam(newPos);
    }

    /** [STEP 2:] 
     *    Move a current position (pos) to another position (target)
     *    by adding the position to the team of the target,
     *    (and bringing the whole subtree of the position with them)
     *    Check the arguments are valid first.
     *
     * [COMPLETION:]
     *   Moving any position to a target that is in the
     *   position's subtree is a problem and should not be allowed. (Why?)
     *   (one consequence is that the CEO position can't be moved at all)
     */
    private void movePosition(Position pos, Position target) {
        if ((pos == null) || (target == null)){return;}   //invalid arguments.
        if (!inSubtree(target, pos))    {target.addToTeam(pos);}
    }

    /** [STEP 2:]
     * Remove a position by removing it from the tree completely.
     * The position cannot be a manager of another position.
     * If this removes the current selected position, then there
     *  should now be no selected position
     */
    public void removePosition(Position pos){
        if (!pos.isManager()){
            pos.removeFromTeam(pos);
            selectedPosition = null;
        }
    }

    /** [COMPLETION:]
     * Return true if position is in the subtree, and false otherwise
     * Uses == to determine node equality
     *  Check if positition is the same as the root of subTree
     *  if not, check if in any of the subtrees of the team members of the root
     *  (recursive call, which must return true if it finds the position)
     */
    private boolean inSubtree(Position pos, Position subtree) {
        if (pos == subtree) {return true;}
        for (Position teamM : subtree.getTeam()){
            if (inSubtree(pos, teamM)) {return true;}
        }
        return false;
    }

    /** Save tree to file **/
    public void saveFile() {
        String fname = UIFileChooser.save("Chose file to save to");
        try {
            PrintStream out = new PrintStream(fname);                   //create printStream
            writeTo(organisation, out);
            out.close();
        } catch (IOException e) {UI.println("File failure: " + e);}
    }

    /** method that writes to a PrintStream using recursion **/
    public void writeTo(Position pos, PrintStream out){
       if (pos == null) {return;}
        out.println(pos.toStringFull());
        for (Position teamM : pos.getTeam()){
            writeTo(teamM, out);
        }
    }
    /** load tree to file **/
    public void loadFile() {
        String fname = UIFileChooser.open("Chose file to load from");
        try {
            Scanner sc = new Scanner(new File(fname));                  //create scanner
            while (sc.hasNext()){
                Position pos = new Position(sc.next(), sc.nextInt());
                int teamSize = sc.nextInt();
                if (teamSize > 0) {
                    for (int i = teamSize; i > 0; i--){
                        sc.nextLine();
                        pos.addToTeam(new Position(sc.next(), sc.nextInt()));
                    }
                }

            }
            sc.close();
            redraw();

        } catch (IOException e) {UI.println("File failure: " + e);}
    }



    /**
     * Redraw the entire organisation chart.
     */
    private void redraw() {
        UI.clearGraphics();
        drawTree(organisation);
        drawNewIcon();
        drawRetireIcon();
    }

    // OTHER DRAWING METHODS =======================================
    /**
     * Redraw the new Person box
     */
    private void drawNewIcon(){
        UI.setColor(Position.BACKGROUND_COL);
        UI.fillRect(NEW_LEFT,NEW_TOP,Position.WIDTH, Position.HEIGHT);
        UI.setColor(Color.black);
        UI.drawRect(NEW_LEFT,NEW_TOP,Position.WIDTH, Position.HEIGHT);
        UI.drawString("NEW", NEW_LEFT+8, NEW_TOP+Position.HEIGHT/2-5);
        UI.drawString("POSN", NEW_LEFT+5, NEW_TOP+Position.HEIGHT/2+10);
    }

    /**
     * Redraw the remove Icon
     */
    private void drawRetireIcon(){
        UI.setColor(Color.red);
        UI.setLineWidth(5);
        UI.drawOval(ICON_X-ICON_RAD, ICON_Y-ICON_RAD, ICON_RAD*2, ICON_RAD*2);
        double off = ICON_RAD*0.68;
        UI.drawLine((ICON_X - off), (ICON_Y - off), (ICON_X + off), (ICON_Y + off));
        UI.setLineWidth(1);
        UI.setColor(Color.black);
    }

    /** is the mouse position on the New Position box */
    private boolean onNewIcon(double x, double y){
        return ((x >= NEW_LEFT) && (x <= NEW_LEFT + Position.WIDTH) &&
            (y >= NEW_TOP) && (y <= NEW_TOP + Position.HEIGHT));
    }

    /** is the mouse position on the remove icon */
    private boolean onRemoveIcon(double x, double y){
        return (Math.abs(x - ICON_X) < ICON_RAD) && (Math.abs(y - ICON_Y) < ICON_RAD);
    }


    // Testing ==============================================
    /**
     * Makes an initial tree so you can test your program
     */
    private void makeTestTree(){
        organisation = new Position("CEO");
        Position aa = new Position("VP1");
        Position bb = new Position("VP2");
        Position cc = new Position("VP3");
        Position dd = new Position("VP4");
        Position a1 = new Position("AL1");
        Position a2 = new Position("AL2");
        Position b1 = new Position("AS");
        Position b2 = new Position("DPA");
        Position d1 = new Position("DBP");
        Position d2 = new Position("SEP");
        Position d3 = new Position("MSP");

        organisation.addToTeam(aa); aa.setOffset(-160);
        organisation.addToTeam(bb); bb.setOffset(-50);
        organisation.addToTeam(cc); cc.setOffset(15);
        organisation.addToTeam(dd); dd.setOffset(150);

        aa.addToTeam(a1); a1.setOffset(-35);
        aa.addToTeam(a2); a2.setOffset(25);
        bb.addToTeam(b1); b1.setOffset(-25);
        bb.addToTeam(b2); b2.setOffset(35);
        dd.addToTeam(d1); d2.setOffset(-60);
        dd.addToTeam(d2); 
        dd.addToTeam(d3); d3.setOffset(60);
        organisation.addToTeam(aa); aa.setOffset(-160);

        selectedPosition = null;
        this.redraw();
    }

    //* Test for printing out the tree structure, indented text */
    private void printTree(Position posn, String indent){
        UI.println(indent+posn+ " " +
            (posn.getManager()==null?"noM":"hasM") + " " +
            posn.getTeam().size()+" reports");
        String subIndent = indent+"  ";
        for (Position tm : posn.getTeam()){
            printTree(tm, subIndent);
        }
    }

    // Main
    public static void main(String[] arguments) {
        OrganisationChart oc = new OrganisationChart();
        oc.setupGUI();
        oc.initialiseChart();
    }
        

}
