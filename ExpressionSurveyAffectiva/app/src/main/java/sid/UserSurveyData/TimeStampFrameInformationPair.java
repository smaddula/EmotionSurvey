package sid.UserSurveyData;

import com.affectiva.android.affdex.sdk.detector.Face;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by siddardha on 8/3/2015.
 */
public class TimeStampFrameInformationPair {
    public Date datetime;
    public FrameInformation score;
    TimeStampFrameInformationPair(Date param_date, FrameInformation value) {
        datetime = param_date;
        score = value;
    }
    TimeStampFrameInformationPair(Date param_date, Face face) {
        datetime = param_date;
        score = new FrameInformation(face);
    }

}
