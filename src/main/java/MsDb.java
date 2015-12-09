package org.openscience.msdb;

import java.util.Map;
import java.util.Collection;
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
	public enum Field { MZ, RT, MOLID, MOLNAMES, MZTHEO, ATTR, COMP, COL, COLRT }

	/**
	 * Get peak M/Z values contained in the database.
	 *
	 * @param mode  The MS mode to consider.
	 * @return      An array of M/Z values.
	 */
	public abstract double[] getMzValues(Mode mode) throws REngineException, REXPMismatchException;

	/**
	 * @param input The input data.
	 */
	public abstract Map<Field, Collection> searchMzRt(Map<Field, Collection> input, Mode mode, double shift, double prec, double rttolx, double rttoly, Collection<String> cols) throws REngineException, REXPMismatchException;
}
