package sid.UserSurveyData;

import com.affectiva.android.affdex.sdk.detector.Face;

/**
 * Created by siddardha on 8/5/2015.
 */
public class FrameInformation {

    //If we dont have a image available for the frame store it as blank
    FrameEmotionInfo frameEmotionInfo;
    FrameExpressionInfo frameExpressionInfo;
    FrameOrientationInfo frameOrientationInfo;
    String userCameraImagePath;
    boolean afterMotorAction;

    public FrameInformation(Face face , String userFaceImage , boolean aftermotoraction ){
        frameEmotionInfo = new FrameEmotionInfo(face);
        frameExpressionInfo = new FrameExpressionInfo(face);
        frameOrientationInfo = new FrameOrientationInfo(face);
        userCameraImagePath = userFaceImage;
        afterMotorAction = aftermotoraction;
    }

}
