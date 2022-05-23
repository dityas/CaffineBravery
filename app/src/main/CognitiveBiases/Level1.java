
package CognitiveBiases;

import java.util.List;
import thinclab.models.POMDP;
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

        var defLevel1 = (IPOMDP) runner.getModel("defLevel1").orElseGet(() ->
                {

                    System.err.println("No model found");
                    System.exit(-1);
                    return null;
                });

        var initBelDefLevel1 = defLevel1.getECDDFromMjDD(runner.getDDs().get("initDefl1Actual"));
        var policy = new SymbolicPerseusSolver<>().solve(List.of(initBelDefLevel1), defLevel1, 100,
                10, AlphaVectorPolicy.fromR(defLevel1.R()));

        var G = PolicyGraph.makePolicyGraph(List.of(initBelDefLevel1), defLevel1, policy);
        System.out.println(String.format("Graph is %s", G));
    }
}
