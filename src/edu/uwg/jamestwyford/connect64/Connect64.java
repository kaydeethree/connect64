package edu.uwg.jamestwyford.connect64;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Game logic for the 8x8 board.
 * 
 * @author jtwyford
 * @version assignment3
 */
public class Connect64 extends Activity {
	private static final int BAD_VALUE = -1;
	private static final int BOARD_MAX = 64;
	private static final String LOG_TAG = "C64";
	private static final int COL_SIZE = 8;
	private static final int ROW_SIZE = 8;

	private static final String STATE_VALUES = "stateValues";
	private static final String STATE_POSITIONS = "statePositions";
	private static final String PUZZLE_VALUE = "puzzle";
	private static final String RANGE_VALUE = "range";
	private static final String INPUT_VALUE = "input";
	private static final String INITIAL_VALUES = "initialValues";
	private static final String INITIAL_POSITIONS = "initialPositions";

	private TableLayout gameBoard;
	private TableLayout inputButtons;
	private ImageButton pauseResume;
	private TextView puzzleLabel;
	private Spinner rangeSpinner;
	private Chronometer timer;

	private int range;
	private int[] initialPositions;
	private int[] initialValues;
	private SparseIntArray boardState;
	private int input;
	private int puzzle;
	private boolean timerRunning;
	private long elapsedTime;

	/**
	 * Input handler for the clear button. Resets the game board to the initial
	 * state.
	 * 
	 * @param view
	 *            ignored
	 */
	public final void clearButtonClick(final View view) {
		this.elapsedTime = 0;
		resumeTimer();
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

	/**
	 * Input handler for the pause/resume button.
	 * 
	 * @param view
	 *            ignored
	 */
	public final void pauseResumeClick(final View view) {
		if (this.timerRunning) {
			pauseTimer();
		} else {
			resumeTimer();
		}
	}

	@Override
	protected final void onCreate(final Bundle savedState) {
		super.onCreate(savedState);
		setContentView(R.layout.activity_connect64);

		this.gameBoard = (TableLayout) findViewById(R.id.connect64);
		this.inputButtons = (TableLayout) findViewById(R.id.inputButtons);
		this.puzzleLabel = (TextView) findViewById(R.id.puzzleLabel);
		this.rangeSpinner = (Spinner) findViewById(R.id.rangeSpinner);
		this.pauseResume = (ImageButton) findViewById(R.id.pauseResumeButton);
		this.timer = (Chronometer) findViewById(R.id.chronometer);
		this.boardState = new SparseIntArray(BOARD_MAX);
		setupRangeSpinner();

		if (savedState == null) {
			loadPuzzle(0);
		}
	}

	@Override
	protected final void onRestoreInstanceState(final Bundle savedState) {
		super.onRestoreInstanceState(savedState);

		if (savedState != null) {
			this.initialPositions = savedState.getIntArray(INITIAL_POSITIONS);
			this.initialValues = savedState.getIntArray(INITIAL_VALUES);
			this.input = savedState.getInt(INPUT_VALUE);
			this.range = savedState.getInt(RANGE_VALUE);
			this.puzzle = savedState.getInt(PUZZLE_VALUE);
			this.puzzleLabel.setText("Puzzle " + this.puzzle);
			this.elapsedTime = savedState.getLong("elapsedTime");
			this.timerRunning = savedState.getBoolean("timerRunning");
			if (this.timerRunning) {
				resumeTimer();
			} else {
				final long elapsed = this.elapsedTime / 1000;
				this.timer.setText(String.format("%02d:%02d", elapsed / 60,
						elapsed % 60));
			}
			resetAndInitialize();
			this.rangeSpinner.setSelection(this.range);

			final SparseIntArray state = this.boardState;
			final int[] positions = savedState.getIntArray(STATE_POSITIONS);
			final int[] values = savedState.getIntArray(STATE_VALUES);
			for (int i = 0; i < positions.length; i++) {
				state.put(positions[i], values[i]);
				getButton(String.valueOf(positions[i])).setText(
						String.valueOf(values[i]));
			}
		}
	}

	@Override
	protected final void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putIntArray(INITIAL_POSITIONS, this.initialPositions);
		outState.putIntArray(INITIAL_VALUES, this.initialValues);
		outState.putInt(INPUT_VALUE, this.input);
		outState.putInt(RANGE_VALUE, this.range);
		outState.putInt(PUZZLE_VALUE, this.puzzle);
		outState.putBoolean("timerRunning", this.timerRunning);

		if (this.timerRunning) {
			pauseTimer();
		}
		outState.putLong("elapsedTime", this.elapsedTime);

		final SparseIntArray state = this.boardState;
		final int size = state.size();
		final int[] positions = new int[size];
		final int[] values = new int[size];
		for (int i = 0; i < size; i++) {
			positions[i] = state.keyAt(i);
			values[i] = state.valueAt(i);
		}
		outState.putIntArray(STATE_POSITIONS, positions);
		outState.putIntArray(STATE_VALUES, values);
	}

