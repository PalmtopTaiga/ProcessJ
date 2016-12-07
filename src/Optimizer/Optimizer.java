
package Optimizer;

/**
 *
 * @author trenton
 */


import AST.*;
import Utilities.Error;
import Utilities.Visitor;
import CFG.*;
import Utilities.Log;

public class Optimizer {
    
    public Optimizer(Compilation co){
    
        System.out.println("**************************************");
        System.out.println("* C O D E   O P T I M I Z A T I O N  *");
        System.out.println("**************************************");
        System.out.println("Optimizer Running");
        
        
        CFG cfg = new CFG(co);
        cfg.printCFG();
    }       
    
}
