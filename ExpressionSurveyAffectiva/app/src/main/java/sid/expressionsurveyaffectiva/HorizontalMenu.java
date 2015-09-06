package sid.expressionsurveyaffectiva;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * TODO: document your custom view class.
 */
public class HorizontalMenu extends LinearLayout {


    public HorizontalMenu(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.horizontalmenu, this, true);
        TextView clickviewvisualization = (TextView) findViewById(R.id.ExternalViewCharts);
        clickviewvisualization.setOnClickListener(
                new OnClickListener(  ) {
                    @Override
                    public void onClick(View arg0) {
                        try {
                            ClickViewVisualizations();
                        } catch (ParseException e) {
                            Toast.makeText(getContext(),"Issue in getting latest Survey",Toast.LENGTH_SHORT).show();
                        }
                    }
        });

    }

    public void ClickViewVisualizations( ) throws ParseException {
        ParseUser currentUser = ParseUser.getCurrentUser() ;
        ParseQuery<ParseObject> query = ParseQuery.getQuery("SurveyData");
        query.whereEqualTo("UserID", currentUser);
        ParseObject result = query.getFirst();
        String url = "http://emotion-maddula.rhcloud.com/survey/"+result.getObjectId();
        Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(url));
        Context context = getContext();
        context.startActivity(intent);
    }

}
