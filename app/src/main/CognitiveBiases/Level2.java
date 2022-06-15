
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

        var G = PolicyGraph.makePolicyGraph(List.of(initBelattl2), attl2, policy);
        System.out.println(String.format("Graph is %s", G));

        var allObs = attl2.oAll;

        var allObsLabels = allObs.stream()
            .map(os -> IntStream.range(0, os.size()).boxed()
                    .map(i -> Global.valNames.get(attl2.i_Om_p.get(i) - 1).get(os.get(i) - 1))
                    .collect(Collectors.toList()))
            .collect(Collectors.toList());

        System.out.println(String.format("Obs are %s", allObs));
        System.out.println(String.format("Obs are %s", allObsLabels));

        var in = new Scanner(System.in);
        var b = initBelattl2;
        while (true) {

            var b_factors = DDOP.factors(b, attl2.i_S());
            var b_EC = b_factors.get(b_factors.size() - 1);

            System.out.println(
                    String.format(
                        "Belief of attacker level 2 is %s", 
                        DDOP.getFrameBelief(
                            b, 
                            attl2.PThetajGivenEC, 
                            attl2.i_EC, 
                            attl2.i_S())));
            System.out.println(
                    String.format(
                        "Belief of attacker level 2 is %s", 
                        b_factors));
            System.out.println(
                    String.format(
                        "Predicted actions: %s", 
                        DDOP.addMultVarElim(
                            List.of(attl2.PAjGivenEC, b_EC), 
                            List.of(attl2.i_EC))));
            System.out.println(
                    String.format(
                        "Enter action from %s (suggested %s)", 
                        attl2.A(), 
                        attl2.A().get(
                            policy.getBestActionIndex(b, attl2.i_S))));
            int aIndex = in.nextInt();

            System.out.println(
                    String.format(
                        "Enter observations from %s", 
                        allObsLabels));
            int oIndex = in.nextInt();

            System.out.println(
                    String.format(
                        "Action: %s, obs: %s", 
                        attl2.A().get(aIndex), allObsLabels.get(oIndex)));
            b = attl2.beliefUpdate(b, aIndex, allObs.get(oIndex));
        }

    }
}
