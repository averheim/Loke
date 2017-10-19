package model;

import java.util.HashMap;

public class Chart {
    private String owner;
    private String htmlURL;
    private String htmlTable;

    public Chart(String owner) {
        this.owner = owner;
    }

    public String getOwner() {
        return owner;
    }

    public String getHtmlURL() {
        return htmlURL;
    }

    public void setHtmlURL(String htmlURL) {
        this.htmlURL = htmlURL;
    }

    public String getHtmlTable() {
        return htmlTable;
    }

    public void setHtmlTable(String htmlTable) {
        this.htmlTable = htmlTable;
    }
}
