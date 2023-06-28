import ecs100.*;
import java.util.*;
import java.awt.Color;
import javax.swing.JColorChooser;

public class StrokeRecord {
    private int currentStrokeCount = 0;
    private double strokelastX;       
    private double strokelastY;       
    private double strokeX;           
    private double strokeY;           
    private Color strokelineColor;    
    private double strokelineWidth;  


    /** 
     * CONSTRUCTOR 
     */
    public StrokeRecord(double lastX, double  lastY, double x , double y, int lineCount, Color lineColor, double lineWidth){
        strokelastX = lastX;
        strokelastY = lastY;
        strokeX = x;
        strokeY= y;
        strokelineColor = lineColor;
        strokelineWidth = lineWidth;
        currentStrokeCount = lineCount;
    }
    
    /** Returns last X value **/
    public double getLastX(){
        return strokelastX;    
    } 
    
    /** Returns last Y value **/
    public double getLastY(){
        return strokelastY;    
    } 
    
    /** Gets x value **/
    public double getX(){
        return strokeX;    
    }
    
    /**  Gets y value **/
    public double getY(){
        return strokeY;    
    } 
    
    /** Returns stroke count **/
    public int getStrokeCount(){
        return currentStrokeCount;    
    } 
    
    /** Returns line color **/
    public Color getColor(){
        return strokelineColor;    
    } 
    
    /** Returns line width **/
    public double getWidth(){
        return strokelineWidth;    
    }    
}