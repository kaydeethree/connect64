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

public class ScoresContentProviderDB extends ContentProvider {
	private static final int ALL_STUDENTS = 1;
	private static final int STUDENT_ID = 2;
	private static final String AUTHORITY = "edu.uwg.jamestwyford.connect64.scoresdbprovider";
	private static final String BASE_PATH = "scores";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + BASE_PATH);
	private static final UriMatcher sURIMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, BASE_PATH, ALL_STUDENTS);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", STUDENT_ID);
	}
	private ScoresDBHelper dbHelper;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = this.dbHelper.getWritableDatabase();
		int rowsDeleted = 0;
		switch (uriType) {
		case ALL_STUDENTS:
			rowsDeleted = sqlDB.delete(
					ScoresContract.Scores.SCORES_TABLE_NAME, selection,
					selectionArgs);
			break;
		case STUDENT_ID:
			String id = uri.getLastPathSegment();
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
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = this.dbHelper.getWritableDatabase();
		long id = 0;
		switch (uriType) {
		case ALL_STUDENTS:
			id = sqlDB.insert(ScoresContract.Scores.SCORES_TABLE_NAME,
					null, values);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		// Notify potential listeners
		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse(BASE_PATH + "/" + id);
	}

	@Override
	public boolean onCreate() {
		this.dbHelper = new ScoresDBHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		checkColumns(projection);
		queryBuilder.setTables(ScoresContract.Scores.SCORES_TABLE_NAME);
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case ALL_STUDENTS:
			break;
		case STUDENT_ID:
			queryBuilder.appendWhere(ScoresContract.Scores.ID + "="
					+ uri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);
		// Notify potential listeners
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = this.dbHelper.getWritableDatabase();
		int rowsUpdated = 0;
		switch (uriType) {
		case ALL_STUDENTS:
			rowsUpdated = sqlDB.update(
					ScoresContract.Scores.SCORES_TABLE_NAME, values,
					selection, selectionArgs);
			break;
		case STUDENT_ID:
			String id = uri.getLastPathSegment();
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

	private void checkColumns(String[] projection) {
		String[] available = { ScoresContract.Scores.ID,
				ScoresContract.Scores.PLAYER,
				ScoresContract.Scores.PUZZLE,
				ScoresContract.Scores.COMPLETION_TIME};
		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(
					Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(
					Arrays.asList(available));
			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException(
						"Unknown columns in projection");
			}
		}
	}
}
