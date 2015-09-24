package sid.UserSurveyData;

import com.affectiva.android.affdex.sdk.detector.Face;

/**
 * Created by siddardha on 9/19/2015.
 */
public class FrameExpressionInfo {

    float smile;
    float browFurrow;
    float browRaise;
    float lipCornerDepressor;
    float Attention;
    float ChinRaise;
    float EyeClosure;
    float InnerBrowRaise;
    float LipPress;
    float LipPucker;
    float LipSuck;
    float MouthOpen;
    float NoseWrinkle;
    float Smirk;

    float UpperLipRaise;

    public FrameExpressionInfo(Face face){

        smile = face.expressions.getSmile();
        browFurrow = face.expressions.getBrowFurrow();
        browRaise = face.expressions.getBrowRaise();
        lipCornerDepressor = face.expressions.getLipCornerDepressor();
        Attention = face.expressions.getAttention();
        ChinRaise = face.expressions.getChinRaise();
        EyeClosure = face.expressions.getEyeClosure();
        InnerBrowRaise = face.expressions.getInnerBrowRaise();
        LipPress = face.expressions.getLipPress();
        LipPucker = face.expressions.getLipPucker();
        MouthOpen = face.expressions.getMouthOpen();
        NoseWrinkle = face.expressions.getNoseWrinkle();
        Smirk = face.expressions.getSmirk();
        UpperLipRaise = face.expressions.getUpperLipRaise();
        LipSuck = face.expressions.getLipSuck();
    }

}
