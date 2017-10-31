package loke.model;

import java.util.ArrayList;
import java.util.List;

public class Report {
    private String owner;
    private List<String> htmlURLs;
    private List<String> htmlTables;

    public Report(String owner) {
        this.owner = owner;
        this.htmlTables = new ArrayList<>();
        this.htmlURLs = new ArrayList<>();
    }

    public String getOwner() {
        return owner;
    }

    public void addHtmlURL(String htmlUrl) {
        this.htmlURLs.add(htmlUrl);
    }

    public void addHtmlURLs(List<String> htmlURLs) {
        this.htmlURLs.addAll(htmlURLs);
    }

    public List<String> getHtmlURLs() {
        return htmlURLs;
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
