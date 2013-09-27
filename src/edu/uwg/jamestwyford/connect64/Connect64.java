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
 * Activity for the 8x8 game board.
 * 
 * @author jtwyford
 * @version assignment3
 */
public class Connect64 extends Activity {
	private static final String LOG_TAG = "C64";
	// keep negative so hasNext() and hasPrev() will always return false
	private static final int BAD_VALUE = -1;

	private TableLayout gameBoard;
	private TableLayout inputButtons;
	private Spinner rangeSpinner;

	private int range;
	private int[] initialPositions;
	private int[] initialValues;
	private SparseIntArray boardState;

	private int input;

	/**
	 * Input handler for the 64 game board buttons. If <code>input</code> is
	 * valid, sets the clicked button to that value. Clears the button
	 * otherwise. If the board is full, calls <code>checkWinCondition()</code>.
	 * 
	 * @param view
	 *            the button clicked
	 */
	public void gameButtonClick(final View view) {
		final Button button = (Button) view;
		Log.d(LOG_TAG, "button: " + button.getTag() + " input: " + this.input);

		setText(button);
		setupInputButtons();

		if (this.boardState.size() == 64) {
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
		this.gameBoard = (TableLayout) findViewById(R.id.connect64);
		this.inputButtons = (TableLayout) findViewById(R.id.inputButtons);
		this.boardState = new SparseIntArray(64);
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
	 * Overall board win condition check. Board must be full and all Buttons
	 * must have a valid higher and lower neighbor to fulfill the win condition.
	 * If the player has won, switch to the next board.
	 */
	private void checkWinCondition() {
		Toast toast = null;
		final boolean boardValid = isBoardCorrect();
		if (boardValid) {
			toast = Toast.makeText(this, R.string.youWin, Toast.LENGTH_LONG);
			// TODO reset board and load next board

		} else if (this.boardState.size() == 64 && !boardValid) {
			toast = Toast.makeText(this, R.string.youLose, Toast.LENGTH_LONG);
		}
		if (toast != null) {
			toast.show();
		}
	}

	private Button getButton(final String tag) {
		return (Button) this.gameBoard.findViewWithTag(tag);
	}

	private int getTag(final Button button) {
		return Integer.valueOf(button.getTag().toString());
	}

	/**
	 * Returns the value of the Button as an integer.
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

	private boolean hasNext(final int thisValue, final Button otherButton) {
		if (otherButton == null) {
			return false;
		}
		return thisValue == getValue(otherButton) - 1;
	}

	private boolean hasPrev(final int thisValue, final Button otherButton) {
		if (otherButton == null) {
			return false;
		}
		return thisValue == getValue(otherButton) + 1;
	}

	/**
	 * Win-condition checking at the Button level. Looks for a neighbor with
	 * value+1 and a neighbor with value-1.
	 * 
	 * @param button
	 *            the button to check against
	 * @return true if one neighbor has value+1 AND another neighbor has value-1
	 */
	private boolean hasValidNeighbors(final Button button) {
		final int tag = getTag(button);
		final int value = getValue(button);

		// XXX hack!
		final int x = tag / 10;
		final int y = tag % 10;

		// TODO WTB more efficient solution
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

	/**
	 * Win-condition checking at the board level. Iterates through each Button
	 * to check that it isn't empty and that it has valid neighbors.
	 * 
	 * @return true if no Button is empty or has all invalid neighbors.
	 */
	private boolean isBoardCorrect() {
		for (int i = 1; i <= 8; i++) {
			for (int j = 1; j <= 8; j++) {
				final Button button = getButton("" + i + j);
				if (isEmpty(button) || !hasValidNeighbors(button)) {
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
	 * Resets the game board and loads values into the specified positions.
	 * <p>
	 * <b>NOTE:</b> The 8x8 game board uses positions starting at
	 * <code>11</code> in the top-left corner and ending at <code>88</code> in
	 * the bottom-right. So, the Button in the third row and the fourth column
	 * has position <code>34</code> and has the same String as its tag.
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

		resetboard();
		setInitialValues();
	}

	private void resetboard() {
		this.rangeSpinner.setSelection(0);

		for (int i = 1; i <= 8; i++) {
			for (int j = 1; j <= 8; j++) {
				final Button button = getButton("" + i + j);
				button.setText("");
				button.setEnabled(true);
			}
		}
		this.boardState.clear();
	}

	private void setInitialValues() {
		for (int i = 0; i < this.initialPositions.length; i++) {
			final Button button = getButton(String
					.valueOf(this.initialPositions[i]));
			button.setText(String.valueOf(this.initialValues[i]));
			button.setEnabled(false);
			this.boardState
					.put(this.initialPositions[i], this.initialValues[i]);
		}
	}

	private void setRange(final int pos) {
		this.range = pos;
	}

	/**
	 * Sets the text of the specified button to the value of <code>input</code>
	 * if valid, clearing the button otherwise. If a value already exists at the
	 * specified button, sets <code>input</code> to that value.
	 * 
	 * @param button
	 *            the button to set the text for.
	 */
	private void setText(final Button button) {
		final int pos = getTag(button);
		final int tempInput = getValue(button);

		if (this.input == BAD_VALUE) {
			button.setText("");
			this.boardState.delete(pos);
		} else {
			button.setText(String.valueOf(this.input));
			this.boardState.put(pos, this.input);
		}
		this.input = tempInput;
	}

	/**
	 * Sets the text on the input buttons based on the current
	 * <code>range</code>. Buttons whose freshly-set text appears on the game
	 * board will be disabled.
	 */
	private void setupInputButtons() {
		for (int i = 1; i <= 16; i++) {
			final int buttonVal = 16 * this.range + i;
			final Button inputButton = (Button) this.inputButtons
					.findViewWithTag("in" + i);
			inputButton.setText(String.valueOf(buttonVal));
			if (this.boardState.indexOfValue(buttonVal) >= 0) {
				inputButton.setEnabled(false);
			} else {
				inputButton.setEnabled(true);
			}
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
						setRange(pos);
						setupInputButtons();
					}

					@Override
					public void onNothingSelected(final AdapterView<?> parent) {
					}
				});
	}
}
