package uk.co.paulcowie.sunshine;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ShareActionProvider;
import android.widget.TextView;

public class DetailActivity extends Activity {

    private static String weatherText;
    private final String LOG_TAG = DetailActivity.class.getSimpleName();
    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();

        }

        Intent intent = this.getIntent();
        weatherText = intent.getStringExtra(Intent.EXTRA_TEXT);


    }

    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    private Intent createIntent() {
        Log.v(LOG_TAG, "Dun");
        TextView weatherTextView = (TextView) findViewById(R.id.weather_data_text);
        String weatherText = weatherTextView.getText() + " #SunShineApp";
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, weatherText);
        Log.v(LOG_TAG, weatherText);
        shareIntent.setType("text/plain");
        return shareIntent;

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        MenuItem menuItemSharer = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) menuItemSharer.getActionProvider();
        mShareActionProvider.setShareIntent(createIntent());


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent().setClass(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        if (id == R.id.action_share) {

            return true;


        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);


            TextView weatherTextView = (TextView) rootView.findViewById(R.id.weather_data_text);
            weatherTextView.setText(weatherText);


            return rootView;
        }


    }
}
