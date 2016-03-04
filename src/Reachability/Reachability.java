package Reachability;

import Utilities.Visitor;
import AST.*;
import Utilities.Error;
import Utilities.Log;
import java.lang.Boolean;

/**
 * If a visit returns 'true', then is it because the code represented
 * by that node can sometimes run to completion, i.e., never _always_
 * returns or breaks or continues.
 */


public class Reachability extends Visitor<Boolean> {

    LoopStatement loopConstruct = null;
    SwitchStat switchConstruct = null;
    boolean inParBlock = false;
    
    public Reachability() {
	Log.logHeader("****************************************");
	Log.logHeader("*        R E A C H A B I L I T Y       *");
	Log.logHeader("****************************************");
    }

    // DONE
    public Boolean visitIfStat(IfStat is) {
	Log.log(is,"Visiting an if-Statement.");
	// if (true) S1 else S2 - S2 is unreachable
	if (is.expr().isConstant() && 
	    (is.expr() instanceof PrimitiveLiteral) &&
	    (((PrimitiveLiteral)is.expr()).constantValue().equals("true")) &&
	    is.elsepart() != null) 
	    Error.error(is, "Else-part of if-statement unreachable.", false, 5000);
	// if (false) S1 ... - S1 is unreachable
	if (is.expr().isConstant() && 
	    (is.expr() instanceof PrimitiveLiteral) &&
	    (((PrimitiveLiteral)is.expr()).constantValue().equals("false")))
	    Error.error(is, "Then-part of if-statement unreachable.", false, 5001);
	boolean thenBranch = true;
	boolean elseBranch = true;
	thenBranch = is.thenpart().visit(this);
	if (is.elsepart() != null)
	    elseBranch = is.elsepart().visit(this);
	return new Boolean(thenBranch || elseBranch);
    }

    // DONE
    public Boolean visitWhileStat(WhileStat ws) {
	Log.log(ws,"Visiting a while-statement.");
	LoopStatement oldLoopConstruct = loopConstruct;
	loopConstruct = ws;

	boolean b = ws.stat().visit(this);
	/*System.out.println("- Can run to completion: " + b);
		System.out.println("- Has a break..........: " + ws.hasBreak);
	System.out.println("- Has a return.........: " + ws.hasReturn);
	System.out.println("- Has a constant expr..: " + ws.expr().isConstant());
	System.out.println("- Expr is Literal......: " + (ws.expr() instanceof PrimitiveLiteral));
	System.out.println("- Is it true? .........: " + (((PrimitiveLiteral)ws.expr()).constantValue()));
	*/     
	if (ws.expr().isConstant() &&       
	    (ws.expr() instanceof PrimitiveLiteral) && 
	    (((PrimitiveLiteral)ws.expr()).constantValue().equals("true")) &&
	    b &&                         // the statement can run to completion
	    !ws.hasBreak && !ws.hasReturn) {  // but has no breaks, so it will loop forever
	    Error.error(ws,"While-statement is an infinite loop", false, 5002);
	    loopConstruct = oldLoopConstruct;
	    return new Boolean(false);
	}

	if (ws.expr() != null &&
	    ws.expr().isConstant() &&
	    (ws.expr() instanceof PrimitiveLiteral) &&
	    (((PrimitiveLiteral)ws.expr()).constantValue().equals("false"))) {
	    Error.error(ws,"Body of while-statement unreachable.", false, 5012);
	    loopConstruct = oldLoopConstruct;
	    return new Boolean(true);
	}

	loopConstruct = oldLoopConstruct;
	if (ws.hasReturn && !b)
	    return new Boolean(false);
	return new Boolean(true);
    }

    // DONE
    public Boolean visitDoStat(DoStat ds) {
	Log.log(ds,"Visiting a do-statement.");
	LoopStatement oldLoopConstruct = loopConstruct;
	loopConstruct = ds;
	
	boolean b = ds.stat().visit(this);

	if (ds.expr().isConstant() && 
	    ds.expr() instanceof PrimitiveLiteral && 
	    (((PrimitiveLiteral)ds.expr()).constantValue().equals("true")) &&
	    b &&                         // the statement can run to completion
	    !ds.hasBreak && !ds.hasReturn) {  // but has no breaks, so it will loop forever
	    loopConstruct = oldLoopConstruct;
	    Error.error(ds,"Do-statement is an infinite loop.", false, 5011);
	    return new Boolean(false);
	}
	loopConstruct = oldLoopConstruct;
	return new Boolean(true);
    }

    // DONE
    public Boolean visitBlock(Block bl) {
	Log.log(bl,"Visiting a block." + bl.stats().size());
	boolean canFinish = true;
	boolean b = true;
	for (int i=0; i<bl.stats().size(); i++) {
	    if (bl.stats().child(i) != null) {
		b = bl.stats().child(i).visit(this);
		if (!b && bl.stats().size()-1 > i) {
		    Error.error(bl.stats().child(i),"Unreachable code following statement beginning on line " + bl.stats().child(i).line + ".", false, 5003); 
		    canFinish = false;
		} 
	    }
	}
	if (!b)
	    canFinish = b;
	return new Boolean(canFinish);
    }

