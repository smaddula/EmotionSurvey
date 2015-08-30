package sid.expressionsurveyaffectiva;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by siddardha on 8/4/2015.
 */
public class Question extends Activity {
    public String ImageURI;
    public String QuestionHeading;
    Question(String imageUri , String questionHeading ){
        ImageURI = imageUri;
        QuestionHeading = questionHeading;
    }
}
