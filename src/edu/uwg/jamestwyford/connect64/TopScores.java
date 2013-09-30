package edu.uwg.jamestwyford.connect64;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleCursorAdapter;
import edu.uwg.jamestwyford.connect64.db.ScoresContract.Scores;
import edu.uwg.jamestwyford.connect64.db.ScoresDBAdapter;

/**
 * Top scores activity.
 * 
 * @author jtwyford
 * @version assignment3
 */
public class TopScores extends ListActivity {
	private static final String[] FIELDS = { Scores.PLAYER, Scores.PUZZLE,
			Scores.COMPLETION_TIME };
	private static final int[] COLUMNS = { R.id.row_player, R.id.row_puzzle, R.id.row_time };
	private ScoresDBAdapter dbAdapter;
	private SimpleCursorAdapter cursorAdapter;

	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.top_scores, menu);
		return true;
	}

	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		final int id = item.getItemId();
		if (id == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
			return true;
		} else if (id == R.id.clearScores) {
			this.dbAdapter.deleteAllScores();
			fillListView();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_top_scores);
		setupActionBar();
		this.dbAdapter = new ScoresDBAdapter(this);
		this.dbAdapter.open();
		fillListView();
	}
	
	@SuppressWarnings("deprecation")
	private void fillListView() {
		Cursor cursor = this.dbAdapter.fetchAllScores();
		this.cursorAdapter = new SimpleCursorAdapter(this,
				R.layout.score_row, cursor, FIELDS, COLUMNS);
		setListAdapter(this.cursorAdapter);
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

}