    // DONE
    public Boolean visitForStat(ForStat fs) {
	Log.log(fs,"Visiting a for-statement.");
	LoopStatement oldLoopConstruct = loopConstruct;
	loopConstruct = fs;

	// for (....; false ; ....) S1
	if (fs.expr() != null &&
	    fs.expr().isConstant() &&
	    (fs.expr() instanceof PrimitiveLiteral) &&
	    (((PrimitiveLiteral)fs.expr()).constantValue().equals("false"))) {
	    Error.error(fs,"Body of for-statement unreachable.", false, 5004);
	    loopConstruct = oldLoopConstruct;
	    return new Boolean(true);
	}
	    
	boolean b = true;
	if (fs.stats() != null)
	    b = fs.stats().visit(this);

	// for (... ; true; ...) S1
	if ((fs.expr() == null ||
	    (fs.expr().isConstant() && 
	    (fs.expr() instanceof PrimitiveLiteral) && 
	     (((PrimitiveLiteral)fs.expr()).constantValue().equals("true")))) &&
	    b &&                        // the statement can run to completion
	    !fs.hasBreak && !fs.hasReturn)  // but has no breaks, so it will loop forever
	    {
		Error.error(fs,"For-statement is an infinite loop.", false, 5005);
		loopConstruct = oldLoopConstruct;
		return new Boolean(false);
	    }	    
	loopConstruct = oldLoopConstruct;
	return new Boolean(true);
    }
    
    //AltStat
    // WHAT TODO??

    // DONE
    public Boolean visitBreakStat(BreakStat bs) {
	Log.log(bs,"Visiting a break-statement.");
	if (inParBlock)
	    Error.error(bs,"Break-statement inside par-block is not legal.", false, 5009);

	if (loopConstruct == null && switchConstruct == null) {
	    Error.error(bs, "Break statement outside loop or switch construct.", false, 5006);
	    return new Boolean(true); // this break doesn't matter cause it can't be here anyways!
	}
	loopConstruct.hasBreak = true;
	return new Boolean(false);
    }
    
    // DONE
    public Boolean visitChannelWriteStat(ChannelWriteStat cws) {
	Log.log(cws,"Visiting a channel-write-statement.");
	return new Boolean(true);
    }
    
    // DONE
    public Boolean visitClaimStat(ClaimStat cs) {
	Log.log(cs,"Visiting a claim-statement.");
	return new Boolean(true);
    }

    // DONE
    public Boolean visitContinueStat(ContinueStat cs) {
	Log.log(cs,"Visiting a continue-statement.");
	if (inParBlock) 
	    Error.error(cs,"Continue-statement inside par-block is not legal.", false, 5009);
	if (loopConstruct == null) {
	    Error.error(cs, "Continue statement outside loop construct.", false, 5007); 
	    return new Boolean(true); // this continue doesn't matter cause it can't be here anyways!
	}
	loopConstruct.hasContinue = true;
	return new Boolean(false);
    }
    // DONE
    public Boolean visitLocalDecl(LocalDecl ld) {
	Log.log(ld,"Visiting a local-decl-statement.");
	return new Boolean(true);
    }

    //ParBlock.java:public class ParBlock extends Statement {
    public Boolean visitParBlock(ParBlock pb) {
	boolean oldInParBlock = inParBlock;
	inParBlock = true;
	pb.stats().visit(this);
	inParBlock = oldInParBlock;
	return new Boolean(true);
    }
    
    // DONE
    public Boolean visitReturnStat(ReturnStat rs) {
	Log.log(rs,"Visiting a return-statement.");
	if (inParBlock) 
	    Error.error(rs,"Return-statement inside par-block is not legal.", false, 5008);

	if (loopConstruct != null)
	    loopConstruct.hasReturn = true;
	return new Boolean(false);
    }
    
    // DONE
    public Boolean visitSkipStat(SkipStat ss) {
	Log.log(ss,"Visiting a skip-statement.");
	return new Boolean(true);
    }

    // DONE
    public Boolean visitStopStat(StopStat ss) {
	Log.log(ss,"Visiting a stop-statement.");
	return new Boolean(false);
    }
    
    // DONE
    public Boolean visitSuspendStat(SuspendStat ss) {
	Log.log(ss,"Visiting a suspend-statement.");
	return new Boolean(true);
    }
    
    public Boolean visitSwitchStat(SwitchStat ss) {
	Log.log(ss,"Visiting a switch-statement.");
	SwitchStat oldSwitchConstruct = switchConstruct;
	switchConstruct = ss;
	// TODO finish this!
	switchConstruct = oldSwitchConstruct;
	return new Boolean(true);
    }

    // DONE
    public Boolean visitSyncStat(SyncStat ss) {
	Log.log(ss,"Visiting a while-statement.");
	return new Boolean(true);
    }

    // DONE
    public Boolean visitTimeoutStat(TimeoutStat ts) {
	Log.log(ts,"Visiting a timeout-statement.");
	return new Boolean(true);
    }
    
    // DONE
    public Boolean visitExprStat(ExprStat es) {
	Log.log(es,"Visiting an expr-statement.");
	return new Boolean(true);
    }
}