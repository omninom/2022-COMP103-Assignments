// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2022T2, Assignment 2
 * Name: Leory Xue
 * Username: xueleor    
 * ID: 300607821
 */

/**
 * Implements a decision tree that asks a user yes/no questions to determine a decision.
 * Eg, asks about properties of an animal to determine the type of animal.
 * 
 * A decision tree is a tree in which all the internal nodes have a question, 
 * The answer to the question determines which way the program will
 *  proceed down the tree.  
 * All the leaf nodes have the decision (the kind of animal in the example tree).
 *
 * The decision tree may be a predermined decision tree, or it can be a "growing"
 * decision tree, where the user can add questions and decisions to the tree whenever
 * the tree gives a wrong answer.
 *
 * In the growing version, when the program guesses wrong, it asks the player
 * for another question that would help it in the future, and adds it (with the
 * correct answers) to the decision tree. 
 *
 */

import ecs100.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.awt.Color;

public class DecisionTree {

    public DTNode theTree;    // root of the decision tree;

    /**
     * Setup the GUI and make a sample tree
     */
    public static void main(String[] args){
        DecisionTree dt = new DecisionTree();
        dt.setupGUI();
        dt.loadTree("sample-animal-tree.txt");
    }

    /**
     * Set up the interface
     */
    public void setupGUI(){
        UI.addButton("Load Tree", ()->{loadTree(UIFileChooser.open("File with a Decision Tree"));});
        UI.addButton("Print Tree", this::printTree);
        UI.addButton("Run Tree", this::runTree);
        UI.addButton("Grow Tree", this::growTree);
        UI.addButton("Save Tree", this::saveTree);  // for completion
        UI.addButton("Draw Tree", this::drawTree);  // for challenge
        UI.addButton("Reset", ()->{loadTree("sample-animal-tree.txt");});
        UI.addButton("Quit", UI::quit);
        UI.setDivider(0.5);
    }

    /**  
     * Print out the contents of the decision tree in the text pane.
     * The root node should be at the top, followed by its "yes" subtree,
     * and then its "no" subtree.
     * Needs a recursive "helper method" which is passed a node.
     * 
     * COMPLETION:
     * Each node should be indented by how deep it is in the tree.
     * The recursive "helper method" is passed a node and an indentation string.
     *  (The indentation string will be a string of space characters)
     */
    public void printTree(){
        UI.clearText();
        printAll(theTree, "", "");
    }
    
    /** Helper method that prints all contents of tree. Passes a node, indent, and string **/
    public void printAll(DTNode n, String indent, String str){
        if (n != null){
            UI.println(indent+str+n.getText());                           //visit node n
            printAll(n.getYes(), indent+"   ", "y: ");                  //traverse left child subtree
            printAll(n.getNo(), indent+"   ", "n: ");                   //traverse rightr child subtree
        }
    }

    /**
     * Run the tree by starting at the top (of theTree), and working
     * down the tree until it gets to a leaf node (a node with no children)
     * If the node is a leaf it prints the answer in the node
     * If the node is not a leaf node, then it asks the question in the node,
     * and depending on the answer, goes to the "yes" child or the "no" child.
     */
    public void runTree() {
        Stack<DTNode> todo = new Stack<>();     //stack LIFO
        todo.push(theTree);
        while (!todo.isEmpty()){                
            DTNode n = todo.pop();              
            if (n.isAnswer()) {UI.println("The answer is: "+n.getText());}      // if node is answer print appropriate
            else {
                String answer = UI.askString("Is it true: "+n.getText()+" (Y/N): ");
                if (answer.equals("yes"))   {todo.push(n.getYes());}
                if (answer.equals("no"))    {todo.push(n.getNo());}
            }
        }
    }

