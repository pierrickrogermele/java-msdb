package org.openscience.msdb;

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
		this.pfdb = this.rengine.parseAndEval("MsPeakForestDb$new(url = \"" + url + "\", useragent = \"" + useragent + "\")");
	}
}
