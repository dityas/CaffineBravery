package thinclab.biases;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import thinclab.DDOP;
import thinclab.legacy.DD;
import thinclab.legacy.Global;
import thinclab.models.IPOMDP.IPOMDP;

class ConfBiasIPOMDP extends IPOMDP {

    public ConfBiasIPOMDP(IPOMDP i) {

        super(i);
    }

    public List<DD> evidenceFactors(DD o) {

        var vars = new ArrayList<>(o.getVars());
        return vars.size() > 1 ? 
            DDOP.factors(o, vars) : vars.size() == 1 ? 
            List.of(o) : List.of();
    }

    public float getWeight(List<DD> OFaoFactors, List<DD> b_pFactors) {

        float sum = 0.0f;

        for (var f : OFaoFactors) {

            var vars = f.getVars();
            
            if (vars.size() == 1) {
                // Because of the variable ordering, the required var 
                // will always be at the var index - (# unprimed vars) 
                // in the array the -1 is to index the var in Globals.

                int varIndex = vars.first() 
                        - 1 - (Global.NUM_VARS / 2);

                if (varIndex < 0)
                    varIndex = b_pFactors.size() - 1;

                var p = b_pFactors.get(varIndex);
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
            var _w = DDOP.pow(ofao, w);
            weighted.add(_w);
            System.out.printf("For %s, Gamma is %s = %s\r\n", ofao, w, _w);
        }

        return weighted;
    }

    @Override
    public DD beliefUpdate(DD b, int a, List<Integer> o) {

        var OFao = DDOP.restrict(this.OF.get(a), i_Om_p, o);

		var factors = new ArrayList<DD>(S().size() + S().size() + Omj.size() + 3);

		factors.add(b);
		factors.add(PAjGivenEC);
		factors.add(PThetajGivenEC);
		factors.add(Taus.get(a));
		factors.addAll(T().get(a));

		var vars = new ArrayList<Integer>(factors.size());
		vars.addAll(i_S());
		vars.add(i_Thetaj);
		// vars.add(i_Aj);

        var b_p = DDOP.addMultVarElim(factors, vars);
		var stateVars = new ArrayList<Integer>(i_S());

        var _vars = new ArrayList<>(i_S_p());
        _vars.add(i_Aj);
        
        // compute evidence weight
        var wOFao = getWeightedEvidence(
                DDOP.factors(b_p, _vars), OFao);

        wOFao.add(b_p);
        b_p = DDOP.addMultVarElim(wOFao, List.of(i_Aj));
		b_p = DDOP.primeVars(b_p, -(Global.NUM_VARS / 2));
		
        var prob = DDOP.addMultVarElim(List.of(b_p), stateVars);

		if (DDOP.abs(DDOP.sub(prob, DD.zero)).getVal() < 1e-6)
			return DD.zero;

		b_p = DDOP.div(b_p, prob);

		return b_p;
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

