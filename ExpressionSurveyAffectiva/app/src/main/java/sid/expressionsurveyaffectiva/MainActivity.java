package sid.expressionsurveyaffectiva;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;


import com.affectiva.android.affdex.sdk.Frame;
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

public class MainActivity extends Activity
        implements Detector.FaceListener, Detector.ImageListener
{
    boolean firstImageLoaded = false;
    long lastImageSavedMillisecond = 0;
    int questionIterator;
    int numberOfFilesUploaded = 0;
    int numberOfFilesToUpload = 0 ;
    boolean surveyComplete = false;
    String surveyImagesDeviceDirectory ;
    String SurveyImagesS3Directory ;
    boolean questionMotorActionPerformed = false;
    FullSurveyData userData ;
    UploadImagesBackgroundTask s3upload ;
    List<Question> allQuestions = new ArrayList<Question>();
    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").serializeSpecialFloatingPointValues().create();
    Question currentQuestion = null;
    RadioGroup valenceRadioGroup ;
    LinearLayout surfaceViewContainer , footerButtonsContainer , uploadProgressBarContainer;
    ProgressBar uploadProgressBar;

    private SurfaceView cameraPreview;
    private CameraDetector detector;


    @Override
    public void onFaceDetectionStarted() {
        if(surfaceViewContainer!=null && footerButtonsContainer!=null){
            cameraPreview.setLayoutParams(new LinearLayout.LayoutParams(50, 50));
            footerButtonsContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onFaceDetectionStopped() {
        if(surfaceViewContainer!=null && footerButtonsContainer!=null){
            cameraPreview.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            footerButtonsContainer.setVisibility(View.GONE);
        }
        return;
    }

    @Override
    public void onImageResults(List<Face> faces, Frame image, float timeStamp) {

        if(currentQuestion == null)
            return;

        if (faces == null) {
            //Log.v(LOG_TAG, "Got unprocessed frame");
            return;
        }
        if (faces.size() == 0) {
            //Log.v(LOG_TAG, "No face found");
            return;
        }
        //TODO:If saving is slowing the frame rate use a ThreadPool and move the saving logic to a different class

        String UserFaceImageName = "";
        long currentMillisecond = System.currentTimeMillis();
        if ( !questionMotorActionPerformed && currentMillisecond - lastImageSavedMillisecond > 200) {
            //Try saving image every 500 millisecond
            lastImageSavedMillisecond = currentMillisecond;

            UserFaceImageName = Long.toString(System.nanoTime()) + ".jpg";
            numberOfFilesToUpload++;
            s3upload.push(image, UserFaceImageName);
        }

        Face face = faces.get(0);

        userData.AddFrameData(currentQuestion, new FrameInformation( face , UserFaceImageName , questionMotorActionPerformed ));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Date currentDate = new Date();
        SurveyImagesS3Directory = ParseUser.getCurrentUser().getUsername()+ "_" +new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss").format(currentDate) ;
        surveyImagesDeviceDirectory = getExternalFilesDir(null).getAbsolutePath() + File.separator + new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss").format(currentDate);

        userData = new FullSurveyData("https://s3.amazonaws.com/surveyfacesnaps/"+SurveyImagesS3Directory+"/");

        surfaceViewContainer = (LinearLayout)findViewById(R.id.surfaceViewContainer);
        footerButtonsContainer = (LinearLayout)findViewById(R.id.footerButtonContainer);
        uploadProgressBarContainer = (LinearLayout)findViewById(R.id.layoutUploadProgress);
        uploadProgressBar = (ProgressBar)findViewById(R.id.uploadProgressBar);


        valenceRadioGroup = (RadioGroup) findViewById( R.id.valenceRadioGroup);

        s3upload = new UploadImagesBackgroundTask( new IUploadedImageEvent() {
            @Override
            public void callback(int completedUploads) {
                numberOfFilesUploaded = completedUploads;
                uploadProgressBar.setMax(numberOfFilesToUpload);
                uploadProgressBar.setProgress(numberOfFilesUploaded);
                if(numberOfFilesUploaded == numberOfFilesToUpload && surveyComplete ){
                    uploadComplete();
                }
            }
        } , this , surveyImagesDeviceDirectory , SurveyImagesS3Directory );

        Thread uploaderThread = new Thread(s3upload);
        uploaderThread.start();

        questionIterator = 0 ;
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
        if(!surveyComplete)
            valenceRadioGroup.clearCheck();
        else
            userData.DoneSurvey();
    }

    public void loadNextQuestion( View view ){
        if(valenceRadioGroup.getCheckedRadioButtonId() == -1)
            return;

        loadData();
    }

    public void onRadioButtonClicked(View view){
        questionMotorActionPerformed = true;
    }

    @Override
    public void onBackPressed(){
        //Dont want to do anything when back is pressed
    }

    public void SaveData(View view) throws IOException, com.parse.ParseException {
        detector.stop();
        surveyComplete = true;
        s3upload.surveyComplete();
        FinishServingQuestion();
        String result = gson.toJson(userData);
        ParseFile emotionFrameData = new ParseFile("FrameEmotionData.txt", gson.toJson(userData).getBytes());
        emotionFrameData.save();

        ParseObject userSurvey = new ParseObject("SurveyData");
        userSurvey.put("UserID", ParseUser.getCurrentUser());
        userSurvey.put("JsonEmotionData", emotionFrameData);
        userSurvey.save();

        uploadProgressBarContainer.setVisibility(View.VISIBLE);
        footerButtonsContainer.setVisibility(View.GONE);

        uploadProgressBar.setMax(numberOfFilesToUpload);
        if(numberOfFilesToUpload == numberOfFilesUploaded){
            uploadComplete();
        }
    }

    public void uploadComplete(){

        //Delete the local directory
        /*File dir = new File(surveyImagesDeviceDirectory);
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(dir, children[i]).delete();
            }
            dir.delete();
        }*/

        //switch to a different view
        Intent intent = new Intent(MainActivity.this,
                UserPickActivity.class);
        startActivity(intent);
        finish();
    }

    public void loadData(  ) {
        if (allQuestions.size() == 0)
            return;
        ImageView imageView = ((ImageView) findViewById(R.id.image));

        new DownloadImageTask(new IDownloadedImageEvent() {
                @Override
                public void callback() {
                    questionMotorActionPerformed = false;
                    if(questionIterator!=0)
                        FinishServingQuestion();
                    currentQuestion = allQuestions.get(questionIterator);
                    ((TextView) findViewById(R.id.text_view)).setText(currentQuestion.QuestionHeading);


                    if(allQuestions.size() -1 == questionIterator){
                        ((Button) findViewById(R.id.lastQuestionSave)).setVisibility(View.VISIBLE);
                        ((Button) findViewById(R.id.nextQuestion)).setVisibility(View.GONE);
                    }else {
                        ((Button) findViewById(R.id.lastQuestionSave)).setVisibility(View.GONE);
                        ((Button) findViewById(R.id.nextQuestion)).setVisibility(View.VISIBLE);
                    }

                    questionIterator++;
                }
            }
            , imageView)
                .execute(allQuestions.get(questionIterator).ImageURI);

    }
}
