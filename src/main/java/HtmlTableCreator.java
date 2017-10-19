package db;

import java.util.ArrayList;
import java.util.List;

public class HtmlTableCreator {
    private final StringBuilder sb;

    public HtmlTableCreator() {
        this.sb = new StringBuilder();
    }

    public String createTable(List<String> head, List<String> body, String foot) {
        sb.append("<div style=\"overflow: scroll;\">");
        sb.append("<table class=\"table table-hover table-bordered table-responsive\">");
        createHead(head);
        createBody(body, head.size());
        createFoot(foot, head.size());
        sb.append("</table>");
        sb.append("</div>");

        return sb.toString();
    }

    private void createHead(List<String> head) {
        sb.append("<thead>");
        for (String string : head) {
            sb.append("<th>").append(string).append("</th>");
        }
        sb.append("</thead>");
    }

    private void createBody(List<String> body, int rowLength) {
        List<List<String>> rows = getBodyRowsData(body, rowLength);

        sb.append("<tbody>");
        for (List<String> row : rows) {
            sb.append("<tr>");
            for (String data : row) {
                sb.append("<td>").append(data).append("</td>");
            }
            sb.append("</tr>");
        }
        sb.append("</tbody>");
    }

    private List<List<String>> getBodyRowsData(List<String> body, int rowLength) {
        List<List<String>> rows = new ArrayList<>();
        int counter = 0;
        List<String> row = new ArrayList<>();
        for (String string : body) {
            row.add(string);
            counter++;
            if (counter == rowLength) {
                rows.add(row);
                row = new ArrayList<>();
                counter = 0;
            }
        }
        return rows;
    }

    private void createFoot(String foot, int colspan) {
        sb.append("<tfoot>");
        sb.append("<tr>");
        sb.append("<td colspan=\"").append(colspan).append("\">").append(foot).append("</td>");
        sb.append("</tr>");
        sb.append("</tfoot>");
    }
}
