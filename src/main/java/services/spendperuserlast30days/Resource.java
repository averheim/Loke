package services.spendperuserlast30days;

import java.util.HashMap;

public class Resource {
    private String resourceName;
    private HashMap<String, Day> days;

    public Resource(String resourceName) {
        this.resourceName = resourceName;
        this.days = new HashMap<>();
    }

    public void addDay(String key, Day day) {
        this.days.put(key, day);
    }

    public String getResourceName() {
        return resourceName;
    }

    public HashMap<String, Day> getDays() {
        return days;
    }
}
