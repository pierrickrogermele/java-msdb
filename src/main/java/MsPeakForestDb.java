package org.openscience.msdb;

import java.util.Map;
import java.util.HashMap;
import java.util.Vector;
import java.util.Collection;
import java.util.Arrays;
import org.rosuda.REngine.REngine;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.REXPMismatchException;

/**
 * An abstract class for modeling a Mass Spectra database.
 *
 * @author Pierrick Roger
 */
public class MsPeakForestDb extends MsDb {

	private REngine rengine = null;
	private REXP pfdb = null; // MsPeakForestDb R instance. 

	/**
	 * Constructor.
	 *
	 * @param rengine An REngine instance.
	 * @param url The URL of the Peakforest database.
	 
	 * @param useragent The user agent string to use when contacting the Peakforest URL.
	 */
	public MsPeakForestDb(REngine rengine, java.net.URL url, String useragent) throws REngineException, REXPMismatchException {

		this.rengine = rengine;

		// Thread safety: lock
		int lock = this.rengine.lock();

		this.rengine.parseAndEval("source('/Users/pierrick/dev/lcmsmatching/r-msdb/MsPeakForestDb.R', chdir = TRUE)");
		this.rengine.parseAndEval("source('/Users/pierrick/dev/lcmsmatching/r-msdb/MsDbInputDataFrameStream.R', chdir = TRUE)");
		this.rengine.parseAndEval("source('/Users/pierrick/dev/lcmsmatching/r-msdb/MsDbOutputDataFrameStream.R', chdir = TRUE)");
		this.rengine.parseAndEval("db <- MsPeakForestDb$new(url = \"" + url + "\", useragent = \"" + useragent + "\")");

		// Thread safety: unlock
		this.rengine.unlock(lock);
	}

	///////////////////
	// GET MZ VALUES //
	///////////////////

	public double[] getMzValues(Mode mode) throws REngineException, REXPMismatchException {

		// Set function parameters
		String params = "mode = " + (mode == Mode.POSITIVE ? "MSDB.TAG.POS" : "MSDB.TAG.NEG");

		// Call method
		return this.rengine.parseAndEval("db$getMzValues(" + params + ")").asDoubles();
	}

	/////////////////////////
	// GET RETENTION TIMES //
	/////////////////////////

	public Map<String, double[]> getRetentionTimes(String molid, String[] cols) throws REngineException, REXPMismatchException {

		// Set function parameters
		String params = "molid = " + molid;
		if (cols != null) {
			params += ", col = c(";
			int i = 0;
			for (String c: cols)
				params += (i > 0 ? ", " : "") + "'" + c + "'";
			params += ")";
		}

		// Call method
		this.rengine.parseAndEval("rt <- db$getRetentionTimes(" + params + ")");

		// Create returned structure
		Map<String, double[]> rt = new HashMap<String, double[]>();

		// Get column IDs
		String[] colids = this.rengine.parseAndEval("names(rt)").asStrings();

		// Fill the map
		for (String c: colids)
			rt.put(c, this.rengine.parseAndEval("rt[['" + c +"']]").asDoubles());

		return rt;
	}

	////////////////////////
	// COLLECTION TO REXP //
	////////////////////////

	private static REXPDouble collectionToREXPDouble(Collection c) {

		double[] v = new double[c.size()];
		int i = 0;
		for (Double x: (Collection<Double>)c)
			v[i++] = x;

		return new REXPDouble(v);
	}

	//////////////////
	// SEARCH MZ RT //
	//////////////////

	public Map<Field, Collection> searchMzRt(Map<Field, Collection> input, Mode mode, double shift, double prec, double rttolx, double rttoly, Collection<String> cols) throws REngineException, REXPMismatchException {

		// Check that MZ is present
		if ( ! input.containsKey(Field.MZ))
			throw new IllegalArgumentException("Input map must contain MZ values.");

		// Check that all vectors in input map have the same length
		int s = -1;
		for (Field f: input.keySet())
			if (s < 0)
				s = input.get(f).size();
			else if (s != input.get(f).size())
				throw new IllegalArgumentException("All collections in input map must have the same size.");

		// Thread safety: lock
		int lock = this.rengine.lock();

		// Create input stream
		this.rengine.assign("mz", collectionToREXPDouble(input.get(Field.MZ)));
		String inputmzrt = "mz = mz";
		if (input.containsKey(Field.RT)) {
			this.rengine.assign("rt", collectionToREXPDouble(input.get(Field.RT)));
			inputmzrt += ", rt = rt";
		}
		this.rengine.parseAndEval("input.stream <- MsDbInputDataFrameStream$new(msdb.make.input.df(" + inputmzrt + "))");
		this.rengine.parseAndEval("db$setInputStream(input.stream)");

		// Create output stream
		this.rengine.parseAndEval("output.stream <- MsDbOutputDataFrameStream$new()");
		this.rengine.parseAndEval("db$addOutputStreams(output.stream)");

		// Set function parameters
		String params = "mode = " + (mode == Mode.POSITIVE ? "MSDB.TAG.POS" : "MSDB.TAG.NEG");
		params += ", shift = " + shift;
		params += ", prec = " + prec;
		if (input.containsKey(Field.RT)) {
			params += ", rt.tol.x = " + rttolx;
			params += ", rt.tol.y = " + rttoly;
			params += ", col = c(";
			int i = 0;
			for (String c: cols)
				params += (i++ > 0 ? ", " : "") + "'" + c + "'";
			params += ")";
		}

		// Call search method
		this.rengine.parseAndEval("db$searchForMzRtList(" + params + ")");

		// Get output
		Map<Field, Collection> output = new HashMap<Field, Collection>();
		this.rengine.parseAndEval("output <- output.stream$getDataFrame()");
		output.put(Field.MOLID, Arrays.asList(this.rengine.parseAndEval("output[[MSDB.TAG.MOLID]]").asStrings()));
		output.put(Field.MOLNAMES, Arrays.asList(this.rengine.parseAndEval("output[[MSDB.TAG.MOLNAMES]]").asStrings()));
		output.put(Field.MZ, Arrays.asList(this.rengine.parseAndEval("output[[MSDB.TAG.MZ]]").asDoubles()));
		output.put(Field.MZTHEO, Arrays.asList(this.rengine.parseAndEval("output[[MSDB.TAG.MZTHEO]]").asDoubles()));
		output.put(Field.ATTR, Arrays.asList(this.rengine.parseAndEval("output[[MSDB.TAG.ATTR]]").asStrings()));
		output.put(Field.COMP, Arrays.asList(this.rengine.parseAndEval("output[[MSDB.TAG.COMP]]").asStrings()));
		if (input.containsKey(Field.RT)) {
			output.put(Field.RT, Arrays.asList(this.rengine.parseAndEval("output[[MSDB.TAG.RT]]").asDoubles()));
			output.put(Field.COLRT, Arrays.asList(this.rengine.parseAndEval("output[[MSDB.TAG.COLRT]]").asDoubles()));
			output.put(Field.COL, Arrays.asList(this.rengine.parseAndEval("output[[MSDB.TAG.COL]]").asStrings()));
		}

		// Thread safety: unlock
		this.rengine.unlock(lock);

		return output;
	}
}
