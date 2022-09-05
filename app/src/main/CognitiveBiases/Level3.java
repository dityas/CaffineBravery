
package CognitiveBiases;

import java.util.List;
import thinclab.models.IPOMDP.IPOMDP;
import thinclab.policy.AlphaVectorPolicy;
import thinclab.solver.SymbolicPerseusSolver;
import thinclab.spuddx_parser.SpuddXMainParser;

public class Level3 {

    public static void main(String[] args) {

        System.out.println("Running cognitive biases");
        System.out.println("Running level 3");

        var runner = new SpuddXMainParser(args[0]);
        runner.run();

        System.out.println(String.format("[=] Parsed %s", args[0]));

        var defl3 = (IPOMDP) runner.getModel("defl3").orElseGet(() ->
                {

                    System.err.println("No model found");
                    System.exit(-1);
                    return null;
                });

        var initBeldefl3 = defl3.getECDDFromMjDD(runner.getDD("initDefl3Actual"));
        var policy = new SymbolicPerseusSolver<>().solve(List.of(initBeldefl3), defl3, 100, defl3.H,
                AlphaVectorPolicy.fromR(defl3.R()));

        IPOMDPSim.run(initBeldefl3, defl3, policy);
    }
}
