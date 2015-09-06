package sid.expressionsurveyaffectiva;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

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
                        ClickViewVisualizations();
                    }
        });

    }

    public void ClickViewVisualizations( ){
        String url = "http://www.example.com";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        Context context = getContext();
        context.startActivity(intent);
    }

}
