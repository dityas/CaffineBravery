
package CognitiveBiases;

import java.util.List;

import thinclab.DDOP;
import thinclab.legacy.DD;
import thinclab.spuddx_parser.SpuddXMainParser;
import thinclab.spuddx_parser.SpuddXParser;

public class ObsTest {

    public static void main(String[] args) {

        var runner = new SpuddXMainParser(args[0]);
        runner.run();

        System.out.println(String.format("[=] Parsed %s", args[0]));

        var s = runner.getDD("pS");
        var mjGivenS = runner.getDD("pMjGivenS");
        var mjT = runner.getDD("pMjGivenAjMjSp");
        var sT = runner.getDD("pSpGivenS");
        System.out.println(String.format("P(S'|S) = %s", sT));

        var ajGivenMj = DDOP.restrict(
                runner.getDD("pAjGivenMj"),
                List.of(2),
                List.of(1));

        System.out.println(
                String.format("P(S) = %s", s));

        System.out.println(
                String.format("P(Mj|S) = %s", mjGivenS));

        // initial state P(S, Mj)
        var mjs = DDOP.mult(s, mjGivenS);
        System.out.println(
                String.format("P(S, Mj) = %s", mjs));

        var spmjp = 
            DDOP.addMultVarElim(
                    List.of(mjs, mjT, sT, ajGivenMj), 
                    List.of(1, 2, 3));

        System.out.println(
                String.format("P(S', Mj') intermediate %s", spmjp));
        spmjp = DDOP.restrict(spmjp, List.of(4), List.of(2));
        System.out.println(
                String.format("P(S', Mj') after observing %s", spmjp));
        var norm = DDOP.addMultVarElim(List.of(spmjp), List.of(6));
        System.out.println(
                String.format(
                    "P(S', Mj') = %s", DDOP.div(spmjp, norm)));
    }
}
