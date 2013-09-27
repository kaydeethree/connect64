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
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity for the 8x8 game board.
 * 
 * @author jtwyford
 * @version assignment3
 */
public class Connect64 extends Activity {
	private static final String LOG_TAG = "C64";
	// keep negative so isNext() and isPrev() will always return false
	private static final int BAD_VALUE = -1;
	private static final int BOARD_MAX = 64;
	private static final int ROW_SIZE = 8;
	private static final int COL_SIZE = 8;

	private TableLayout gameBoard;
	private TableLayout inputButtons;
	private Spinner rangeSpinner;
	private TextView puzzleLabel;

	private int range;
	private int[] initialPositions;
	private int[] initialValues;
	private SparseIntArray boardState;
	private int input;
	private int puzzle;

	/**
	 * Input handler for the clear button. Resets the game board to the initial
	 * state.
	 * 
	 * @param view
	 *            ignored
	 */
	public final void clearButtonClick(final View view) {
		resetAndInitialize();
	}

	/**
	 * Input handler for the 64 game board Buttons. If <code>input</code> is
	 * valid, sets the clicked Button to that value. Clears the Button
	 * otherwise. Updates the input Buttons to dis/enable them as appropriate.
	 * If the board is full, calls {@link #checkWinCondition()}
	 * 
	 * @param view
	 *            the Button clicked
	 */
	public final void gameButtonClick(final View view) {
		setText((Button) view);
		setupInputButtons();

		if (this.boardState.size() == BOARD_MAX) {
			checkWinCondition();
		}
	}

