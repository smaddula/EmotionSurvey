package sid.expressionsurveyaffectiva;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by siddardha on 8/3/2015.
 */
public class TimeStampScorePair {
    public Date datetime;
    public FrameInformation score;
    private static SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");
    TimeStampScorePair(Date param_date , FrameInformation value) {
        datetime = param_date;
        score = value;
    }
    TimeStampScorePair(Date param_date , float smile , float browfurrow , float browrise , float valience , float engagement , float lipcornerdepressor) {
        datetime = param_date;
        score = new FrameInformation(smile , browfurrow , browrise , valience , engagement , lipcornerdepressor);
    }
    
    public String getLine()
    {
        return formatter.format(datetime) + "\t" + score.getLine() ;
    }
}
