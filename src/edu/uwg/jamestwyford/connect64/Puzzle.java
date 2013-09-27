package edu.uwg.jamestwyford.connect64;

/**
 * Storage for a puzzle.
 * 
 * @author jtwyford
 * @version assignment3
 */
public class Puzzle {
	final private int[] positions;
	final private int[] values;

	/**
	 * Constructs a Puzzle using the specified integer arrays.
	 * 
	 * @param positions
	 *            the initial positions of the puzzle
	 * @param values
	 *            the initial values for the puzzle
	 */
	public Puzzle(final int[] positions, final int[] values) {
		this.positions = positions;
		this.values = values;
	}

	/**
	 * Returns the positions array for this Puzzle.
	 * 
	 * @return the initial positions
	 */
	public final int[] getPositions() {
		return this.positions;
	}

	/**
	 * Returns the values array for this Puzzle
	 * 
	 * @return the initial values
	 */
	public final int[] getValues() {
		return this.values;
	}

}
