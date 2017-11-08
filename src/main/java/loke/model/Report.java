package loke.model;

public class Report {
    private String owner;
    private String chartUrl;
    private String htmlTable;

    public Report(String owner) {
        this.owner = owner;
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

    public String getHtmlTable() {
        return htmlTable;
    }

    public void setHtmlTable(String htmlTable) {
        this.htmlTable = htmlTable;
    }
}
