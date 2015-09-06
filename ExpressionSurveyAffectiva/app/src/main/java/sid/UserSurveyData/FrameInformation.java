package sid.UserSurveyData;

/**
 * Created by siddardha on 8/5/2015.
 */
public class FrameInformation {

    float smileScore;
    float browFurrowScore;
    float browRaiseScore;
    float lipCornerDepressorScore;
    float valenceScore;
    float engagementScore;



    public FrameInformation(float smile , float browfurrow , float browrise , float valence , float engagement , float lipcornerdepressor){
        smileScore = smile;
        browFurrowScore = browfurrow;
        browRaiseScore = browrise;
        valenceScore = valence;
        engagementScore = engagement;
        lipCornerDepressorScore = lipcornerdepressor;
    }

    public String getLine()
    {
        String rtn = Integer.toString((int)smileScore ) + "\t" + Integer.toString((int)browFurrowScore ) + "\t" + Integer.toString((int)browRaiseScore) + "\t"
                + Integer.toString((int)valenceScore )+ "\t"  + Integer.toString((int)engagementScore )+ "\t"  + Integer.toString((int)lipCornerDepressorScore );
        return  rtn;
    }
}
