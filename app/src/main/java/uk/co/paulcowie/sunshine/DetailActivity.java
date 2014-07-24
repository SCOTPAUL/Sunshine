package uk.co.paulcowie.sunshine;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ShareActionProvider;
import android.widget.TextView;

public class DetailActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();

        }


    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment {

        //Identifier used for creating logs
        private final String LOG_TAG = DetailActivity.class.getSimpleName();
        private TextView weatherTextView;
        private String weatherText;
        private View rootView;
        private ShareActionProvider mShareActionProvider;

        public DetailFragment() {

        }

        @Override
        public void onCreate(Bundle savedInstance) {
            //Get the extra text data held by the intent which created the activity.
            super.onCreate(savedInstance);
            Intent intent = getActivity().getIntent();
            weatherText = intent.getStringExtra(Intent.EXTRA_TEXT);
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_detail, container, false);


            //Set the textview to hold the weatherText sent by the intent which was sent to
            //the activity
            weatherTextView = (TextView) rootView.findViewById(R.id.weather_data_text);
            weatherTextView.setText(weatherText);


            return rootView;
        }

        private void setShareIntent(Intent shareIntent) {
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(shareIntent);
            }
        }

        private Intent createWeatherTextIntent() {
            //Create an implicit share intent to send to other apps
            TextView weatherTextView = (TextView) rootView.findViewById(R.id.weather_data_text);

            //The contents of the shared text
            String weatherText = weatherTextView.getText() + " #SunshineApp";

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.putExtra(Intent.EXTRA_TEXT, weatherText);
            shareIntent.setType("text/plain");
            return shareIntent;

        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
            menuInflater.inflate(R.menu.detailfragment, menu);
            MenuItem menuItemSharer = menu.findItem(R.id.action_share);
            mShareActionProvider = (ShareActionProvider) menuItemSharer.getActionProvider();

            //Set the share action provider to share the text generated by createWeatherTextIntent
            mShareActionProvider.setShareIntent(createWeatherTextIntent());


        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();
            if (id == R.id.action_settings) {
                Intent settingsIntent = new Intent().setClass(getActivity(), SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            }
            if (id == R.id.action_share) {

                return true;


            }
            return super.onOptionsItemSelected(item);


        }
    }
}
