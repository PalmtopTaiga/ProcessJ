/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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

public class LVAVisitor extends Visitor<AST> {
    
    public CFG cfg;
    public BasicBlock currBlock = null;
    public boolean LHS = false;
    
    public LVAVisitor(BasicBlock b, CFG controlFlowGraph)
    {
        currBlock = b;
        cfg = controlFlowGraph;
        if(b.hasParents())
        {
            System.out.println(b.getLabel() + " does have parents");
            System.out.println("And they are " +b.getParentVector());
            HashSet<String> tmp1;
            HashSet<String> tmp2;
            HashSet<AST> tmp3;
            for(int i = 0; i < b.parentSize(); i++)
            {                
                tmp1 = b.getParent(i).killSet;
                tmp2 = b.getParent(i).ueVar;
                tmp3 = b.getParent(i).ueVarNodes;
                b.killSet.addAll(tmp1);
                b.ueVar.addAll(tmp2);
                b.ueVarNodes.addAll(tmp3);
            }
        }
        b.tree.visit(this);
    }
    
    public AST visitParamDecl(ParamDecl pd)
    {
        /*System.out.println("ParamDecl ----");
        pd.print();
        System.out.println("--------------");
        //System.out.println("visiting a paramdecl");
        //System.out.println("Adding "+ pd.name() + " to the killSet via ParamDecl");*/
        currBlock.killSet.add(pd.name());
        return null;
    }
    
    public AST visitAssignment(Assignment as)
    {
        /*System.out.println("Assignment ---");
        as.print();
        System.out.println("--------------");
        //System.out.println("Visiting an assignment");*/
        boolean oldLHS = LHS;
        LHS = true;
        //System.out.println("visiting the lhs of that assignment");
        as.left().visit(this);
        LHS = oldLHS;
        //System.out.println("visiting the rhs of that assignment");
        as.right().visit(this);
        return null;
    }
    
    public AST visitNameExpr(NameExpr ne)
    {
        /*System.out.println("NameExpr -----");
        ne.print();
        System.out.println("--------------");*/
        //System.out.println("Visiting a name expr");
        if(LHS)
        {
            if(currBlock.ueVar.contains(ne.name().getname()));
            {
                currBlock.ueVar.remove(ne.name().getname());
                currBlock.ueVarNodes.remove(ne);
            }
            System.out.println("Adding "+ ne.name().getname() + " to the killSet via NameExpr");
            currBlock.killSet.add(ne.name().getname());
        }
        else
        {
            if(!currBlock.killSet.contains(ne.name().getname()))
            {
                currBlock.ueVar.add(ne.name().getname());
                currBlock.ueVarNodes.add(ne);
            }
        }
        return null;
    }
    
    public AST visitVar(Var v)
    {
        /*System.out.println("Var ----------");
        v.print();
        System.out.println("--------------");*/
        //System.out.println("visiting a var");
        if(v.init() != null)
        {
            System.out.println("Adding " +v.name().getname() + " to the killSet via Var");
            currBlock.killSet.add(v.name().getname());
        }
        if(v.init() == null)
        {
            System.out.println("Addinv " +v.name().getname() + " to the ueVar via Var");
            currBlock.ueVar.add(v.name().getname());
            currBlock.ueVarNodes.add(v);
        }
        return null;
    }
    
    public AST visitSequence(Sequence se) {
        //System.out.println("in a sequence");
        for (int i = 0; i < se.size(); i++)
            if (se.child(i) != null)
                se.child(i).visit(this);
        return null;
    }
    
    public AST visitProcTypeDecl(ProcTypeDecl pd)
    {
        //do nothing
        return null;
    }
    
}


