package loke.model;

import java.util.ArrayList;
import java.util.List;

public class Chart {
    private String owner;
    private String htmlURL;
    private List<String> htmlTables;

    public Chart(String owner) {
        this.owner = owner;
        this.htmlTables = new ArrayList<>();
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

    public List<String> getHtmlTables() {
        return htmlTables;
    }

    public void addHtmlTable(String htmlTable) {
        this.htmlTables.add(htmlTable);
    }

    public void addHtmlTables(List<String> htmlTables) {
        this.htmlTables.addAll(htmlTables);
    }
}