	/**
	 * Overall board win condition check. Board must be full and all Buttons
	 * must have a valid higher and lower neighbor to fulfill the win condition.
	 * If the player has won, switch to the next Puzzle if one exists.
	 */
	private void checkWinCondition() {
		Toast toast = null;
		final boolean boardValid = isBoardCorrect();
		final boolean onLastPuzzle = isOnLastPuzzle();

		if (boardValid && onLastPuzzle) {
			toast = Toast.makeText(this, "All puzzles complete!",
					Toast.LENGTH_SHORT);
			this.timer.stop();
		} else if (boardValid && !onLastPuzzle) {
			toast = Toast.makeText(this, R.string.youWin, Toast.LENGTH_SHORT);
			final int nextPuzzle = this.puzzle + 1;
			loadPuzzle(nextPuzzle);
		} else if (!boardValid && onLastPuzzle) {
			toast = Toast.makeText(this, R.string.youLose, Toast.LENGTH_SHORT);
		}
		if (toast != null) {
			toast.show();
		}
	}

	private Button getButton(final String tag) {
		final Button button = (Button) this.gameBoard.findViewWithTag(tag);
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
		final int value = getValue(button);
		if (value == BAD_VALUE) {
			return false;
		}
		final int xDelta = 1;
		final int yDelta = 10;
		final int tag = getTag(button);

		final int left = this.boardState.get(tag - xDelta, BAD_VALUE);
		final int right = this.boardState.get(tag + xDelta, BAD_VALUE);
		final int up = this.boardState.get(tag - yDelta, BAD_VALUE);
		final int down = this.boardState.get(tag + yDelta, BAD_VALUE);

		final boolean hasNext = (value == BOARD_MAX) || isNext(value, left)
				|| isNext(value, right) || isNext(value, up)
				|| isNext(value, down);
		final boolean hasPrev = (value == 1) || isPrev(value, left)
				|| isPrev(value, right) || isPrev(value, up)
				|| isPrev(value, down);

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
				final Button button = getButton("" + i + j);
				if (!hasValidNeighbors(button)) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean isNext(final int thisValue, final int otherValue) {
		return thisValue == otherValue - 1;
	}

	private boolean isOnBoard(final int value) {
		return this.boardState.indexOfValue(value) >= 0;
	}

	private boolean isOnLastPuzzle() {
		return this.puzzle == PuzzleFactory.numPuzzles();
	}

	private boolean isPrev(final int thisValue, final int otherValue) {
		return thisValue == otherValue + 1;
	}

	private void loadPuzzle(final int newPuzzle) {
		resetBoard();
		this.puzzle = newPuzzle;
		this.puzzleLabel.setText("Puzzle " + this.puzzle);
		final Puzzle newPuzzleObj = PuzzleFactory.getPuzzle(this.puzzle);
		this.initialPositions = newPuzzleObj.getPositions();
		this.initialValues = newPuzzleObj.getValues();
		this.elapsedTime = 0;
		resumeTimer();
		resetAndInitialize();
	}

	private void pauseTimer() {
		this.timerRunning = false;
		this.elapsedTime = SystemClock.elapsedRealtime() - this.timer.getBase();
		this.timer.stop();
		this.pauseResume.setImageResource(android.R.drawable.ic_media_play);
		this.gameBoard.setAlpha(0);

	}

	private void resetAndInitialize() {
		resetBoard();
		setInitialValues();
		setupInputButtons();
	}

	private void resetBoard() {
		for (int i = 1; i <= COL_SIZE; i++) {
			for (int j = 1; j <= ROW_SIZE; j++) {
				final Button button = getButton("" + i + j);
				button.setText("");
				button.setEnabled(true);
			}
		}

		this.boardState.clear();
		this.rangeSpinner.setSelection(0);
		this.input = BAD_VALUE;
	}

	private void resumeTimer() {
		this.timerRunning = true;
		this.timer.setBase(SystemClock.elapsedRealtime() - this.elapsedTime);
		this.timer.start();
		this.pauseResume.setImageResource(android.R.drawable.ic_media_pause);
		this.gameBoard.setAlpha(1);
	}

	private void setInitialValues() {
		final int[] pos = this.initialPositions;
		final int[] vals = this.initialValues;

		for (int i = 0; i < pos.length; i++) {
			final Button button = getButton(String.valueOf(pos[i]));
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
		final int numInputButtons = 16;
		final TableLayout inputs = this.inputButtons;
		final int newRange = this.range;

		for (int i = 1; i <= numInputButtons; i++) {
			final int value = numInputButtons * newRange + i;
			final Button inputButton = (Button) inputs
					.findViewWithTag("in" + i);
			inputButton.setText(String.valueOf(value));
			inputButton.setEnabled(!isOnBoard(value));
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
