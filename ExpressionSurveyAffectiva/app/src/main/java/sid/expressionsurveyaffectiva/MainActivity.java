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
    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
    Question currentQuestion;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    //SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */

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
        userData.AddFrameData(currentQuestion, new FrameInformation(face.getSmileScore(), face.getBrowFurrowScore(),
                face.getBrowRaiseScore(), face.getValenceScore(), face.getEngagementScore(), face.getLipCornerDepressorScore()));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Enable Local Datastore.
/*        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "DYlc2hnyF3zlDZNcxhDE2zgSk87eQOdRCOJhgVSQ", "C5AaStSC1L4qx8S6rV2QnGGh3SgyphIKc2L6hiYe");

        ParseObject testObject = new ParseObject("TestObject");
        testObject.put("foo", "bar");
        testObject.saveInBackground();
*/

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        segmentId = 0 ;
        FolderPath = getExternalFilesDir(null) + File.separator + APP_UUID;
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
/*        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);*/



        cameraPreview = (SurfaceView) findViewById(R.id.cameraId);
        // Put the SDK in camera mode by using this constructor. The SDK will be in control of
        // the camera. If a SurfaceView is passed in as the last argument to the constructor,
        // that view will be painted with what the camera sees.
        detector = new CameraDetector(this, CameraDetector.CameraType.CAMERA_FRONT, cameraPreview);

        // NOTE: replace "Affectiva.license" with your license file, which should be stored in /assets/Affdex/
        detector.setLicensePath("sdk_kusuma.chunduru@gmail.com.license");

        // We want to detect all expressions, so turn on all classifiers.
        detector.setDetectSmile(true);
        detector.setDetectLipCornerDepressor(true);
        detector.setDetectBrowFurrow(true);
        detector.setDetectBrowRaise(true);
        detector.setDetectEngagement(true);
        detector.setDetectValence(true);

        detector.setMaxProcessRate(20);

        detector.setImageListener(this);
        detector.setFaceListener(this);

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

    public void loadData( View view ){
        loadData();
    }
    public void logout( View view ){
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

    public void SaveData(View view) throws IOException, com.parse.ParseException {
        //File directory = new File(FolderPath);
        //directory.mkdir();
        //File file = new File(FolderPath, "Scores.txt");
        //FileOutputStream os = new FileOutputStream(file);
        //String Header = "date\tsmile\tbrowfurrow\tbrowrise\tvalence\tengagement\tlipcornerdepressor";
        //OutputStreamWriter out = new OutputStreamWriter(os);
        //out.write(Header + "\n");
        //for(TimeStampFrameInformationPair smileTimeScore: framesScore){
        //    out.write(smileTimeScore.getLine() + "\n");
        //}
        //out.close();
        String result = gson.toJson(userData);
        ParseFile emotionFrameData = new ParseFile("FrameEmotionData.txt", gson.toJson(userData).getBytes());
        emotionFrameData.save();

        ParseObject userSurvey = new ParseObject("SurveyData");
        userSurvey.put("UserID",ParseUser.getCurrentUser());
        userSurvey.put("JsonEmotionData",emotionFrameData);
        userSurvey.save();
    }

    public void loadData(  )
    {
        if(allQuestions.size()==0)
            return;
        currentQuestion = allQuestions.get( segmentId );
        ImageView imageView = ((ImageView) findViewById(R.id.image));//.setImageURI(Uri.parse(getArguments().getString(ARG_IMAGE_SOURCE)));
        new DownloadImageTask(imageView)
                .execute(currentQuestion.ImageURI);
        ((TextView)findViewById(R.id.text_view)).setText( currentQuestion.QuestionHeading);

        if(segmentId == allQuestions.size() - 1)
        {
            ((Button)findViewById(R.id.lastQuestionSave)).setVisibility(View.VISIBLE);
            segmentId = 0;
        } else
        {
            ((Button)findViewById(R.id.lastQuestionSave)).setVisibility(View.INVISIBLE);
            segmentId++;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    /*public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }*/

    /**
     * A placeholder fragment containing a simple view.
     */
    /*public static class PlaceholderFragment extends Fragment {
        *//**
         * The fragment argument representing the section number for this
         * fragment.
         *//*
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static String ARG_IMAGE_SOURCE ;
        *//**
         * Returns a new instance of this fragment for the given section
         * number.
         *//*
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            if(sectionNumber == 1)
            {
                args.putString(ARG_IMAGE_SOURCE, "http://www.google.com/images/srpr/logo11w.png");
            }else
            {
                args.putString(ARG_IMAGE_SOURCE, "https://upload.wikimedia.org/wikipedia/commons/8/8c/Lynn_-_detail_first_state_1987.jpg");
            }
            fragment.setArguments(args);
            return fragment;
        }
        //Use bundle instead
        public PlaceholderFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            int sectionNumber =  getArguments().getInt(ARG_SECTION_NUMBER);
            ((TextView) rootView.findViewById(R.id.text_view)).setText("hey Bro .. " + sectionNumber);
            ImageView imageView = ((ImageView) rootView.findViewById(R.id.image));//.setImageURI(Uri.parse(getArguments().getString(ARG_IMAGE_SOURCE)));
            new DownloadImageTask(imageView)
                    .execute(getArguments().getString(ARG_IMAGE_SOURCE));
            return rootView;
        }
    }*/

}
