package edu.uwg.jamestwyford.connect64.db;

import android.provider.BaseColumns;

public final class ScoresContract {
	private ScoresContract() {
	}
	
	static class Scores implements BaseColumns {
		public static final String SCORES_TABLE_NAME = "scores";
		public static final String ID = "_id";
		public static final String PLAYER = "player";
		public static final String PUZZLE = "puzzle";
		public static final String COMPLETION_TIME = "completiontime";
	}

}
