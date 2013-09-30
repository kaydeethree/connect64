package edu.uwg.jamestwyford.connect64.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import edu.uwg.jamestwyford.connect64.db.ScoresContract.Scores;

/**
 * Helper class to open/create the Scores database.
 * 
 * @author jtwyford
 * @version assignment 3
 */
public class ScoresDBHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "scores.db";
	private static final int DATABASE_VERSION = 1;

	private static final String COMMA_SEP = ", ";
	private static final String TEXT_TYPE = " TEXT";
	private static final String INTEGER_TYPE = " INT";
	private static final String SQL_CREATE_DATABASE = "CREATE TABLE "
			+ Scores.SCORES_TABLE_NAME + " (" + Scores.ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA_SEP + Scores.PLAYER
			+ TEXT_TYPE + COMMA_SEP + Scores.PUZZLE + INTEGER_TYPE + COMMA_SEP
			+ Scores.COMPLETION_TIME + TEXT_TYPE + ");";

	/**
	 * Constructs a helper using the given context.
	 * 
	 * @param context
	 *            the Context in which to open the table.
	 */
	public ScoresDBHelper(final Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public final void onCreate(final SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_DATABASE);
	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
			final int newVersion) {
	}
}
