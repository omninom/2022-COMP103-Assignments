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
import java.awt.Color;
import javax.swing.JColorChooser;
import javax.swing.*;

/** Pencil   */
public class Pencil{
    //FIELDS
    private double lastX;
    private double lastY;
    private int strokeCount = 1;
    private double strokeWidth = 5;
    private Color strokeColor = Color.black;
    private JButton buttonColor;                    //Jbutton used to color the button background
    
    private Stack<StrokeRecord> history = new Stack<StrokeRecord>(); // Undo Stack
    private Stack<StrokeRecord> redoStack = new Stack<StrokeRecord>(); // Redo Stack

    /**
     * Setup the GUI
     */
    public void setupGUI(){
        UI.setMouseMotionListener(this::doMouse);
        UI.addButton("Undo", this::undo);
        UI.addButton("Redo", this::redo);
        UI.addSlider("Line Width", 1, 20, this::setLineWidth);
        buttonColor = UI.addButton("Color", this::chooseColor);
        UI.addButton("Quit", UI::quit);
        UI.setLineWidth(3);
        UI.setDivider(0.0);
    }

    /**
     * Respond to mouse events
     */
    public void doMouse(String action, double x, double y) {
        UI.setColor(this.strokeColor);
        UI.setLineWidth(this.strokeWidth);
        if (action.equals("pressed")){
            lastX = x;
            lastY = y;
        }
        else if (action.equals("dragged")){
            UI.drawLine(lastX, lastY, x, y);
            history.push(new StrokeRecord(lastX, lastY, x, y, strokeCount, strokeColor, strokeWidth)); 
            lastX = x;
            lastY = y;
        }
        else if (action.equals("released")){
            UI.println(strokeCount);
            UI.drawLine(lastX, lastY, x, y);
            history.push(new StrokeRecord(lastX, lastY, x, y, strokeCount, strokeColor, strokeWidth)); 
            strokeCount += 1;                           //a released press means the end of a stroke
        }
    }
    
    /** Undo method **/
    public void undo(){
        if (history.isEmpty()){
            UI.printMessage("Nothing to undo"); 
            return;
        }
        UI.clearGraphics();
        int countStroke = history.peek().getStrokeCount() ;           //set stroke count to the top item of the stacks count, latest stroke
        while (countStroke == history.peek().getStrokeCount()){       //  while the current count of the stroke is the same count of the stroke at the top of the stack
            StrokeRecord stroke = history.pop();                    // remove the strokes with identifier countStroke from the stack
            redoStack.push(new StrokeRecord(stroke.getLastX(), stroke.getLastY(), stroke.getX(), stroke.getY(), countStroke, stroke.getColor(),  stroke.getWidth())); //add this action to the redo stack
            if (history.isEmpty()){ return; }
        } 
        
        countStroke -= 1;   // the total count of strokes must decrease by one as a stroke has been removed
        
        Stack<StrokeRecord> redrawStack = (Stack<StrokeRecord>)history.clone();     //clone the stack which has the last stroke removed
        while (!redrawStack.isEmpty()){
            StrokeRecord redrawStroke  = redrawStack.pop(); 
            UI.setColor(redrawStroke.getColor());
            UI.setLineWidth(redrawStroke.getWidth());
            UI.drawLine(redrawStroke.getLastX(), redrawStroke.getLastY(), redrawStroke.getX(), redrawStroke.getY());    //redraw
        }
    }
    
    /** Redo method **/
    public void redo(){
        if (redoStack.isEmpty()){
            UI.printMessage("Nothing to redo"); 
            return;
        }

        int countStroke = redoStack.peek().getStrokeCount();
        
        while (countStroke == redoStack.peek().getStrokeCount()){         //similar structure to redraw section in undo function, however for redo;
            StrokeRecord stroke  = redoStack.pop(); 
            UI.setColor(stroke.getColor());
            UI.setLineWidth(stroke.getWidth());
            UI.drawLine(stroke.getLastX(), stroke.getLastY(), stroke.getX(), stroke.getY());
            history.push(new StrokeRecord(stroke.getLastX(), stroke.getLastY(), stroke.getX(), stroke.getY(), countStroke, stroke.getColor(), stroke.getWidth()));      //adds action back into the history stack
            if (redoStack.isEmpty()){return;}           //error handling
        }
    }

    /** Choosing color method **/
    public void chooseColor(){
        this.strokeColor = JColorChooser.showDialog(null, "Choose Color", this.strokeColor);
        buttonColor.setBackground(strokeColor);
        UI.setColor(this.strokeColor);
    }
    
    /** Setting line width **/
    public void setLineWidth(double width){
        this.strokeWidth = width;
    }
    
    public static void main(String[] arguments){
        new Pencil().setupGUI();
    }

}
