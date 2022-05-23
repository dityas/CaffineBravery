
package CognitiveBiases;

import java.util.List;
import thinclab.models.POMDP;
import thinclab.models.datastructures.PolicyGraph;
import thinclab.policy.AlphaVectorPolicy;
import thinclab.solver.SymbolicPerseusSolver;
import thinclab.spuddx_parser.SpuddXMainParser;

public class Level0 {

    public static void main(String[] args) {

        System.out.println("Running cognitive biases");
        System.out.println("Running level 0");

        var runner = new SpuddXMainParser(args[0]);
        runner.run();

        System.out.println(String.format("[=] Parsed %s", args[0]));

        var attLevel0 = (POMDP) runner.getModel("attLevel0").orElseGet(() ->
                {

                    System.err.println("No model found");
                    System.exit(-1);
                    return null;
                });

        var b = runner.getDDs().get("initAttl0");

        var policy = new SymbolicPerseusSolver<>()
            .solve(
                    List.of(b), 
                    attLevel0, 100, 
                    10, AlphaVectorPolicy.fromR(attLevel0.R()));

        var G = PolicyGraph.makePolicyGraph(
                List.of(b), 
                attLevel0, 
                policy);

        System.out.println(String.format("Graph is %s", G));
    }
}
