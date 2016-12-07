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
        currentBlock = cfg.createBlock();
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
        return as.visitChildren(this);
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
        return as.visitChildren(this);
    }

    public AST visitBinaryExpr(BinaryExpr be) {
        return be.visitChildren(this);
    }

    public AST visitBlock(Block bl) {
        return bl.visitChildren(this);
    }

    public AST visitBreakStat(BreakStat bs) {
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
        return cr.visitChildren(this);
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
        return null;
    }

    public AST visitDoStat(DoStat ds) {
        return ds.visitChildren(this);
    }

    public AST visitErrorType(ErrorType et) {
        return et.visitChildren(this);
    }

    public AST visitExprStat(ExprStat es) {
        return es.visitChildren(this);
    }

    public AST visitExternType(ExternType et) {
        return null;
    }

    public AST visitForStat(ForStat fs) {
        return fs.visitChildren(this);
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
        
        
        currentBlock = convergeBlock;
        
        
        return null;
    }

    public AST visitImplicitImport(ImplicitImport ii) {
        return ii.visitChildren(this);
    }

    public AST visitImport(Import im) {
        return im.visitChildren(this);
    }

    public AST visitInvocation(Invocation in) {
        return in.visitChildren(this);
    }

    public AST visitLocalDecl(LocalDecl ld) {
        currentBlock.addNode(ld);
        return null;
    }

    public AST visitModifier(Modifier mo) {
        return null;
    }

    public AST visitName(Name na) {
        return null;
    }

    public AST visitNamedType(NamedType nt) {
        return nt.visitChildren(this);
    }

    public AST visitNameExpr(NameExpr ne) {
   
        currentBlock.addNode(ne);
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
        return pb.visitChildren(this);
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
        pd.visitChildren(this);
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
        return rs.visitChildren(this);
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
        return sl.visitChildren(this);
    }

    public AST visitSwitchStat(SwitchStat st) {
        return st.visitChildren(this);
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
        return up.visitChildren(this);
    }

    public AST visitUnaryPreExpr(UnaryPreExpr up) {
        return up.visitChildren(this);
    }

    public AST visitVar(Var va) {
        return va.visitChildren(this);
    }

    public AST visitWhileStat(WhileStat ws) {
        return ws.visitChildren(this);
    }
}
