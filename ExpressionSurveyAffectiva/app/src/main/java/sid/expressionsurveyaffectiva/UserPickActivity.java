package sid.expressionsurveyaffectiva;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class UserPickActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_pick);
        TextView userBanner = (TextView)findViewById(R.id.loggedInUserName);
        userBanner.setText("Logged in as "+ParseUser.getCurrentUser().getUsername());
    }
    public void onLogOffClick(View view){
        ParseUser.logOut();
        // FLAG_ACTIVITY_CLEAR_TASK only works on API 11, so if the user
        // logs out on older devices, we'll just exit.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Intent intent = new Intent(UserPickActivity.this,
                    MainDispatchActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            finish();
        }
    }

    public void onStartSurveyClick(View view) {

        Boolean doneSurveyAlready = ParseUser.getCurrentUser().getBoolean("startedSurvey");

        if(doneSurveyAlready){
            Toast.makeText(this.getApplicationContext(), "Logging off since you have already started the survey before", Toast.LENGTH_LONG).show();
            onLogOffClick(null);
            return;
        }

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Survey");
        query.whereEqualTo("isValid", true);
        query.setLimit(1);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objectList, com.parse.ParseException e) {
                if (e == null) {
                    String surveyId = objectList.get(0).getObjectId();
                    Intent intent = new Intent(UserPickActivity.this,
                            ConfirmationStart.class);
                    intent.putExtra("SurveyId", surveyId);
                    startActivity(intent);
                } else {
                    // something went wrong
                }
            }
        });
    }
}
