package sid.expressionsurveyaffectiva;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

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
        public Frame frame;
        public String imageName;
        public FrameIOContainer(Frame frame , String imageName){
            this.frame = frame;
            this.imageName = imageName;
        }
    }

    private boolean isSurveyComplete = false;
    protected BlockingQueue<FrameIOContainer> queue;
    private TransferUtility transferUtility;
    private Semaphore semaphore = new Semaphore(1,true);

    private IEvent ImageUploaded;
    private String uploadPath;
    private String DeviceDirectory;
    private int uploadsCompleted = 0;
    private int uploadRequests = 0;

    public UploadImagesBackgroundTask( IEvent imageUploaded ,Context context , String deviceDirectory , String s3Directory ){
        this.queue = new LinkedBlockingQueue<FrameIOContainer>() ;
        ImageUploaded = imageUploaded;
        transferUtility = Util.getTransferUtility(context);
        uploadPath = s3Directory;
        DeviceDirectory = deviceDirectory;
    }

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
                itemToWork = queue.take();
                saveToDrive(itemToWork);
                saveToS3(itemToWork);
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
        int width = itemToWork.frame.getWidth();
        int height = itemToWork.frame.getHeight();

        // Naming the file randomly
        //String dirPath =  ;//+ File.separator +  surveyImagesDeviceDirectory;
        File file = new File(DeviceDirectory+ File.separator + itemToWork.imageName);
        file.getParentFile().mkdirs();

        OutputStream fOut = new FileOutputStream(file);
        //TODO: Save file as Yuv instead of bitmap and converting to jpg
        //investigate how to rotate a Yuv image to make this optimization
        YuvImage img = new YuvImage(
                ((Frame.ByteArrayFrame) itemToWork.frame).getByteArray(), ImageFormat.NV21 , width,height,null);

        //Rotating the image in each frame is slowing down the application a lot ..
        //Be content with the rotated image and adjust the visualization part (not cool though )
        //Write another program that edits the files stored in S3 - batch script every night ?


        /*ByteArrayOutputStream out = new ByteArrayOutputStream();
        img.compressToJpeg(new Rect(0,0,width,height),100,out);
        byte[] imageBytes = out.toByteArray();
        Bitmap bitmapImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

        switch (itemToWork.frame.getTargetRotation()) {
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
        }*/

        //bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fOut);

        img.compressToJpeg( new Rect( 0,0,img.getWidth(),img.getHeight() ) , 100,fOut);
        fOut.flush();
        fOut.close();

    }

    public void saveToS3(FrameIOContainer itemToWork){

        File file = new File(DeviceDirectory+File.separator+itemToWork.imageName);
        TransferObserver transferObserver= transferUtility.upload(Util.BUCKET_NAME, uploadPath+File.separator+file.getName(),file);
        transferObserver.setTransferListener(
                new TransferListener() {
                    @Override
                    public void onStateChanged(int i, TransferState transferState) {
                        semaphore.release();
                        if (transferState == TransferState.COMPLETED) {
                            uploadsCompleted++;
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
    }

}
