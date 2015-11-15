package sid.expressionsurveyaffectiva;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by siddardha on 11/14/2015.
 */
public enum IntensityEnum {
    highest(5,"highestintensity"),high(4,"highintensity"),moderate(3,"moderateintensity"),low(2,"lowintensity"),lowest(1,"lowestintensity");

    private static final Map<Integer, IntensityEnum> intToTypeMap = new HashMap<Integer, IntensityEnum>();

    static {
        for (IntensityEnum type : IntensityEnum.values()) {
            intToTypeMap.put(type.value, type);
        }
    }

    private final int value;
    private final String representation;

    IntensityEnum(int value, String representation) {
        this.value = value;
        this.representation = representation;
     }

    public String getRepresentation(){
        return representation;
    }

    public int getIntValue(){
        return value;
    }

    public static IntensityEnum getEnumFromInt(int value){
        return intToTypeMap.get(value);
    }


    @Override
    public String toString(){
        return representation;
    }
}
