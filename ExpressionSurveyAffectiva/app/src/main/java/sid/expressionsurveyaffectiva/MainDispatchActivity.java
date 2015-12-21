package sid.expressionsurveyaffectiva;

import com.parse.ui.ParseLoginDispatchActivity;

public class MainDispatchActivity extends ParseLoginDispatchActivity {
    //Activity that starts after parse login activity
    @Override
    protected Class<?> getTargetClass() {
        return UserPickActivity.class;
    }
}

