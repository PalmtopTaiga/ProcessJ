
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
    private int blocksInGraph = 0;
    
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
            
            BasicBlock newBlock = new BasicBlock(blockCount);
            System.out.println("Created block " + newBlock.getLabel());
            blockCount++;
            firstBlock = false;            
            return newBlock;            
        }
        else
        {
            
            BasicBlock newBlock = new BasicBlock(blockCount);
            System.out.println("Created block " + newBlock.getLabel());
            blockCount++;
            return newBlock;
        }
    }
    
    
    //this function takes a created cfg that has empty blocks
    //and closes those gaps, getting rid of the empty blocks
    public void closeGaps()
    {
        BasicBlock ptr = null;
        BasicBlock parentPtr;
        for(int i = 0; i < graph.size() ; i++)
        {
            ptr = graph.get(i);
            if(ptr != null)
            {
                if(!ptr.hasCode())
                {
                    for(int j = 0; j < ptr.parentSize(); j++)
                    {
                        parentPtr = ptr.getParent(j);
                        parentPtr.removeChild(ptr);
                        for(int k = 0; k < ptr.childSize(); k++)
                        {
                            parentPtr.addChild(ptr.getChild(k));
                        }
                    }
                    ptr.nullBlock();
                }
            }   
        }
    }
    public BasicBlock getBlock(int i)
    {
        return graph.get(i);
    }
    
    public void printCFG()
    {
        System.out.println("The Graph has " + graph.size() +" blocks");
        BasicBlock block = null;
        for(int i = 0; i < graph.size(); i++)
        {
            block = (graph.get(i));
            if(block == null)
            {
                System.out.println("Block was null");
                
            }
            else
            {
                if(block.hasCode())
                {
                    block.print();
                }
            }
            
           
            System.out.println("\n");
        }
    }
    
    public void putBlock(BasicBlock bl)
    {
        graph.add(bl);
        blocksInGraph++;
    }
    
    public void linkBlocks(BasicBlock parent, BasicBlock child)
    {
        parent.addChild(child);
        child.addParent(parent);
        child.proc = parent.proc;
    }
    
}
