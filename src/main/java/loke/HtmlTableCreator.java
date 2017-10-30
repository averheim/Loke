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
            + "width: 100%; "
            + "font-size: small; ";
    private final static String TH_TD_STYLE = " border: 1px solid #ddd; "
            + "padding: 8px; ";
    private final static String LEFT_ALIGN = " text-align: left; ";
    private final static String RIGHT_ALIGN = " text-align: right; ";
    private final static String FIXED_WIDTH = " width: 270px; ";
    private final StringBuilder sb;

    public HtmlTableCreator() {
        this.sb = new StringBuilder();
    }

    public String createTable(List<String> head, List<String> body, String foot, String heading, boolean leftAligned) {
        if (body.size() % head.size() != 0) {
            throw new IllegalArgumentException("The head is not proportionate to the body. " +
                    "Head size: " + head.size() + " Body size: " + body.size());
        }
        sb.setLength(0);
        sb.append("<div style=\"").append(OUTER_DIV_STYLE).append("\">");
        if (heading != null) {
            createHeading(heading);
        }
        sb.append("<div style=\"").append(INNER_DIV_STYLE).append("\">")
                .append("<table style=\"").append(TABLE_STYLE).append("\">");
        createHead(head);

        createBody(body, head.size(), leftAligned);

        if (foot != null) {
            createFoot(foot, head.size());
        }
        sb.append("</table>")
                .append("</div>")
                .append("</div>");

        return sb.toString();
    }

    public String createMarkedRowTable(List<String> head, List<List<String>> bodies, String foot, String heading, String rowMarkKeyWord) {
        for (List<String> body : bodies) {
            if (body.size() % head.size() != 0) {
                throw new IllegalArgumentException("The head is not proportionate to the body. " +
                        "Head size: " + head.size() + " Body size: " + body.size());
            }
        }
        sb.setLength(0);
        sb.append("<div style=\"").append(OUTER_DIV_STYLE).append("\">");
        if (heading != null) {
            createHeading(heading);
        }
        sb.append("<div style=\"").append(INNER_DIV_STYLE).append("\">")
                .append("<table style=\"").append(TABLE_STYLE).append("\">");
        createHead(head);

        for (List<String> body : bodies) {
            createMarkedRowBody(body, head.size(), rowMarkKeyWord);
        }

        if (foot != null) {
            createFoot(foot, head.size());
        }
        sb.append("</table>").append("</div>").append("</div>");

        return sb.toString();
    }

    private void createHeading(String text) {
        sb.append("<h4 style=\"text-align: left;\">").append(text).append("</h4>");
    }

    private void createHead(List<String> head) {
        sb.append("<thead>").append("<tr>");

        for (int i = 0; i < head.size(); i++) {
            if (i == 0) {
                sb.append("<th nowrap style=\"")
                        .append(TH_TD_STYLE)
                        .append(FIXED_WIDTH)
                        .append("\">").append(head.get(i))
                        .append("</th>");
            } else {
                sb.append("<th nowrap style=\"")
                        .append(TH_TD_STYLE)
                        .append("\">").append(head.get(i))
                        .append("</th>");
            }
        }
        sb.append("</tr>").append("</thead>");
    }

    private void createMarkedRowBody(List<String> body, int rowLength, String accountRowKeyWord) {
        List<List<String>> rows = getBodyRowsData(body, rowLength);
        sb.append("<tbody>");
        for (List<String> row : rows) {
            for (String s : row) {
                sb.append((s.contains(accountRowKeyWord)) ? "<tr style=\"background-color: #b3ccff;\">" : "<tr>");
                break;
            }
            for (int i = 0; i < row.size(); i++) {
                if (i == 0) {
                    sb.append("<td style=\"").append(TH_TD_STYLE).append(LEFT_ALIGN).append(FIXED_WIDTH).append("\">")
                            .append(row.get(i)).append("</td>");
                } else {
                    sb.append("<td style=\"").append(TH_TD_STYLE).append(RIGHT_ALIGN).append("\">")
                            .append(row.get(i)).append("</td>");
                }
            }
            sb.append("</tr>");
        }
        sb.append("</tbody>");
    }

    private void createBody(List<String> body, int rowLength, boolean leftAligned) {
        List<List<String>> rows = getBodyRowsData(body, rowLength);
        int rowCount = 0;
        sb.append("<tbody>");
        for (List<String> row : rows) {
            sb.append((rowCount % 2 == 0) ? "<tr style=\"background-color: #f2f2f2\">" : "<tr>");

            for (int i = 0; i < row.size(); i++) {
                if (i == 0) {
                    sb.append("<td style=\"").append(TH_TD_STYLE)
                            .append(LEFT_ALIGN)
                            .append(FIXED_WIDTH)
                            .append("\">")
                            .append(row.get(i))
                            .append("</td>");
                } else if(i == row.size() -1) {
                    sb.append("<td style=\"")
                            .append(TH_TD_STYLE)
                            .append(RIGHT_ALIGN)
                            .append("\">")
                            .append(row.get(i))
                            .append("</td>");
                } else {
                    sb.append("<td style=\"")
                            .append(TH_TD_STYLE)
                            .append((leftAligned) ? LEFT_ALIGN : RIGHT_ALIGN)
                            .append("\">")
                            .append(row.get(i))
                            .append("</td>");
                }
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
        sb.append("<tfoot>")
                .append("<tr>")
                .append("<td style=\"")
                .append(TH_TD_STYLE)
                .append("background-color: #428aff;")
                .append("\" colspan=\"")
                .append(colspan)
                .append("\">")
                .append(foot)
                .append("</td>")
                .append("</tr>")
                .append("</tfoot>");
    }
}
