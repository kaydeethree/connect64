package edu.uwg.jamestwyford.connect64;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 * 
 * @author jtwyford
 * @version assignment3
 */
public class SettingsActivity extends PreferenceActivity {

	/** key for the "cell color" preference. */
	public static final String KEY_PREF_CELL_COLOR = "pref_cell_color";
	/** default value for the "cell color" preference. */
	public static final int PREF_CELL_COLOR_DEFAULT = 0xFFFFFFFF;
	/** key for the "text color" preference. */
	public static final String KEY_PREF_NUMBER_COLOR = "pref_number_color";
	/** default value for the "text color" preference. */
	public static final int PREF_NUMBER_COLOR_DEFAULT = 0xFF000000;
	/** key for the "feedback" (aural or haptic) preference. */
	public static final String KEY_PREF_FEEDBACK = "pref_feedback";

	/** default value for the "feedback" (aural or haptic) preference. */
	public static final String PREF_FEEDBACK_DEFAULT = "0";
	/** value for Feedback: "None". */
	public static final int FEEDBACK_NONE = 0;
	/** value for Feedback: "Aural". */
	public static final int FEEDBACK_AURAL = 1;

	/** value for Feedback: "Haptic". */
	public static final int FEEDBACK_HAPTIC = 2;

	/**
	 * Determines whether to always show the simplified settings UI, where
	 * settings are presented in a single list. When false, settings are shown
	 * as a master/detail two-pane view on tablets. When true, a single pane is
	 * shown on tablets.
	 */
	private static final boolean ALWAYS_SIMPLE_PREFS = false;

	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(final Preference preference,
				final Object value) {
			final String stringValue = value.toString();

			if (preference instanceof ListPreference) {
				final ListPreference listPreference = (ListPreference) preference;
				final int index = listPreference.findIndexOfValue(stringValue);
				preference.setSummary(listPreference.getEntries()[index]);
			} else {
				preference.setSummary(stringValue);
			}
			return true;
		}
	};

	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 * 
	 * @param preference
	 *            the Preference changed
	 * @see #sBindPreferenceSummaryToValueListener
	 */
	private static void bindPreferenceSummaryToValue(final Preference preference) {
		// Set the listener to watch for value changes.
		preference
				.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

		// Trigger the listener immediately with the preference's
		// current value.
		sBindPreferenceSummaryToValueListener.onPreferenceChange(
				preference,
				PreferenceManager.getDefaultSharedPreferences(
						preference.getContext()).getString(preference.getKey(),
						""));
	}

	/**
	 * Determines whether the simplified settings UI should be shown. This is
	 * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
	 * doesn't have newer APIs like {@link PreferenceFragment}, or the device
	 * doesn't have an extra-large screen. In these cases, a single-pane
	 * "simplified" settings UI should be shown.
	 * 
	 * @param context
	 *            the activity context
	 * @return true if the simplified settings UI should be shown
	 */
	private static boolean isSimplePreferences(final Context context) {
		return ALWAYS_SIMPLE_PREFS
				|| Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
				|| !isXLargeTablet(context);
	}

	/**
	 * Helper method to determine if the device has an extra-large screen. For
	 * example, 10" tablets are extra-large.
	 * 
	 * @param context
	 *            the activty context
	 * 
	 * @return true if the device has an extra-large scren
	 */
	private static boolean isXLargeTablet(final Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setupActionBar();
	}

	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	@Override
	public final boolean onIsMultiPane() {
		return isXLargeTablet(this) && !isSimplePreferences(this);
	}

	@Override
	public final boolean onOptionsItemSelected(final MenuItem item) {
		final int id = item.getItemId();
		if (id == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
			return true;
		} else if (id == R.id.defaults) {
			setDefaults();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected final void onPostCreate(final Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		setupSimplePreferencesScreen();
	}

	private void setDefaults() {
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		final SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(KEY_PREF_CELL_COLOR, PREF_CELL_COLOR_DEFAULT);
		editor.putInt(KEY_PREF_NUMBER_COLOR, PREF_NUMBER_COLOR_DEFAULT);
		editor.putString(KEY_PREF_FEEDBACK, PREF_FEEDBACK_DEFAULT);
		editor.apply();
		Toast toast = Toast.makeText(this.getBaseContext(),R.string.prefs_reset,Toast.LENGTH_SHORT);
		toast.show();
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// Show the Up button in the action bar.
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	/**
	 * Shows the simplified settings UI if the device configuration if the
	 * device configuration dictates that a simplified, single-pane UI should be
	 * shown.
	 */
	@SuppressWarnings("deprecation")
	private void setupSimplePreferencesScreen() {
		if (!isSimplePreferences(this)) {
			return;
		}
		addPreferencesFromResource(R.xml.pref_general);
	}
	/**
	 * This fragment shows general preferences only. It is used when the
	 * activity is showing a two-pane settings UI.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class GeneralPreferenceFragment extends PreferenceFragment {
		@Override
		public final void onCreate(final Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_general);

			bindPreferenceSummaryToValue(findPreference(KEY_PREF_CELL_COLOR));
			bindPreferenceSummaryToValue(findPreference(KEY_PREF_NUMBER_COLOR));
			bindPreferenceSummaryToValue(findPreference(KEY_PREF_FEEDBACK));
		}
	}
}
