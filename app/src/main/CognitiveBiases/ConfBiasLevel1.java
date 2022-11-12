
package CognitiveBiases;

import thinclab.models.IPOMDP.IPOMDP;
import thinclab.executables.SimulateBeliefUpdates;
import thinclab.spuddx_parser.SpuddXMainParser;

public class ConfBiasLevel1 {

    public static void main(String[] args) {

        System.out.println("Running cognitive biases");
        System.out.println("Running level 1");

        var runner = new SpuddXMainParser(args[0]);
        runner.run();

        System.out.println(String.format("[=] Parsed %s", args[0]));

        var i = (IPOMDP) runner.getModel("attl1").orElseGet(() ->
                {

                    System.err.println("No model found");
                    System.exit(-1);
                    return null;
                });

        var attl1 = new ConfBiasIPOMDP(i);
        var b = runner.getDD("initAttl1");
        b = attl1.getECDDFromMjDD(b);
        
        SimulateBeliefUpdates.runSimulator(attl1, b, null);
    }
}
