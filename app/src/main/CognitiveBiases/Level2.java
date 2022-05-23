
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
		System.out.println("Running level 1");

		var runner = new SpuddXMainParser(args[0]);
		runner.run();

		System.out.println(String.format("[=] Parsed %s", args[0]));

		var attLevel2 = (IPOMDP) runner.getModel("attLevel2").orElseGet(() ->
			{

				System.err.println("No model found");
				System.exit(-1);
				return null;
			});

		var initBelAttLevel2 = attLevel2.getECDDFromMjDD(runner.getDDs().get("initAttl2"));
		var policy = new SymbolicPerseusSolver<>().solve(List.of(initBelAttLevel2), attLevel2, 100, attLevel2.H,
				AlphaVectorPolicy.fromR(attLevel2.R()));

		var G = PolicyGraph.makePolicyGraph(List.of(initBelAttLevel2), attLevel2, policy);
		System.out.println(String.format("Graph is %s", G));

		var allObs = attLevel2.oAll;

		var allObsLabels = allObs.stream()
				.map(os -> IntStream.range(0, os.size()).boxed()
						.map(i -> Global.valNames.get(attLevel2.i_Om_p.get(i) - 1).get(os.get(i) - 1))
						.collect(Collectors.toList()))
				.collect(Collectors.toList());
		
		System.out.println(String.format("Obs are %s", allObs));
		System.out.println(String.format("Obs are %s", allObsLabels));
	
		var in = new Scanner(System.in);
		var b = initBelAttLevel2;
		while (true) {
			
			System.out.println(String.format("Belief of attacker level 2 is %s", DDOP.getFrameBelief(b, attLevel2.PThetajGivenEC, attLevel2.i_EC, attLevel2.i_S())));
			System.out.println(String.format("Belief of attacker level 2 is %s", DDOP.factors(b, attLevel2.i_S())));
			
			System.out.println(String.format("Enter action from %s (suggested %s)", attLevel2.A(), attLevel2.A().get(policy.getBestActionIndex(b, attLevel2.i_S))));
			int aIndex = in.nextInt();
			
			System.out.println(String.format("Enter observations from %s", allObsLabels));
			int oIndex = in.nextInt();
			
			System.out.println(String.format("Action: %s, obs: %s", attLevel2.A().get(aIndex), allObsLabels.get(oIndex)));
			b = attLevel2.beliefUpdate(b, aIndex, allObs.get(oIndex));
			
			
		}
		
	}
}
