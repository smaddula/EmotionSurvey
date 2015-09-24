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

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

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
        Intent intent = new Intent(UserPickActivity.this,
                MainActivity.class);
        startActivity(intent);
    }

    public void onPracticeSurveyClick(View view){
        Intent intent = new Intent(UserPickActivity.this,
                MainActivity.class);
        startActivity(intent);
    }
}
