package edu.uwg.jamestwyford.connect64;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;
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
	// keep negative so hasNext() and hasPrev() will always return false
	private static final int BAD_VALUE = -1;

	private TableLayout grid;
	private TableLayout inputGrid;
	private Spinner rangeSpinner;

	private int range;
	private int[] initialPositions;
	private int[] initialValues;
	private SparseIntArray gridState;

	private int input;

	/**
	 * Input handler for the 64 game grid buttons. If <code>input</code> is
	 * valid, sets the clicked button to that value. Clears the button
	 * otherwise. If the grid is full, calls <code>checkWinCondition()</code>.
	 * 
	 * @param view
	 *            the button clicked
	 */
	public void gameButtonClick(final View view) {
		final Button button = (Button) view;
		Log.d(LOG_TAG, "" + button.getTag() + " input: " + this.input);

		if (this.input != BAD_VALUE) {
			setText(button);
		} else {
			clearText(button);
		}
		configureInputButtons();

		if (this.gridState.size() == 64) {
			checkWinCondition();
		}
	}

	/**
	 * Input handler for the 16 input buttons. Sets <code>input</code> to the
	 * value of the button.
	 * 
	 * @param view
	 *            the button clicked
	 */
	public void inputButtonClick(final View view) {
		this.input = getValue((Button) view);
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
		this.gridState = new SparseIntArray(64);
		this.input = BAD_VALUE;

		setupRangeSpinner();

		// final int[] testPos = new int[] { 11, 18, 88, 81, 27, 33, 66, 54 };
		// final int[] testVals = new int[] { 1, 8, 15, 22, 34, 49, 55, 64 };
		final int[] testPos = new int[] { 12, 13, 14, 15, 16, 17, 18, 28, 27,
				26, 25, 24, 23, 22, 21, 31, 32, 33, 34, 35, 36, 37, 38, 48, 47,
				46, 45, 44, 43, 42, 41, 51, 52, 53, 54, 55, 56, 57, 58, 68, 67,
				66, 65, 64, 63, 62, 61, 71, 72, 73, 74, 75, 76, 77, 78, 88, 87,
				86, 85, 84, 83, 82 };
		final int[] testVals = new int[] { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
				13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
				29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44,
				45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60,
				61, 62, 63 };
		resetAndInitialize(testPos, testVals);
	}

	/**
	 * Overall board win condition check. Board must be full and all 64 spots
	 * must have a valid higher and lower neighor to fulfill the win condition.
	 * If the player has won, switch to the next board.
	 */
	private void checkWinCondition() {
		Toast toast = null;
		boolean boardValid = isBoardCorrect();
		if (boardValid) {
			toast = Toast.makeText(this, R.string.youWin, Toast.LENGTH_LONG);
			// TODO reset grid and load next board

		} else if (this.gridState.size() == 64 && !boardValid) {
			toast = Toast.makeText(this, R.string.youLose, Toast.LENGTH_LONG);
		}
		if (toast != null) {
			toast.show();
		}
	}

	/**
	 * Clears the text of the specified button. Sets <code>input</code> to the
	 * removed value so it may be quickly set elsewhere.
	 * 
	 * @param button
	 *            the button to clear text from
	 */
	private void clearText(final Button button) {
		final int pos = getTag(button);
		this.gridState.delete(pos);
		this.input = getValue(button);
		button.setText("");
	}

	/**
	 * Sets the text on the input buttons based on the current
	 * <code>range</code>. Buttons whose freshly-set text appears on the game
	 * grid will be disabled.
	 */
	private void configureInputButtons() {
		this.inputGrid = (TableLayout) findViewById(R.id.inputButtons);
		int buttonVal;
		Button inputButton;
		for (int i = 1; i <= 16; i++) {
			buttonVal = 16 * this.range + i;
			inputButton = (Button) this.inputGrid.findViewWithTag("in" + i);
			inputButton.setText("" + buttonVal);
			if (this.gridState.indexOfValue(buttonVal) >= 0) {
				inputButton.setEnabled(false);
			} else {
				inputButton.setEnabled(true);
			}
		}
	}

	private Button getButton(final String tag) {
		return (Button) this.grid.findViewWithTag(tag);
	}

	private Integer getTag(final Button button) {
		return Integer.valueOf(button.getTag().toString());
	}

	/**
	 * Returns the value as an integer of the button or <code>BAD_VALUE</code>
	 * if empty/null
	 * 
	 * @param button
	 *            the button to get the value of
	 * @return the integer value of the button or <code>BAD_VALUE</code> if
	 *         empty/null
	 */
	private int getValue(final Button button) {
		try {
			return Integer.valueOf(button.getText().toString());
		} catch (final NumberFormatException ex) {
			return BAD_VALUE;
		}
	}

	/**
	 * Win-condition checking at the button level. Looks for a neighbor with
	 * value+1 and a neighbor with value-1.
	 * 
	 * @param button
	 *            the button to check against
	 * @return true if one neighbor has value+1 AND another neighbor has value-1
	 */
	private boolean hasCorrectNeighbors(final Button button) {
		final int tag = getTag(button);
		final int value = getValue(button);

		// XXX hack!
		final int x = tag / 10;
		final int y = tag % 10;

		// TODO come up with more efficient solution
		final Button left = getButton("" + (x - 1) + y);
		final Button right = getButton("" + (x + 1) + y);
		final Button up = getButton("" + x + (y - 1));
		final Button down = getButton("" + x + (y + 1));

		final boolean hasNext = (value == 64) || hasNext(value, left)
				|| hasNext(value, right) || hasNext(value, up)
				|| hasNext(value, down);
		final boolean hasPrev = (value == 1) || hasPrev(value, left)
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

	/**
	 * Win-condition checking at the board level. Iterates through all the
	 * buttons to make sure each one isn't empty and that it has valid
	 * neighbors.
	 * 
	 * @return true if no button is empty or has all invalid neighbors.
	 */
	private boolean isBoardCorrect() {
		for (int i = 1; i <= 8; i++) {
			for (int j = 1; j <= 8; j++) {
				final Button button = getButton("" + i + j);
				if (this.isEmpty(button) || !this.hasCorrectNeighbors(button)) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean isEmpty(final Button button) {
		if (getValue(button) == BAD_VALUE) {
			return true;
		}
		return false;
	}

	/**
	 * Resets the game grid and loads values into the specified positions.
	 * <p>
	 * <b>NOTE:</b> The 8x8 game grid uses subscripts starting at
	 * <code>11</code> in the top-left corner and ending at <code>88</code> in
	 * the bottom-right. So, the button in the third row and the fourth column
	 * has position <code>34</code> and has the same string as its tag.
	 * 
	 * @param positions
	 *            an integer array with values from 11-88 (but no 0s or 9s).
	 *            Does not need to be sorted.
	 * @param values
	 *            an equal-length integer array with values from 1-64.
	 */
	private void resetAndInitialize(final int[] positions, final int[] values) {
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
				final Button button = getButton("" + i + j);
				button.setText("");
				button.setEnabled(true);
			}
		}
		this.gridState.clear();
	}

	private void setRange(final int pos) {
		this.range = pos;
	}

	/**
	 * Sets the text of the specified button to the value of <code>input</code>.
	 * Analogously to <code>clearText()</code>, if a value already exists at the
	 * specified button, sets <code>input</code> to that value.
	 * 
	 * @param button
	 *            the button to set the text for.
	 */
	private void setText(final Button button) {
		if (this.input == BAD_VALUE) {
			return;
		}
		final int pos = getTag(button);
		int tempInput = getValue(button);
		button.setText("" + this.input);
		this.gridState.put(pos, this.input);
		this.input = tempInput;
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
						setRange(pos);
						configureInputButtons();
					}

					@Override
					public void onNothingSelected(final AdapterView<?> parent) {
					}
				});
	}

	private void storeInitialValues() {
		for (int i = 0; i < this.initialPositions.length; i++) {
			final Button button = getButton("" + this.initialPositions[i]);
			button.setText("" + this.initialValues[i]);
			button.setEnabled(false);
			this.gridState.put(this.initialPositions[i], this.initialValues[i]);
		}
	}
}
