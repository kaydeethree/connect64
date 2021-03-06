package net.kaydeethree.connect64.db;

import android.provider.BaseColumns;

/**
 * Contract of the database.
 * 
 * @author jtwyford
 * @version 1.0
 * 
 */
public final class ScoresContract {
	private ScoresContract() {
	}

	/**
	 * Contract of the Scores table.
	 * 
	 * @author jtwyford
	 * @version 1.0
	 */
	public static class Scores implements BaseColumns {
		/** Scores table name. */
		public static final String SCORES_TABLE_NAME = "scores";
		/** Scores table ID column. */
		public static final String ID = BaseColumns._ID;
		/** Scores table player column. */
		public static final String PLAYER = "player";
		/** Scores table puzzle column. */
		public static final String PUZZLE = "puzzle";
		/** Scores table completion time column. */
		public static final String COMPLETION_TIME = "completiontime";
	}

}
