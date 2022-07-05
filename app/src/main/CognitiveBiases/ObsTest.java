
package CognitiveBiases;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import thinclab.DDOP;
import thinclab.legacy.DD;
import thinclab.legacy.Global;
import thinclab.models.POMDP;
import thinclab.models.IPOMDP.IPOMDP;
import thinclab.models.datastructures.PolicyGraph;
import thinclab.policy.AlphaVectorPolicy;
import thinclab.solver.SymbolicPerseusSolver;
import thinclab.spuddx_parser.SpuddXMainParser;

public class ObsTest {

    public static DD update(
            DD b, 
            List<Integer> i_S, 
            List<Integer> i_Om_p,
            List<Integer> o,
            List<DD> T, List<DD> O) {
   
        var OFao = DDOP.restrict(O, i_Om_p, o);

        var allDDs = new ArrayList<DD>(T);
        allDDs.add(b);
        allDDs.addAll(OFao);

        var b_p = DDOP.addMultVarElim(allDDs, i_S);
        var next_b = DDOP.primeVars(b_p, - (Global.NUM_VARS / 2));
		DD obsProb = DDOP.addMultVarElim(List.of(next_b), i_S);

        return DDOP.div(next_b, obsProb);
    }

    public static void main(String[] args) {

        var runner = new SpuddXMainParser(args[0]);
        runner.run();

        System.out.println(String.format("[=] Parsed %s", args[0]));

        // Define vars
        var i_S = List.of(1, 2);
        var i_Om = List.of(4, 5);
        var i_Om_p = i_Om.stream()
            .map(i -> i + (Global.NUM_VARS / 2))
            .collect(Collectors.toList());

		// all possible observations
		var allObs = DDOP.cartesianProd(i_Om.stream()
                .map(o -> IntStream.range(1, Global.valNames.get(o - 1).size() + 1)
                    .boxed()
                    .collect(Collectors.toList()))
				.collect(Collectors.toList()));

        var allObsLabels = allObs.stream()
            .map(os -> IntStream.range(0, os.size()).boxed()
                    .map(i -> Global.valNames.get(i_Om.get(i) - 1).get(os.get(i) - 1))
                    .collect(Collectors.toList()))
            .collect(Collectors.toList());

//        System.out.println(String.format("Obs are %s", allObs));
        System.out.println(String.format("Obs are %s", allObsLabels));

        var stateT = runner.getDDs().get("hostTransition");
        var mjT = runner.getDDs().get("modeljTransition");
        var stateObs = runner.getDDs().get("assetObs");
        var actionObs = runner.getDDs().get("actionObs");
        var frameB = runner.getDDs().get("thetajBelief");

        var T = List.of(stateT, mjT);
        var O = List.of(stateObs, actionObs);

        var in = new Scanner(System.in);
        var b = runner.getDDs().get("initState");

        var stateVars = new ArrayList<Integer>();
        stateVars.add(1);

        while (true) {

            var b_factors = DDOP.factors(b, i_S);
            System.out.println(String.format("Belief over state is %s", b_factors));

            var frame = DDOP.addMultVarElim(List.of(b, frameB), i_S);
            System.out.println(String.format("Belief over state is %s", frame));

            System.out.println(
                    String.format(
                        "Enter observations from %s", 
                        allObsLabels));
            IntStream.range(0, allObsLabels.size()).forEach(o -> {
                System.out.println(
                        String.format("%s: %s", o, allObsLabels.get(o)));
            });
            int oIndex = in.nextInt();

            System.out.println(
                    String.format("obs: %s", allObsLabels.get(oIndex)));

            b = ObsTest.update(b, i_S, i_Om_p, allObs.get(oIndex), T, O);

        }

    }
}
