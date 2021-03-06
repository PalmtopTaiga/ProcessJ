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

public class CFGVisitor extends Visitor<AST>{
    
    public CFG cfg;
    public BasicBlock currentBlock;
    
    public CFGVisitor(CFG controlFlowGraph)
    {
        cfg = controlFlowGraph;
    }
    
    public AST visitAnnotation(Annotation at) {
        currentBlock.addNode(at);
        return null;
    }

    public AST visitAnnotations(Annotations as) {
        currentBlock.addNode(as);
        return null;
    }

    public AST visitAltCase(AltCase ac) {
        return ac.visitChildren(this);
    }

    public AST visitAltStat(AltStat as) {
        BasicBlock expressionBlock = cfg.createBlock();
        AltCase acptr = null;
        cfg.putBlock(currentBlock);
        cfg.linkBlocks(currentBlock, expressionBlock);
        currentBlock = expressionBlock;
        for(int i = 0; i < as.body().size(); i++)
        {
            acptr = as.body().child(i);
            if(acptr.precondition() != null)
            {
                acptr.precondition().visit(this);
            }
        }
         
        cfg.putBlock(currentBlock);
        BasicBlock guardBlock, statBlock, convergingBlock;
        
        convergingBlock = cfg.createBlock();
        for(int i = 0; i < as.body().size(); i++)
        {
            acptr = as.body().child(i);
            guardBlock = cfg.createBlock();
            cfg.linkBlocks(expressionBlock, guardBlock);
            statBlock = cfg.createBlock();
            currentBlock = guardBlock;
            acptr.guard().visit(this);
            cfg.putBlock(currentBlock);
            
            cfg.linkBlocks(currentBlock, statBlock);
            currentBlock = statBlock;
            acptr.stat().visit(this);
            cfg.putBlock(currentBlock);
            cfg.linkBlocks(currentBlock, convergingBlock);
        }
        
        currentBlock = convergingBlock;
        return null;
    }

    public AST visitArrayAccessExpr(ArrayAccessExpr ae) {
        return ae.visitChildren(this);
    }

    public AST visitArrayLiteral(ArrayLiteral al) {
        return al.visitChildren(this);
    }

    public AST visitArrayType(ArrayType at) {
        return at.visitChildren(this);
    }

    public AST visitAssignment(Assignment as) {
        currentBlock.addNode(as);
        return as.visitChildren(this);
    }

    public AST visitBinaryExpr(BinaryExpr be) {
        currentBlock.addNode(be);
        return be.visitChildren(this);
    }

    public AST visitBlock(Block bl) {
        bl.visitChildren(this);
        return null;
    }

    public AST visitBreakStat(BreakStat bs) {
        currentBlock.addNode(bs);
        currentBlock.jumpTarget = -2;
        cfg.putBlock(currentBlock);
        BasicBlock newBlock = cfg.createBlock();
        currentBlock = newBlock;
        return null;
    }

    public AST visitCastExpr(CastExpr ce) {
        return ce.visitChildren(this);
    }

    public AST visitChannelType(ChannelType ct) {
        return ct.visitChildren(this);
    }

    public AST visitChannelEndExpr(ChannelEndExpr ce) {
        return ce.visitChildren(this);
    }

    public AST visitChannelEndType(ChannelEndType ct) {
        return ct.visitChildren(this);
    }

    public AST visitChannelReadExpr(ChannelReadExpr cr) {
        currentBlock.addNode(cr);
        cr.channel().visit(this);
        if(cr.extRV() != null)
        {
            cr.extRV().visit(this);
        }
        return null;
    }

    public AST visitChannelWriteStat(ChannelWriteStat cw) {
        return cw.visitChildren(this);
    }

    public AST visitClaimStat(ClaimStat cs) {
        return cs.visitChildren(this);
    }

    public AST visitCompilation(Compilation co) {
        return co.visitChildren(this);
    }

    public AST visitConstantDecl(ConstantDecl cd) {
        return cd.visitChildren(this);
    }

    public AST visitContinueStat(ContinueStat cs) {
        currentBlock.addNode(cs);
        currentBlock.jumpTarget = -3;
        cfg.putBlock(currentBlock);
        BasicBlock newBlock = cfg.createBlock();
        currentBlock = newBlock;
        return null;
    }

