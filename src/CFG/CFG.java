
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
    private HashSet<BasicBlock> unreachableBlocks = new HashSet<BasicBlock>();
    
    public CFG(Compilation co)
    {
        graph = new Vector<BasicBlock>();
        System.out.println("Running CFG builder");
        
        co.visit(new CFGVisitor(this));
        System.out.println("CFG complete!");
    }
    
    public BasicBlock createBlock()
    {
        if(firstBlock)
        {
            
            BasicBlock newBlock = new BasicBlock(blockCount);
            //System.out.println("Created block " + newBlock.getLabel());
            blockCount++;
            firstBlock = false;            
            return newBlock;            
        }
        else
        {
            
            BasicBlock newBlock = new BasicBlock(blockCount);
            //System.out.println("Created block " + newBlock.getLabel());
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
        if(child.proc.equals(""))
        {
            child.proc = parent.proc;
        }
    }
    
    public void liveVariableAnalysis()
    {
        System.out.println("Running Live Variable Analysis");
        BasicBlock b;
        AST ast;
        HashSet<AST> totalUEVar = new HashSet<AST>();
        Iterator iterator;
        Iterator totalIterator;
        for(int i = 0; i < graph.size(); i++)
        {
            b = graph.get(i);
            if(b != null)
            {
                //System.out.println("Running LVA on " + b.getLabel());
                new LVAVisitor(b, this);
            }
        }
        System.out.println("LVA complete");
       
        for(int i = 0; i < graph.size(); i++)
        {
            b = graph.get(i);
            if(b != null)
            {
                if(!b.ueVar.isEmpty())
                {
                    iterator = b.ueVarNodes.iterator();
                    
                    while(iterator.hasNext())
                    {
                        ast = (AST)iterator.next();
                        System.out.println("adding " + ast + " to the totalUEvar set");
                        totalUEVar.add(ast);
                    }
                }
            }
        }
        
        totalIterator = totalUEVar.iterator();
        
        while(totalIterator.hasNext())
        {
            ast = (AST)totalIterator.next();
            if(ast instanceof NameExpr)
            {
                Error.error(ast, "Variable '"+ ((NameExpr)ast).name().getname() + "' may not have been initialized");
                //System.out.println("Line " + ast.line + ": Variable " + ((NameExpr)ast).name().getname() + " may not have been initialized!");
            }
            else if(ast instanceof Var)
            {
                Error.error(ast, "Variable '"+ ((Var)ast).name().getname() + "' may not have been initialized");
                //System.out.println("Line " + ast.line + ": Variable " + ((Var)ast).name().getname() + " may not have been initialized!");
            }
        }
    }
    
    public void reachability()
    {
        System.out.println("Running reachability check of blocks");
        for(int i = 0; i < this.graph.size(); i++)
        {
            if(graph.elementAt(i) != null)
            {
                if(graph.elementAt(i).firstBlock)
                {
                    recursiveReachable(graph.elementAt(i));
                }
            }
        }
        System.out.println("Finished finding unreachable blocks");
        
        for(int i = 0; i < this.graph.size(); i++)
        {
            if(graph.elementAt(i) != null)
            {
                if(!graph.elementAt(i).reachable)
                {
                    unreachableBlocks.add(graph.elementAt(i));
                }
            }   
        }
        
        
        
    }
    
    public void recursiveReachable(BasicBlock b)
    {
        b.reachable = true;
        for(int i = 0; i < b.childSize(); i++)
        {
            if(!b.getChild(i).reachable)
            {
                recursiveReachable(b.getChild(i));
            }    
        }
    }
    
}
