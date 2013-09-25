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
	public final static String LOG_TAG = "C64";
	private TableLayout grid;

	public void gameButtonClick(View view) {
		Button button = (Button) view;
		Log.d(LOG_TAG, "" + button.getTag());
		button.setText(button.getTag().toString());
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

		grid = (TableLayout) findViewById(R.id.connect64);
		((Button) findViewById(R.id.button1)).setText("1");
		((Button) findViewById(R.id.button11)).setText("11");
		((Button) grid.findViewWithTag("5")).setText("5");
		((Button) grid.findViewWithTag("16")).setText("16");
		((Button) grid.findViewWithTag("60")).setText("60");

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
	}

	private void setupInputButtons(int range) {
		TableLayout inputGrid = (TableLayout) findViewById(R.id.inputButtons);
		for (int i = 1; i <= 16; i++) {
			((Button) inputGrid.findViewWithTag("in" + i)).setText(""
					+ (16 * range + i));
		}
	}

}
