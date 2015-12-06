package org.openscience.msdb;

import java.util.Map;
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
		this.rengine.parseAndEval("source('/Users/pierrick/dev/lcmsmatching/r-msdb/MsDbOutputDataFrameStream.R', chdir = TRUE)");
		this.rengine.parseAndEval("db <- MsPeakForestDb$new(url = \"" + url + "\", useragent = \"" + useragent + "\")");
	}

	//////////////////
	// SEARCH MZ RT //
	//////////////////

	public Map<Field, REXP> searchMzRt(Map<Field, REXP> input, Mode mode, double shift, double prec) throws REngineException, REXPMismatchException {

		// Check that MZ is present
		if ( ! input.containsKey(Field.MZ))
			throw new IllegalArgumentException("Input map must contain MZ values.");

		// Create input stream
		this.rengine.assign("mz", input.get(Field.MZ));
		this.rengine.parseAndEval("input.stream <- msdb.make.input.df(mz)");
		this.rengine.parseAndEval("db$setInputStream(input.stream)");

		// Create output stream
		this.rengine.parseAndEval("output.stream <- MsDbOutputDataFrameStream$new()");
		this.rengine.parseAndEval("db$addOutputStreams(output.stream)");

		// Set MS mode value
		this.rengine.parseAndEval("mode <- " + (mode == Mode.POSITIVE ? "MSDB.TAG.POS" : "MSDB.TAG.NEG"));

		// Call search method
		this.rengine.parseAndEval("db$searchForMzRtList(mode = mode)");

		// Get output
		this.rengine.parseAndEval("output <- output.stream$getDataFrame()");

		return null;
	}
}
