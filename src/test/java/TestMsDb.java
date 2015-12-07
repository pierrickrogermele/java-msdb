import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import org.openscience.msdb.MsDb;
import org.openscience.msdb.MsPeakForestDb;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import org.rosuda.REngine.REngine;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.REXPMismatchException;

/**
 * @author Pierrick Roger
 */
public class TestMsDb {

	REngine rengine = null;

	///////////////////
	// START RENGINE //
	///////////////////

	@Before
	public void startREngine() throws REngineException {
		this.rengine = org.rosuda.REngine.JRI.JRIEngine.createEngine();
	}

	//////////////////
	// STOP RENGINE //
	//////////////////

	@After
	public void stopREngine() {
		if (rengine != null) {
			this.rengine.close();
			this.rengine = null;
		}
	}

	////////////////////
	// TEST MZ SEARCH //
	////////////////////

	@Test
	public void testMzSearch() throws java.net.MalformedURLException, REngineException, REXPMismatchException {
		MsPeakForestDb db = new MsPeakForestDb(this.rengine, new java.net.URL("http://rest.peakforest.org/"), "java-msdb.test ; pierrick.roger@gmail.com");
		Map<MsDb.Field, REXP> input = new HashMap<MsDb.Field, REXP>();
		REXP mz = new REXPDouble(100.0);
		input.put(MsDb.Field.MZ, mz);
		Map<MsDb.Field, Collection> output = db.searchMzRt(input, MsDb.Mode.POSITIVE, 0.0, 5.0);

		// Check that all requested fields are present
		assertTrue(output.containsKey(MsDb.Field.MOLID));
		assertTrue(output.containsKey(MsDb.Field.MZ));
		assertTrue(output.containsKey(MsDb.Field.MZTHEO));
		assertTrue(output.containsKey(MsDb.Field.ATTR));
		assertTrue(output.containsKey(MsDb.Field.COMP));

		// Check that we have the same number of values for each field
		int s = -1;
		for (MsDb.Field f: output.keySet()) {
			if (s < 0)
				s = output.get(f).size();
			else
				assertTrue(output.get(f).size() == s);
		}

		// Check that at least one line is returned.
		assertTrue(s >= 1);
	}
}
