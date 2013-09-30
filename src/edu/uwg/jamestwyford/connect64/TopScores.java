package edu.uwg.jamestwyford.connect64;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
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
	ListView listView;
	ScoresDBAdapter dbAdapter;

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.top_scores, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		final int id = item.getItemId();
		if (id == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_top_scores);
		// Show the Up button in the action bar.
		setupActionBar();
		listView = (ListView) findViewById(android.R.id.list);
		dbAdapter = new ScoresDBAdapter(this);
		dbAdapter.open();
		Cursor cursor = dbAdapter.fetchAllScores();
		String[] fields = { Scores.PLAYER, Scores.PUZZLE,
				Scores.COMPLETION_TIME };
		int[] columns = { R.id.row_player, R.id.row_puzzle, R.id.row_time };
		SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(this,
				R.layout.score_row, cursor, fields, columns);
		setListAdapter(cursorAdapter);
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

}
