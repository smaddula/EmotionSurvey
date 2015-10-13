package sid.expressionsurveyaffectiva;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

/**
 * Created by siddardha on 8/2/2015.
 */

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;
    IDownloadedImageEvent imageDownloadedEvent;

    public DownloadImageTask(IDownloadedImageEvent imagedDownloadedEvent ,ImageView bmImage) {
        this.bmImage = bmImage;
        this.imageDownloadedEvent = imagedDownloadedEvent;
    }

    protected Bitmap doInBackground(  String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        imageDownloadedEvent.callback();
        bmImage.setImageBitmap(result);
    }
}