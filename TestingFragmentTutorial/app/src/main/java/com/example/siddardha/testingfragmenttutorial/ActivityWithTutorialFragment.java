package com.example.siddardha.testingfragmenttutorial;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ActivityWithTutorialFragment extends AppCompatActivity {


    ProgressBar progressBar;
    ImageView imageView;

    ArrayList<String> imagesToDownload = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_with_tutorial_fragment);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        progressBar.setMax(3);
        progressBar.setProgress(0);
        imageView = (ImageView)findViewById(R.id.imageView);

        imagesToDownload.add("http://s3.amazonaws.com/emotionsurveyimages/Images/911.jpg");
        imagesToDownload.add("http://s3.amazonaws.com/emotionsurveyimages/Images/EMOTIONAL-AFFAIR.jpg");
        imagesToDownload.add("http://s3.amazonaws.com/emotionsurveyimages/Images/Ways-To-Raise-Your-Emotional-Intelligence.jpg");
        for(String url : imagesToDownload ){
            Picasso.with(getApplicationContext())
                    .load(url)
                            //.resizeDimen(R.dimen.article_image_preview_width, R.dimen.article_image_preview_height)
                    //.centerCrop()
                    .fetch(
                            new Callback() {
                                @Override
                                public void onSuccess() {
                                    progressBar.setProgress(progressBar.getProgress() + 1);
                                    if(progressBar.getProgress() == progressBar.getMax()){
                                        Picasso.with(getApplicationContext()).load(imagesToDownload.get(1)).into(imageView);
                                    }
                                }

                                @Override
                                public void onError() {
                                    progressBar.setProgress(progressBar.getProgress() +1);

                        }
                    });
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_with_tutorial, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
