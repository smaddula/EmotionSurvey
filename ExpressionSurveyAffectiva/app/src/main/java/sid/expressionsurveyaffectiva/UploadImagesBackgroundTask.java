package sid.expressionsurveyaffectiva;

import android.content.Context;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class UploadImagesBackgroundTask implements Runnable  {
    protected BlockingQueue queue = null;
    private TransferUtility transferUtility;
    private Semaphore semaphore = new Semaphore(1,true);

    private Object mPauseLock;
    private boolean mPaused;

    private IEvent ImageUploaded;
    private String uploadPath;

    public UploadImagesBackgroundTask( IEvent imageUploaded ,Context context , String s3Directory ){
        this.queue = new LinkedBlockingQueue<String>() ;
        ImageUploaded = imageUploaded;
        transferUtility = Util.getTransferUtility(context);
        uploadPath = s3Directory;
    }

    public void run() {

        while(true){
            //limit to only one thread since upload is killing the performance
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
                continue;
            }
            try {
                saveToS3(queue.take().toString());
            } catch (InterruptedException e) {
                semaphore.release();
                e.printStackTrace();
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            synchronized (mPauseLock) {
                while (mPaused) {
                    try {
                        mPauseLock.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    public void saveToS3(String path){
        File file = new File(path);
        TransferObserver transferObserver= transferUtility.upload(Util.BUCKET_NAME, uploadPath+File.separator+file.getName(),file);
        transferObserver.setTransferListener(
                new TransferListener() {
                    @Override
                    public void onStateChanged(int i, TransferState transferState) {
                        semaphore.release();
                        if (transferState == TransferState.COMPLETED) {
                            //let the main thread know that a image got uploaded
                            ImageUploaded.callback();
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

    public void push(String path){
        queue.add(path);
    }

    /**
     * Call this on pause.
     */
    public void Pause() {
        synchronized (mPauseLock) {
            mPaused = true;
        }
    }

    /**
     * Call this on resume.
     */
    public void Resume() {
        synchronized (mPauseLock) {
            mPaused = false;
            mPauseLock.notifyAll();
        }
    }

}