	/**
	 * Input handler for the 16 input buttons. Sets <code>input</code> to the
	 * value of the Button.
	 * 
	 * @param view
	 *            the Button clicked
	 */
	public final void inputButtonClick(final View view) {
		this.input = getValue((Button) view);
	}

	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.connect64, menu);
		return true;
	}

	@Override
	protected final void onCreate(final Bundle savedState) {
		super.onCreate(savedState);
		Log.d(LOG_TAG, "onCreate()");
		setContentView(R.layout.activity_connect64);

		this.gameBoard = (TableLayout) findViewById(R.id.connect64);
		this.inputButtons = (TableLayout) findViewById(R.id.inputButtons);
		this.puzzleLabel = (TextView) findViewById(R.id.puzzleLabel);
		this.rangeSpinner = (Spinner) findViewById(R.id.rangeSpinner);
		this.boardState = new SparseIntArray(BOARD_MAX);
		setupRangeSpinner();

		if (savedState == null) {
			Log.d(LOG_TAG, "savedState = null. Initializing");
			loadPuzzle(0);
		} else {
			Log.d(LOG_TAG, "savedState != null");
		}
	}

	private void loadPuzzle(int puzzle) {
		resetBoard();
		this.puzzle = puzzle;
		this.puzzleLabel.setText("Puzzle " + this.puzzle);
		Puzzle newPuzzle = PuzzleFactory.getPuzzle(this.puzzle);
		this.initialPositions = newPuzzle.getPositions();
		this.initialValues = newPuzzle.getValues();
		Log.d(LOG_TAG, "Loading puzzle " + puzzle + ". posLength: "
				+ this.initialPositions.length + " valLength: "
				+ this.initialValues.length);
		resetAndInitialize();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedState) {
		super.onRestoreInstanceState(savedState);
		Log.d(LOG_TAG, "onRestoreInstanceState()");

		if (savedState != null) {
			this.initialPositions = savedState.getIntArray("initialPositions");
			this.initialValues = savedState.getIntArray("initialValues");
			this.input = savedState.getInt("input");
			this.range = savedState.getInt("range");
			this.puzzle = savedState.getInt("puzzle");
			this.puzzleLabel.setText("Puzzle " + this.puzzle);
			resetAndInitialize();
			this.rangeSpinner.setSelection(this.range);

			SparseIntArray state = this.boardState;
			int[] positions = savedState.getIntArray("statePositions");
			int[] values = savedState.getIntArray("stateValues");
			for (int i = 0; i < positions.length; i++) {
				state.put(positions[i], values[i]);
				getButton(String.valueOf(positions[i])).setText(
						String.valueOf(values[i]));
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.d(LOG_TAG, "onSaveInstanceState()");

		outState.putIntArray("initialPositions", this.initialPositions);
		outState.putIntArray("initialValues", this.initialValues);
		outState.putInt("input", this.input);
		outState.putInt("range", this.range);
		outState.putInt("puzzle", this.puzzle);

		SparseIntArray state = this.boardState;
		int size = state.size();
		int[] positions = new int[size];
		int[] values = new int[size];
		for (int i = 0; i < size; i++) {
			positions[i] = state.keyAt(i);
			values[i] = state.valueAt(i);
		}
		outState.putIntArray("statePositions", positions);
		outState.putIntArray("stateValues", values);
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
			if (this.puzzle < PuzzleFactory.numPuzzles()) {
				toast = Toast.makeText(this, R.string.youWin,
						Toast.LENGTH_SHORT);
				int nextPuzzle = this.puzzle += 1;
				loadPuzzle(nextPuzzle);
			} else {
				toast = Toast.makeText(this, "All puzzles complete!",
						Toast.LENGTH_SHORT);
			}
		} else if (this.boardState.size() == BOARD_MAX && !boardValid) {
			toast = Toast.makeText(this, R.string.youLose, Toast.LENGTH_SHORT);
		}
		if (toast != null) {
			toast.show();
		}
	}

	private Button getButton(final String tag) {
		Button button = (Button) this.gameBoard.findViewWithTag(tag);
		if (button == null) {
			Log.e(LOG_TAG, "invalid tag passed: " + tag);
		}
		return button;
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

	/**
	 * Win-condition checking at the Button level. Looks for a neighbor with
	 * value+1 and a neighbor with value-1.
	 * 
	 * @param button
	 *            the button to check against
	 * @return true if one neighbor has value+1 AND another neighbor has value-1
	 */
	private boolean hasValidNeighbors(final Button button) {
		final int value = this.getValue(button);
		if (value == BAD_VALUE) {
			return false;
		}
		final int xDelta = 1;
		final int yDelta = 10;
		final int tag = this.getTag(button);

		final int left = this.boardState.get(tag - xDelta, BAD_VALUE);
		final int right = this.boardState.get(tag + xDelta, BAD_VALUE);
		final int up = this.boardState.get(tag - yDelta, BAD_VALUE);
		final int down = this.boardState.get(tag + yDelta, BAD_VALUE);

		final boolean hasNext = (value == BOARD_MAX)
				|| this.isNext(value, left) || this.isNext(value, right)
				|| this.isNext(value, up) || this.isNext(value, down);
		final boolean hasPrev = (value == 1) || this.isPrev(value, left)
				|| this.isPrev(value, right) || this.isPrev(value, up)
				|| this.isPrev(value, down);

		return hasNext && hasPrev;
	}

	/**
	 * Win-condition checking at the board level. Iterates through each Button
	 * to check that it isn't empty and that it has valid neighbors.
	 * 
	 * @return true if no Button is empty or has all invalid neighbors.
	 */
	private boolean isBoardCorrect() {
		for (int i = 1; i <= COL_SIZE; i++) {
			for (int j = 1; j <= ROW_SIZE; j++) {
				final Button button = this.getButton("" + i + j);
				if (!this.hasValidNeighbors(button)) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean isNext(final int thisValue, final int otherValue) {
		return thisValue == otherValue - 1;
	}

	private boolean isPrev(final int thisValue, final int otherValue) {
		return thisValue == otherValue + 1;
	}

	/**
	 * Resets the game board and loads values at <code>initialValues</code> into
	 * the positions at <code>initialPositions</code>.
	 * <p>
	 * <b>NOTE:</b> The 8x8 game board uses positions starting at
	 * <code>11</code> in the top-left corner and ending at <code>88</code> in
	 * the bottom-right. So, the Button in the third row and the fourth column
	 * has position <code>34</code> and has the same String as its tag.
	 */
	private void resetAndInitialize() {
		resetBoard();
		setInitialValues();
		setupInputButtons();
	}

	private void resetBoard() {
		Log.d(LOG_TAG, "resetting board");
		for (int i = 1; i <= COL_SIZE; i++) {
			for (int j = 1; j <= ROW_SIZE; j++) {
				final Button button = this.getButton("" + i + j);
				button.setText("");
				button.setEnabled(true);
			}
		}
		this.boardState.clear();
		this.rangeSpinner.setSelection(0);
		this.input = BAD_VALUE;
	}

	private void setInitialValues() {
		Log.d(LOG_TAG, "loading initial values.");
		int[] pos = this.initialPositions;
		int[] vals = this.initialValues;
		if (pos.length < 1) {
			Log.e(LOG_TAG, "no data in initialPositions");
		}
		for (int i = 0; i < pos.length; i++) {
			final Button button = this.getButton(String.valueOf(pos[i]));
			button.setText(String.valueOf(vals[i]));
			button.setEnabled(false);
			this.boardState.put(pos[i], vals[i]);
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
		final int pos = this.getTag(button);
		final int tempInput = this.getValue(button);

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
		final int numInputButtons = 16;
		for (int i = 1; i <= numInputButtons; i++) {
			final int buttonVal = numInputButtons * this.range + i;
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
