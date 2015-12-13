package sid.expressionsurveyaffectiva;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CacheImages extends Activity {

    boolean cacheTrainingImages, isTestSurvey;
    ArrayList<String> imagesToCache = new ArrayList<String>();
    ProgressBar progressBar;
    String surveyId;

    protected void NavigateToNextActivity() {
        Intent intent = new Intent(CacheImages.this,
                MainActivity.class);
        intent.putExtra("SurveyId", surveyId);
        intent.putExtra("isTestSurvey", isTestSurvey);
        startActivity(intent);
    }

    protected void startCacheImages(){

        for (String url : imagesToCache) {
            PicassoSingletonImageHandler.getSharedInstance(getApplicationContext()).with(getApplicationContext())
                    .load(url)
                    .fetch(
                            new Callback() {
                                @Override
                                public void onSuccess() {
                                    progressBar.setProgress(progressBar.getProgress() + 1);
                                    if (progressBar.getProgress() == progressBar.getMax()) {
                                        NavigateToNextActivity();
                                    }
                                }

                                @Override
                                public void onError() {
                                    progressBar.setProgress(progressBar.getProgress() + 1);
                                    if (progressBar.getProgress() == progressBar.getMax()) {
                                        NavigateToNextActivity();
                                    }
                                }
                            }
                    )
            ;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cache_images);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(3);
        progressBar.setProgress(0);

        cacheTrainingImages = getIntent().getBooleanExtra("cacheTrainingImages", false);

        if (!cacheTrainingImages) {
            surveyId = getIntent().getStringExtra("SurveyId");
            isTestSurvey = getIntent().getBooleanExtra("SurveyId", false);

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
                                imagesToCache.add(questionObj.getString("ImageURI"));
                            } catch (ParseException e1) {
                                e1.printStackTrace();
                            }
                        }
                        progressBar.setMax(imagesToCache.size());
                        progressBar.setProgress(0);
                        startCacheImages();
                        //NavigateToNextActivity();
                    } else {
                        // something went wrong
                    }
                }
            });
        }

    }

}
