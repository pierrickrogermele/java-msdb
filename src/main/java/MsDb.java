package org.openscience.msdb;

import java.util.Map;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.REXPMismatchException;

/**
 * An abstract class for modeling a Mass Spectra database.
 *
 * @author Pierrick Roger
 */
public abstract class MsDb {

	////////////////
	// ENUM TYPES //
	////////////////
	
	// MS Mode
	public enum Mode { POSITIVE, NEGATIVE }

	// Data fields
	public enum Field { MZ, RT }

	/**
	 * Default constructor.
	 */
	public MsDb() {
	}

	/**
	 * @param input The input data.
	 */
	public abstract Map<Field, REXP> searchMzRt(Map<Field, REXP> input, Mode mode, double shift, double prec) throws REngineException, REXPMismatchException;
}
