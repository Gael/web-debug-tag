package fr.figarocms.web.tag;

import com.google.common.collect.Maps;

import java.util.Map;

public class Test{
    private String toto="dummy value";

    private Titi titi = new Titi();

    private class Titi {
        private Integer dummyInteger = 8;
        private Map map = Maps.newHashMap();

        private Titi() {
            map.put("test","another dummy data");
        }
    }
}
