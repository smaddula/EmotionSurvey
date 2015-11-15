package sid.expressionsurveyaffectiva;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;


public class IntensityValenceUIManager {

    RadioGroup valenceRadioGroup;
    RadioGroup intensityRadioGroup;

    RadioButton lowestIntensityRadioButton,lowIntensityRadioButton,moderateIntensityRadioButton,
            highestIntensityRadioButton,highIntensityRadioButton;

    Activity activity;

    IntensityValenceUIManager(Activity activity){
        lowestIntensityRadioButton = (RadioButton) activity.findViewById(R.id.intensity_lowest);
        lowIntensityRadioButton = (RadioButton) activity.findViewById(R.id.intensity_low);
        moderateIntensityRadioButton = (RadioButton) activity.findViewById(R.id.intensity_moderate);
        highIntensityRadioButton = (RadioButton) activity.findViewById(R.id.intensity_high);
        highestIntensityRadioButton = (RadioButton) activity.findViewById(R.id.intensity_highest);
        valenceRadioGroup = (RadioGroup) activity.findViewById(R.id.valenceRadioGroup);
        intensityRadioGroup = (RadioGroup) activity.findViewById(R.id.intensityRadioGroup);
        this.activity = activity;
    }

    public void resetRadioGroups(){
        intensityRadioGroup.setVisibility(View.INVISIBLE);
        valenceRadioGroup.clearCheck();
        intensityRadioGroup.clearCheck();
    }

    public void updateIntensityRadioButtons(){
        intensityRadioGroup.setVisibility(View.VISIBLE);
        ValenceEnum valenceEnum =  getValenceSelected();
        intensityRadioGroup.clearCheck();

        try {
            Class res = R.drawable.class;
            highestIntensityRadioButton.setButtonDrawable( res.getField("radiobutton_selector_"+IntensityEnum.highest+"_"+valenceEnum).getInt(null));
            highIntensityRadioButton.setButtonDrawable(res.getField("radiobutton_selector_" + IntensityEnum.high + "_" + valenceEnum).getInt(null));
            moderateIntensityRadioButton.setButtonDrawable( res.getField("radiobutton_selector_"+IntensityEnum.moderate+"_"+valenceEnum).getInt(null));
            lowIntensityRadioButton.setButtonDrawable( res.getField("radiobutton_selector_"+IntensityEnum.low+"_"+valenceEnum).getInt(null));
            lowestIntensityRadioButton.setButtonDrawable( res.getField("radiobutton_selector_"+IntensityEnum.lowest+"_"+valenceEnum).getInt(null));
        } catch (Exception e) {
            Log.e("ResetIntensity", "Failure to get drawable id.", e);
        }
    }

    public ValenceEnum getValenceSelected(){
        return  ValenceEnum.getEnumFromInt (Integer.parseInt(((RadioButton) activity.findViewById(valenceRadioGroup.getCheckedRadioButtonId())).getTag().toString()));
    }


    public IntensityEnum getIntensitySelected(){
        return  IntensityEnum.getEnumFromInt (Integer.parseInt(((RadioButton) activity.findViewById(intensityRadioGroup.getCheckedRadioButtonId())).getTag().toString()));
    }

    public boolean intensityValenceSelected(){

        if (valenceRadioGroup.getCheckedRadioButtonId() == -1)
            return false;

        if (intensityRadioGroup.getCheckedRadioButtonId() == -1)
            return false;

        return true;
    }

}
