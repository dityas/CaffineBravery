
package CognitiveBiases;

import thinclab.legacy.Global;
import thinclab.models.POMDP;
import thinclab.executables.SimulateBeliefUpdates;
import thinclab.spuddx_parser.SpuddXMainParser;

public class ConfBiasLevel0 {

    public static void main(String[] args) {

        System.out.println("Running cognitive biases");
        System.out.println("Running level 0");

        var runner = new SpuddXMainParser(args[0]);
        runner.run();

        System.out.println(String.format("[=] Parsed %s", args[0]));

        var _pomdp = (POMDP) runner.getModel("attl0").orElseGet(() ->
                {

                    System.err.println("No model found");
                    System.exit(-1);
                    return null;
                });

        var attLevel0 = new ConfBiasPOMDP(
                _pomdp.S, _pomdp.O, Global.varNames.get(_pomdp.i_A - 1), 
                _pomdp.TF, _pomdp.OF, _pomdp.R, _pomdp.discount);
        var b = runner.getDD("initAttl0");
        
        SimulateBeliefUpdates.runSimulator(attLevel0, b, null);
    }
}
