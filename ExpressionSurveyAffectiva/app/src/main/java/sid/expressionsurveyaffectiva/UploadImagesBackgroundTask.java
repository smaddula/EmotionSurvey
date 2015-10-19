package sid.expressionsurveyaffectiva;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.Log;

import com.affectiva.android.affdex.sdk.Frame;
import com.affectiva.android.affdex.sdk.detector.Face;
import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//Use this if you need pause and resume functionality

public class UploadImagesBackgroundTask implements Runnable  {

    class FrameIOContainer{
        public String imageName;
        public byte[] byteArray;
        public int height;
        public int width;
        public FrameIOContainer(Frame frame , String imageName){
            this.imageName = imageName;
            byteArray = ((Frame.ByteArrayFrame) frame).getByteArray().clone();
            height = frame.getHeight();
            width = frame.getWidth();
        }
    }

    private boolean isSurveyComplete = false;
    protected BlockingQueue<FrameIOContainer> queue;
    private TransferUtility transferUtility;
    private Semaphore semaphore = new Semaphore(1,true);

    private IUploadedImageEvent ImageUploaded;
    private String uploadPath;
    private String DeviceDirectory;
    private int uploadsCompleted = 0;
    private int uploadRequests = 0;

    public UploadImagesBackgroundTask( IUploadedImageEvent imageUploaded ,Context context , String deviceDirectory , String s3Directory ){
        this.queue = new LinkedBlockingQueue<FrameIOContainer>() ;
        ImageUploaded = imageUploaded;
        transferUtility = Util.getTransferUtility(context);
        uploadPath = s3Directory;
        DeviceDirectory = deviceDirectory;
    }

    long endTime;
    public void run() {

        FrameIOContainer itemToWork;

        while(true){
            //limit to only one thread since upload is killing the performance
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
                continue;
            }
            try {
                long startTime = System.nanoTime();
                itemToWork = queue.take();
                saveToDrive(itemToWork);
                long savedtoFileTime = System.nanoTime();
                saveToS3(itemToWork);
                endTime = System.nanoTime();
                Log.d("Image SaveImage" , Long.toString ((savedtoFileTime-startTime)/1000000) +" "+ itemToWork.imageName );
                Log.d("Image S3UploadCall" , Long.toString ((endTime-savedtoFileTime)/1000000) + " " + itemToWork.imageName );
            } catch (InterruptedException e) {
                semaphore.release();
                e.printStackTrace();
            } catch (IOException e) {
                semaphore.release();
                e.printStackTrace();
            }

            try {
                if(!isSurveyComplete)
                    Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveToDrive( FrameIOContainer itemToWork ) throws IOException {
        int width = itemToWork.width;
        int height = itemToWork.height;

        // Naming the file randomly
        //String dirPath =  ;//+ File.separator +  surveyImagesDeviceDirectory;
        File file = new File(DeviceDirectory+ File.separator + itemToWork.imageName);
        file.getParentFile().mkdirs();
        OutputStream fOut = new FileOutputStream(file);
        //TODO: Save file as Yuv instead of bitmap and converting to jpg
        //investigate how to rotate a Yuv image to make this optimization
        YuvImage img = new YuvImage(
                itemToWork.byteArray, ImageFormat.NV21 , width,height,null);

        img.compressToJpeg( new Rect( 0,0,img.getWidth(),img.getHeight() ) , 100,fOut);
        fOut.flush();
        fOut.close();

    }

    public void saveToS3(FrameIOContainer itemToWork){

        final File file = new File(DeviceDirectory+File.separator+itemToWork.imageName);
        final TransferObserver transferObserver= transferUtility.upload(Util.BUCKET_NAME, uploadPath+File.separator+file.getName(),file);

        transferObserver.setTransferListener(
                new TransferListener() {
                    @Override
                    public void onStateChanged(int i, TransferState transferState) {
                        if(transferState == TransferState.IN_PROGRESS)
                            return;

                        if (transferState == TransferState.COMPLETED || transferState == TransferState.CANCELED || transferState == TransferState.FAILED) {

                            if(transferState == TransferState.CANCELED || transferState == TransferState.FAILED)
                                Log.d("Image Failed Saving","Upload Image Failed");
                            //ignore all the failed uploads .. no need to be perfect here
                            semaphore.release();
                            uploadsCompleted++;
                            Log.d("SaveUpload UploadedFile", Long.toString((System.nanoTime() - endTime) / 1000000) + " " + file.getName() + " " + transferState.toString());
                            if(isSurveyComplete || uploadsCompleted == uploadRequests )
                            //let the main thread know that a image got uploaded
                                ImageUploaded.callback(uploadsCompleted);
                        }
                    }

                    @Override
                    public void onProgressChanged(int i, long l, long l1) {

                    }

                    @Override
                    public void onError(int i, Exception e) {

                    }
                });

    }

    public void push(Frame frame , String imageFileName){
        queue.add(new FrameIOContainer(frame,imageFileName));
        uploadRequests++;
    }


    public void surveyComplete(){
        isSurveyComplete = true;
        //Increase number of threads by 5
        semaphore.release(5);
    }

}
