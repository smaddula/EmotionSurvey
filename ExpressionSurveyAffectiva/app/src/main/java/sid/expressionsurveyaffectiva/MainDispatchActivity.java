package sid.expressionsurveyaffectiva;

import com.parse.ui.ParseLoginDispatchActivity;

public class MainDispatchActivity extends ParseLoginDispatchActivity {
    @Override
    protected Class<?> getTargetClass() {
        return UserPickActivity.class;
    }
}

