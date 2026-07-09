package org.hibernate.criterion;
public class Order {
    private String propertyName;
    private boolean ascending;
    protected Order(String propertyName, boolean ascending) {
        this.propertyName = propertyName;
        this.ascending = ascending;
    }
    public static Order asc(String propertyName) { return new Order(propertyName, true); }
    public static Order desc(String propertyName) { return new Order(propertyName, false); }
    public String toString() { return propertyName + (ascending ? " asc" : " desc"); }
}
