package edu.uwg.jamestwyford.connect64;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleCursorAdapter;
import edu.uwg.jamestwyford.connect64.db.ScoresContentProviderDB;
import edu.uwg.jamestwyford.connect64.db.ScoresContract.Scores;

/**
 * Top scores activity.
 * 
 * @author jtwyford
 * @version assignment3
 */
public class TopScores extends ListActivity implements
		LoaderManager.LoaderCallbacks<Cursor> {
	private static final String[] SCORES_PROJECTION = { Scores.ID,
			Scores.PLAYER, Scores.PUZZLE, Scores.COMPLETION_TIME };
	private static final int[] COLUMNS = { R.id.row_player, R.id.row_puzzle,
			R.id.row_time };
	private SimpleCursorAdapter adapter;

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_top_scores);
		setupActionBar();
		setupListAdapter();
	}

	@Override
	public final Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
		return new CursorLoader(TopScores.this,
				ScoresContentProviderDB.CONTENT_URI, SCORES_PROJECTION, null,
				null, Scores.COMPLETION_TIME);
	}

	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.top_scores, menu);
		return true;
	}

	@Override
	public final void onLoaderReset(final Loader<Cursor> loader) {
		this.adapter.swapCursor(null);
	}

	@Override
	public final void onLoadFinished(final Loader<Cursor> loader,
			final Cursor cursor) {
		this.adapter.swapCursor(cursor);
	}

	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		final int id = item.getItemId();
		if (id == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
			return true;
		} else if (id == R.id.clearScores) {
			getContentResolver().delete(ScoresContentProviderDB.CONTENT_URI,
					null, null);
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	private void setupListAdapter() {
		final String[] dataColumns = new String[] { Scores.PLAYER,
				Scores.PUZZLE, Scores.COMPLETION_TIME };
		getLoaderManager().initLoader(0, null, this);
		this.adapter = new SimpleCursorAdapter(this, R.layout.score_row, null,
				dataColumns, COLUMNS, 0);
		setListAdapter(this.adapter);
	}

}