    /**
     * Grow the tree by allowing the user to extend the tree.
     * Like runTree, it starts at the top (of theTree), and works its way down the tree
     *  until it finally gets to a leaf node. 
     * If the current node has a question, then it asks the question in the node,
     * and depending on the answer, goes to the "yes" child or the "no" child.
     * If the current node is a leaf it prints the decision, and asks if it is right.
     * If it was wrong, it
     *  - asks the user what the decision should have been,
     *  - asks for a question to distinguish the right decision from the wrong one
     *  - changes the text in the node to be the question
     *  - adds two new children (leaf nodes) to the node with the two decisions.
     */
    public void growTree () {
        Stack<DTNode> todo = new Stack<>();
        todo.push(theTree);
        while (!todo.isEmpty()){
            DTNode n = todo.pop();
            if (!n.isAnswer()) {                                                                // if the node is not an answer continue asking questions
                String answer = UI.askString("Is it true: "+n.getText()+" (Y/N): ");
                if (answer.equals("yes"))   {todo.push(n.getYes());}
                if (answer.equals("no"))    {todo.push(n.getNo());}
            }
            else if (n.isAnswer()) {                                                            // if the node is an answer, ask if correct
                String answer = UI.askString("I think I know. Is it a "+n.getText()+"?");
                if (answer.equals("yes")) {return;}  
                if (answer.equals("no")){                                                       // if no then add new property to tree
                    String currentText = n.getText();
                    answer = UI.askString("OK, what should the answer be?");
                    UI.println("Oh. I can't distinguish a "+currentText+" from a "+answer);
                    String property = UI.askString("Tell me something that's true for a "+answer+" but not for a "+currentText+"?");
                    UI.println("Property: "+property);
                    UI.println("Thank you! I've updated my decision tree.");
                    n.setText(property);                                                        // set node to new property
                    n.setChildren(new DTNode(answer), new DTNode(currentText));                 // set children of the property to the nodes worked with earlier
                }
            }
        }

    }

    /** Saves a decision tree to a file
        calls a recurisve method to save the tree
        **/
    public void saveTree() {
        String fname = UIFileChooser.save("Chose file to save to");
        try {
            PrintStream out = new PrintStream(fname);                   //create printStream
            writeTo(theTree, out);
            out.close();                                            
        } catch (IOException e) {UI.println("File failure: " + e);}
    }
    
    /** method that writes to a PrintStream using recursion **/
    public void writeTo(DTNode n, PrintStream out){
        String str = "";
        if (n != null){
            if (n.isAnswer()) {str = "Answer: ";}
            else {str = "Question: ";}
            out.println(str+n.getText());                  //visit node n
            writeTo(n.getYes(), out);                  //traverse left child subtree
            writeTo(n.getNo(), out);                   //traverse right child subtree
        }
    }
    
    /** Draws tree **/
    public void drawTree() {
        UI.clearGraphics();
        drawAll(theTree, 50, 200, 50, 200, 0);
    }
    
    /** method that draws the tree using recursion **/
    public void drawAll(DTNode n, int x, int y, int oldX, int oldY, int count){
        int newCount = count + 1;                                                       //count used for y distance so that sub trees do not draw over each other
        if (n != null){
            n.draw(x, y);
            UI.drawLine(x,y,oldX,oldY);
            drawAll(n.getYes(), x+n.WIDTH, y-n.HEIGHT-80/newCount, x, y, newCount);    //traverse left child subtree
            drawAll(n.getNo(), x+n.WIDTH, y+n.HEIGHT+80/newCount, x, y, newCount);     //traverse right child subtree
        }    
    }
    
    // Written for you

    /** 
     * Loads a decision tree from a file.
     * Each line starts with either "Question:" or "Answer:" and is followed by the text
     * Calls a recursive method to load the tree and return the root node,
     *  and assigns this node to theTree.
     */
    public void loadTree (String filename) { 
        if (!Files.exists(Path.of(filename))){
            UI.println("No such file: "+filename);
            return;
        }
        try{theTree = loadSubTree(new ArrayDeque<String>(Files.readAllLines(Path.of(filename))));}
        catch(IOException e){UI.println("File reading failed: " + e);}
    }

    /**
     * Loads a tree (or subtree) from a Scanner and returns the root.
     * The first line has the text for the root node of the tree (or subtree)
     * It should make the node, and 
     *   if the first line starts with "Question:", it loads two subtrees (yes, and no)
     *    from the scanner and add them as the  children of the node,
     * Finally, it should return the  node.
     */
    public DTNode loadSubTree(Queue<String> lines){
        Scanner line = new Scanner(lines.poll());
        String type = line.next();
        String text = line.nextLine().trim();
        DTNode node = new DTNode(text);
        if (type.equals("Question:")){
            DTNode yesCh = loadSubTree(lines);
            DTNode noCh = loadSubTree(lines);
            node.setChildren(yesCh, noCh);
        }
        return node;

    }



}
