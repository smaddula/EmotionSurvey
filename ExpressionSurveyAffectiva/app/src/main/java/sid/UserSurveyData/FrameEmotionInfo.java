package sid.UserSurveyData;

import com.affectiva.android.affdex.sdk.detector.Face;

/**
 * Created by siddardha on 9/19/2015.
 */
public class FrameEmotionInfo {

    float Anger;
    float Contempt;
    float Disgust;
    float Engagement;
    float Fear;
    float Joy;
    float Sadness;
    float Surprise;
    float Valence;

    public FrameEmotionInfo(Face face){
        Anger = face.emotions.getAnger();
        Contempt = face.emotions.getContempt();
        Disgust = face.emotions.getDisgust();
        Engagement = face.emotions.getEngagement();
        Fear = face.emotions.getFear();
        Joy = face.emotions.getJoy();
        Sadness = face.emotions.getSadness();
        Surprise = face.emotions.getSurprise();
        Valence = face.emotions.getValence();
    }

}
