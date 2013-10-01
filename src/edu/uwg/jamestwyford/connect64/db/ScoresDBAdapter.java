package edu.uwg.jamestwyford.connect64.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import edu.uwg.jamestwyford.connect64.db.ScoresContract.Scores;

/**
 * Database adapter for the Scores database/table.
 * 
 * @author jtwyford
 * @version assignment3
 * 
 */
public class ScoresDBAdapter {
	private ScoresDBHelper databaseHelper = null;
	private SQLiteDatabase theDB = null;
	private Context context = null;

	/**
	 * Constructs the adapter using the given Activity Context.
	 * 
	 * @param newContext
	 *            the Context of the Activity.
	 */
	public ScoresDBAdapter(final Context newContext) {
		this.context = newContext;
	}

	/**
	 * Opens the table for write access.
	 * 
	 * @return this instance with the table open for write access.
	 */
	public final ScoresDBAdapter open() {
		this.databaseHelper = new ScoresDBHelper(this.context);
		this.theDB = this.databaseHelper.getWritableDatabase();
		return this;
	}

	/**
	 * Closes the table if necessary.
	 */
	public final void close() {
		if (this.databaseHelper != null) {
			this.databaseHelper.close();
		}
		if (this.theDB != null) {
			this.theDB.close();
		}
	}

	/**
	 * Inserts a score with the specified values into the table.
	 * 
	 * @param player
	 *            the player who completed the puzzle
	 * @param puzzle
	 *            the puzzle completed by the player
	 * @param time
	 *            the time taken to complete the puzzle
	 * @return the ID of the row just entered or -1
	 */
	public final long insertScore(final String player, final int puzzle,
			final String time) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(Scores.PLAYER, player);
		initialValues.put(Scores.PUZZLE, puzzle);
		initialValues.put(Scores.COMPLETION_TIME, time);
		return this.theDB.insert(Scores.SCORES_TABLE_NAME, null, initialValues);
	}

	/**
	 * Given a score id, deletes the score with that id.
	 * 
	 * @param id
	 *            the score to delete
	 * @return true if the row was deleted
	 */
	public final boolean deleteScore(final long id) {
		String[] ids = { "" + id };
		int retVal = this.theDB.delete(Scores.SCORES_TABLE_NAME, Scores.ID
				+ " = ?", ids);
		return (retVal > 0);
	}

	/**
	 * Clear the table.
	 * 
	 * @return true if the table was cleared
	 */
	public final boolean deleteAllScores() {
		int retVal = this.theDB.delete(Scores.SCORES_TABLE_NAME, "1", null);
		return (retVal > 0);
	}

	/**
	 * Gets a Cursor over the whole table.
	 * 
	 * @return a Cursor over the whole table
	 */
	public final Cursor fetchAllScores() {
		String[] columns = new String[] { Scores.ID, Scores.PLAYER,
				Scores.PUZZLE, Scores.COMPLETION_TIME };
		String order = Scores.COMPLETION_TIME;
		return this.theDB.query(Scores.SCORES_TABLE_NAME, columns, null, null,
				null, null, order);
	}

}
