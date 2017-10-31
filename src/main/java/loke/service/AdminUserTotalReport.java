package loke.service;

import com.googlecode.charts4j.*;
import loke.model.Report;
import loke.service.dao.SpendPerUserDao;
import loke.utils.CalendarGenerator;
import loke.utils.DecimalFormatter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static com.googlecode.charts4j.Color.*;

public class AdminUserTotalReport implements Service {
    private static final Logger log = LogManager.getLogger(AdminUserTotalReport.class);
    private static final List<Calendar> THIRTY_DAYS_BACK = CalendarGenerator.getDaysBack(60);
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SpendPerUserDao spendPerUserDao;
    private int colorCounter = 0;

    public AdminUserTotalReport(SpendPerUserDao spendPerUserDao) {
        this.spendPerUserDao = spendPerUserDao;
    }


    @Override
    public List<Report> getReports() {
        Map<String, SpendPerUserDao.User> users = spendPerUserDao.getUsers();
        return generateReports(users);
    }

    private List<Report> generateReports(Map<String, SpendPerUserDao.User> users) {
        List<Report> reports = new ArrayList<>();
        for (SpendPerUserDao.User user : users.values()) {
            resetColor();
            Scale scale = checkScale(user);
            List<String> xAxisLabels = getXAxisLabels();
            List<Line> lineChartPlots = createPlots(user, scale);
            LineChart chart = GCharts.newLineChart(lineChartPlots);
            configureChart(xAxisLabels, chart, user, scale);
            Report report = new Report(user.getUserName());
            report.addHtmlURL(chart.toURLForHTML());
            reports.add(report);
        }
        return reports;
    }

    private Scale checkScale(SpendPerUserDao.User user) {
        List<Double> dailyCosts = new ArrayList<>();

        for (Calendar calendar : THIRTY_DAYS_BACK) {
            double dailyCost = 0.0;
            for (SpendPerUserDao.Resource resource : user.getResources().values()) {

                SpendPerUserDao.Day day = resource.getDays().get(dateFormat.format(calendar.getTime()));
                dailyCost += (day == null) ? 0.0 : day.getDailyCost();
            }
            dailyCosts.add(dailyCost);
        }

        dailyCosts.sort((o1, o2) -> (int) (o1 + o2));

        if (dailyCosts.get(0) > 100) return Scale.OVER_HUNDRED;
        if (dailyCosts.get(0) < 10) return Scale.UNDER_TEN;
        return Scale.UNDER_HUNDRED;
    }

    private List<String> getXAxisLabels() {
        List<String> labels = new ArrayList<>();

        // add labels
        for (Calendar day : THIRTY_DAYS_BACK) {
            String date = dateFormat.format(day.getTime());
            if (!labels.contains(date)) {
                labels.add(date.substring(8, 10));
            }
        }
        return labels;
    }

    private void configureChart(List<String> daysXAxisLabels, LineChart chart, SpendPerUserDao.User user, Scale scale) {
        int chartWidth = 1000;
        int chartHeight = 300;
        chart.addYAxisLabels(AxisLabelsFactory.newNumericAxisLabels(scale.getyAxisLabels()));
        chart.addXAxisLabels(AxisLabelsFactory.newAxisLabels(daysXAxisLabels));
        chart.addYAxisLabels(AxisLabelsFactory.newAxisLabels("Cost in " + scale.getSuffix(), 50));
        chart.addXAxisLabels(AxisLabelsFactory.newAxisLabels("Day", 50));
        chart.setSize(chartWidth, chartHeight);
        chart.setTitle("Total cost for " + user.getUserName() + " the past 30 days " + DecimalFormatter.format(user.calculateTotalCost(), 2) + " USD");
    }

    private List<Line> createPlots(SpendPerUserDao.User user, Scale scale) {
        List<Line> plots = new ArrayList<>();
        log.info(user.getUserName());
        List<Double> lineSizeValues = getLineSize(user.getResources(), scale);
        Line lineChartPlot = Plots.newLine(Data.newData(lineSizeValues), getNextColor());
        plots.add(0, lineChartPlot);
        return plots;
    }

    private List<Double> getLineSize(Map<String, SpendPerUserDao.Resource> resources, Scale scale) {
        List<Double> lineSizeValues = new ArrayList<>();

        for (Calendar calendar : THIRTY_DAYS_BACK) {
            double total = 0;
            for (SpendPerUserDao.Resource resource : resources.values()) {
                SpendPerUserDao.Day day = resource.getDays().get(dateFormat.format(calendar.getTime()));
                if (day != null) {
                    total += day.getDailyCost() / scale.getDivideBy();
                }
            }
            lineSizeValues.add(total);
        }
        return lineSizeValues;
    }

    private void resetColor() {
        this.colorCounter = 0;
    }

    private Color getNextColor() {
        List<Color> colors = new ArrayList<>();
        colors.add(BLUE);
        colors.add(RED);
        colors.add(YELLOW);
        colors.add(GREEN);
        colors.add(GRAY);
        colors.add(AQUAMARINE);
        colors.add(ORANGE);
        Color color = colors.get(colorCounter);
        colorCounter++;
        if (colorCounter == colors.size()) {
            colorCounter = 0;
        }
        return color;
    }


}
