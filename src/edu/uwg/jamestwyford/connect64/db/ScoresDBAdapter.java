package edu.uwg.jamestwyford.connect64.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import edu.uwg.jamestwyford.connect64.db.ScoresContract.Scores;

public class ScoresDBAdapter {
	private ScoresDBHelper databaseHelper = null;
	private SQLiteDatabase theDB = null;
	private Context context = null;

	public ScoresDBAdapter(Context context) {
		this.context = context;
	}

	public ScoresDBAdapter open() throws SQLException {
		this.databaseHelper = new ScoresDBHelper(this.context);
		this.theDB = this.databaseHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		if (this.databaseHelper != null) {
			this.databaseHelper.close();
		}
		if (this.theDB != null) {
			this.theDB.close();
		}
	}

	public long insertScore(String player, int puzzle, long time) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(Scores.PLAYER, player);
		initialValues.put(Scores.PUZZLE, puzzle);
		initialValues.put(Scores.COMPLETION_TIME, time);
		return theDB.insert(Scores.SCORES_TABLE_NAME, null, initialValues);
	}

	public boolean deleteScore(long id) {
		String[] ids = { "" + id };
		int retVal = theDB.delete(Scores.SCORES_TABLE_NAME, Scores.ID + " = ?",
				ids);
		return (retVal > 0);
	}

	public boolean deleteAllScores() {
		int retVal = theDB.delete(Scores.SCORES_TABLE_NAME, "1", null);
		return (retVal > 0);
	}

	public Cursor fetchAllScores() {
		String[] columns = new String[] { Scores.ID, Scores.PLAYER,
				Scores.PUZZLE, Scores.COMPLETION_TIME };
		String order = Scores.COMPLETION_TIME;
		return this.theDB.query(Scores.SCORES_TABLE_NAME, columns, null, null,
				null, null, order);
	}

}
