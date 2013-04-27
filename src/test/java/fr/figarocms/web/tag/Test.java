package fr.figarocms.web.tag;

import com.google.common.collect.Maps;

import java.util.Map;

public class Test{
    private String toto="dummy value";

    public Titi getTiti() {
        return titi;
    }

    private Titi titi = new Titi();

    public String getToto() {
        return toto;
    }

    private class Titi {
        private Integer dummyInteger = 8;
        private Map map = Maps.newHashMap();

        private Titi() {
            map.put("test","another dummy data");
        }

        public Integer getDummyInteger() {
            return dummyInteger;
        }
    }
}
