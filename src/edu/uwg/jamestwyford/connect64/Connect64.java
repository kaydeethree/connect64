package edu.uwg.jamestwyford.connect64;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;

/**
 * Activity for the 8x8 game grid.
 * 
 * @author jtwyford
 * @version assignment3
 */
public class Connect64 extends Activity {
	private static final String LOG_TAG = "C64";
	private TableLayout grid;
	private Spinner rangeSpinner;
	private String clickedButton = "";

	/**
	 * Input handler for the 16 input buttons.
	 * 
	 * @param view
	 *            the button clicked
	 */
	public void inputButtonClick(View view) {
		Button button = (Button) view;
		this.clickedButton = button.getText().toString();
	}

	/**
	 * Input handler for the 64 game grid buttons.
	 * 
	 * @param view
	 *            the button clicked
	 */
	public void gameButtonClick(View view) {
		Button button = (Button) view;
		Log.d(LOG_TAG, "" + button.getTag());
		button.setText(this.clickedButton);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.connect64, menu);
		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connect64);
		this.grid = (TableLayout) findViewById(R.id.connect64);

		setupRangeSpinner();

		int[] positions = { 11, 18, 88, 81, 27, 33, 66, 54 };
		int[] values = { 1, 8, 15, 22, 34, 49, 55, 64 };
		initializePuzzle(positions, values);
	}

	private void setupRangeSpinner() {
		this.rangeSpinner = (Spinner) findViewById(R.id.rangeSpinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.ranges, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		this.rangeSpinner.setAdapter(adapter);
		this.rangeSpinner
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int pos, long id) {
						setupInputButtons(pos);
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
					}
				});
	}

	private void setupInputButtons(int range) {
		TableLayout inputGrid = (TableLayout) findViewById(R.id.inputButtons);
		for (int i = 1; i <= 16; i++) {
			((Button) inputGrid.findViewWithTag("in" + i)).setText(""
					+ (16 * range + i));
		}
	}

	/**
	 * Resets the grid and loads values into the specified positions.<br>
	 * <br>
	 * <b>NOTE:</b> The 8x8 game grid uses subscripts starting at 11 in the
	 * top-left corner and ending at 88 in the bottom-right. So, row 3 column 4
	 * is position 34.
	 * 
	 * @param positions
	 *            an integer array with values from 11-88 (but no 9s). Does not
	 *            need to be sorted.
	 * @param values
	 *            an equal-length integer array with values from 1-64.
	 */
	private void initializePuzzle(int[] positions, int[] values) {
		if (positions.length != values.length) {
			throw new IllegalArgumentException(
					"positions.length != values.length");
		}

		resetPuzzle();
		for (int i = 0; i < positions.length; i++) {
			Log.d(LOG_TAG, "setting position: g" + positions[i] + " value: "
					+ values[i]);
			Button button = (Button) this.grid.findViewWithTag("g"
					+ positions[i]);
			button.setText("" + values[i]);
			button.setEnabled(false);
		}
	}

	private void resetPuzzle() {
		this.rangeSpinner.setSelection(0);

		for (int i = 1; i <= 8; i++) {
			for (int j = 1; j <= 8; j++) {
				Log.d(LOG_TAG, "resetting position: g" + i + j);
				Button button = (Button) this.grid.findViewWithTag("g" + i + j);
				button.setText("");
				button.setEnabled(true);
			}
		}
	}
}
