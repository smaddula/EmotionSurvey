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
    public List<TimeStampFrameInformationPair> frameData;
    public UserQuestionData(String quetionid){
        QuestionObjectId = quetionid;
        frameData = new ArrayList<TimeStampFrameInformationPair>();
    }
    public void AddFrameData( FrameInformation frameInformation) {

        Date date = new Date();
        if(frameData.size() == 0){
            questionStartTime = date;
        }
        frameData.add( new TimeStampFrameInformationPair( date,frameInformation));
    }

    public void FinishedQuestion(){
        questionEndTime = frameData.get(frameData.size()-1).datetime;
    }

}
