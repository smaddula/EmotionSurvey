package sid.UserSurveyData;

import com.affectiva.android.affdex.sdk.detector.Face;

/**
 * Created by siddardha on 8/5/2015.
 */
public class FrameInformation {

    FrameEmotionInfo frameEmotionInfo;
    FrameExpressionInfo frameExpressionInfo;
    FrameOrientationInfo frameOrientationInfo;





    public FrameInformation(Face face ){
        frameEmotionInfo = new FrameEmotionInfo(face);
        frameExpressionInfo = new FrameExpressionInfo(face);
        frameOrientationInfo = new FrameOrientationInfo(face);
    }

}
