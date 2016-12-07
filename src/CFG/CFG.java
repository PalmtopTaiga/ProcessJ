
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

public class CFG {
   
    private Vector<BasicBlock> graph;
    private boolean firstBlock = true;
    private int blockCount = 0;
    
    public CFG(Compilation co)
    {
        graph = new Vector<BasicBlock>();
        System.out.println("Running CFG builder");
        
        co.visit(new CFGVisitor(this));
    }
    
    public BasicBlock createBlock()
    {
        if(firstBlock)
        {
            System.out.println("Created the first block - here we go");
            BasicBlock newBlock = new BasicBlock(blockCount);
            blockCount++;
            firstBlock = false;
            return newBlock;
            
        }
        else
        {
         
        }
    }
    
    public void printCFG()
    {
        for(int i = 0; i < graph.size(); i++)
        {
            (graph.get(i)).print();
            System.out.println("\n");
        }
    }
    
    public void putBlock(BasicBlock bl)
    {
        graph.add(bl);
    }
    
    public void linkBlocks(BasicBlock parent, BasicBlock child)
    {
        parent.addChild(child);
        child.addParent(parent);
        child.proc = parent.proc;
    }
    
}
