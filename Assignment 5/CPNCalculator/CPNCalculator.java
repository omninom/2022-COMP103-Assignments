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

/** 
 * Calculator for Cambridge-Polish Notation expressions
 * (see the description in the assignment page)
 * User can type in an expression (in CPN) and the program
 * will compute and print out the value of the expression.
 * The template provides the method to read an expression and turn it into a tree.
 * You have to write the method to evaluate an expression tree.
 *  and also check and report certain kinds of invalid expressions
 */

public class CPNCalculator{

    /**
     * Setup GUI then run the calculator
     */
    public static void main(String[] args){
        CPNCalculator calc = new CPNCalculator();
        calc.setupGUI();
        calc.runCalculator();
    }

    /** Setup the gui */
    public void setupGUI(){
        UI.addButton("Clear", UI::clearText); 
        UI.addButton("Quit", UI::quit); 
        UI.setDivider(1.0);
    }

    /**
     * Run the calculator:
     * loop forever:  (a REPL - Read Eval Print Loop)
     *  - read an expression,
     *  - evaluate the expression,
     *  - print out the value
     * Invalid expressions could cause errors when reading or evaluating
     * The try-catch prevents these errors from crashing the program - 
     *  the error is caught, and a message printed, then the loop continues.
     */
    public void runCalculator(){
        UI.println("Enter expressions in pre-order format with spaces");
        UI.println("eg   ( * ( + 4 5 8 3 -10 ) 7 ( / 6 4 ) 18 )");
        while (true){
            UI.println();
            try {
                GTNode<ExpElem> expr = readExpr();
                double value = evaluate(expr);
                UI.println(" -> " + value);
            }catch(Exception e){UI.println("Something went wrong! "+e);}
        }
    }

    /**
     * Evaluate an expression and return the value
     * Returns Double.NaN if the expression is invalid in some way.
     * If the node is a number
     *  => just return the value of the number
     * or it is a named constant
     *  => return the appropriate value
     * or it is an operator node with children
     *  => evaluate all the children and then apply the operator.
     */
    public double evaluate(GTNode<ExpElem> expr){
        if (expr==null) { return Double.NaN; }
        if (expr.getItem().operator.equals("PI"))   { return Math.PI; }
        if (expr.getItem().operator.equals("E"))    { return Math.E; }
        if (expr.numberOfChildren()==0) { return expr.getItem().value; }
        else {
            double ans = 0;
            if (expr.getItem().operator.equals("+") && expr.numberOfChildren() > 0){
                ans = 0;
                for (GTNode<ExpElem> child : expr)  { ans += evaluate(child); }
            }
            else if (expr.getItem().operator.equals("-") && expr.numberOfChildren() > 0){
                ans = evaluate(expr.removeChild(0));
                for (GTNode<ExpElem> child : expr)  { ans -= evaluate(child); }
            }
            else if (expr.getItem().operator.equals("*") && expr.numberOfChildren() > 0){
                ans = 1;
                for (GTNode<ExpElem> child : expr)  { ans *= evaluate(child); }
            }
            else if (expr.getItem().operator.equals("/") && expr.numberOfChildren() > 0){
                ans = evaluate(expr.removeChild(0));
                for (GTNode<ExpElem> child : expr)  { ans /= evaluate(child); }
            }
            else if (expr.getItem().operator.equals("^") && expr.numberOfChildren() > 0){
                ans = evaluate(expr.removeChild(0));
                for (GTNode<ExpElem> child : expr)  { ans = Math.pow(ans, evaluate(child)); }
            }
            else if (expr.getItem().operator.equals("sqrt") && expr.numberOfChildren() == 1){
                ans = Math.sqrt(evaluate(expr.getChild(0)));
            }
            else if (expr.getItem().operator.equals("log")){
                if (expr.numberOfChildren() == 1){
                    ans = Math.log(evaluate(expr.getChild(0)));
                }
                else if (expr.numberOfChildren() == 2){
                    ans = Math.log(evaluate(expr.getChild(0))) / Math.log(evaluate(expr.getChild(1)));
                }
                else { UI.println("log must have one or two operands");     ans = Double.NaN; }
            }
            else if (expr.getItem().operator.equals("ln") && expr.numberOfChildren() == 1){
                ans = Math.log(evaluate(expr.getChild(0)));
            }
            else if (expr.getItem().operator.equals("sin") && expr.numberOfChildren() == 1){
                ans = Math.sin(evaluate(expr.getChild(0)));
            }
            else if (expr.getItem().operator.equals("cos") && expr.numberOfChildren() == 1){
                ans = Math.cos(evaluate(expr.getChild(0)));
            }
            else if (expr.getItem().operator.equals("tan") && expr.numberOfChildren() == 1){
                ans = Math.tan(evaluate(expr.getChild(0)));
            }
            else if (expr.getItem().operator.equals("dist")) {
                if (expr.numberOfChildren() == 4){
                    ans = Math.sqrt(Math.pow(evaluate(expr.getChild(0)) - evaluate(expr.getChild(2)), 2) + Math.pow(evaluate(expr.getChild(1)) - evaluate(expr.getChild(3)), 2));
                }
                else if (expr.numberOfChildren() == 6){
                    ans = Math.sqrt(Math.pow(evaluate(expr.getChild(0)) - evaluate(expr.getChild(3)), 2) + Math.pow(evaluate(expr.getChild(1)) - evaluate(expr.getChild(4)), 2) + Math.pow(evaluate(expr.getChild(2)) - evaluate(expr.getChild(5)), 2));
                }
                else { UI.println("dist must have four or six operands");   ans = Double.NaN;}
            }
            else if (expr.getItem().operator.equals("avg") && expr.numberOfChildren() > 0){
                ans = evaluate(expr.removeChild(0));
                for (GTNode<ExpElem> child : expr)  { ans += evaluate(child); }
                ans /= expr.numberOfChildren()+1;
            }
            else {
                UI.println("The operator is invalid or the number of operands is wrong");
                return Double.NaN;
            }
            return ans;
        }
    }








    /** 
     * Reads an expression from the user and constructs the tree.
     */ 
    public GTNode<ExpElem> readExpr(){
        String expr = UI.askString("expr:");
        return readExpr(new Scanner(expr));   // the recursive reading method
    }

    /**
     * Recursive helper method.
     * Uses the hasNext(String pattern) method for the Scanner to peek at next token
     */
    public GTNode<ExpElem> readExpr(Scanner sc){
        if (sc.hasNextDouble()) {                     // next token is a number: return a new node
            return new GTNode<ExpElem>(new ExpElem(sc.nextDouble()));
        }
        else if (sc.hasNext("\\(")) {                 // next token is an opening bracket
            sc.next();                                // read and throw away the opening '('
            ExpElem opElem = new ExpElem(sc.next());  // read the operator
            GTNode<ExpElem> node = new GTNode<ExpElem>(opElem);  // make the node, with the operator in it.
            while (! sc.hasNext("\\)")){              // loop until the closing ')'
                GTNode<ExpElem> child = readExpr(sc); // read each operand/argument
                node.addChild(child);                 // and add as a child of the node
            }
            sc.next();                                // read and throw away the closing ')'
            return node;
        }
        else {                                        // next token must be a named constant (PI or E)
            // make a token with the name as the "operator"
            return new GTNode<ExpElem>(new ExpElem(sc.next()));
        }
    }

}



