package sid.expressionsurveyaffectiva;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.Parse;
import com.parse.ParseObject;

/**
 * Created by siddardha on 8/4/2015.
 */
public class Question extends Activity {
    public String ImageURI;
    public String QuestionHeading;
    public transient ParseObject parseQuestionData;
    Question(String imageUri , String questionHeading, ParseObject question){
        ImageURI = imageUri;
        QuestionHeading = questionHeading;
        parseQuestionData = question;
    }
}