    public AST visitDoStat(DoStat ds) {
       
        //create the block to contain the statements for this do while
        BasicBlock statBlock;
        cfg.putBlock(currentBlock);
        statBlock = cfg.createBlock();
        cfg.linkBlocks(currentBlock, statBlock);
        currentBlock = statBlock;
        ds.stat().visit(this);
        
        //create the block to contain the expression that this do while uses
        BasicBlock expressionBlock;
        cfg.putBlock(currentBlock);
        expressionBlock = cfg.createBlock();
        cfg.linkBlocks(currentBlock, expressionBlock);
        currentBlock = expressionBlock;
        ds.expr().visit(this);
        
        cfg.linkBlocks(currentBlock, statBlock);
        
        //create the block that the loop will escape to
        BasicBlock escapeBlock;
        cfg.putBlock(currentBlock);
        escapeBlock = cfg.createBlock();
        cfg.linkBlocks(currentBlock, escapeBlock);
        currentBlock = escapeBlock;
        
        //check for any continues and breaks
        //System.out.println("We are going to check blocks " + expressionBlock.getLabel() 
        //        +" through " + escapeBlock.getLabel() + " for any break or continue statements");
        for(int i = statBlock.getNo(); i < escapeBlock.getNo(); i++)
        {
            BasicBlock ptr = cfg.getBlock(i);
            //-2 denotes a break statement
            if(ptr.jumpTarget == -2)
            {
                ptr.jumpTarget = -1;
                ptr.clearChildren();
                ptr.addChild(escapeBlock);
            }
            //-3 denotes a continue statement
            if(ptr.jumpTarget == -3)
            {
                ptr.jumpTarget = -1;
                ptr.clearChildren();
                ptr.addChild(statBlock);
            }
        }
        
        return null;
    }

    public AST visitErrorType(ErrorType et) {
        return et.visitChildren(this);
    }

    public AST visitExprStat(ExprStat es) {
        //currentBlock.addNode(es);
        return es.visitChildren(this);
    }

    public AST visitExternType(ExternType et) {
        return null;
    }

    public AST visitForStat(ForStat fs) {
        
        if(fs.init() !=null)
        {
            fs.init().visit(this);
        }
        
        BasicBlock expressionBlock = cfg.createBlock();
        cfg.putBlock(currentBlock);
        cfg.linkBlocks(currentBlock, expressionBlock);
        currentBlock = expressionBlock;
        
        if(fs.expr() != null)
        {
            fs.expr().visit(this);
        }
        
        BasicBlock statBlock = cfg.createBlock();
        cfg.putBlock(currentBlock);
        cfg.linkBlocks(currentBlock, statBlock);
        currentBlock = statBlock;
        
        fs.stats().visit(this);
        fs.incr().visit(this);
        
        BasicBlock escapeBlock = cfg.createBlock();
        cfg.putBlock(currentBlock);
        cfg.linkBlocks(currentBlock, expressionBlock);
        currentBlock = escapeBlock;
        
        cfg.linkBlocks(expressionBlock, escapeBlock);
        
        //check for any continues and breaks
        //System.out.println("We are going to check blocks " + expressionBlock.getLabel() 
        //        +" through " + escapeBlock.getLabel() + " for any break or continue statements");
        for(int i = expressionBlock.getNo(); i < escapeBlock.getNo(); i++)
        {
            BasicBlock ptr = cfg.getBlock(i);
            //-2 denotes a break statement
            if(ptr.jumpTarget == -2)
            {
                ptr.jumpTarget = -1;
                ptr.clearChildren();
                ptr.addChild(escapeBlock);
            }
            //-3 denotes a continue statement
            if(ptr.jumpTarget == -3)
            {
                ptr.jumpTarget = -1;
                ptr.clearChildren();
                ptr.addChild(expressionBlock);
            }
        }
        
        
        return null;
    }

    public AST visitGuard(Guard gu) {
        return gu.visitChildren(this);
    }

    public AST visitIfStat(IfStat is) {
        //add the boolean expression to the current block
        currentBlock.addNode(is.expr());
        //create the block for the start of the then part
        BasicBlock thenBlock = cfg.createBlock();
        //set a temporary block pointer to the currentblock (parent to the branches)
        BasicBlock tmpBlock = currentBlock;
        //link the parent and the then blocks
        cfg.linkBlocks(tmpBlock, thenBlock);
        //place the parent block into the graph because we're done adding code, but not done adding children
        cfg.putBlock(tmpBlock);
        //set the currentblock to the thenblock
        currentBlock = thenBlock;
        //visit the thenpart
        is.thenpart().visit(this);
        
        //we have to put the currentBlock into the cfg after we visit the then part
        //this could be the one we made in this function, but there could have been 
        //new block structures within the thenpart, and they would have handled the 
        //put of the thenBlock
        BasicBlock tmpthen = currentBlock;
        cfg.putBlock(tmpthen);
        
        
        //check if it has an else part before we bother with this
        BasicBlock tmpelse = null;
        if(is.elsepart() != null)
        {
            BasicBlock elseBlock = cfg.createBlock();
            cfg.linkBlocks(tmpBlock, elseBlock);
            currentBlock = elseBlock;
            is.elsepart().visit(this);
            tmpelse = currentBlock;
            cfg.putBlock(tmpelse);
        }
        
        //create the block that the branches will converge on
        BasicBlock convergeBlock = cfg.createBlock();
        //link the then 
        cfg.linkBlocks(tmpthen, convergeBlock);
        if(is.elsepart() != null)
        {
            //link the else if it exists
            cfg.linkBlocks(tmpelse, convergeBlock);
        }
        else
        {
            //if there is no else, then we must link the parent block
            //with the converging block
            cfg.linkBlocks(tmpBlock, convergeBlock);
        }
        
        currentBlock = convergeBlock;
        
        
        return null;
    }

