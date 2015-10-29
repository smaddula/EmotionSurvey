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

    public void onVisualizationsLatestClick(View view) throws ParseException {
        ParseUser currentUser = ParseUser.getCurrentUser() ;
        ParseQuery<ParseObject> query = ParseQuery.getQuery("SurveyData");
        query.whereEqualTo("UserID", currentUser);
        query.orderByDescending("createdAt");
        ParseObject result = query.getFirst();
        String url = "http://emotion-maddula.rhcloud.com/survey/"+result.getObjectId();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    public void onStartSurveyClick(View view){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Survey");
        query.whereEqualTo("isTestSurvey",false);
        query.whereEqualTo("isValid",true);
        query.setLimit(1);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objectList, com.parse.ParseException e) {
                if (e == null) {
                    String surveyId = objectList.get(0).getObjectId();
                    Intent intent = new Intent(UserPickActivity.this,
                            CacheImages.class);
                    intent.putExtra("SurveyId", surveyId);
                    intent.putExtra("isTestSurvey", false);
                    startActivity(intent);
                } else {
                    // something went wrong
                }
            }
        });
    }

    public void onPracticeSurveyClick(View view){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Survey");
        query.whereEqualTo("isTestSurvey",true);
        query.whereEqualTo("isValid",true);
        query.setLimit(1);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objectList, com.parse.ParseException e) {
                if (e == null) {
                    String surveyId = objectList.get(0).getObjectId();
                    Intent intent = new Intent(UserPickActivity.this,
                            CacheImages.class);
                    intent.putExtra("SurveyId", surveyId);
                    intent.putExtra("isTestSurvey", true);
                    startActivity(intent);
                } else {
                    // something went wrong
                }
            }
        });
    }
}
