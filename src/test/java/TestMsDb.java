import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

/**
 * @author Pierrick Roger
 */
public class TestMsDb {

	//////////////////
	// START RSERVE //
	//////////////////

	static Process rserve_process = null;

	@BeforeClass
	public static void startRserve() throws java.io.IOException, RserveException, InterruptedException {
		rserve_process = Runtime.getRuntime().exec("R -e 'library(Rserve);run.Rserve()'");
		int i = 0;
		while(true) {
			Thread.sleep(500);
			try {
				RConnection c = new RConnection();// make a new local connection on default port (6311)
			} catch (RserveException e) {
				++i;
				if (i < 5)
					continue;
			}
			break;
		}
	}

	/////////////////
	// STOP RSERVE //
	/////////////////

	@AfterClass
	public static void stopRserve() {
		if (rserve_process != null)
			rserve_process.destroy();
	}

	////////////////////
	// TEST MZ SEARCH //
	////////////////////

	@Test
	public void testMzSearch() {
		System.err.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
		System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
	}
}
