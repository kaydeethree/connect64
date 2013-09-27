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
import android.widget.Toast;

/**
 * Activity for the 8x8 game grid.
 * 
 * @author jtwyford
 * @version assignment3
 */
public class Connect64 extends Activity {
	private static final String LOG_TAG = "C64";
	private TableLayout grid;
	private TableLayout inputGrid;
	private Spinner rangeSpinner;
	private String clickedButton = "";
	private int[] initialPositions;
	private int[] initialValues;

	/**
	 * Input handler for the 64 game grid buttons.
	 * 
	 * @param view
	 *            the button clicked
	 */
	public void gameButtonClick(final View view) {
		final Button button = (Button) view;
		Log.d(LOG_TAG, "" + button.getTag());
		button.setText(this.clickedButton);

		if (isBoardFull()) {
			final Toast toast;
			if (checkWinCondition()) {
				toast = Toast.makeText(this, "Winner!", Toast.LENGTH_LONG);
			} else {
				toast = Toast.makeText(this, "Loser!", Toast.LENGTH_LONG);
			}
			toast.show();
		}
	}

	/**
	 * Input handler for the 16 input buttons.
	 * 
	 * @param view
	 *            the button clicked
	 */
	public void inputButtonClick(final View view) {
		final Button button = (Button) view;
		this.clickedButton = button.getText().toString();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.connect64, menu);
		return true;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connect64);
		this.grid = (TableLayout) findViewById(R.id.connect64);

		setupRangeSpinner();

		// final int[] testPositions = new int[] { 11, 18, 88, 81, 27, 33, 66,
		// 54 };
		// final int[] testValues = new int[] { 1, 8, 15, 22, 34, 49, 55, 64 };
		final int[] testPositions = new int[] { 12, 13, 14, 15, 16, 17, 18, 28,
				27, 26, 25, 24, 23, 22, 21, 31, 32, 33, 34, 35, 36, 37, 38, 48,
				47, 46, 45, 44, 43, 42, 41, 51, 52, 53, 54, 55, 56, 57, 58, 68,
				67, 66, 65, 64, 63, 62, 61, 71, 72, 73, 74, 75, 76, 77, 78, 88,
				87, 86, 85, 84, 83, 82 };
		final int[] testValues = new int[] { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
				12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27,
				28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43,
				44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59,
				60, 61, 62, 63 };
		resetAndInitializePuzzle(testPositions, testValues);
	}

	private boolean checkWinCondition() {
		for (int i = 1; i <= 8; i++) {
			for (int j = 1; j <= 8; j++) {
				if (isEmpty(i, j) || !hasCorrectNeighbors(i, j)) {
					return false;
				}
			}
		}
		return true;
	}

	private Button getButton(final String tag) {
		return (Button) this.grid.findViewWithTag(tag);
	}

	/**
	 * returns the value as an integer of the button or -2 if empty
	 * 
	 * @param button
	 *            the button to get the value of
	 * @return the integer value of the button or -2 if empty
	 */
	private int getValue(final Button button) {
		final String val = button.getText().toString();
		if (!val.equals("")) {
			return Integer.valueOf(val);
		} else {
			return -2;
		}
	}

	/**
	 * Win-condition checking at the button level. Looks for a neighbor with
	 * value+1 and a neighbor with value-1.
	 * 
	 * @param x
	 *            the column of the desired button [1-8]
	 * @param y
	 *            the row of the desired button [1-8]
	 * @return true if one neighbor has value+1 AND another neighbor has value-1
	 */
	private boolean hasCorrectNeighbors(final int x, final int y) {
		// TODO come up with more efficient solution
		final int value = getValue(getButton("g" + x + y));
		final Button left = getButton("g" + (x - 1) + y);
		final Button right = getButton("g" + (x + 1) + y);
		final Button up = getButton("g" + x + (y - 1));
		final Button down = getButton("g" + x + (y + 1));

		final boolean hasNext = value == 64 || hasNext(value, left)
				|| hasNext(value, right) || hasNext(value, up)
				|| hasNext(value, down);
		final boolean hasPrev = value == 1 || hasPrev(value, left)
				|| hasPrev(value, right) || hasPrev(value, up)
				|| hasPrev(value, down);

		return hasNext && hasPrev;
	}

	private boolean hasNext(final int thisValue, final Button otherButton) {
		if (otherButton == null) {
			return false;
		}
		return getValue(otherButton) == thisValue + 1;
	}

	private boolean hasPrev(final int thisValue, final Button otherButton) {
		if (otherButton == null) {
			return false;
		}
		return getValue(otherButton) == thisValue - 1;
	}

	private boolean isBoardFull() {
		for (int i = 1; i <= 8; i++) {
			for (int j = 1; j <= 8; j++) {
				if (isEmpty(i, j)) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean isEmpty(final int x, final int y) {
		Button button = getButton("g" + x + y);
		if (button == null || button.getText().equals("")) {
			return true;
		}
		return false;
	}

	/**
	 * Resets the grid and loads values into the specified positions.
	 * <p>
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
	private void resetAndInitializePuzzle(final int[] positions,
			final int[] values) {
		if (positions.length != values.length) {
			throw new IllegalArgumentException(
					"positions.length != values.length");
		}
		this.initialPositions = positions;
		this.initialValues = values;

		resetGrid();
		storeInitialValues();
	}

	private void resetGrid() {
		this.rangeSpinner.setSelection(0);

		for (int i = 1; i <= 8; i++) {
			for (int j = 1; j <= 8; j++) {
				Log.d(LOG_TAG, "resetting position: g" + i + j);
				final Button button = getButton("g" + i + j);
				button.setText("");
				button.setEnabled(true);
			}
		}
	}

	private void setupInputButtons(final int range) {
		this.inputGrid = (TableLayout) findViewById(R.id.inputButtons);
		for (int i = 1; i <= 16; i++) {
			((Button) this.inputGrid.findViewWithTag("in" + i)).setText(""
					+ (16 * range + i));
		}
	}

	private void setupRangeSpinner() {
		this.rangeSpinner = (Spinner) findViewById(R.id.rangeSpinner);
		final ArrayAdapter<CharSequence> adapter = ArrayAdapter
				.createFromResource(this, R.array.ranges,
						android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		this.rangeSpinner.setAdapter(adapter);
		this.rangeSpinner
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					@Override
					public void onItemSelected(final AdapterView<?> parent,
							final View view, final int pos, final long id) {
						setupInputButtons(pos);
					}

					@Override
					public void onNothingSelected(final AdapterView<?> parent) {
					}
				});
	}

	private void storeInitialValues() {
		for (int i = 0; i < this.initialPositions.length; i++) {
			final Button button = getButton("g" + this.initialPositions[i]);
			button.setText("" + this.initialValues[i]);
			button.setEnabled(false);
		}
	}
}
