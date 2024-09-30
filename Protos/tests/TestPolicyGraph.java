import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import thinclab.DDOP;
import thinclab.legacy.DDleaf;
import thinclab.legacy.DDnode;
import thinclab.legacy.Global;
import thinclab.models.POMDP;
import thinclab.models.IPOMDP.IPOMDP;
import thinclab.models.IPOMDP.MjThetaSpace;
import thinclab.models.datastructures.PolicyGraph;
import thinclab.models.datastructures.PolicyTreeFSC;
import thinclab.policy.AlphaVectorPolicy;
import thinclab.solver.SymbolicPerseusSolver;
import thinclab.spuddx_parser.SpuddXMainParser;
import thinclab.utils.Tuple;

/*
 *	THINC Lab at UGA | Cyber Deception Group
 *
 *	Author: Aditya Shinde
 * 
 *	email: shinde.aditya386@gmail.com
 */

/*
 * @author adityas
 *
 */
class TestPolicyGraph {

	private static final Logger LOGGER = LogManager.getLogger(TestPolicyGraph.class);

	@BeforeEach
	void setUp() throws Exception {

		Global.clearAll();
	}

	@AfterEach
	void tearDown() throws Exception {

		Global.clearAll();
	}

	void printMemConsumption() throws Exception {

		var total = Runtime.getRuntime().totalMemory() / 1000000.0;
		var free = Runtime.getRuntime().freeMemory() / 1000000.0;

		LOGGER.info(String.format("Free mem: %s", free));
		LOGGER.info(String.format("Used mem: %s", (total - free)));
		Global.logCacheSizes();
	}
	
	@Test
	void testPOMDPPolicyGraph() throws Exception {

		System.gc();

		LOGGER.info("Running Single agent tiger domain");
		String domainFile = this.getClass().getClassLoader()
            .getResource("test_domains/test_tiger_domain.spudd").getFile();

		// Run domain
		var domainRunner = new SpuddXMainParser(domainFile);
		domainRunner.run();

		// Get agent I
		var I = (POMDP) domainRunner.getModel("agentI").orElseGet(() ->
			{
				LOGGER.error("Model not found");
				System.exit(-1);
				return null;
			});

		var solver = new SymbolicPerseusSolver<POMDP>(I);
		var policy = solver.solve(List.of(DDleaf.getDD(0.5f)), 100, 10);

        var T = new PolicyTreeFSC(List.of(DDleaf.getDD(0.5f)), I, policy, 10);
        T.makeFSC(I.oAll);

        var mj = new MjThetaSpace(List.of(DDleaf.getDD(0.5f)), 0, I);
	}

	@Test
	void testL1IPOMDPSolution() throws Exception {

		System.gc();

		LOGGER.info("Testing L1 IPOMDP Solver Gaoi on single frame tiger problem");
		String domainFile = this.getClass().getClassLoader().getResource("test_domains/test_ipomdpl1.spudd").getFile();

		// Run domain
		var domainRunner = new SpuddXMainParser(domainFile);
		domainRunner.run();

		// Get agent I
		var I = (IPOMDP) domainRunner.getModel("agentI").orElseGet(() ->
			{

				LOGGER.error("Model not found");
				System.exit(-1);
				return null;
			});

		var solver = new SymbolicPerseusSolver<IPOMDP>(I);

		var b_i = DDOP.mult(DDleaf.getDD(0.5f),
				DDnode.getDistribution(I.i_Mj, List.of(Tuple.of("m0", 0.5f),
                        Tuple.of("m1", 0.5f))));

		LOGGER.debug(String.format("Level 1 recursive belief is %s", b_i));

		var dd = I.getECDDFromMjDD(b_i);
		var policy = solver.solve(List.of(dd), 100, I.H);
    }
}
