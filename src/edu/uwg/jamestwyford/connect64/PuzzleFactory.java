package edu.uwg.jamestwyford.connect64;

/**
 * Factory class for puzzles. Call {{@link #getPuzzle(int)} to construct the
 * specified Puzzle.
 * 
 * @author jtwyford
 * @version assignment3
 * 
 */
public final class PuzzleFactory {
	private static int[][] puzzlePos = {
			/* 0 */{ 12, 13, 14, 15, 16, 17, 18, 28, 27, 26, 25, 24, 23, 22,
					21, 31, 32, 33, 34, 35, 36, 37, 38, 48, 47, 46, 45, 44, 43,
					42, 41, 51, 52, 53, 54, 55, 56, 57, 58, 68, 67, 66, 65, 64,
					63, 62, 61, 71, 72, 73, 74, 75, 76, 77, 78, 88, 87, 86, 85,
					84, 83, 82 }, /* 1 */{ 11, 18, 88, 81, 27, 33, 66, 54 } };
	private static int[][] puzzleVals = {
			/* 0 */{ 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17,
					18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32,
					33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47,
					48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62,
					63 }, /* 1 */{ 1, 8, 15, 22, 34, 49, 55, 64 } };

	private PuzzleFactory() {
	}

	/**
	 * Returns a new Puzzle.
	 * 
	 * @param puzzle
	 *            the Puzzle to create
	 * @return the newly-constructed specified Puzzle
	 */
	public static Puzzle getPuzzle(final int puzzle) {
		return new Puzzle(puzzlePos[puzzle], puzzleVals[puzzle]);
	}

	/**
	 * Returns the number of Puzzles stored in the factory.
	 * 
	 * @return the number of Puzzles stored in the factory
	 */
	public static int numPuzzles() {
		return puzzlePos.length - 1;
	}

}
