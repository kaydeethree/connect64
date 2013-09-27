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
		enableValidButtons();
		if (checkWinCondition()) {
			Toast toast = Toast.makeText(this, "Winner!", Toast.LENGTH_LONG);
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

	/**
	 * Checks neighbors looking for any with a stored value. Will return true if
	 * a number can be placed in this position.
	 * <p>
	 * For the button at position 34, this method checks the buttons at 24, 44,
	 * 33, and 35.
	 * 
	 * @param x
	 *            the column of the desired button [1-8]
	 * @param y
	 *            the row of the desired button [1-8]
	 * @return true if any of the four neighbors has a stored value
	 * @see hasValidNeighbor(x, y) for win-condition checking
	 */
	private boolean canEnablePosition(final int x, final int y) {
		/*
		 * this is not the most efficient. if we're looping through the board
		 * starting top-left, we only need to check down and right... until I
		 * get around to that, we'll end up checking each button up to 4 times
		 */
		final Button left = getButton("g" + (x - 1) + y);
		final Button right = getButton("g" + (x + 1) + y);
		final Button up = getButton("g" + x + (y - 1));
		final Button down = getButton("g" + x + (y + 1));

		return notEmpty(left) || notEmpty(right) || notEmpty(up)
				|| notEmpty(down);
	}

	private boolean checkWinCondition() {
		for (int i = 1; i <= 8; i++) {
			for (int j = 1; j <= 8; j++) {
				if (!hasCorrectNeighbors(i, j)) {
					return false;
				}
			}
		}
		return true;
	}

	private void enableValidButtons() {
		for (int i = 1; i <= 8; i++) {
			for (int j = 1; j <= 8; j++) {
				final Button button = getButton("g" + i + j);
				button.setEnabled(canEnablePosition(i, j));
			}
		}

		// the for-loop likely enabled our initial buttons, so re-disable them
		storeInitialValues();
	}

	/**
	 * returns the value as an integer of the button or -2 if empty
	 * @param button the button to get the value of
	 * @return the integer value of the button or -2 if empty
	 */
	private int getValue(Button button) {
		String val = button.getText().toString();
		if (!val.equals("")) {
		return Integer.valueOf(val);
		} else {
			return -2;
		}
	}

	private Button getButton(final String tag) {
		return (Button) this.grid.findViewWithTag(tag);
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
		final int value = getValue(getButton("g" + x + y));
		final Button left = getButton("g" + (x - 1) + y);
		final Button right = getButton("g" + (x + 1) + y);
		final Button up = getButton("g" + x + (y - 1));
		final Button down = getButton("g" + x + (y + 1));

		boolean hasNext = value == 64 || hasNext(value, left)
				|| hasNext(value, right) || hasNext(value, up)
				|| hasNext(value, down);
		boolean hasPrev = value == 1 || hasPrev(value, left)
				|| hasPrev(value, right) || hasPrev(value, up)
				|| hasPrev(value, down);

		return hasNext && hasPrev;
	}

	private boolean hasNext(int thisValue, Button otherButton) {
		if (otherButton == null) {
			return false;
		}
		return getValue(otherButton) == thisValue + 1;
	}

	private boolean hasPrev(int thisValue, Button otherButton) {
		if (otherButton == null) {
			return false;
		}
		return getValue(otherButton) == thisValue - 1;
	}

	private boolean notEmpty(final Button button) {
		if (button == null || button.getText().equals("")) {
			return false;
		}
		return true;
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
		enableValidButtons();
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
