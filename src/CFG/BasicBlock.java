package CFG;

/**
 *
 * @author trenton
 */

import AST.*;
import java.util.*;
import java.math.*;
import Utilities.Error;
import Utilities.Visitor;
import CFG.*;
import Utilities.Log;

public class BasicBlock {

    //label of the block eg B0
    private String label = "";
    
    //block number should match the number in the label
    private int number;
    
    //the proc that this block is a part of
    public String proc = "";
    
    //the AST nodes that this block contains
    private Sequence tree = new Sequence();
    
    //set of children that this block points to
    private Vector<BasicBlock> children = new Vector<BasicBlock>();
    
    //set of parents that point to this block
    private Vector<BasicBlock> parents = new Vector<BasicBlock>();
    
    //boolean used to determine if this is the first block of a procedure
    public boolean firstBlock = false;
    
    public int jumpTarget = -1;
    
    public String s = "";
    
    public BasicBlock(int no)
    {
        this.label = "B" + no;
        this.number = no;
    }
    
    public void addChild(BasicBlock bl)
    {
        children.add(bl);
    }
    
    public void addNode(AST node)
    {
        System.out.println("adding " + node.getClass().getName() + " to " + this.getLabel());
        tree.append(node);
    }
    
    public void addParent(BasicBlock bl)
    {
        parents.add(bl);
    }
    
    public int childSize()
    {
        return this.children.size();
    }
    
    public void clearChildren()
    {
        this.children.removeAllElements();
    }
    
    public BasicBlock getChild(int i)
    {
        return children.get(i);
    }
    
    public String getLabel()
    {
        return label;
    }
    
    public BasicBlock getParent(int i)
    {
        return parents.get(i);
    }
    
    public int getNo()
    {
        return number;
    }
    
    public boolean hasChildren()
    {
        if(this.children.size() == 0)
        {
            return false;
        }
        return true;
    }
    
    public boolean hasCode()
    {
        if(this.tree.size() > 0)
        {
            return true;
        }
        return false;
    }

    public void nullBlock()
    {
        this.children.removeAllElements();
        this.parents.removeAllElements();
        
    }

    public int parentSize()
    {
        return this.parents.size();
    }
    
    
    public void print()
    {
        System.out.println(label);
        System.out.println("[");
        System.out.println("Procedure: " + proc);
        if(tree.size() > 0)
        {
            for(int i = 0 ; i < tree.size(); i++)
            {
                tree.child(i).print(System.out);
            }    
        }
        System.out.println("Children of this Block:");
        System.out.println(children);
        System.out.println("Parents of this Block:");
        System.out.println(parents);
        System.out.println("]");
    }
    
    public void removeChild(BasicBlock b)
    {
        
        if(this.children.contains(b))
        {
            this.children.remove(b);
        }
    }
    
    public String toString()
    {
        return label;
    }
}
