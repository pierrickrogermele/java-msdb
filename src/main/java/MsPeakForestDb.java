package org.openscience.msdb;

import java.util.Map;
import java.util.HashMap;
import java.util.Vector;
import java.util.Collection;
import java.util.Arrays;
import org.rosuda.REngine.REngine;
import org.rosuda.REngine.REXP;
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
		this.rengine.parseAndEval("source('/Users/pierrick/dev/lcmsmatching/r-msdb/MsPeakForestDb.R', chdir = TRUE)");
		this.rengine.parseAndEval("source('/Users/pierrick/dev/lcmsmatching/r-msdb/MsDbInputDataFrameStream.R', chdir = TRUE)");
		this.rengine.parseAndEval("source('/Users/pierrick/dev/lcmsmatching/r-msdb/MsDbOutputDataFrameStream.R', chdir = TRUE)");
		this.rengine.parseAndEval("db <- MsPeakForestDb$new(url = \"" + url + "\", useragent = \"" + useragent + "\")", null, true);
	}

	//////////////////
	// SEARCH MZ RT //
	//////////////////

	public Map<Field, Collection> searchMzRt(Map<Field, REXP> input, Mode mode, double shift, double prec) throws REngineException, REXPMismatchException {

		// Check that MZ is present
		if ( ! input.containsKey(Field.MZ))
			throw new IllegalArgumentException("Input map must contain MZ values.");

		// TODO Check that all vectors in input map have the same length

		// Is RT present ?

		// TODO Thread safe ? lock/unlock

		// Create input stream
		this.rengine.assign("mz", input.get(Field.MZ));
		this.rengine.parseAndEval("input.stream <- MsDbInputDataFrameStream$new(msdb.make.input.df(mz))");
		this.rengine.parseAndEval("db$setInputStream(input.stream)");

		// Create output stream
		this.rengine.parseAndEval("output.stream <- MsDbOutputDataFrameStream$new()");
		this.rengine.parseAndEval("db$addOutputStreams(output.stream)");

		// Set MS mode value
		this.rengine.parseAndEval("mode <- " + (mode == Mode.POSITIVE ? "MSDB.TAG.POS" : "MSDB.TAG.NEG"));

		// Call search method
		this.rengine.parseAndEval("db$searchForMzRtList(mode = mode)");

		// Get output
		Map<Field, Collection> output = new HashMap<Field, Collection>();
		this.rengine.parseAndEval("output <- output.stream$getDataFrame()");
		String[] cols = this.rengine.parseAndEval("colnames(output)").asStrings();
		for (String c: cols)
			System.out.println(c);
		output.put(Field.MOLID, Arrays.asList(this.rengine.parseAndEval("output[[MSDB.TAG.MOLID]]").asStrings()));
		output.put(Field.MZ, Arrays.asList(this.rengine.parseAndEval("output[[MSDB.TAG.MZ]]").asDoubles()));
		output.put(Field.MZTHEO, Arrays.asList(this.rengine.parseAndEval("output[[MSDB.TAG.MZTHEO]]").asDoubles()));
		output.put(Field.ATTR, Arrays.asList(this.rengine.parseAndEval("output[[MSDB.TAG.ATTR]]").asStrings()));
		output.put(Field.COMP, Arrays.asList(this.rengine.parseAndEval("output[[MSDB.TAG.COMP]]").asStrings()));

		return output;
	}
}
