package io.kite.Utils;

import java.util.HashMap;
import java.util.Map;

public class Maps {
    public static <K,V> Map<K,V> mutableOf(Map<K,V> map){
        return new HashMap<>(map);
    }
}
