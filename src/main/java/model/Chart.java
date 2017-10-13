package model;

public class Chart {
    private String owner;
    private String htmlURL;

    public Chart(String owner, String htmlURL) {
        this.owner = owner;
        this.htmlURL = htmlURL;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getHtmlURL() {
        return htmlURL;
    }

    public void setHtmlURL(String htmlURL) {
        this.htmlURL = htmlURL;
    }
}
