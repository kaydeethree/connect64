package edu.uwg.jamestwyford.connect64;

/**
 * Storage for a puzzle.
 * 
 * @author jtwyford
 * @version assignment3
 */
public class Puzzle {
	private final int[] positions;
	private final int[] values;

	/**
	 * Constructs a Puzzle using the specified integer arrays.
	 * 
	 * @param newPositions
	 *            the initial positions of the puzzle
	 * @param newValues
	 *            the initial values for the puzzle
	 */
	public Puzzle(final int[] newPositions, final int[] newValues) {
		this.positions = newPositions;
		this.values = newValues;
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
	 * Returns the values array for this Puzzle.
	 * 
	 * @return the initial values
	 */
	public final int[] getValues() {
		return this.values;
	}

}
