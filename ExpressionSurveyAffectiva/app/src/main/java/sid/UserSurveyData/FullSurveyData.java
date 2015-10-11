package sid.UserSurveyData;

import java.util.ArrayList;
import java.util.List;

import sid.expressionsurveyaffectiva.Question;

/**
 * Created by siddardha on 9/6/2015.
 */
public class FullSurveyData {
    public List<UserQuestionData> questionSurveyData ;
    private transient Question currentQuestion;
    private transient UserQuestionData currentUserQuestionData;
    public String serverImagesPath ;
    public void AddFrameData(Question question , FrameInformation frameInformation){
        if(question!=null) {
            if (currentQuestion == null || question.parseQuestionData.getObjectId() != currentUserQuestionData.QuestionObjectId) {
                currentQuestion = question;
                if (currentUserQuestionData != null)
                    currentUserQuestionData.FinishedQuestion();
                currentUserQuestionData = new UserQuestionData(currentQuestion.parseQuestionData.getObjectId(), currentQuestion.ImageURI);
                questionSurveyData.add(currentUserQuestionData);
            }
            currentUserQuestionData.AddFrameData(frameInformation);
        }
    }

    public void setUserInput(int valenceUserInput){
        currentUserQuestionData.valenceUserInput = valenceUserInput;
    }

    public FullSurveyData(String imagesPath){
        serverImagesPath = imagesPath;
        questionSurveyData = new ArrayList<UserQuestionData>();
        currentQuestion = null;
        currentUserQuestionData = null;
    }

    public void DoneSurvey(){
        if(currentUserQuestionData!=null)
            currentUserQuestionData.FinishedQuestion();
    }

}
