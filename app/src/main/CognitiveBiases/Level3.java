
package CognitiveBiases;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import thinclab.DDOP;
import thinclab.legacy.Global;
import thinclab.models.POMDP;
import thinclab.models.IPOMDP.IPOMDP;
import thinclab.models.datastructures.PolicyGraph;
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

        var initBeldefl3 = defl3.getECDDFromMjDD(runner.getDDs().get("initDefl3hp"));
        var policy = new SymbolicPerseusSolver<>().solve(List.of(initBeldefl3), defl3, 100, defl3.H,
                AlphaVectorPolicy.fromR(defl3.R()));

        var G = PolicyGraph.makePolicyGraph(List.of(initBeldefl3), defl3, policy);
        System.out.println(String.format("Graph is %s", G));

        var allObs = defl3.oAll;

        var allObsLabels = allObs.stream()
            .map(os -> IntStream.range(0, os.size()).boxed()
                    .map(i -> Global.valNames.get(defl3.i_Om_p.get(i) - 1).get(os.get(i) - 1))
                    .collect(Collectors.toList()))
            .collect(Collectors.toList());

        System.out.println(String.format("Obs are %s", allObs));
        System.out.println(String.format("Obs are %s", allObsLabels));

        var in = new Scanner(System.in);
        var b = initBeldefl3;
        while (true) {

            var b_factors = DDOP.factors(b, defl3.i_S());
            var b_EC = b_factors.get(b_factors.size() - 1);

            System.out.println(
                    String.format(
                        "Belief of attacker level 2 is %s", 
                        DDOP.getFrameBelief(
                            b, 
                            defl3.PThetajGivenEC, 
                            defl3.i_EC, 
                            defl3.i_S())));
            System.out.println(
                    String.format(
                        "Belief of attacker level 2 is %s", 
                        b_factors));
            System.out.println(
                    String.format(
                        "Predicted actions: %s", 
                        DDOP.addMultVarElim(
                            List.of(defl3.PAjGivenEC, b_EC), 
                            List.of(defl3.i_EC))));
            System.out.println(
                    String.format(
                        "Enter action from %s (suggested %s)", 
                        defl3.A(), 
                        defl3.A().get(
                            policy.getBestActionIndex(b, defl3.i_S))));
            int aIndex = in.nextInt();

            System.out.println(
                    String.format(
                        "Enter observations from %s", 
                        allObsLabels));
            int oIndex = in.nextInt();

            System.out.println(
                    String.format(
                        "Action: %s, obs: %s", 
                        defl3.A().get(aIndex), allObsLabels.get(oIndex)));
            b = defl3.beliefUpdate(b, aIndex, allObs.get(oIndex));
        }

    }
}
