package sid.expressionsurveyaffectiva;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;


import com.affectiva.android.affdex.sdk.Frame;
import com.affectiva.android.affdex.sdk.Frame.ROTATE;
import com.affectiva.android.affdex.sdk.detector.CameraDetector;
import com.affectiva.android.affdex.sdk.detector.Detector;
import com.affectiva.android.affdex.sdk.detector.Face;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.parse.FindCallback;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import sid.UserSurveyData.FrameInformation;
import sid.UserSurveyData.FullSurveyData;
import sid.UserSurveyData.TimeStampFrameInformationPair;

public class MainActivity extends Activity
        implements Detector.FaceListener, Detector.ImageListener
{
    static int segmentId;
    static String APP_UUID = UUID.randomUUID().toString();
    static String FolderPath;
    FullSurveyData userData = new FullSurveyData();
    List<Question> allQuestions = new ArrayList<Question>();
    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").serializeSpecialFloatingPointValues().create();
    Question currentQuestion;
    RadioGroup valenceRadioGroup ;

    ViewPager mViewPager;

    private SurfaceView cameraPreview;
    private CameraDetector detector;


    @Override
    public void onFaceDetectionStarted() {

    }

    @Override
    public void onFaceDetectionStopped() {
        return;
    }

    @Override
    public void onImageResults(List<Face> faces, Frame image, float timeStamp) {
        if (faces == null) {
            //Log.v(LOG_TAG, "Got unprocessed frame");
            return;
        }
        if (faces.size() == 0) {
            //Log.v(LOG_TAG, "No face found");
            return;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        // If rotated by 90, then facial feature points are in a different coordinate space.
        // Width swapped with height from original image.
        if ((ROTATE.BY_90_CW == image.getTargetRotation()) || (ROTATE.BY_90_CCW == image.getTargetRotation())) {
            height = image.getWidth();
            width = image.getHeight();
        }

        Face face = faces.get(0);
        userData.AddFrameData(currentQuestion, new FrameInformation( face ));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Enable Local Datastore.

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        valenceRadioGroup = (RadioGroup) findViewById( R.id.valenceRadioGroup);

        segmentId = 0 ;
        FolderPath = getExternalFilesDir(null) + File.separator + APP_UUID;

        cameraPreview = (SurfaceView) findViewById(R.id.cameraId);
        // Put the SDK in camera mode by using this constructor. The SDK will be in control of
        // the camera. If a SurfaceView is passed in as the last argument to the constructor,
        // that view will be painted with what the camera sees.
        detector = new CameraDetector(this, CameraDetector.CameraType.CAMERA_FRONT, cameraPreview);

        detector.setLicensePath("sdk_kusuma.chunduru@gmail.com.license");

        detector.setMaxProcessRate(20);

        detector.setImageListener(this);
        detector.setFaceListener(this);

        detector.setDetectAllEmotions(true);
        detector.setDetectAllExpressions(true);

        detector.start();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Question");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objectList, com.parse.ParseException e) {
                if (e == null) {
                    // object will be your game score
                    for (ParseObject obj : objectList) {
                        allQuestions.add(new Question(obj.getString("ImageURI"), obj.getString("Title"),obj));
                    }
                    loadData();
                } else {
                    // something went wrong
                }
            }
        });

    }

    public void FinishServingQuestion(){
        RadioButton rb = (RadioButton) findViewById( valenceRadioGroup.getCheckedRadioButtonId() );
        userData.setUserInput(Integer.parseInt(rb.getTag().toString()));
        valenceRadioGroup.clearCheck();
    }

    public void loadNextQuestion( View view ){
        if(valenceRadioGroup.getCheckedRadioButtonId() == -1)
            return;
        FinishServingQuestion();
        loadData();
    }

    @Override
    public void onBackPressed(){
        //Dont want to do anything when back is pressed
    }

    public void SaveData(View view) throws IOException, com.parse.ParseException {
        FinishServingQuestion();
        String result = gson.toJson(userData);
        ParseFile emotionFrameData = new ParseFile("FrameEmotionData.txt", gson.toJson(userData).getBytes());
        emotionFrameData.save();

        ParseObject userSurvey = new ParseObject("SurveyData");
        userSurvey.put("UserID",ParseUser.getCurrentUser());
        userSurvey.put("JsonEmotionData",emotionFrameData);
        userSurvey.save();
        Intent intent = new Intent(MainActivity.this,
                UserPickActivity.class);
        startActivity(intent);
        finish();
    }

    public void loadData(  )
    {
        if(allQuestions.size()==0)
            return;
        currentQuestion = allQuestions.get( segmentId );
        ImageView imageView = ((ImageView) findViewById(R.id.image));
        new DownloadImageTask(imageView)
                .execute(currentQuestion.ImageURI);
        ((TextView)findViewById(R.id.text_view)).setText( currentQuestion.QuestionHeading);

        if(segmentId == allQuestions.size() - 1)
        {
            ((Button)findViewById(R.id.lastQuestionSave)).setVisibility(View.VISIBLE);
            ((Button)findViewById(R.id.nextQuestion)).setVisibility(View.GONE);
            segmentId = 0;
        } else
        {
            ((Button)findViewById(R.id.lastQuestionSave)).setVisibility(View.GONE);
            ((Button)findViewById(R.id.nextQuestion)).setVisibility(View.VISIBLE);
            segmentId++;
        }
    }
}
