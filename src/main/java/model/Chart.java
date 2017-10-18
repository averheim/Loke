package model;

import java.util.HashMap;

public class Chart {
    private String owner;
    private String htmlURL;
    private String htmlTable;

    public Chart(String owner, String htmlURL) {
        this.owner = owner;
        this.htmlURL = htmlURL;
        new HashMap<String, HashMap<String, String>>();
    }

    public String getOwner() {
        return owner;
    }

    public String getHtmlURL() {
        return htmlURL;
    }

    public String getHtmlTable() {
        return htmlTable;
    }

    public void setHtmlTable(String htmlTable) {
        this.htmlTable = htmlTable;
    }
}
