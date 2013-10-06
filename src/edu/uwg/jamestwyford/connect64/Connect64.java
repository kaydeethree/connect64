package edu.uwg.jamestwyford.connect64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
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
import edu.uwg.jamestwyford.connect64.db.ScoresContentProviderDB;
import edu.uwg.jamestwyford.connect64.db.ScoresContract.Scores;

/**
 * Game logic and GUI handling for the 8x8 board.
 * 
 * @author jtwyford
 * @version assignment3
 */
public class Connect64 extends Activity implements
		AdapterView.OnItemSelectedListener, OnSharedPreferenceChangeListener {

	private static final int BAD_VALUE = -1;
	private static final int BOARD_MAX = 64;
	private static final int COL_SIZE = 8;
	private static final int NUM_INPUT_BUTTONS = 16;
	private static final int MENU_PUZZLE_OFFSET = 100;
	private static final int ROW_SIZE = 8;
	private static final int STARTING_PUZZLE = 0;
	private static final String CURRENT_INPUT = "currentInput";
	private static final String CURRENT_PUZZLE = "currentPuzzle";
	private static final String CURRENT_RANGE = "currentRange";
	private static final String ELAPSED_TIME = "elapsedTime";
	private static final String GAME_STATE_PREFS = "gameStatePrefs";
	private static final String LOG_TAG = "C64";
	private static final String MAX_PUZZLE_ATTEMPTED = "maxPuzzleAttempted";
	private static final String STATE_POSITIONS = "statePositions";
	private static final String STATE_VALUES = "stateValues";
	private static final String TIMER_RUNNING = "timerRunning";
	private static final String PLAYER_NAME = "Player";

	// views
	private TableLayout gameBoard;
	private Button[] inputButtons;
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

	// prefs
	private SharedPreferences preferences;
	private boolean autoFillIn;
	private int prefNumberColor;

	/**
	 * Input handler for the clear button. Reloads the current puzzle and resets
	 * the timer.
	 * 
	 * @param view
	 *            ignored
	 */
	public final void clearButtonClick(final View view) {
		Log.d(LOG_TAG, "clearButtonClick()");
		loadPuzzle(this.currentPuzzle, true);
	}

	/**
	 * Input handler for the 64 game board Buttons. If the current input is
	 * valid, sets the clicked Button to that value. Clears the Button
	 * otherwise. If the board is full, checks if the player has won and moves
	 * to the next Puzzle if so.
	 * 
	 * @param view
	 *            the Button clicked
	 */
	public final void gameButtonClick(final View view) {
		setGameButtonText((Button) view);
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
		this.currentInput = Integer.parseInt(((Button) view).getText()
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
			Log.d(LOG_TAG, "calling setupInputButtons() from onItemSelected()");
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
		if (id == R.id.topScores) {
			final Intent intent = new Intent(this, TopScores.class);
			startActivity(intent);
		} else if (id == R.id.clearScores) {
			getContentResolver().delete(ScoresContentProviderDB.CONTENT_URI,
					null, null);
		} else if (id == R.id.action_settings) {
			final Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
		} else if (id >= MENU_PUZZLE_OFFSET
				&& id <= MENU_PUZZLE_OFFSET + PuzzleFactory.numPuzzles()) {
			final int puzzle = id - MENU_PUZZLE_OFFSET;
			final String outString = String.format(Locale.US, getResources()
					.getString(R.string.loading_puzzle_s), puzzle);
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
	public final void onSharedPreferenceChanged(
			final SharedPreferences sharedPreferences, final String key) {
		if (key.equals(SettingsActivity.KEY_PREF_NUMBER_COLOR)) {
			this.prefNumberColor = sharedPreferences.getInt(
					SettingsActivity.KEY_PREF_NUMBER_COLOR,
					SettingsActivity.PREF_NUMBER_COLOR_DEFAULT);
			changeButtonTextColor();
			Log.d(LOG_TAG, "new number color: " + this.prefNumberColor);
		} else if (key.equals(SettingsActivity.KEY_PREF_CELL_COLOR)) {
			this.gameBoard.setBackgroundColor(sharedPreferences.getInt(
					SettingsActivity.KEY_PREF_CELL_COLOR,
					SettingsActivity.PREF_CELL_COLOR_DEFAULT));
		} else if (key.equals(SettingsActivity.KEY_PREF_AUTO_FILLIN)) {
			this.autoFillIn = sharedPreferences.getBoolean(
					SettingsActivity.KEY_PREF_AUTO_FILLIN,
					SettingsActivity.PREF_AUTO_FILLIN_DEFAULT);
		}
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
		Log.d(LOG_TAG, "pauseResumeClick()");
		if (this.timerRunning) {
			pauseTimer();
		} else {
			resumeTimer();
		}
	}

	@Override
	protected final void onCreate(final Bundle savedState) {
		super.onCreate(savedState);
		Log.d(LOG_TAG, "====================onCreate()====================");
		setContentView(R.layout.activity_connect64);
		setupViews();
		setupPreferences();
	}

	@Override
	protected final void onPause() {
		super.onPause();
		Log.d(LOG_TAG, "onPause()");
		final SharedPreferences statePrefs = getSharedPreferences(
				GAME_STATE_PREFS, MODE_PRIVATE);
		final SharedPreferences.Editor stateEditor = statePrefs.edit();
		stateEditor.putBoolean(TIMER_RUNNING, this.timerRunning);
		if (this.timerRunning) {
			setElapsedTime();
			this.timerRunning = false;
		}
		Log.d(LOG_TAG, "pausing. elapsed time: " + this.elapsedTime);
		stateEditor.putLong(ELAPSED_TIME, this.elapsedTime);
		stateEditor.putInt(CURRENT_INPUT, this.currentInput);
		stateEditor.putInt(CURRENT_PUZZLE, this.currentPuzzle);
		stateEditor.putInt(CURRENT_RANGE, this.currentRange);
		stateEditor.putInt(MAX_PUZZLE_ATTEMPTED, this.maxPuzzleAttempted);

		final SparseIntArray state = this.boardState;
		final int size = state.size();
		final int[] positions = new int[size];
		final int[] values = new int[size];
		for (int i = 0; i < size; i++) {
			positions[i] = state.keyAt(i);
			values[i] = state.valueAt(i);
		}
		stateEditor.putString(STATE_POSITIONS, serializeIntArray(positions));
		stateEditor.putString(STATE_VALUES, serializeIntArray(values));
		stateEditor.apply();
	}

	@Override
	protected final void onResume() {
		super.onResume();
		Log.d(LOG_TAG, "onResume()");
		final SharedPreferences statePrefs = getSharedPreferences(
				GAME_STATE_PREFS, MODE_PRIVATE);
		this.currentInput = statePrefs.getInt(CURRENT_INPUT, BAD_VALUE);
		this.currentPuzzle = statePrefs.getInt(CURRENT_PUZZLE, STARTING_PUZZLE);
		this.maxPuzzleAttempted = statePrefs.getInt(MAX_PUZZLE_ATTEMPTED,
				STARTING_PUZZLE);

		this.timerRunning = statePrefs.getBoolean(TIMER_RUNNING, true);
		loadPuzzle(this.currentPuzzle, false); // to disable initial buttons
		this.currentRange = statePrefs.getInt(CURRENT_RANGE, 0);
		this.rangeSpinner.setSelection(this.currentRange);

		final String posString = statePrefs.getString(STATE_POSITIONS, null);
		final String valString = statePrefs.getString(STATE_VALUES, null);

		this.elapsedTime = statePrefs.getLong(ELAPSED_TIME, 0L);
		Log.d(LOG_TAG, "resuming elapsed time: " + this.elapsedTime);
		if (posString != null && valString != null) {
			Log.d(LOG_TAG, "loading saved puzzle state");
			final int[] positions = deserializeIntArray(posString);
			final int[] values = deserializeIntArray(valString);
			final SparseIntArray state = this.boardState;
			for (int i = 0; i < positions.length; i++) {
				state.put(positions[i], values[i]);
				getGameButton(String.valueOf(positions[i])).setText(
						String.valueOf(values[i]));
			}
		} else {
			Log.d(LOG_TAG, "starting from scratch, starting timer.");
			resumeTimer();
		}
	}

	private void addScoretoDB() {
		final ContentValues newScore = new ContentValues(3);
		newScore.put(Scores.PLAYER, PLAYER_NAME);
		newScore.put(Scores.PUZZLE, this.currentPuzzle);
		newScore.put(Scores.COMPLETION_TIME, formatTime(this.elapsedTime));
		getContentResolver().insert(ScoresContentProviderDB.CONTENT_URI,
				newScore);
	}

	private void changeButtonTextColor() {
		final int color = this.prefNumberColor;
		for (int i = 1; i <= ROW_SIZE; i++) {
			for (int j = 1; j <= COL_SIZE; j++) {
				getGameButton("" + i + j).setTextColor(color);
			}
		}
	}

	/**
	 * Overall board win condition check. Board must be full and all positions
	 * must have a valid higher and lower neighbor to fulfill the win condition.
	 * If the player has won, load the next Puzzle if one exists.
	 */
	private void checkWinCondition() {
		Toast toast = null;
		final boolean boardCorrect = isBoardCorrect();
		final boolean onLastPuzzle = this.currentPuzzle == PuzzleFactory
				.numPuzzles();
		if (boardCorrect) {
			setElapsedTime(); // so checkstyle is ok with no "this." here...
			this.addScoretoDB(); // but it wants one here?
		}
		if (boardCorrect && onLastPuzzle) {
			toast = Toast.makeText(this, R.string.all_puzzles_complete,
					Toast.LENGTH_SHORT);
			this.timerRunning = false;
			this.timer.stop();
		} else if (boardCorrect && !onLastPuzzle) {
			final String outString = String.format(Locale.US, getResources()
					.getString(R.string.you_won_in_s),
					formatTime(this.elapsedTime));
			toast = Toast.makeText(this, outString, Toast.LENGTH_SHORT);
			final int nextPuzzle = this.currentPuzzle + 1;
			loadPuzzle(nextPuzzle, true);
		} else if (!boardCorrect) {
			toast = Toast
					.makeText(this, R.string.try_again, Toast.LENGTH_SHORT);
		} else {
			return;
		}
		toast.show();
		performFeedback(boardCorrect);
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

	private Button getGameButton(final String tag) {
		final Button button = (Button) this.gameBoard.findViewWithTag(tag);
		if (button == null) {
			Log.e(LOG_TAG, "invalid tag passed: " + tag);
		}
		return button;
	}

	private int[] getValidOptions(final int loc) {
		final int xDelta = 1;
		final int yDelta = 10;
		final int left = getValue(loc - xDelta);
		final int right = getValue(loc + xDelta);
		final int up = getValue(loc - yDelta);
		final int down = getValue(loc + yDelta);
		int[] out = new int[8];
		int count = 0;

		if (left > 1 && !isValueOnBoard(left - 1)) {
			out[count++] = left - 1;
		}
		if (left > 0 && left < 64 && !isValueOnBoard(left + 1)
				&& !inIntArray(left + 1, out)) {
			out[count++] = left + 1;
		}
		if (right > 1 && !isValueOnBoard(right - 1)
				&& !inIntArray(right - 1, out)) {
			out[count++] = right - 1;
		}
		if (right > 0 && right < 64 && !isValueOnBoard(right + 1)
				&& !inIntArray(right + 1, out)) {
			out[count++] = right + 1;
		}
		if (up > 1 && !isValueOnBoard(up - 1) && !inIntArray(up - 1, out)) {
			out[count++] = up - 1;
		}
		if (up > 0 && up < 64 && !isValueOnBoard(up + 1)
				&& !inIntArray(up + 1, out)) {
			out[count++] = up + 1;
		}
		if (down > 1 && !isValueOnBoard(down - 1) && !inIntArray(down - 1, out)) {
			out[count++] = down - 1;
		}
		if (down > 0 && down < 64 && !isValueOnBoard(down + 1)
				&& !inIntArray(down + 1, out)) {
			out[count++] = down + 1;
		}

		int[] out2 = new int[count];
		count = 0;
		for (final int i : out) {
			if (i > 0) {
				out2[count++] = i;
			}
		}
		return out2;
	}

	private boolean inIntArray(int needle, int[] haystack) {
		for (final int i : haystack) {
			if (needle == i) {
				return true;
			}
		}
		return false;
	}

	private int getValue(int position) {
		return this.boardState.get(position, BAD_VALUE);
	}

	/**
	 * Win-condition checking at the position level. Looks for a neighbor whose
	 * value is 1 higher than this position's value, and another neighbor whose
	 * value is 1 lower.
	 * 
	 * @param pos
	 *            the position to check against
	 * @return true if one neighbor has value+1 AND another neighbor has value-1
	 */
	private boolean hasValidNeighbors(final int pos) {
		final int value = getValue(pos);
		if (value == BAD_VALUE) {
			return false;
		}
		final int xDelta = 1;
		final int yDelta = 10;

		final int left = getValue(pos - xDelta);
		final int right = getValue(pos + xDelta);
		final int up = getValue(pos - yDelta);
		final int down = getValue(pos + yDelta);

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
	 * to check that it has valid neighbors.
	 * 
	 * @return true if no position has all invalid neighbors.
	 */
	private boolean isBoardCorrect() {
		final int rowOffset = 10;
		for (int i = 1; i <= COL_SIZE; i++) {
			for (int j = 1; j <= ROW_SIZE; j++) {
				if (!this.hasValidNeighbors(i * rowOffset + j)) {
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
	 * Loads the specified Puzzle. Will reset the timer if restartTimer is true
	 * 
	 * @param newPuzzle
	 *            the puzzle to load
	 * @param restartTimer
	 *            whether or not to restart the timer
	 */
	private void loadPuzzle(final int newPuzzle, final boolean restartTimer) {
		Log.d(LOG_TAG, "loadPuzzle(" + newPuzzle + ", " + restartTimer + ")");
		this.currentPuzzle = newPuzzle;
		this.puzzleLabel.setText(String.format(Locale.US, getResources()
				.getString(R.string.puzzle_s), newPuzzle));
		if (newPuzzle > this.maxPuzzleAttempted) {
			this.maxPuzzleAttempted = newPuzzle;
			invalidateOptionsMenu(); // to add the new puzzle to the menu
		}
		resetAndInitializeBoard();
		if (restartTimer) {
			this.elapsedTime = 0;
			resumeTimer();
		}
		if (!this.timerRunning) {
			pauseTimer();
		}
	}

	private void pauseTimer() {
		if (this.timerRunning) {
			setElapsedTime();
		}
		if (!this.isBoardCorrect()) {
			// hide the board only if we're not done with it
			this.gameBoard.setVisibility(View.INVISIBLE);
		}
		this.timerRunning = false;
		Log.d(LOG_TAG, "pauseTimer(). elapsed: " + this.elapsedTime);
		this.timer.stop();
		this.pauseResume.setImageResource(android.R.drawable.ic_media_play);
		Log.d(LOG_TAG, "calling setupInputButtons() from pauseTimer()");
		setupInputButtons();
	}

	private void performFeedback(final boolean hasWon) {
		Log.d(LOG_TAG, "performFeedback(" + hasWon + ")");
		final int prefFeedback = Integer.parseInt(this.preferences.getString(
				SettingsActivity.KEY_PREF_FEEDBACK, "0"));
		switch (prefFeedback) {
		case SettingsActivity.FEEDBACK_AURAL:
			Log.d(LOG_TAG, "music play goes here");
			// @formatter:off
			/*
			MediaPlayer mp;
			if (hasWon) {
				mp = MediaPlayer.create(this, R.raw.foo);
			} else {
				mp = MediaPlayer.create(this, R.raw.bar);
			}
			mp.start();
			*/
			// @formatter:on
			break;
		case SettingsActivity.FEEDBACK_HAPTIC:
			Log.d(LOG_TAG, "BZZZTT!!1!");
			final int shortLength = 1000;
			final int longLength = 2000;
			final Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			if (hasWon) {
				vibe.vibrate(shortLength);
			} else {
				vibe.vibrate(longLength);
			}
			break;
		default:
		}
	}

	private void resetAndInitializeBoard() {
		Log.d(LOG_TAG, "resetAndInitializeBoard()");
		for (int i = 1; i <= COL_SIZE; i++) {
			for (int j = 1; j <= ROW_SIZE; j++) {
				final Button button = this.getGameButton("" + i + j);
				button.setText("");
				button.setEnabled(true);
			}
		}
		this.boardState.clear();
		this.rangeSpinner.setSelection(0);
		this.currentInput = BAD_VALUE;

		final Puzzle puzzle = PuzzleFactory.getPuzzle(this.currentPuzzle);
		final int[] pos = puzzle.getPositions();
		final int[] vals = puzzle.getValues();

		for (int i = 0; i < pos.length; i++) {
			final Button button = this.getGameButton(String.valueOf(pos[i]));
			button.setText(String.valueOf(vals[i]));
			button.setEnabled(false);
			this.boardState.put(pos[i], vals[i]);
		}

		Log.d(LOG_TAG,
				"calling setupInputButtons() from resetAndInitializeBoard()");
		setupInputButtons();
	}

	private void resumeTimer() {
		Log.d(LOG_TAG, "resumeTimer(); elapsed: " + this.elapsedTime);
		this.timer.setBase(SystemClock.elapsedRealtime() - this.elapsedTime);
		this.timer.start();
		this.timerRunning = true;
		this.pauseResume.setImageResource(android.R.drawable.ic_media_pause);
		this.gameBoard.setVisibility(View.VISIBLE);
		Log.d(LOG_TAG, "calling setupInputButtons() from resumeTimer()");
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
		final int pos = Integer.parseInt(button.getTag().toString());
		final int oldValue = getValue(pos);

		if (autoFillIn && this.currentInput == BAD_VALUE && oldValue == BAD_VALUE) {
			int[] options = getValidOptions(pos);
			if (options.length == 1) {
				this.currentInput = options[0];
			} else if (options.length > 1) {
				Toast t = Toast.makeText(this, arrayToString(options),
						Toast.LENGTH_SHORT);
				t.show();
			}
		}

		if (this.currentInput == BAD_VALUE) {
			button.setText("");
			this.boardState.delete(pos);
		} else {
			button.setText(String.valueOf(this.currentInput));
			this.boardState.put(pos, this.currentInput);
		}
		this.currentInput = oldValue;
		Log.d(LOG_TAG, "calling setupInputButtons() from setGameButtonText()");
		setupInputButtons();
	}

	private String arrayToString(int[] array) {
		String out = "[";
		for (final int i : array) {
			out += " " + i;
		}
		return out + " ]";
	}

	/**
	 * Sets the text on the input Buttons based on the current range. Buttons
	 * whose freshly-set text appears on the game board will be disabled. If the
	 * timer is paused, disables all Buttons.
	 */
	private void setupInputButtons() {
		Log.d(LOG_TAG, "setupInputButtons()");
		final int range = this.currentRange;
		final Button[] inputs = this.inputButtons;
		final boolean timerActive = this.timerRunning;

		for (int i = 0; i < NUM_INPUT_BUTTONS; i++) {
			final int value = NUM_INPUT_BUTTONS * range + i + 1;
			final Button inputButton = inputs[i];
			inputButton.setText(String.valueOf(value));
			inputButton.setEnabled(timerActive && !isValueOnBoard(value));
		}
	}

	private boolean isValueOnBoard(final int value) {
		return this.boardState.indexOfValue(value) >= 0;
	}

	private void setupPreferences() {
		this.preferences = PreferenceManager.getDefaultSharedPreferences(this);
		this.preferences.registerOnSharedPreferenceChangeListener(this);
		this.gameBoard.setBackgroundColor(this.preferences.getInt(
				SettingsActivity.KEY_PREF_CELL_COLOR,
				SettingsActivity.PREF_CELL_COLOR_DEFAULT));
		this.prefNumberColor = this.preferences.getInt(
				SettingsActivity.KEY_PREF_NUMBER_COLOR,
				SettingsActivity.PREF_NUMBER_COLOR_DEFAULT);
		this.changeButtonTextColor();
		this.autoFillIn = this.preferences.getBoolean(
				SettingsActivity.KEY_PREF_AUTO_FILLIN,
				SettingsActivity.PREF_AUTO_FILLIN_DEFAULT);
	}

	private void setupViews() {
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
	}
}
