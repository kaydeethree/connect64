package edu.uwg.jamestwyford.connect64;

import java.util.Locale;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
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
public class Connect64 extends Activity implements
		AdapterView.OnItemSelectedListener {

	private static final int BAD_VALUE = -1;
	private static final int BOARD_MAX = 64;
	private static final int COL_SIZE = 8;
	private static final int ROW_SIZE = 8;
	private static final String ELAPSED_TIME = "elapsedTime";
	private static final String INPUT_VALUE = "input";
	private static final String LOG_TAG = "C64";
	private static final String MAX_PUZZLE_ATTEMPTED = "maxPuzzleAttempted";
	private static final String PUZZLE_VALUE = "puzzle";
	private static final String RANGE_VALUE = "range";
	private static final String STATE_POSITIONS = "statePositions";
	private static final String STATE_VALUES = "stateValues";
	private static final String TIMER_RUNNING = "timerRunning";

	// views
	private TableLayout gameBoard;
	private TableLayout inputButtons;
	private ImageButton pauseResume;
	private TextView puzzleLabel;
	private Spinner rangeSpinner;
	private Chronometer timer;

	// game state
	private SparseIntArray boardState;
	private int currentInput;
	private int currentPuzzle;
	private int currentRange;
	private long elapsedTime;
	private int maxPuzzleAttempted;
	private boolean timerRunning;

	/**
	 * Input handler for the clear button. Resets the game board to the initial
	 * state and resets the timer.
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
	 * Input handler for the 64 game board Buttons. If the current input is
	 * valid, sets the clicked Button to that value. Clears the Button
	 * otherwise. Updates the input Buttons to dis/enable them as appropriate.
	 * If the board is full, checks if the player has won and moves to the next
	 * puzzle if so.
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
	 * Input handler for the 16 input buttons. Sets the current input to the
	 * value of the Button.
	 * 
	 * @param view
	 *            the Button clicked
	 */
	public final void inputButtonClick(final View view) {
		this.currentInput = getValue((Button) view);
	}

	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.connect64, menu);
		return true;
	}

	@Override
	public final void onItemSelected(final AdapterView<?> parent,
			final View view, final int pos, final long id) {
		this.currentRange = pos;
		setupInputButtons();
	}

	@Override
	public final void onNothingSelected(final AdapterView<?> parent) {
	}

	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		final boolean retVal = true;
		switch (item.getItemId()) {
		case R.id.clearPrefs:
			final SharedPreferences prefs = getPreferences(MODE_PRIVATE);
			final SharedPreferences.Editor editor = prefs.edit();
			editor.clear();
			editor.apply();
			break;
		case R.id.loadPuzzle:
			break;
		default:
			break;
		}
		return retVal;
	}

	@Override
	public final void onWindowFocusChanged(final boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		Log.d(LOG_TAG, "onWindowFocusedChanged(" + hasFocus + ") timer? "
				+ this.timerRunning);
		if (hasFocus && this.timerRunning) {
			resumeTimer();
		} else if (hasFocus && !this.timerRunning) {
			this.timer.setText(convertTime());
		} else if (!hasFocus && this.timerRunning) {
			pauseTimer();
			this.timerRunning = true;
		}
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
		Log.d(LOG_TAG, "onCreate()");
		setContentView(R.layout.activity_connect64);

		this.gameBoard = (TableLayout) findViewById(R.id.connect64);
		this.inputButtons = (TableLayout) findViewById(R.id.inputButtons);
		this.puzzleLabel = (TextView) findViewById(R.id.puzzleLabel);
		this.rangeSpinner = (Spinner) findViewById(R.id.rangeSpinner);
		this.pauseResume = (ImageButton) findViewById(R.id.pauseResumeButton);
		this.timer = (Chronometer) findViewById(R.id.chronometer);

		this.boardState = new SparseIntArray(BOARD_MAX);
		this.maxPuzzleAttempted = 0;
		setupRangeSpinner();
		if (savedState == null) {
			loadPuzzle(0);
		}
	}

	@Override
	protected final void onPause() {
		super.onPause();
		Log.d(LOG_TAG, "onPause()");
		// @formatter:off
		/*
		final SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		final SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(INPUT_VALUE, this.currentInput);
		editor.putInt(RANGE_VALUE, this.currentRange);
		editor.putInt(PUZZLE_VALUE, this.currentPuzzle);
		editor.putBoolean(TIMER_RUNNING, this.timerRunning);
		editor.putLong(ELAPSED_TIME, this.elapsedTime);

		// grr prefs only taking primitives... (de)serialization sucks!
		final SparseIntArray state = this.boardState;
		final int size = state.size();
		final int[] positions = new int[size];
		final int[] values = new int[size];
		for (int i = 0; i < size; i++) {
			positions[i] = state.keyAt(i);
			values[i] = state.valueAt(i);
		}
		editor.putString(STATE_POSITIONS, serializeIntArray(positions));
		editor.putString(STATE_VALUES, serializeIntArray(values));
		editor.apply();
		*/
		// @formatter:on
	}

	@Override
	protected final void onRestoreInstanceState(final Bundle savedState) {
		super.onRestoreInstanceState(savedState);
		Log.d(LOG_TAG, "onRestoreInstanceState()");
		if (savedState == null) {
			return;
		}

		this.currentInput = savedState.getInt(INPUT_VALUE);
		this.currentRange = savedState.getInt(RANGE_VALUE);
		this.currentPuzzle = savedState.getInt(PUZZLE_VALUE);
		this.maxPuzzleAttempted = savedState.getInt(MAX_PUZZLE_ATTEMPTED);
		loadPuzzle(this.currentPuzzle);
		pauseTimer();

		this.rangeSpinner.setSelection(this.currentRange);
		this.elapsedTime = savedState.getLong(ELAPSED_TIME);
		this.timerRunning = savedState.getBoolean(TIMER_RUNNING);

		final SparseIntArray state = this.boardState;
		final int[] positions = savedState.getIntArray(STATE_POSITIONS);
		final int[] values = savedState.getIntArray(STATE_VALUES);
		for (int i = 0; i < positions.length; i++) {
			state.put(positions[i], values[i]);
			getButton(String.valueOf(positions[i])).setText(
					String.valueOf(values[i]));
		}
	}

	@Override
	protected final void onResume() {
		super.onResume();
		Log.d(LOG_TAG, "onResume()");
		// @formatter:off
		/*
		final SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		this.currentInput = prefs.getInt(INPUT_VALUE, BAD_VALUE);
		this.currentRange = prefs.getInt(RANGE_VALUE, 0);
		this.currentPuzzle = prefs.getInt(PUZZLE_VALUE, 0);
		this.timerRunning = prefs.getBoolean(TIMER_RUNNING, true);
		this.elapsedTime = prefs.getLong(ELAPSED_TIME, 0);
		resetAndInitialize();

		final SparseIntArray state = this.boardState;
		final String posString = prefs.getString(STATE_POSITIONS, null);
		final String valString = prefs.getString(STATE_VALUES, null);
		if (posString != null && valString != null) {
			final int[] positions = deserializeIntArray(posString);
			final int[] values = deserializeIntArray(valString);
			for (int i = 0; i < positions.length; i++) {
				state.put(positions[i], values[i]);
				getButton(String.valueOf(positions[i])).setText(
						String.valueOf(values[i]));
			}
		}
		*/
		// @formatter:on
	}

	@Override
	protected final void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.d(LOG_TAG, "onSaveInstanceState()");

		outState.putInt(INPUT_VALUE, this.currentInput);
		outState.putInt(RANGE_VALUE, this.currentRange);
		outState.putInt(PUZZLE_VALUE, this.currentPuzzle);
		outState.putInt(MAX_PUZZLE_ATTEMPTED, this.maxPuzzleAttempted);
		outState.putBoolean(TIMER_RUNNING, this.timerRunning);
		outState.putLong(ELAPSED_TIME, this.elapsedTime);

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
		final boolean boardCorrect = isBoardCorrect();
		final boolean onLastPuzzle = isOnLastPuzzle();

		if (boardCorrect && onLastPuzzle) {
			toast = Toast.makeText(this, R.string.all_puzzles_complete,
					Toast.LENGTH_SHORT);
			this.timerRunning = false;
			this.timer.stop();
		} else if (boardCorrect && !onLastPuzzle) {
			pauseTimer();
			final String outString = String.format(Locale.US, getResources()
					.getString(R.string.you_won_in_s), convertTime());
			toast = Toast.makeText(this, outString, Toast.LENGTH_SHORT);
			final int nextPuzzle = this.currentPuzzle + 1;
			loadPuzzle(nextPuzzle);
		} else if (!boardCorrect && onLastPuzzle) {
			toast = Toast
					.makeText(this, R.string.try_again, Toast.LENGTH_SHORT);
		}
		if (toast != null) {
			toast.show();
		}
	}

	private String convertTime() {
		final int millisToSec = 1000;
		final int secToMin = 60;
		final long elapsed = this.elapsedTime / millisToSec;
		return String.format(Locale.US, "%02d:%02d", elapsed / secToMin,
				elapsed % secToMin);
	}

	// @formatter:off
	/*
	private int[] deserializeIntArray(final String string) {
		try {
			final byte[] bytes = string.getBytes();
			final ObjectInputStream in = new ObjectInputStream(
					new ByteArrayInputStream(bytes));
			final int[] inArray = (int[]) in.readObject();
			return inArray;
		} catch (final Exception ex) {
			return null;
		}
	}
	*/
	// @formatter:on

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
	 *            the Button to get the value of
	 * @return the integer value of the Button or <code>BAD_VALUE</code> if
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
	 *            the Button to check against
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

	private boolean isOnLastPuzzle() {
		return this.currentPuzzle == PuzzleFactory.numPuzzles();
	}

	private boolean isPrev(final int thisValue, final int otherValue) {
		return thisValue == otherValue + 1;
	}

	private boolean isValueOnBoard(final int value) {
		return this.boardState.indexOfValue(value) >= 0;
	}

	private void loadPuzzle(final int newPuzzle) {
		resetBoard();
		this.currentPuzzle = newPuzzle;
		this.puzzleLabel.setText(String.format(Locale.US, getResources()
				.getString(R.string.puzzle_s), this.currentPuzzle));
		if (newPuzzle > this.maxPuzzleAttempted) {
			this.maxPuzzleAttempted = newPuzzle;
		}
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
		this.currentInput = BAD_VALUE;
	}

	private void resumeTimer() {
		this.timerRunning = true;
		this.timer.setBase(SystemClock.elapsedRealtime() - this.elapsedTime);
		this.timer.start();
		this.pauseResume.setImageResource(android.R.drawable.ic_media_pause);
		this.gameBoard.setAlpha(1);
	}

	// @formatter:off
	/*
	private String serializeIntArray(final int[] array) {
		try {
			final ObjectOutputStream out = new ObjectOutputStream(
					new ByteArrayOutputStream());
			out.writeObject(array);
			out.flush();
			return out.toString();
		} catch (final IOException ex) {
			return null;
		}
	}
	*/
	// @formatter:on

	private void setInitialValues() {
		final Puzzle newPuzzleObj = PuzzleFactory.getPuzzle(this.currentPuzzle);
		final int[] pos = newPuzzleObj.getPositions();
		final int[] vals = newPuzzleObj.getValues();

		for (int i = 0; i < pos.length; i++) {
			final Button button = getButton(String.valueOf(pos[i]));
			button.setText(String.valueOf(vals[i]));
			button.setEnabled(false);
			this.boardState.put(pos[i], vals[i]);
		}
	}

	/**
	 * Sets the text of the specified Button to the value of the current input
	 * if valid, clearing the Button otherwise. If a value already exists at the
	 * specified Button, sets the current input to that value.
	 * 
	 * @param button
	 *            the Button to set the text for.
	 */
	private void setText(final Button button) {
		final int pos = getTag(button);
		final int tempInput = getValue(button);

		if (this.currentInput == BAD_VALUE) {
			button.setText("");
			this.boardState.delete(pos);
		} else {
			button.setText(String.valueOf(this.currentInput));
			this.boardState.put(pos, this.currentInput);
		}
		this.currentInput = tempInput;
	}

	/**
	 * Sets the text on the input Buttons based on the current range. Buttons
	 * whose freshly-set text appears on the game board will be disabled.
	 */
	private void setupInputButtons() {
		final int numInputButtons = 16;
		final TableLayout inputs = this.inputButtons;
		final int newRange = this.currentRange;

		for (int i = 1; i <= numInputButtons; i++) {
			final int value = numInputButtons * newRange + i;
			final Button inputButton = (Button) inputs
					.findViewWithTag("in" + i);
			inputButton.setText(String.valueOf(value));
			inputButton.setEnabled(!isValueOnBoard(value));
		}
	}

	private void setupRangeSpinner() {
		final ArrayAdapter<CharSequence> adapter = ArrayAdapter
				.createFromResource(this, R.array.ranges,
						android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		this.rangeSpinner.setAdapter(adapter);
		this.rangeSpinner.setOnItemSelectedListener(this);
	}
}
