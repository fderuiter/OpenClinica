package org.hibernate.criterion;
import java.util.Collections;
import java.util.Map;
public class Restrictions {
    public static Criterion eq(String propertyName, Object value) {
        return new Criterion() {
            public String toSqlString() { return "do." + propertyName + " = :" + propertyName.replace('.', '_'); }
            public Map<String, Object> getParameters() { return Collections.singletonMap(propertyName.replace('.', '_'), value); }
        };
    }
    public static Criterion like(String propertyName, Object value) {
        return new Criterion() {
            public String toSqlString() { return "lower(do." + propertyName + ") like lower(:" + propertyName.replace('.', '_') + ")"; }
            public Map<String, Object> getParameters() { return Collections.singletonMap(propertyName.replace('.', '_'), value); }
        };
    }
    public static Criterion between(String propertyName, Object lo, Object hi) {
        return new Criterion() {
            public String toSqlString() { return "do." + propertyName + " between :" + propertyName.replace('.', '_') + "_lo and :" + propertyName.replace('.', '_') + "_hi"; }
            public Map<String, Object> getParameters() { 
                Map<String, Object> map = new java.util.HashMap<>();
                map.put(propertyName.replace('.', '_') + "_lo", lo);
                map.put(propertyName.replace('.', '_') + "_hi", hi);
                return map;
            }
        };
    }
    public static Criterion in(String propertyName, java.util.Collection<?> values) {
        return new Criterion() {
            public String toSqlString() { return "do." + propertyName + " in (:" + propertyName.replace('.', '_') + ")"; }
            public Map<String, Object> getParameters() { return Collections.singletonMap(propertyName.replace('.', '_'), values); }
        };
    }
}
