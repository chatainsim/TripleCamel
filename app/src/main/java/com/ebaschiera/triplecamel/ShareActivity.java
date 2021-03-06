package com.ebaschiera.triplecamel;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.content.pm.*;
import android.widget.Toast;

import org.piwik.sdk.TrackHelper;
import org.piwik.sdk.Tracker;

import java.util.regex.*;
import java.util.List;


/**
 * Created by ebaschiera on 01/01/15.
 */
public class ShareActivity extends Activity {

    private Tracker getTracker() {
        return ((TripleCamelApp) getApplication()).getTracker();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TrackHelper.track().screen("/share").title("Share intent").with(getTracker());

        // Get the intent that started this activity
        Intent intent = getIntent();
        Uri data = intent.getData();

        String amazon_share_text = intent.getStringExtra(Intent.EXTRA_TEXT);
        //Log.d("triple", amazon_share_text);
        if (amazon_share_text.matches(".*http[s]?://www\\.amazon\\.(?:com|ca|cn|de|es|fr|in|it|co\\.jp|co\\.uk)/.*")) {
            //Log.d("triple", "It matches!");
            Pattern p = Pattern.compile(".*(http[s]?://www\\.amazon\\.(?:com|ca|cn|de|es|fr|in|it|co\\.jp|co\\.uk|com)/.*)");
            Matcher m = p.matcher(amazon_share_text);
            String amazon_share_url = "";
            while (m.find()) { // Find each match in turn; String can't do this.
                amazon_share_url = m.group(1); // Access a submatch group; String can't do this.
            }
            if (amazon_share_url == "") {
                //return a warning and stop the intent
                TrackHelper.track().event("share", "failed").name("label").value(1f).with(getTracker());
                Context context = getApplicationContext();
                String text = getResources().getString(R.string.amazon_link_not_matching);
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                finish();
            }
            amazon_share_url = Uri.encode(amazon_share_url);
            Uri camel_search_uri = Uri.parse("http://camelcamelcamel.com/search?sq=" + amazon_share_url);
            //Log.d("triple", amazon_share_url);
            //Log.d("new_intent", camel_search_uri.toString());
            Intent webIntent = new Intent(Intent.ACTION_VIEW, camel_search_uri);

            // Verify it resolves
            PackageManager packageManager = getPackageManager();
            List<ResolveInfo> activities = packageManager.queryIntentActivities(webIntent, 0);
            boolean isIntentSafe = activities.size() > 0;

            // Start an activity if it's safe
            if (isIntentSafe) {
                TrackHelper.track().event("share", "successful").name("label").value(1f).with(getTracker());
                startActivity(webIntent);
                finish();
            } else {
                TrackHelper.track().event("share", "failed").name("label").value(1f).with(getTracker());
                Context context = getApplicationContext();
                String text = getResources().getString(R.string.no_web_browser);
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                finish();
            }
        } else {
            //return a warning and stop the intent
            TrackHelper.track().event("share", "failed").name("label").value(1f).with(getTracker());
            Context context = getApplicationContext();
            String text = getResources().getString(R.string.amazon_link_not_matching);
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            finish();
        }



    }
}
