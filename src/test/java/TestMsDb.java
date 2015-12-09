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
import java.util.Vector;
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
	MsPeakForestDb db = null;

	//////////////////////////
	// START RENGINE AND DB //
	//////////////////////////

	@Before
	public void startREngineAndDb() throws java.net.MalformedURLException, REngineException, REXPMismatchException {
		this.rengine = org.rosuda.REngine.JRI.JRIEngine.createEngine();
		this.db = new MsPeakForestDb(this.rengine, new java.net.URL("http://rest.peakforest.org/"), "java-msdb.test ; pierrick.roger@gmail.com");
	}

	/////////////////////////
	// STOP RENGINE AND DB //
	/////////////////////////

	@After
	public void stopREngineAndDb() {
		if (rengine != null) {
			this.rengine.close();
			this.rengine = null;
			this.db = null;
		}
	}

	////////////////////////
	// TEST GET MZ VALUES //
	////////////////////////

	@Test
	public void testGetMzValues() throws REngineException, REXPMismatchException {
		double[] mzpos = this.db.getMzValues(MsDb.Mode.POSITIVE);
		double[] mzneg = this.db.getMzValues(MsDb.Mode.NEGATIVE);
		assertTrue(mzpos.length >= 0);
		assertTrue(mzneg.length >= 0);
		assertTrue(mzpos.length > 0 || mzneg.length > 0);
	}

	///////////////////////////
	// TEST SEARCH ARGUMENTS //
	///////////////////////////

	@Test(expected=IllegalArgumentException.class)
	public void testSearchNoMzInInput() throws REngineException, REXPMismatchException {
		Map<MsDb.Field, Collection> input = new HashMap<MsDb.Field, Collection>();
		Map<MsDb.Field, Collection> output = this.db.searchMzRt(input, MsDb.Mode.POSITIVE, 0.0, 5.0, Double.NaN, Double.NaN, null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testSearchWrongSizesInInput() throws REngineException, REXPMismatchException {
		Map<MsDb.Field, Collection> input = new HashMap<MsDb.Field, Collection>();
		Vector<Double> mz = new Vector<Double>();
		mz.add(100.0);
		mz.add(110.0);
		Vector<Double> rt = new Vector<Double>();
		rt.add(3.5);
		input.put(MsDb.Field.MZ, mz);
		input.put(MsDb.Field.RT, rt);
		Map<MsDb.Field, Collection> output = this.db.searchMzRt(input, MsDb.Mode.POSITIVE, 0.0, 5.0, Double.NaN, Double.NaN, null);
	}

	////////////////////
	// TEST MZ SEARCH //
	////////////////////

	@Test
	public void testMzSearch() throws REngineException, REXPMismatchException {

		Map<MsDb.Field, Collection> input = new HashMap<MsDb.Field, Collection>();
		Vector<Double> mz = new Vector<Double>();
		mz.add(100.0);
		input.put(MsDb.Field.MZ, mz);
		Map<MsDb.Field, Collection> output = db.searchMzRt(input, MsDb.Mode.POSITIVE, 0.0, 5.0, Double.NaN, Double.NaN, null);

		// Check that all requested fields are present
		assertTrue(output.containsKey(MsDb.Field.MOLID));
		assertTrue(output.containsKey(MsDb.Field.MOLNAMES));
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

	////////////////////////////////////
	// TEST MZ SEARCH EXISTING  VALUE //
	////////////////////////////////////

	@Test
	public void testMzSearchExistingValue() throws REngineException, REXPMismatchException {
		for (MsDb.Mode mode: MsDb.Mode.class.getEnumConstants()) {

			// Get list of existing mz values in POS mode
			double[] mzvals = this.db.getMzValues(mode);

			if (mzvals.length > 0) {

				double mz = mzvals[0];

				// Search for the first mz
				Map<MsDb.Field, Collection> input = new HashMap<MsDb.Field, Collection>();
				Vector<Double> vmz = new Vector<Double>();
				vmz.add(mz);
				input.put(MsDb.Field.MZ, vmz);
				Map<MsDb.Field, Collection> output = db.searchMzRt(input, mode, 0.0, 5.0, Double.NaN, Double.NaN, null);

				// Check that all requested fields are present
				assertTrue(output.containsKey(MsDb.Field.MOLID));
				assertTrue(output.containsKey(MsDb.Field.MZ));

				// Check that at least one line is returned.
				assertTrue(output.get(MsDb.Field.MOLID).size() >= 1);

				// Check that molid field is set.
				Collection<String> molids = (Collection<String>)output.get(MsDb.Field.MOLID);
				for (String molid: molids)
					assertTrue(molid.length() > 0);
			}
		}
	}

	///////////////////////
	// TEST MZ RT SEARCH //
	///////////////////////

	@Test
	public void testMzRtSearch() throws REngineException, REXPMismatchException {

		Map<MsDb.Field, Collection> input = new HashMap<MsDb.Field, Collection>();
		Vector<Double> mz = new Vector<Double>();
		Vector<Double> rt = new Vector<Double>();
		mz.add(100.0);
		rt.add(6.5);
		input.put(MsDb.Field.MZ, mz);
		input.put(MsDb.Field.RT, rt);

		Vector<String> cols = new Vector<String>();
		cols.add("blabla"); // TODO

		Map<MsDb.Field, Collection> output = db.searchMzRt(input, MsDb.Mode.POSITIVE, 0.0, 5.0, 5.0, 0.8, cols);

		// Check that all requested fields are present
		assertTrue(output.containsKey(MsDb.Field.MOLID));
		assertTrue(output.containsKey(MsDb.Field.MOLNAMES));
		assertTrue(output.containsKey(MsDb.Field.MZ));
		assertTrue(output.containsKey(MsDb.Field.MZTHEO));
		assertTrue(output.containsKey(MsDb.Field.ATTR));
		assertTrue(output.containsKey(MsDb.Field.COMP));
		assertTrue(output.containsKey(MsDb.Field.RT));
		assertTrue(output.containsKey(MsDb.Field.COLRT));
		assertTrue(output.containsKey(MsDb.Field.COL));

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
