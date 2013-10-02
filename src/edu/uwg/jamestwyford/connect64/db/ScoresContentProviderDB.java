package edu.uwg.jamestwyford.connect64.db;

import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Content provider for the Scores DB.
 * 
 * @author jtwyfor1
 * @version assignment3
 */
public class ScoresContentProviderDB extends ContentProvider {
	private static final int ALL_SCORES = 1;
	private static final int SCORE_ID = 2;
	private static final String AUTHORITY = "edu.uwg.jamestwyford.connect64.scoresdbprovider";
	private static final String BASE_PATH = "scores";
	/** content URI used to access this provider. */
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + BASE_PATH);
	private static final UriMatcher S_URI_MATCHER = new UriMatcher(
			UriMatcher.NO_MATCH);
	static {
		S_URI_MATCHER.addURI(AUTHORITY, BASE_PATH, ALL_SCORES);
		S_URI_MATCHER.addURI(AUTHORITY, BASE_PATH + "/#", SCORE_ID);
	}
	private ScoresDBHelper dbHelper;

	private void checkColumns(final String[] projection) {
		final String[] available = { ScoresContract.Scores.ID,
				ScoresContract.Scores.PLAYER, ScoresContract.Scores.PUZZLE,
				ScoresContract.Scores.COMPLETION_TIME };
		if (projection != null) {
			final HashSet<String> requestedColumns = new HashSet<String>(
					Arrays.asList(projection));
			final HashSet<String> availableColumns = new HashSet<String>(
					Arrays.asList(available));
			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException(
						"Unknown columns in projection");
			}
		}
	}

	@Override
	public final int delete(final Uri uri, final String selection,
			final String[] selectionArgs) {
		final int uriType = S_URI_MATCHER.match(uri);
		final SQLiteDatabase sqlDB = this.dbHelper.getWritableDatabase();
		int rowsDeleted = 0;
		switch (uriType) {
		case ALL_SCORES:
			rowsDeleted = sqlDB.delete(ScoresContract.Scores.SCORES_TABLE_NAME,
					selection, selectionArgs);
			break;
		case SCORE_ID:
			final String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = sqlDB.delete(
						ScoresContract.Scores.SCORES_TABLE_NAME,
						ScoresContract.Scores.ID + "=" + id, null);
			} else {
				rowsDeleted = sqlDB.delete(
						ScoresContract.Scores.SCORES_TABLE_NAME,
						ScoresContract.Scores.ID + "=" + id + " and "
								+ selection, selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		// Notify potential listeners
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}

	@Override
	public final String getType(final Uri uri) {
		return null;
	}

	@Override
	public final Uri insert(final Uri uri, final ContentValues values) {
		final int uriType = S_URI_MATCHER.match(uri);
		final SQLiteDatabase sqlDB = this.dbHelper.getWritableDatabase();
		long id = 0;
		switch (uriType) {
		case ALL_SCORES:
			id = sqlDB.insert(ScoresContract.Scores.SCORES_TABLE_NAME, null,
					values);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		// Notify potential listeners
		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse(BASE_PATH + "/" + id);
	}

	@Override
	public final boolean onCreate() {
		this.dbHelper = new ScoresDBHelper(getContext());
		return true;
	}

	@Override
	public final Cursor query(final Uri uri, final String[] projection,
			final String selection, final String[] selectionArgs,
			final String sortOrder) {
		final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		this.checkColumns(projection);
		queryBuilder.setTables(ScoresContract.Scores.SCORES_TABLE_NAME);
		final int uriType = S_URI_MATCHER.match(uri);
		switch (uriType) {
		case ALL_SCORES:
			break;
		case SCORE_ID:
			queryBuilder.appendWhere(ScoresContract.Scores.ID + "="
					+ uri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		final SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		final Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);
		// Notify potential listeners
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public final int update(final Uri uri, final ContentValues values,
			final String selection, final String[] selectionArgs) {
		final int uriType = S_URI_MATCHER.match(uri);
		final SQLiteDatabase sqlDB = this.dbHelper.getWritableDatabase();
		int rowsUpdated = 0;
		switch (uriType) {
		case ALL_SCORES:
			rowsUpdated = sqlDB.update(ScoresContract.Scores.SCORES_TABLE_NAME,
					values, selection, selectionArgs);
			break;
		case SCORE_ID:
			final String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = sqlDB.update(
						ScoresContract.Scores.SCORES_TABLE_NAME, values,
						ScoresContract.Scores.ID + "=" + id, null);
			} else {
				rowsUpdated = sqlDB.update(
						ScoresContract.Scores.SCORES_TABLE_NAME, values,
						ScoresContract.Scores.ID + "=" + id + " and "
								+ selection, selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		// Notify potential listeners
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}
}
