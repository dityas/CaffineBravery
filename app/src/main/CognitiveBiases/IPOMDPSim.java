
package CognitiveBiases;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import thinclab.DDOP;
import thinclab.legacy.DD;
import thinclab.legacy.Global;
import thinclab.models.IPOMDP.IPOMDP;
import thinclab.policy.AlphaVectorPolicy;

public class IPOMDPSim {

    public static void run(
            DD initBel, IPOMDP m, AlphaVectorPolicy policy) {
        
        var allObs = m.oAll;

        var allObsLabels = allObs.stream()
            .map(os -> IntStream.range(0, os.size()).boxed()
                    .map(i -> Global.valNames.get(m.i_Om_p.get(i) - 1)
                        .get(os.get(i) - 1))
                    .collect(Collectors.toList()))
            .collect(Collectors.toList());

        var in = new Scanner(System.in);
        var b = initBel;
        while (true) {

            var b_factors = DDOP.factors(b, m.i_S());
            var b_EC = b_factors.get(b_factors.size() - 1);

            System.out.println(
                    String.format(
                        "Belief of %s over frames is %s",
                        m.getName(),
                        DDOP.getFrameBelief(
                            b, 
                            m.PThetajGivenEC, 
                            m.i_EC, 
                            m.i_S())));
            System.out.println(
                    String.format(
                        "Belief of %s is %s", 
                        m.getName(),
                        b_factors));
            System.out.println(
                    String.format(
                        "Predicted actions: %s", 
                        DDOP.addMultVarElim(
                            List.of(m.PAjGivenEC, b_EC), 
                            List.of(m.i_EC))));
            System.out.println(
                    String.format(
                        "Enter action from %s (suggested %s)", 
                        m.A(), 
                        m.A().get(
                            policy.getBestActionIndex(b, m.i_S))));
            int aIndex = in.nextInt();

            System.out.println(
                    String.format(
                        "Enter observations from %s", 
                        allObsLabels));
            int oIndex = in.nextInt();

            var reward = DDOP.dotProduct(b, m.R().get(aIndex), m.i_S());
            System.out.println(
                    String.format(
                        "Action: %s, obs: %s, R: %s", 
                        m.A().get(aIndex), allObsLabels.get(oIndex),
                        reward));
            
            System.out.println("Vn:");
            policy.printPolicyValuationAtBelief(b, m.A(), m.i_S());
            System.out.println();

            b = m.beliefUpdate(b, aIndex, allObs.get(oIndex));
        }
    }
}
