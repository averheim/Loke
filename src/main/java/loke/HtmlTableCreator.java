package loke;

import java.util.ArrayList;
import java.util.List;

public class HtmlTableCreator {
    private final static String OUTER_DIV_STYLE = " font-family: 'arial'; ";
    private final static String INNER_DIV_STYLE = " overflow-x: auto; "
            + "width: 75%; "
            + "border: 1px solid #ddd; ";
    private final static String TABLE_STYLE = " border-collapse: collapse; "
            + "border-spacing: 0; "
            + "width: 100%; ";
    private final static String TH_TD_STYLE = " border: 1px solid #ddd; "
            + "text-align: left; "
            + "padding: 8px; ";
    private final StringBuilder sb;

    public HtmlTableCreator() {
        this.sb = new StringBuilder();
    }

    public String createTable(List<String> head, List<List<String>> bodys, String foot, String heading) {
        for (List<String> body : bodys) {
            if (body.size() % head.size() != 0) {
                throw new IllegalArgumentException("The head is not proportionate to the body. Head size: " + head.size() + " Body size: " + body.size());
            }
        }

        // empty string builder
        sb.setLength(0);
        sb.append("<div style=\"").append(OUTER_DIV_STYLE).append("\">");

        if (heading != null) {
            createHeading(heading);
        }

        sb.append("<div style=\"").append(INNER_DIV_STYLE).append("\">");
        sb.append("<table style=\"").append(TABLE_STYLE).append("\">");


        createHead(head);
        for (List<String> body : bodys) {
            createBody(body, head.size());
        }

        if (foot != null) {
            createFoot(foot, head.size());
        }

        sb.append("</table>");
        sb.append("</div>");
        sb.append("</div>");

        return sb.toString();
    }

    private void createHeading(String text) {
        sb.append("<h3 style=\"text-align: left;\">").append(text).append("</h3>");
    }

    private void createHead(List<String> head) {
        sb.append("<thead>");
        sb.append("<tr>");
        for (String string : head) {
            sb.append("<th nowrap style=\"").append(TH_TD_STYLE).append("\">").append(string).append("</th>");
        }
        sb.append("</tr>");
        sb.append("</thead>");
    }

    private void createBody(List<String> body, int rowLength) {
        List<List<String>> rows = getBodyRowsData(body, rowLength);
        int rowCount = 0;
        sb.append("<tbody>");
        for (List<String> row : rows) {
            sb.append((rowCount % 2 == 0) ? "<tr style=\"background-color: #f2f2f2\">" : "<tr>");
            for (String data : row) {
                sb.append("<td style=\"").append(TH_TD_STYLE).append("\">").append(data).append("</td>");
            }
            sb.append("</tr>");
            rowCount++;
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
        sb.append("<td style=\"").append(TH_TD_STYLE).append("\" colspan=\"").append(colspan).append("\">").append(foot).append("</td>");
        sb.append("</tr>");
        sb.append("</tfoot>");
    }
}