    public AST visitImplicitImport(ImplicitImport ii) {
        return ii.visitChildren(this);
    }

    public AST visitImport(Import im) {
        currentBlock.addNode(im);
        return im.visitChildren(this);
    }

    public AST visitInvocation(Invocation in) {
        currentBlock.addNode(in);
        return in.visitChildren(this);
    }

    public AST visitLocalDecl(LocalDecl ld) {
        currentBlock.addNode(ld);
        return null;
    }

    public AST visitModifier(Modifier mo) {
        currentBlock.addNode(mo);
        return null;
    }

    public AST visitName(Name na) {
        return null;
    }

    public AST visitNamedType(NamedType nt) {
        return nt.visitChildren(this);
    }

    public AST visitNameExpr(NameExpr ne) {
        return null;
    }

    public AST visitNewArray(NewArray ne) {
        return ne.visitChildren(this);
    }

    public AST visitNewMobile(NewMobile nm) {
        return nm.visitChildren(this);
    }

    public AST visitParamDecl(ParamDecl pd) {
        return pd.visitChildren(this);
    }

    public AST visitParBlock(ParBlock pb) {
        
        
        currentBlock.addNode(pb);
     
        BasicBlock parentPtr = currentBlock;
        BasicBlock statBlock;
        BasicBlock convergingBlock = cfg.createBlock();
        cfg.putBlock(parentPtr);
        for(int i = 0; i < pb.stats().size();i++)
        {
            statBlock = cfg.createBlock();
            currentBlock = statBlock;
            cfg.linkBlocks(parentPtr, currentBlock);
            pb.stats().child(i).visit(this);
            cfg.linkBlocks(currentBlock, convergingBlock);
            cfg.putBlock(currentBlock);
        }
        
        currentBlock = convergingBlock;
        
        return null;
    }

    public AST visitPragma(Pragma pr) {
        return null;
    }

    public AST visitPrimitiveLiteral(PrimitiveLiteral li) {
        
        return null;
    }

    public AST visitPrimitiveType(PrimitiveType py) {
        return null;
    }

    public AST visitProcTypeDecl(ProcTypeDecl pd) {
        BasicBlock newProcBlock = cfg.createBlock();
        newProcBlock.proc = pd.name()+pd.signature();
        newProcBlock.addNode(pd);
        cfg.putBlock(currentBlock);
        currentBlock = newProcBlock;
        currentBlock.firstBlock = true;
        super.visitProcTypeDecl(pd);
        return null;
    }

    public AST visitProtocolLiteral(ProtocolLiteral pl) {
        return pl.visitChildren(this);
    }

    public AST visitProtocolCase(ProtocolCase pc) {
        return pc.visitChildren(this);
    }

    public AST visitProtocolTypeDecl(ProtocolTypeDecl pd) {
        return pd.visitChildren(this);
    }

    public AST visitQualifiedName(QualifiedName qn) {
        return qn.visitChildren(this);
    }

    public AST visitRecordAccess(RecordAccess ra) {
        return ra.visitChildren(this);
    }

    public AST visitRecordLiteral(RecordLiteral rl) {
        return rl.visitChildren(this);
    }

    public AST visitRecordMember(RecordMember rm) {
        return rm.visitChildren(this);
    }

    public AST visitRecordTypeDecl(RecordTypeDecl rt) {
        return rt.visitChildren(this);
    }

    public AST visitReturnStat(ReturnStat rs) {
        currentBlock.addNode(rs);
        currentBlock.jumpTarget = -4;
        cfg.putBlock(currentBlock);
        BasicBlock newBlock = cfg.createBlock();
        currentBlock = newBlock;
        rs.visitChildren(this);
        return null;
    }

    public AST visitSequence(Sequence se) {
        for (int i = 0; i < se.size(); i++)
            if (se.child(i) != null)
                se.child(i).visit(this);
        return null;
    }

    public AST visitSkipStat(SkipStat ss) {
        return null;
    }

