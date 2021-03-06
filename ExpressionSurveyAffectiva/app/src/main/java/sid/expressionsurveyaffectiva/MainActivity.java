package sid.expressionsurveyaffectiva;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import android.widget.Toast;


import com.affectiva.android.affdex.sdk.Frame;
import com.affectiva.android.affdex.sdk.detector.CameraDetector;
import com.affectiva.android.affdex.sdk.detector.Detector;
import com.affectiva.android.affdex.sdk.detector.Face;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Callback;

import sid.UserSurveyData.FrameInformation;
import sid.UserSurveyData.FullSurveyData;

public class MainActivity extends Activity
        implements Detector.FaceListener, Detector.ImageListener {
    boolean savedDataToParse = false;
    long lastImageSavedNano = 0;
    int questionIterator;
    int numberOfFilesUploaded = 0;
    int numberOfFilesToUpload = 0;
    HashMap<Question, Integer> imagesSavedPerQuestion;
    int maxImagesPerQuestion = 6;
    boolean surveyComplete = false;
    String surveyImagesDeviceDirectory;
    String SurveyImagesS3Directory;
    boolean questionMotorActionPerformed = false;
    FullSurveyData userData;
    UploadImagesBackgroundTask s3upload;
    List<Question> allQuestions = new ArrayList<Question>();
    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").serializeSpecialFloatingPointValues().create();
    Question currentQuestion = null;
    LinearLayout surfaceViewContainer, footerButtonsContainer, uploadProgressBarContainer, ValenceIntensityRadioContainer, imageContainer;
    ProgressBar uploadProgressBar;
    IntensityValenceUIManager intensityValenceUIManager;

    private SurfaceView cameraPreview;
    private CameraDetector detector;


    @Override
    public void onFaceDetectionStarted() {
        if (surfaceViewContainer != null && footerButtonsContainer != null) {
            imageContainer.setVisibility(View.VISIBLE);
            //surfaceViewContainer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
            cameraPreview.setLayoutParams(new LinearLayout.LayoutParams(50, 50));
            footerButtonsContainer.setVisibility(View.VISIBLE);
            ValenceIntensityRadioContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onFaceDetectionStopped() {
        if (surfaceViewContainer != null && footerButtonsContainer != null) {
            imageContainer.setVisibility(View.GONE);
            //surfaceViewContainer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT));
            cameraPreview.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            footerButtonsContainer.setVisibility(View.GONE);
            ValenceIntensityRadioContainer.setVisibility(View.GONE);
        }
        return;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Date currentDate = new Date();
        SurveyImagesS3Directory = ParseUser.getCurrentUser().getUsername() + "_" + new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss").format(currentDate);
        surveyImagesDeviceDirectory = getExternalFilesDir(null).getAbsolutePath() + File.separator + new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss").format(currentDate);

        userData = new FullSurveyData("https://s3.amazonaws.com/surveyfacesnaps/" + SurveyImagesS3Directory + "/");

        imagesSavedPerQuestion = new HashMap<Question, Integer>();

        surfaceViewContainer = (LinearLayout) findViewById(R.id.surfaceViewContainer);
        footerButtonsContainer = (LinearLayout) findViewById(R.id.footerButtonContainer);
        uploadProgressBarContainer = (LinearLayout) findViewById(R.id.layoutUploadProgress);
        uploadProgressBar = (ProgressBar) findViewById(R.id.uploadProgressBar);
        ValenceIntensityRadioContainer = (LinearLayout) findViewById(R.id.smileyLayout);
        imageContainer = (LinearLayout) findViewById(R.id.imageContainer);

        intensityValenceUIManager = new IntensityValenceUIManager(this);


        s3upload = new UploadImagesBackgroundTask(new IUploadedImageEvent() {
            @Override
            public void callback(int completedUploads) {
                numberOfFilesUploaded = completedUploads;
                uploadProgressBar.setMax(numberOfFilesToUpload);
                uploadProgressBar.setProgress(numberOfFilesUploaded);
                if (numberOfFilesUploaded == numberOfFilesToUpload && surveyComplete) {
                    uploadComplete();
                }
            }
        }, this, surveyImagesDeviceDirectory, SurveyImagesS3Directory);

        Thread uploaderThread = new Thread(s3upload);
        uploaderThread.start();
        questionIterator = 0;
        cameraPreview = (SurfaceView) findViewById(R.id.cameraId);

        // Put the SDK in camera mode by using this constructor. The SDK will be in control of
        // the camera. If a SurfaceView is passed in as the last argument to the constructor,
        // that view will be painted with what the camera sees.

        detector = new CameraDetector(this, CameraDetector.CameraType.CAMERA_FRONT, cameraPreview);
        detector.setLicensePath("sdk_parisa.rashidi@ufl.edu.license");
        detector.setMaxProcessRate(5);
        detector.setImageListener(this);
        detector.setFaceListener(this);
        detector.setDetectAllEmotions(true);
        detector.setDetectAllExpressions(true);

        detector.start();

        String surveyId = getIntent().getStringExtra("SurveyId");

        ParseQuery<ParseObject> innerquery = ParseQuery.getQuery("Survey");
        innerquery.whereEqualTo("objectId", surveyId);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("SurveyQuestions");
        query.whereMatchesQuery("SurveyId", innerquery);

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objectList, com.parse.ParseException e) {
                if (e == null) {
                    // object will be your game score
                    ParseObject questionObj;
                    for (ParseObject obj : objectList) {
                        try {
                            questionObj = obj.getParseObject("QuestionId");
                            questionObj.fetchIfNeeded();
                            allQuestions.add(new Question(questionObj.getString("ImageURI"), questionObj.getString("Title"), questionObj));
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                    }
                    loadData();
                    //If the control comes here then user had no issues starting the survey hence
                    // mark that user has seen the survey data and hence cannot start again
                    ParseUser.getCurrentUser().put("startedSurvey", true);
                } else {
                    // something went wrong
                }
            }
        });

    }


    @Override
    public void onImageResults(List<Face> faces, Frame image, float timeStamp) {

        if (currentQuestion == null)
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
        long currentNanosecond = System.nanoTime();
        if (!imagesSavedPerQuestion.containsKey(currentQuestion))
            imagesSavedPerQuestion.put(currentQuestion, 0);
        if (allQuestions.size() > 0 && !questionMotorActionPerformed) {
            if (currentNanosecond - lastImageSavedNano <= 300 * 1000000)
                Log.d("skipped saving image", "Not saving image since we have saved an imaged very recently");
        } else {
            if (imagesSavedPerQuestion.get(currentQuestion).compareTo(maxImagesPerQuestion) >= 0)
                Log.d("skipped saving image", "Not saving image since we have already saved " + Integer.toString(maxImagesPerQuestion) + " images for this question");
        }
        if (imagesSavedPerQuestion.get(currentQuestion).compareTo(maxImagesPerQuestion) < 0 && allQuestions.size() > 0 && !questionMotorActionPerformed && currentNanosecond - lastImageSavedNano > 400 * 1000000) {
            //Try saving image every 500 millisecond
            lastImageSavedNano = currentNanosecond;

            imagesSavedPerQuestion.put(currentQuestion, imagesSavedPerQuestion.get(currentQuestion) + 1);

            UserFaceImageName = Long.toString(System.nanoTime()) + ".jpg";
            numberOfFilesToUpload++;
            s3upload.push(image, UserFaceImageName);
        }

        Face face = faces.get(0);

        // Storing emotion data in collections which is uploaded at the end
        userData.AddFrameData(currentQuestion, new FrameInformation(face, UserFaceImageName, questionMotorActionPerformed));
    }


    public void FinishServingQuestion() {
        userData.setUserInput(intensityValenceUIManager.getValenceSelected().getIntValue(), intensityValenceUIManager.getIntensitySelected().getIntValue());
        if (!surveyComplete) {
        intensityValenceUIManager.resetRadioGroups();
        } else
            userData.DoneSurvey();
    }

    public void loadNextQuestion(View view) {

        if(!intensityValenceUIManager.intensityValenceSelected())
            return;

        loadData();
    }

    public void onIntensityRadioButtonClicked(View view) {
        questionMotorActionPerformed = true;
    }

    public void onValenceRadioButtonClicked(View view) {
        intensityValenceUIManager.updateIntensityRadioButtons();
        questionMotorActionPerformed = true;
    }

    @Override
    public void onBackPressed() {
        //Dont want to do anything when back is pressed
    }

    public void SaveData(View view) throws IOException, com.parse.ParseException {

        if(!intensityValenceUIManager.intensityValenceSelected())
            return;

        detector.stop();
        surveyComplete = true;
        FinishServingQuestion();

        final ParseFile emotionFrameData = new ParseFile("FrameEmotionData.txt", gson.toJson(userData).getBytes());
        emotionFrameData.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {

                    ParseObject userSurvey = new ParseObject("SurveyData");
                    userSurvey.put("UserID", ParseUser.getCurrentUser());
                    userSurvey.put("JsonEmotionData", emotionFrameData);
                    try {
                        userSurvey.save();
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                    savedDataToParse = true;
                    if (numberOfFilesToUpload == numberOfFilesUploaded) {
                        uploadComplete();
                    }
                }

            }
        });


        for(Question question : allQuestions){
            PicassoSingletonImageHandler.getSharedInstance(getApplicationContext()).invalidate(question.ImageURI);
        }

        s3upload.surveyComplete();
        uploadProgressBarContainer.setVisibility(View.VISIBLE);
        footerButtonsContainer.setVisibility(View.GONE);

        uploadProgressBar.setMax(numberOfFilesToUpload);
        if (numberOfFilesToUpload == numberOfFilesUploaded && savedDataToParse) {
            uploadComplete();
        }
    }

    public void uploadComplete() {

        if ( savedDataToParse) {
            //Delete the local directory
            File dir = new File(surveyImagesDeviceDirectory);
            if (dir.isDirectory()) {
                String[] children = dir.list();
                for (int i = 0; i < children.length; i++) {
                    new File(dir, children[i]).delete();
                }
                dir.delete();
            }

            Toast.makeText(this.getApplicationContext(), "Survey completed and you are being logged out", Toast.LENGTH_LONG).show();

            ParseUser.logOut();

            // FLAG_ACTIVITY_CLEAR_TASK only works on API 11, so if the user
            // logs out on older devices, we'll just exit.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                Intent intent = new Intent(MainActivity.this,
                        MainDispatchActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                finish();
            }
        }
    }

    public void loadData() {
        if (allQuestions.size() == 0)
            return;
        ImageView imageView = ((ImageView) findViewById(R.id.image));

        if(((BitmapDrawable)imageView.getDrawable())!=null)
            ((BitmapDrawable)imageView.getDrawable()).getBitmap().recycle();

        PicassoSingletonImageHandler.getSharedInstance(getApplicationContext())
                .load(allQuestions.get(questionIterator).ImageURI)
                .fit()
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        questionMotorActionPerformed = false;
                        if (questionIterator != 0)
                            FinishServingQuestion();
                        currentQuestion = allQuestions.get(questionIterator);

                        if (allQuestions.size() - 1 == questionIterator) {
                            findViewById(R.id.lastQuestionSave).setVisibility(View.VISIBLE);
                            findViewById(R.id.nextQuestion).setVisibility(View.GONE);
                        } else {
                            findViewById(R.id.lastQuestionSave).setVisibility(View.GONE);
                            findViewById(R.id.nextQuestion).setVisibility(View.VISIBLE);
                        }

                        questionIterator++;
                    }

                    @Override
                    public void onError() {

                    }
                });
    }
}
