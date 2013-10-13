package net.kaydeethree.connect64;

/**
 * Factory class for puzzles. Call {@link #getPuzzlePositions(int)} and
 * {@link #getPuzzleValues(int)} to retrieve the specified data for the puzzle.
 * 
 * @author jtwyford
 * @version 1.0
 * 
 */
public final class PuzzleFactory {
	private static final int[][] PUZZLE_POSITIONS = {
			// 0
			{ 12, 13, 14, 15, 16, 17, 18, 28, 27, 26, 25, 24, 23, 22, 21, 31,
					32, 33, 34, 35, 36, 37, 38, 48, 47, 46, 45, 44, 43, 42, 41,
					51, 52, 53, 54, 55, 56, 57, 58, 68, 67, 66, 65, 64, 63, 62,
					61, 71, 72, 73, 74, 75, 76, 77, 78, 88, 87, 86, 85, 84, 83,
					82 },
			// 1
			{ 11, 18, 88, 81, 27, 33, 66, 54 },
			// 2
			{ 14, 15, 38, 48, 87, 86, 61, 51 },
			// 3
			{ 13, 14, 51, 61, 44, 54, 47, 57 },
			// 4
			{ 25, 35, 42, 52, 47, 57, 86, 87 },
			// 5
			{ 15, 25, 33, 34, 55, 65, 71, 72 },
			// 6
			{ 34, 35, 41, 42, 67, 68, 73, 74 },
			// 7
			{ 36, 37, 52, 62, 85, 86 },
			// 8
			{ 33, 43, 56, 57, 75, 76 },
			// 9
			{ 34, 35, 41, 42, 64, 65 },
			// 10
			{ 33, 34, 57, 67, 73, 74 },
			// 11
			{ 33, 42, 43, 65, 74, 75 },
			// 12
			{ 15, 16, 52, 53, 56, 57 },
			// 13
			{ 34, 35, 41, 51, 66, 67 },
			// 14
			{ 36, 37, 51, 52 },
			// 15
			{ 41, 51, 56, 57 },
			// 16
			{ 42, 52, 46, 56 },
			// 17
			{ 24, 25, 74, 75 },
			// 18
			{ 14, 24, 75, 85 },
			// 19
			{ 33, 43, 67, 77 },
			// 20
			{ 51, 52, 57, 58 } };
	private static final int[][] PUZZLE_VALUES = {
			// 0
			{ 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
					20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34,
					35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49,
					50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63 },
			// 1
			{ 1, 8, 15, 22, 34, 49, 55, 64 },
			// 2
			{ 6, 61, 56, 43, 38, 29, 20, 13 },
			// 3
			{ 58, 47, 64, 11, 52, 5, 39, 26 },
			// 4
			{ 52, 35, 43, 4, 56, 31, 15, 26 },
			// 5
			{ 41, 62, 31, 60, 55, 8, 23, 12 },
			// 6
			{ 4, 43, 26, 9, 48, 59, 19, 52 },
			// 7
			{ 17, 52, 7, 28, 23, 60 },
			// 8
			{ 38, 7, 33, 60, 14, 25 },
			// 9
			{ 29, 4, 43, 36, 54, 9 },
			// 10
			{ 7, 52, 43, 32, 19, 28 },
			// 11
			{ 31, 21, 4, 62, 10, 57 },
			// 12
			{ 59, 16, 50, 5, 10, 25 },
			// 13
			{ 63, 48, 57, 14, 8, 33 },
			// 14
			{ 7, 54, 24, 37 },
			// 15
			{ 19, 2, 61, 46 },
			// 16
			{ 32, 19, 54, 7 },
			// 17
			{ 10, 37, 23, 54 },
			// 18
			{ 56, 29, 19, 12 },
			// 19
			{ 9, 24, 42, 63 },
			// 20
			{ 25, 46, 37, 12 } };

	private PuzzleFactory() {
	}

	/**
	 * Returns the initial positions of the specified puzzle.
	 * 
	 * @param puzzle
	 *            the puzzle to get the initial positions of
	 * @return the positions of the specified puzzle
	 */
	public static int[] getPuzzlePositions(final int puzzle) {
		return PUZZLE_POSITIONS[puzzle];
	}

	/**
	 * Returns the initial values of the specified puzzle.
	 * 
	 * @param puzzle
	 *            the puzzle to get the values of
	 * @return the positions of the specified puzzle
	 */
	public static int[] getPuzzleValues(final int puzzle) {
		return PUZZLE_VALUES[puzzle];
	}

	/**
	 * Returns the number of Puzzles stored in the factory.
	 * 
	 * @return the number of Puzzles stored in the factory
	 */
	public static int numPuzzles() {
		return PUZZLE_POSITIONS.length - 1;
	}

}