    public AST visitStopStat(StopStat ss) {
        return null;
    }

    public AST visitSuspendStat(SuspendStat ss) {
        return ss.visitChildren(this);
    }

    public AST visitSwitchGroup(SwitchGroup sg) {
        
        return sg.visitChildren(this);
    }

    public AST visitSwitchLabel(SwitchLabel sl) {
        currentBlock.addNode(sl);
        return sl.visitChildren(this);
    }

    public AST visitSwitchStat(SwitchStat st) {
        st.expr().visit(this);
        cfg.putBlock(currentBlock);
        BasicBlock caseBlock;
        BasicBlock tmp = currentBlock;
        for(int i = 0; i < st.switchBlocks().size(); i++)
        {
            caseBlock = cfg.createBlock();
            cfg.linkBlocks(tmp, caseBlock);
            currentBlock = caseBlock;
            st.switchBlocks().child(i).visit(this);
            cfg.putBlock(currentBlock);
        }
        
        BasicBlock escapeBlock = cfg.createBlock();
        cfg.linkBlocks(currentBlock, escapeBlock);
        
        for(int i = tmp.getNo()+2; i < escapeBlock.getNo()+1; i++)
        {
            BasicBlock ptr = cfg.getBlock(i);
            //-2 denotes break statement
            System.out.println("Checking of B" + ptr.getNo() + " has a break statement");
            if(ptr.jumpTarget == -2)
            {
                ptr.jumpTarget = -1;
                System.out.println("It does");
                ptr.clearChildren();
                ptr.addChild(escapeBlock);
            }          
        }
        for(int i = tmp.getNo()+2; i < escapeBlock.getNo()+1; i++)
        {
            BasicBlock ptr = cfg.getBlock(i);
            //if some blocks in this sequence don't have any children
            //then they should be linked with the next case over
            System.out.println("Checking if B" +ptr.getNo() + " has children");
            if(!ptr.hasChildren())
            {
                System.out.println("It doesn't");
                if(ptr.jumpTarget != -4)
                {
                    ptr.addChild(cfg.getBlock(i+1));
                }            
            }
        }
        
        currentBlock = escapeBlock;
        return null;
    }

    public AST visitSyncStat(SyncStat st) {
        return st.visitChildren(this);
    }

    public AST visitTernary(Ternary te) {
        return te.visitChildren(this);
    }

    public AST visitTimeoutStat(TimeoutStat ts) {
        return ts.visitChildren(this);
    }

    public AST visitUnaryPostExpr(UnaryPostExpr up) {
        currentBlock.addNode(up);
        return up.visitChildren(this);
    }

    public AST visitUnaryPreExpr(UnaryPreExpr up) {
        currentBlock.addNode(up);
        return up.visitChildren(this);
    }

    public AST visitVar(Var va) {
        return va.visitChildren(this);
    }

    public AST visitWhileStat(WhileStat ws) {
        /*
            while(e)
            {
                s
            }
        */
        
        BasicBlock expressionBlock;
        BasicBlock statBlock;
        BasicBlock escapeBlock;
        cfg.putBlock(currentBlock);
        
        //create expression block
        expressionBlock = cfg.createBlock();
        cfg.linkBlocks(currentBlock, expressionBlock);
        currentBlock = expressionBlock;
        if(ws.expr() instanceof PrimitiveLiteral)
        {
            currentBlock.addNode(ws.expr());
        }
        else
        {
            ws.expr().visit(this);
        }
        cfg.putBlock(expressionBlock);
        
        //create the stat block
        statBlock = cfg.createBlock();
        cfg.linkBlocks(currentBlock, statBlock);
        currentBlock = statBlock;
        ws.stat().visit(this);
        cfg.linkBlocks(currentBlock, expressionBlock);
        cfg.putBlock(currentBlock);
        
        //create the escape block
        escapeBlock = cfg.createBlock();
        currentBlock = escapeBlock;
        cfg.linkBlocks(expressionBlock, escapeBlock);
        
        //System.out.println("We are going to check blocks " + expressionBlock.getLabel() 
        //        +" through " + escapeBlock.getLabel() + " for any break or continue statements");
        for(int i = expressionBlock.getNo(); i < escapeBlock.getNo(); i++)
        {
            BasicBlock ptr = cfg.getBlock(i);
            //-2 denotes a break statement
            if(ptr.jumpTarget == -2)
            {
                ptr.jumpTarget = -1;
                ptr.clearChildren();
                ptr.addChild(escapeBlock);
            }
            if(ptr.jumpTarget == -3)
            {
                ptr.jumpTarget = -1;
                ptr.clearChildren();
                ptr.addChild(statBlock);
            }
        }
        
        
        
        return null;
    }
}
