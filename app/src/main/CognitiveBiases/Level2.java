
package CognitiveBiases;

import java.util.List;
import thinclab.models.IPOMDP.IPOMDP;
import thinclab.policy.AlphaVectorPolicy;
import thinclab.solver.SymbolicPerseusSolver;
import thinclab.spuddx_parser.SpuddXMainParser;

public class Level2 {

    public static void main(String[] args) {

        System.out.println("Running cognitive biases");
        System.out.println("Running level 2");

        var runner = new SpuddXMainParser(args[0]);
        runner.run();

        System.out.println(String.format("[=] Parsed %s", args[0]));

        var attl2 = (IPOMDP) runner.getModel("attl2").orElseGet(() ->
                {

                    System.err.println("No model found");
                    System.exit(-1);
                    return null;
                });

        var initBelattl2 = attl2.getECDDFromMjDD(runner.getDDs().get("initAttl2"));
        var policy = new SymbolicPerseusSolver<>().solve(List.of(initBelattl2), attl2, 100, attl2.H,
                AlphaVectorPolicy.fromR(attl2.R()));

        IPOMDPSim.run(initBelattl2, attl2, policy);
    }
}
