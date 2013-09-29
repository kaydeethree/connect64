package edu.uwg.jamestwyford.connect64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
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
 * Game logic and GUI handling for the 8x8 board.
 * 
 * @author jtwyford
 * @version assignment3
 */
public class Connect64 extends Activity implements
		AdapterView.OnItemSelectedListener {

	private static final int BAD_VALUE = -1;
	private static final int BOARD_MAX = 64;
	private static final int COL_SIZE = 8;
	private static final int NUM_INPUT_BUTTONS = 16;
	private static final int MENU_PUZZLE_OFFSET = 100;
	private static final int ROW_SIZE = 8;
	private static final int STARTING_PUZZLE = 0;
	private static final int TOP_SCORES = 1;
	private static final String CURRENT_INPUT = "currentInput";
	private static final String CURRENT_PUZZLE = "currentPuzzle";
	private static final String CURRENT_RANGE = "currentRange";
	private static final String ELAPSED_TIME = "elapsedTime";
	private static final String LOG_TAG = "C64";
	private static final String MAX_PUZZLE_ATTEMPTED = "maxPuzzleAttempted";
	private static final String STATE_POSITIONS = "statePositions";
	private static final String STATE_VALUES = "stateValues";
	private static final String TIMER_RUNNING = "timerRunning";

	// views
	private TableLayout gameBoard;
	private ImageButton pauseResume;
	private TextView puzzleLabel;
	private Spinner rangeSpinner;
	private Chronometer timer;
	private Button[] inputButtons;

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
		setGameButtonText((Button) view);
		Log.d(LOG_TAG, "calling setupInputButtons() from gameButtonClick()");
		setupInputButtons();

		if (this.boardState.size() == BOARD_MAX) {
			checkWinCondition();
		}
	}

	/**
	 * Input handler for the 16 input buttons. Sets the current input to the
	 * value of the clicked Button.
	 * 
	 * @param view
	 *            the Button clicked
	 */
	public final void inputButtonClick(final View view) {
		this.currentInput = Integer.valueOf(((Button) view).getText()
				.toString());
	}

	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		Log.d(LOG_TAG, "onCreateOptionsMenu()");
		getMenuInflater().inflate(R.menu.connect64, menu);
		return true;
	}

	@Override
	public final void onItemSelected(final AdapterView<?> parent,
			final View view, final int pos, final long id) {
		if (parent.getId() == R.id.rangeSpinner) {
			this.currentRange = pos;
			Log.d(LOG_TAG, "calling setupInputButtons from onItemSelected");
			setupInputButtons();
		}
	}

	@Override
	public final void onNothingSelected(final AdapterView<?> parent) {
		// no-op
	}

	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		final boolean retVal = true;
		final int id = item.getItemId();
		if (id == R.id.clearPrefs) {
			final SharedPreferences prefs = getPreferences(MODE_PRIVATE);
			final SharedPreferences.Editor editor = prefs.edit();
			editor.clear();
			editor.apply();
		} else if (id == R.id.topScores) {
			final Intent intent = new Intent(getApplicationContext(),
					TopScores.class);
			startActivityForResult(intent, TOP_SCORES);
		} else if (id >= MENU_PUZZLE_OFFSET
				&& id <= MENU_PUZZLE_OFFSET + PuzzleFactory.numPuzzles()) {
			final int puzzle = id - MENU_PUZZLE_OFFSET;
			final String outString = String.format(Locale.US, getResources()
					.getString(R.string.loading_puzzle_s), puzzle);
			Log.d(LOG_TAG, "Loading old puzzle " + puzzle);
			final Toast toast = Toast.makeText(this, outString,
					Toast.LENGTH_SHORT);
			toast.show();
			loadPuzzle(puzzle, true);
		}
		return retVal;
	}

	@Override
	public final boolean onPrepareOptionsMenu(final Menu menu) {
		Log.d(LOG_TAG, "onPrepareOptionsMenu(). maxPuzzle: "
				+ this.maxPuzzleAttempted);
		final SubMenu submenu = menu.findItem(R.id.loadPuzzle).getSubMenu();
		submenu.clear();

		for (int i = STARTING_PUZZLE; i <= this.maxPuzzleAttempted; i++) {
			submenu.add(
					Menu.NONE,
					MENU_PUZZLE_OFFSET + i,
					Menu.NONE,
					String.format(Locale.US,
							getResources().getString(R.string.puzzle_s), i));
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public final void onWindowFocusChanged(final boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		Log.d(LOG_TAG, "onWindowFocusedChanged(" + hasFocus + ") timer? "
				+ this.timerRunning);
		if (hasFocus && this.timerRunning) {
			resumeTimer();
		} else if (hasFocus && !this.timerRunning) {
			this.timer.setText(formatTime(this.elapsedTime));
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
		Log.d(LOG_TAG, "==================onCreate()=======================");
		setContentView(R.layout.activity_connect64);
		this.boardState = new SparseIntArray(BOARD_MAX);
		this.gameBoard = (TableLayout) findViewById(R.id.connect64);
		this.puzzleLabel = (TextView) findViewById(R.id.puzzleLabel);
		this.rangeSpinner = (Spinner) findViewById(R.id.rangeSpinner);
		this.pauseResume = (ImageButton) findViewById(R.id.pauseResumeButton);
		this.timer = (Chronometer) findViewById(R.id.chronometer);
		final ArrayAdapter<CharSequence> adapter = ArrayAdapter
				.createFromResource(this, R.array.ranges,
						android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		this.rangeSpinner.setAdapter(adapter);
		this.rangeSpinner.setOnItemSelectedListener(this);

		final TableLayout inputs = (TableLayout) findViewById(R.id.inputButtons);
		final Button[] buttons = new Button[NUM_INPUT_BUTTONS];
		for (int i = 0; i < NUM_INPUT_BUTTONS; i++) {
			buttons[i] = (Button) inputs.findViewWithTag("in" + (i + 1));
		}
		this.inputButtons = buttons;
		// load puzzle/state in onResume/onRestoreInstanceState()
	}

	@Override
	protected final void onPause() {
		super.onPause();
		Log.d(LOG_TAG, "onPause()");

		final SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		final SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(CURRENT_INPUT, this.currentInput);
		editor.putInt(CURRENT_RANGE, this.currentRange);
		editor.putInt(CURRENT_PUZZLE, this.currentPuzzle);
		editor.putInt(MAX_PUZZLE_ATTEMPTED, this.maxPuzzleAttempted);
		editor.putBoolean(TIMER_RUNNING, this.timerRunning);
		if (this.timerRunning) {
			setElapsedTime();
		}
		editor.putLong(ELAPSED_TIME, this.elapsedTime);

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
	}

	@Override
	protected final void onRestoreInstanceState(final Bundle savedState) {
		super.onRestoreInstanceState(savedState);
		Log.d(LOG_TAG,
				"onRestoreInstanceState(); local elapsed: " + this.elapsedTime
						+ " bundle elapsed: "
						+ savedState.getLong(ELAPSED_TIME));

		this.currentInput = savedState.getInt(CURRENT_INPUT, BAD_VALUE);
		this.currentRange = savedState.getInt(CURRENT_RANGE, 0);
		this.currentPuzzle = savedState.getInt(CURRENT_PUZZLE, STARTING_PUZZLE);
		this.maxPuzzleAttempted = savedState.getInt(MAX_PUZZLE_ATTEMPTED,
				STARTING_PUZZLE);
		loadPuzzle(this.currentPuzzle, false);

		this.rangeSpinner.setSelection(this.currentRange);
		this.elapsedTime = savedState.getLong(ELAPSED_TIME, 0L);
		this.timerRunning = savedState.getBoolean(TIMER_RUNNING, true);

		final int[] positions = savedState.getIntArray(STATE_POSITIONS);
		final int[] values = savedState.getIntArray(STATE_VALUES);
		final SparseIntArray state = this.boardState;
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
		final SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		this.currentInput = prefs.getInt(CURRENT_INPUT, BAD_VALUE);
		this.currentRange = prefs.getInt(CURRENT_RANGE, 0);
		this.currentPuzzle = prefs.getInt(CURRENT_PUZZLE, STARTING_PUZZLE);
		this.maxPuzzleAttempted = prefs.getInt(MAX_PUZZLE_ATTEMPTED,
				STARTING_PUZZLE);
		loadPuzzle(this.currentPuzzle, false);

		this.rangeSpinner.setSelection(this.currentRange);
		this.elapsedTime = prefs.getLong(ELAPSED_TIME, 0L);
		this.timerRunning = prefs.getBoolean(TIMER_RUNNING, true);

		final String posString = prefs.getString(STATE_POSITIONS, null);
		final String valString = prefs.getString(STATE_VALUES, null);

		if (posString != null && valString != null) {
			final int[] positions = deserializeIntArray(posString);
			final int[] values = deserializeIntArray(valString);
			final SparseIntArray state = this.boardState;
			for (int i = 0; i < positions.length; i++) {
				state.put(positions[i], values[i]);
				getButton(String.valueOf(positions[i])).setText(
						String.valueOf(values[i]));
			}
		} else {
			loadPuzzle(this.currentPuzzle, true);
		}
	}

	@Override
	protected final void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.d(LOG_TAG, "onSaveInstanceState(). elapsedTime: "
				+ this.elapsedTime);

		outState.putInt(CURRENT_INPUT, this.currentInput);
		outState.putInt(CURRENT_RANGE, this.currentRange);
		outState.putInt(CURRENT_PUZZLE, this.currentPuzzle);
		outState.putInt(MAX_PUZZLE_ATTEMPTED, this.maxPuzzleAttempted);
		outState.putBoolean(TIMER_RUNNING, this.timerRunning);
		if (this.timerRunning) {
			setElapsedTime();
		}
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
	 * Overall board win condition check. Board must be full and all positions
	 * must have a valid higher and lower neighbor to fulfill the win condition.
	 * If the player has won, load the next Puzzle if one exists.
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
			setElapsedTime();
			final String outString = String.format(Locale.US, getResources()
					.getString(R.string.you_won_in_s),
					formatTime(this.elapsedTime));
			toast = Toast.makeText(this, outString, Toast.LENGTH_SHORT);
			final int nextPuzzle = this.currentPuzzle + 1;
			loadPuzzle(nextPuzzle, true);
		} else if (!boardCorrect && onLastPuzzle) {
			toast = Toast
					.makeText(this, R.string.try_again, Toast.LENGTH_SHORT);
		}
		if (toast != null) {
			toast.show();
		}
	}

	private int[] deserializeIntArray(final String string) {
		try {
			final byte[] bytes = Base64.decode(string, Base64.DEFAULT);
			final ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
			final ObjectInputStream in = new ObjectInputStream(bin);
			return (int[]) in.readObject();
		} catch (final Exception ex) {
			return null;
		}
	}

	private String formatTime(final long millis) {
		// if there's a built-in version of this, I'd much rather use it.
		final int millisToSec = 1000;
		final int secToMin = 60;
		final long elapsed = millis / millisToSec;
		return String.format(Locale.US, "%02d:%02d", elapsed / secToMin,
				elapsed % secToMin);
	}

	private Button getButton(final String tag) {
		final Button button = (Button) this.gameBoard.findViewWithTag(tag);
		if (button == null) {
			Log.e(LOG_TAG, "invalid tag passed: " + tag);
		}
		return button;
	}

	/**
	 * Returns the value at the specified position.
	 * 
	 * @param pos
	 *            the position to get the value of
	 * @return the value of the position or <code>BAD_VALUE</code> if empty
	 */
	private int getValue(final int pos) {
		return this.boardState.get(pos, BAD_VALUE);
	}

	/**
	 * Win-condition checking at the position level. Looks for a neighbor with
	 * value+1 and a neighbor with value-1.
	 * 
	 * @param pos
	 *            the position to check against
	 * @return true if one neighbor has value+1 AND another neighbor has value-1
	 */
	private boolean hasValidNeighbors(final int pos) {
		final int value = this.boardState.get(pos, BAD_VALUE);
		if (value == BAD_VALUE) {
			return false;
		}
		final int xDelta = 1;
		final int yDelta = 10;

		final int left = this.boardState.get(pos - xDelta, BAD_VALUE);
		final int right = this.boardState.get(pos + xDelta, BAD_VALUE);
		final int up = this.boardState.get(pos - yDelta, BAD_VALUE);
		final int down = this.boardState.get(pos + yDelta, BAD_VALUE);

		final boolean hasNext = (value == BOARD_MAX) || isNext(value, left)
				|| isNext(value, right) || isNext(value, up)
				|| isNext(value, down);
		final boolean hasPrev = (value == 1) || isPrev(value, left)
				|| isPrev(value, right) || isPrev(value, up)
				|| isPrev(value, down);

		return hasNext && hasPrev;
	}

	/**
	 * Win-condition checking at the board level. Iterates through each position
	 * to check that it isn't empty and that it has valid neighbors.
	 * 
	 * @return true if no position is empty or has all invalid neighbors.
	 */
	private boolean isBoardCorrect() {
		final int rowOffset = 10;
		for (int i = 1; i <= COL_SIZE; i++) {
			for (int j = 1; j <= ROW_SIZE; j++) {
				if (!hasValidNeighbors(i * rowOffset + j)) {
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

	/**
	 * Loads the specified Puzzle. Will reset the timer if restartTimer is true
	 * 
	 * @param newPuzzle
	 *            the puzzle to load
	 * @param restartTimer
	 *            whether or not to restart the timer
	 */
	private void loadPuzzle(final int newPuzzle, final boolean restartTimer) {
		Log.d(LOG_TAG, "loadPuzzle(" + newPuzzle + ")");
		this.currentPuzzle = newPuzzle;
		this.puzzleLabel.setText(String.format(Locale.US, getResources()
				.getString(R.string.puzzle_s), this.currentPuzzle));
		if (newPuzzle > this.maxPuzzleAttempted) {
			this.maxPuzzleAttempted = newPuzzle;
			invalidateOptionsMenu(); // to add the new puzzle to the menu
		}
		resetAndInitialize();
		if (restartTimer) {
			this.elapsedTime = 0;
			resumeTimer();
		}
	}

	private void pauseTimer() {
		setElapsedTime();
		this.timerRunning = false;
		Log.d(LOG_TAG, "pauseTimer(). elapsed: " + this.elapsedTime);
		this.timer.stop();
		this.pauseResume.setImageResource(android.R.drawable.ic_media_play);
		this.gameBoard.setVisibility(View.INVISIBLE);
		Log.d(LOG_TAG, "calling setupInputButtons() from pauseTimer");
		setupInputButtons();
	}

	private void resetAndInitialize() {
		Log.d(LOG_TAG, "resetAndInitialize()");
		resetGame();
		setInitialValues();
		Log.d(LOG_TAG, "calling setupInputButtons from resetAndInitialize");
		setupInputButtons();
	}

	private void resetGame() {
		Log.d(LOG_TAG, "resetBoard()");
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
		Log.d(LOG_TAG, "resumeTimer(); elapsed: " + this.elapsedTime);
		this.timerRunning = true;
		this.timer.setBase(SystemClock.elapsedRealtime() - this.elapsedTime);
		this.timer.start();
		this.pauseResume.setImageResource(android.R.drawable.ic_media_pause);
		this.gameBoard.setVisibility(View.VISIBLE);
		Log.d(LOG_TAG, "calling setupInputButtons from resumeTimer()");
		setupInputButtons();
	}

	private String serializeIntArray(final int[] array) {
		try {
			final ByteArrayOutputStream bo = new ByteArrayOutputStream();
			final ObjectOutputStream out = new ObjectOutputStream(bo);
			out.writeObject(array);
			out.flush();
			return Base64.encodeToString(bo.toByteArray(), Base64.DEFAULT);
		} catch (final Exception ex) {
			return null;
		}
	}

	private void setElapsedTime() {
		this.elapsedTime = SystemClock.elapsedRealtime() - this.timer.getBase();
	}

	/**
	 * Sets the text of the specified Button to the value of the current input
	 * if valid, clearing the Button otherwise. If a value already exists at the
	 * specified Button, sets the current input to that value. Also updates the
	 * internal state as appropriate.
	 * 
	 * @param button
	 *            the Button to set the text for.
	 */
	private void setGameButtonText(final Button button) {
		Log.d(LOG_TAG, "setGameButtonText(): " + this.currentInput);
		final int pos = Integer.valueOf(button.getTag().toString());
		final int oldValue = getValue(pos);

		if (this.currentInput == BAD_VALUE) {
			button.setText("");
			this.boardState.delete(pos);
		} else {
			button.setText(String.valueOf(this.currentInput));
			this.boardState.put(pos, this.currentInput);
		}
		this.currentInput = oldValue;
	}

	/**
	 * Fills the (assumed clear) board with values specified by the current
	 * Puzzle at the appropriate positions and disables those positions so
	 * players can't modify them. Also inserts those (position, value) pairs
	 * specified by the current Puzzle into the internal state.
	 */
	private void setInitialValues() {
		Log.d(LOG_TAG, "setInitialValues()");
		final Puzzle puzzle = PuzzleFactory.getPuzzle(this.currentPuzzle);
		final int[] pos = puzzle.getPositions();
		final int[] vals = puzzle.getValues();

		for (int i = 0; i < pos.length; i++) {
			final Button button = getButton(String.valueOf(pos[i]));
			button.setText(String.valueOf(vals[i]));
			button.setEnabled(false);
			this.boardState.put(pos[i], vals[i]);
		}
	}

	/**
	 * Sets the text on the input Buttons based on the current range. Buttons
	 * whose freshly-set text appears on the game board will be disabled. If the
	 * timer is paused, disables all Buttons.
	 */
	private void setupInputButtons() {
		Log.d(LOG_TAG, "setupInputButtons()");
		final int newRange = this.currentRange;

		for (int i = 0; i < NUM_INPUT_BUTTONS; i++) {
			final int value = NUM_INPUT_BUTTONS * newRange + i + 1;
			final Button inputButton = this.inputButtons[i];
			inputButton.setText(String.valueOf(value));
			inputButton.setEnabled(this.timerRunning && !isValueOnBoard(value));
		}
	}
}
