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
	private static final int MENU_PUZZLE_OFFSET = 100;
	private static final int ROW_SIZE = 8;
	private static final int STARTING_PUZZLE = 0;
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
		setGameButtonText((Button) view);
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
			setupInputButtons();
		}
	}

	@Override
	public final void onNothingSelected(final AdapterView<?> parent) {
		// shouldn't be possible
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
		} else if (id >= MENU_PUZZLE_OFFSET
				&& id <= MENU_PUZZLE_OFFSET + PuzzleFactory.numPuzzles()) {
			final int puzzle = id - MENU_PUZZLE_OFFSET;
			final String outString = String.format(Locale.US, getResources()
					.getString(R.string.loading_puzzle_s), puzzle);
			Log.d(LOG_TAG, "Loading old puzzle " + puzzle);
			final Toast toast = Toast.makeText(this, outString,
					Toast.LENGTH_SHORT);
			toast.show();
			loadPuzzle(puzzle);
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
			this.timer.setText(formatElapsedTime());
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
		setupViews();

		this.boardState = new SparseIntArray(BOARD_MAX);
		this.maxPuzzleAttempted = STARTING_PUZZLE;
		if (savedState == null) {
			loadPuzzle(STARTING_PUZZLE);
		} // else let onRestoreInstanceState() handle it
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
		if (savedState == null) {
			return;
		}
		Log.d(LOG_TAG,
				"onRestoreInstanceState(); local elapsedTime = "
						+ this.elapsedTime + " bundle elapsed time: "
						+ savedState.getLong(ELAPSED_TIME));

		this.currentInput = savedState.getInt(CURRENT_INPUT);
		this.currentRange = savedState.getInt(CURRENT_RANGE);
		this.currentPuzzle = savedState.getInt(CURRENT_PUZZLE);
		this.maxPuzzleAttempted = savedState.getInt(MAX_PUZZLE_ATTEMPTED);
		loadPuzzle(this.currentPuzzle); // this resets the timer, but...
		pauseTimer(); // the timer will be resumed in onWindowFocusChanged()

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
		Log.d(LOG_TAG, "onSaveInstanceState(). elapsedTime: "
				+ this.elapsedTime);

		outState.putInt(CURRENT_INPUT, this.currentInput);
		outState.putInt(CURRENT_RANGE, this.currentRange);
		outState.putInt(CURRENT_PUZZLE, this.currentPuzzle);
		outState.putInt(MAX_PUZZLE_ATTEMPTED, this.maxPuzzleAttempted);
		outState.putBoolean(TIMER_RUNNING, this.timerRunning);
		setElapsedTime();
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
			setElapsedTime();
			final String outString = String.format(Locale.US, getResources()
					.getString(R.string.you_won_in_s), formatElapsedTime());
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

	private String formatElapsedTime() {
		// if there's a built-in version of this, I'd much rather use it.
		final int millisToSec = 1000;
		final int secToMin = 60;
		final long elapsed = this.elapsedTime / millisToSec;
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
	 * Returns the value on the board at the specified position.
	 * 
	 * @param pos
	 *            the position to get the value of
	 * @return the integer value of the Button or <code>BAD_VALUE</code> if
	 *         empty/null
	 */
	private int getValue(final int pos) {
		return this.boardState.get(pos, BAD_VALUE);
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

	/**
	 * Win-condition checking at the Button level. Looks for a neighbor with
	 * value+1 and a neighbor with value-1.
	 * 
	 * @param button
	 *            the Button to check against
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
	 * Win-condition checking at the board level. Iterates through each Button
	 * to check that it isn't empty and that it has valid neighbors.
	 * 
	 * @return true if no Button is empty or has all invalid neighbors.
	 */
	private boolean isBoardCorrect() {
		for (int i = 1; i <= COL_SIZE; i++) {
			for (int j = 1; j <= ROW_SIZE; j++) {
				if (!hasValidNeighbors(i * 10 + j)) {
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
		Log.d(LOG_TAG, "loadPuzzle(" + newPuzzle + ")");
		resetBoard();
		this.currentPuzzle = newPuzzle;
		this.puzzleLabel.setText(String.format(Locale.US, getResources()
				.getString(R.string.puzzle_s), this.currentPuzzle));
		if (newPuzzle > this.maxPuzzleAttempted) {
			this.maxPuzzleAttempted = newPuzzle;
			invalidateOptionsMenu(); // to add the new puzzle to the menu
		}
		resetAndInitialize();
		this.elapsedTime = 0;
		resumeTimer();
	}

	private void pauseTimer() {
		setElapsedTime();
		this.timerRunning = false;
		Log.d(LOG_TAG, "pauseTimer(). elapsed: " + this.elapsedTime);
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
		Log.d(LOG_TAG, "resumeTimer(); elapsed: " + this.elapsedTime);
		this.timerRunning = true;
		this.timer.setBase(SystemClock.elapsedRealtime() - this.elapsedTime);
		this.timer.start();
		this.pauseResume.setImageResource(android.R.drawable.ic_media_pause);
		this.gameBoard.setAlpha(1);
	}

	private void setElapsedTime() {
		this.elapsedTime = SystemClock.elapsedRealtime() - this.timer.getBase();
	}

	/**
	 * Sets the text of the specified Button to the value of the current input
	 * if valid, clearing the Button otherwise. If a value already exists at the
	 * specified Button, sets the current input to that value.
	 * 
	 * @param button
	 *            the Button to set the text for.
	 */
	private void setGameButtonText(final Button button) {
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

	private void setupViews() {
		this.gameBoard = (TableLayout) findViewById(R.id.connect64);
		this.inputButtons = (TableLayout) findViewById(R.id.inputButtons);
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
	}
}
