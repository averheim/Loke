package loke.model;

import java.util.ArrayList;
import java.util.List;

public class Report {
    private String owner;
    private String chartUrl;
    private List<String> htmlTables;

    public Report(String owner) {
        this.owner = owner;
        this.htmlTables = new ArrayList<>();
    }

    public String getOwner() {
        return owner;
    }

    public String getChartUrl() {
        return chartUrl;
    }

    public void setChartUrl(String chartUrl) {
        this.chartUrl = chartUrl;
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
