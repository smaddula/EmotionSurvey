package sid.expressionsurveyaffectiva;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by siddardha on 11/14/2015.
 */
public enum ValenceEnum {
    Happiest(5,"highestvalence"),Happy(4,"highvalence"),Neutral(3,"moderatevalence"),Sad(2,"lowvalence"),Saddest(1,"lowestvalence");

    private static final Map<Integer, ValenceEnum> intToTypeMap = new HashMap<Integer, ValenceEnum>();

    static {
        for (ValenceEnum type : ValenceEnum.values()) {
            intToTypeMap.put(type.value, type);
        }
    }

    private final int value;
    private final String representation;

    ValenceEnum(int value,String representation) {
        this.value = value;
        this.representation = representation;
     }

    public String getRepresentation(){
        return representation;
    }

    public int getIntValue(){
        return value;
    }

    @Override
    public String toString(){
        return representation;
    }

    public static ValenceEnum getEnumFromInt(int value){
        return intToTypeMap.get(value);
    }

}
