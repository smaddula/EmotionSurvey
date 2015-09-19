package sid.UserSurveyData;

import com.affectiva.android.affdex.sdk.detector.Face;

/**
 * Created by siddardha on 9/19/2015.
 */
public class FrameOrientationInfo {


    float Pitch;
    float Roll;
    float Yaw;

    public FrameOrientationInfo(Face face){
        Pitch = face.measurements.orientation.getPitch();
        Roll = face.measurements.orientation.getRoll();
        Yaw = face.measurements.orientation.getYaw();
    }

}
