package sid.expressionsurveyaffectiva;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Environment;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    int questionIterator;
    String surveyImagesDeviceDirectory = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss'.tsv'").format(new Date());
    boolean saveImage = true;
    FullSurveyData userData = new FullSurveyData();
    List<Question> allQuestions = new ArrayList<Question>();
    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").serializeSpecialFloatingPointValues().create();
    Question currentQuestion;
    RadioGroup valenceRadioGroup ;
    LinearLayout surfaceViewContainer , footerButtonsContainer;

    private SurfaceView cameraPreview;
    private CameraDetector detector;


    @Override
    public void onFaceDetectionStarted() {
        if(surfaceViewContainer!=null && footerButtonsContainer!=null){
            surfaceViewContainer.setLayoutParams(new LinearLayout.LayoutParams(50,50));
            footerButtonsContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onFaceDetectionStopped() {
        if(surfaceViewContainer!=null && footerButtonsContainer!=null){
            surfaceViewContainer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
            footerButtonsContainer.setVisibility(View.GONE);
        }
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
        //TODO:If saving is slowing the frame rate use a ThreadPool and move the saving logic to a different class

        saveImage = false;
        String UserFaceImageName = "";
        if (!saveImage) {
            try {
                UserFaceImageName = Long.toString(System.nanoTime())+".jpg";
                int width = image.getWidth();
                int height = image.getHeight();

                String path = Environment.getExternalStorageDirectory().toString();
                OutputStream fOut = null;
                // Naming the file randomly
                File file = new File(surveyImagesDeviceDirectory+ "/" + UserFaceImageName);
                fOut = new FileOutputStream(file);
                //TODO: Save file as Yuv instead of bitmap and converting to jpg
                //investigate how to rotate a Yuv image to make this optimization
                YuvImage img = new YuvImage(
                        ((Frame.ByteArrayFrame) image).getByteArray(), ImageFormat.NV21 , width,height,null);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                img.compressToJpeg(new Rect(0,0,width,height),100,out);
                byte[] imageBytes = out.toByteArray();
                Bitmap bitmapImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                switch (image.getTargetRotation()) {
                    case BY_90_CCW:
                        bitmapImage = Frame.rotateImage(bitmapImage,-90);
                        break;
                    case BY_90_CW:
                        bitmapImage = Frame.rotateImage(bitmapImage,90);
                        break;
                    case BY_180:
                        bitmapImage = Frame.rotateImage(bitmapImage,180);
                        break;
                    default:
                        //keep bitmap as it is
                }

                bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fOut);

                fOut.flush();
                fOut.close();
                saveImage=true;
            } catch (IOException e) {
                UserFaceImageName = "";
                e.printStackTrace();
            }
        }

        Face face = faces.get(0);
        userData.AddFrameData(currentQuestion, new FrameInformation( face , UserFaceImageName ));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Enable Local Datastore.

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        surfaceViewContainer = (LinearLayout)findViewById(R.id.surfaceViewContainer);
        footerButtonsContainer = (LinearLayout)findViewById(R.id.footerButtonContainer);

        valenceRadioGroup = (RadioGroup) findViewById( R.id.valenceRadioGroup);

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
        valenceRadioGroup.clearCheck();
    }

    public void loadNextQuestion( View view ){
        if(valenceRadioGroup.getCheckedRadioButtonId() == -1)
            return;
        saveImage = true;
        FinishServingQuestion();
        loadData();
    }


    public void onRadioButtonClicked(View view){
        saveImage = false;
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
        currentQuestion = allQuestions.get( questionIterator );
        ImageView imageView = ((ImageView) findViewById(R.id.image));
        new DownloadImageTask(imageView)
                .execute(currentQuestion.ImageURI);
        ((TextView)findViewById(R.id.text_view)).setText( currentQuestion.QuestionHeading);

        if(questionIterator == allQuestions.size() - 1)
        {
            ((Button)findViewById(R.id.lastQuestionSave)).setVisibility(View.VISIBLE);
            ((Button)findViewById(R.id.nextQuestion)).setVisibility(View.GONE);
            questionIterator = 0;
        } else
        {
            ((Button)findViewById(R.id.lastQuestionSave)).setVisibility(View.GONE);
            ((Button)findViewById(R.id.nextQuestion)).setVisibility(View.VISIBLE);
            questionIterator++;
        }
    }
}
