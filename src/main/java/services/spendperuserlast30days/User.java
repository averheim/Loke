package services.spendperuserlast30days;

import java.util.HashMap;

public class User {
    private String userName;
    private HashMap<String, Resource> resources;

    private double totalCost;

    public User(String userName) {
        this.userName = userName;
        this.resources = new HashMap<>();
        this.totalCost = 0;
    }

    public void addResource(Resource resource) {
        this.resources.put(resource.getResourceName(), resource);
    }

    public String getUserName() {
        return userName;
    }

    public HashMap<String, Resource> getResources() {
        return resources;
    }

    public Resource getResource(String resourceName) {
        return this.resources.get(resourceName);
    }

    public double calculateTotalCost() {
        for (Resource resource : resources.values()) {
            for (Day day : resource.getDays().values()) {
                this.totalCost += day.getDailyCost();
            }
        }
        return this.totalCost;
    }
}
