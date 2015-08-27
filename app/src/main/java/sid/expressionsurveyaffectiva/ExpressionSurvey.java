package sid.expressionsurveyaffectiva;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseTwitterUtils;

/**
 * Created by siddardha on 8/22/2015.
 */
public class ExpressionSurvey extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Required - Initialize the Parse SDK
        Parse.initialize(this);

        Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);

    }
}
