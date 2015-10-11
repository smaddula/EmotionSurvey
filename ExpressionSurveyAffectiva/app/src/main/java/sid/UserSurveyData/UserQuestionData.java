package sid.UserSurveyData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by siddardha on 9/5/2015.
 */

public class UserQuestionData {
    public String QuestionObjectId;
    public Date questionStartTime;
    public Date questionEndTime;
    public Date motorActionPerformed;
    public String imageURI;
    public int valenceUserInput;
    public List<TimeStampFrameInformationPair> frameData;
    public UserQuestionData(String quetionid, String imageuri){
        QuestionObjectId = quetionid;
        imageURI = imageuri;
        frameData = new ArrayList<TimeStampFrameInformationPair>();
        motorActionPerformed = null;
    }
    public void AddFrameData( FrameInformation frameInformation) {

        Date date = new Date();
        if(frameData.size() == 0){
            questionStartTime = date;
        }
        if(motorActionPerformed == null && frameInformation.afterMotorAction )
        {
            motorActionPerformed = date;
        }
        frameData.add( new TimeStampFrameInformationPair( date,frameInformation));
    }

    public void FinishedQuestion(){
        questionEndTime = frameData.get(frameData.size()-1).datetime;
    }

}
