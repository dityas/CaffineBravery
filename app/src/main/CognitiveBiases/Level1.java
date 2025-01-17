
package CognitiveBiases;

import java.util.List;
import thinclab.models.IPOMDP.IPOMDP;
import thinclab.models.datastructures.PolicyGraph;
import thinclab.policy.AlphaVectorPolicy;
import thinclab.solver.SymbolicPerseusSolver;
import thinclab.spuddx_parser.SpuddXMainParser;

public class Level1 {

    public static void main(String[] args) {

        System.out.println("Running cognitive biases");
        System.out.println("Running level 1");

        var runner = new SpuddXMainParser(args[0]);
        runner.run();

        System.out.println(String.format("[=] Parsed %s", args[0]));

        var defl1 = (IPOMDP) runner.getModel("defl1").orElseGet(() ->
                {

                    System.err.println("No model found");
                    System.exit(-1);
                    return null;
                });

        var initBeldefl1 = defl1.getECDDFromMjDD(
                runner.getDD("initDefl1Actual"));

        System.out.println(initBeldefl1);

        var policy = new SymbolicPerseusSolver<>()
            .solve(
                    List.of(initBeldefl1), 
                    defl1, 100, 10, 
                    AlphaVectorPolicy.fromR(defl1.R()));

        var G = PolicyGraph.makePolicyGraph(
                List.of(initBeldefl1), 
                defl1, policy);

        System.out.println(String.format("Graph is %s", G));

        IPOMDPSim.run(initBeldefl1, defl1, policy);
    }
}
