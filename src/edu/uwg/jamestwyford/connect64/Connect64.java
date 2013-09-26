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

/**
 * Activity for the 8x8 game grid.
 * 
 * @author jtwyford
 * @version assignment3
 */
public class Connect64 extends Activity {
	/**
	 * debug id tag.
	 */
	public static final String LOG_TAG = "C64";
	private TableLayout grid;
	private String clickedButton = "";

	/**
	 * Input handler for the 16 input buttons.
	 * 
	 * @param view
	 *            the button clicked
	 */
	public void inputButtonClick(View view) {
		Button button = (Button) view;
		this.clickedButton = button.getText().toString();
	}

	/**
	 * Input handler for the 64 game grid buttons.
	 * 
	 * @param view
	 *            the button clicked
	 */
	public void gameButtonClick(View view) {
		Button button = (Button) view;
		Log.d(LOG_TAG, "" + button.getTag());
		button.setText(this.clickedButton);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.connect64, menu);
		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connect64);
		this.grid = (TableLayout) findViewById(R.id.connect64);

		Spinner rangeSpinner = (Spinner) findViewById(R.id.rangeSpinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.ranges, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		rangeSpinner.setAdapter(adapter);
		rangeSpinner
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int pos, long id) {
						setupInputButtons(pos);
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
					}
				});
		
		initializePuzzle();
	}

	private void setupInputButtons(int range) {
		TableLayout inputGrid = (TableLayout) findViewById(R.id.inputButtons);
		for (int i = 1; i <= 16; i++) {
			((Button) inputGrid.findViewWithTag("in" + i)).setText(""
					+ (16 * range + i));
		}
	}

	private void initializePuzzle() {
		int[] positions = { 1, 8, 64, 57, 15, 19, 46, 36 };
		int[] values    = { 1, 8, 15, 22, 34, 49, 55, 64 };

		for (int i = 0; i < positions.length; i++) {
			Log.d(LOG_TAG, "Position: " + positions[i] + " Value: " + values[i]);
			Button button = ((Button) this.grid.findViewWithTag("" + positions[i]));
			button.setText("" + values[i]);
			button.setEnabled(false);
		}
	}

}
