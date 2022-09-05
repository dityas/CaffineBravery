
package CognitiveBiases;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import thinclab.DDOP;
import thinclab.legacy.DD;
import thinclab.legacy.Global;
import thinclab.models.POMDP;

class ConfBiasPOMDP extends POMDP {

    public ConfBiasPOMDP(
            List<String> S, List<String> O, String A,
            List<List<DD>> TF, List<List<DD>> OF, 
            List<DD> R, float discount) {

        super(S, O, A, TF, OF, R, discount);
            }

    public List<DD> evidenceFactors(DD o) {

        var vars = new ArrayList<>(o.getVars());
        return vars.size() > 1 ? 
            DDOP.factors(o, vars) : vars.size() == 1 ? 
            List.of(o) : List.of();
    }

    public float getWeight(List<DD> factors, List<DD> fPreds) {
        
        float sum = 0.0f;
        for (var f : factors) {

            var vars = f.getVars();
            
            if (vars.size() == 1) {
                // Because of the variable ordering, the required var 
                // will always be at the var index - (# unprimed vars) 
                // in the array the -1 is to index the var in Globals.
                var p = fPreds.get(vars.first() 
                        - 1 - (Global.NUM_VARS / 2));
                sum += DDOP.l2NormSq(
                        p, f, 
                        Global.valNames.get(vars.first() - 1).size());
            }
        }
        
        return 1.0f / (1.0f + sum);
    }

    public List<DD> getWeightedEvidence(List<DD> p, List<DD> OFao) {

        var weighted = new ArrayList<DD>(OFao.size());
        for (var ofao : OFao) {
            var w = getWeight(evidenceFactors(ofao), p);
            weighted.add(DDOP.pow(ofao, w));
        }

        return weighted;
    }
    
    @Override
    public DD beliefUpdate(DD b, int a, List<Integer> o) {

        var OFao = DDOP.restrict(this.OF.get(a), i_Om_p, o);

        // concat b, TF and OF
        var dynamicsArray = 
            new ArrayList<DD>(1 + i_S.size() + i_Om.size());
        dynamicsArray.add(b);
        dynamicsArray.addAll(TF.get(a));

        // Sumout[S] P(O'=o| S, A=a) x P(S'| S, A=a) x P(S)
        DD nextBelState = DDOP.addMultVarElim(dynamicsArray, i_S);

        // compute evidence weight
        var wOFao = getWeightedEvidence(
                DDOP.factors(nextBelState, i_S_p), OFao);
        
        // f_wevidence(S') x f_pred(S')
        wOFao.add(nextBelState);
        nextBelState = DDOP.mult(wOFao);

        nextBelState = 
            DDOP.primeVars(nextBelState, 
                    -(Global.NUM_VARS / 2));

        DD obsProb = 
            DDOP.addMultVarElim(List.of(nextBelState), i_S);

        if (obsProb.getVal() < 1e-8)
            return DD.zero;

        nextBelState = DDOP.div(nextBelState, obsProb);

        return nextBelState;
    }

    @Override
    public DD beliefUpdate(DD b, String a, List<String> o) {

        int actIndex = Collections.binarySearch(this.A, a);
        var obs = new ArrayList<Integer>(i_Om.size());

        for (int i = 0; i < i_Om_p.size(); i++) {

            obs.add(
                    Collections.binarySearch(
                        Global.valNames.get(i_Om.get(i) - 1), 
                        o.get(i)) + 1);
        }

        return this.beliefUpdate(b, actIndex, obs);
    }
}

