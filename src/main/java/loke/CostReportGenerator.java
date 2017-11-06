package loke;

import loke.db.athena.AthenaClient;
import loke.model.Employee;
import loke.model.Report;
import loke.service.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CostReportGenerator {
    private Logger log = LogManager.getLogger(CostReportGenerator.class);
    private Map<String, Service> services;

    public CostReportGenerator(AthenaClient athena, HtmlTableCreator htmlTableCreator, String userOwnerRegExp, double showAccountThreshold, Map<String, String> accounts) {
        services = new HashMap<>();
        services.put(TotalSpendPerEmployee.class.getName(), new TotalSpendPerEmployee(athena, userOwnerRegExp));
        services.put(SpendPerEmployeeByResource.class.getName(), new SpendPerEmployeeByResource(athena, userOwnerRegExp, htmlTableCreator));
        services.put(SpendPerEmployeeByAccount.class.getName(), new SpendPerEmployeeByAccount(athena, htmlTableCreator, userOwnerRegExp,showAccountThreshold, accounts));
        services.put(ResourceStartedLastWeek.class.getName(), new ResourceStartedLastWeek(athena, htmlTableCreator, userOwnerRegExp, accounts));
    }

    public List<Employee> generateReports() {
        return orderChartsByUser(getReports());
    }

    public List<Employee> generateAdminReports() {
        return orderChartsByUser(getAdminReports());
    }

    private List<Report> getReports() {
        log.info("Generating reports");
        List<Report> reports = new ArrayList<>();
        reports.addAll(services.get(SpendPerEmployeeByResource.class.getName()).getReports());
        reports.addAll(services.get(SpendPerEmployeeByAccount.class.getName()).getReports());
        reports.addAll(services.get(ResourceStartedLastWeek.class.getName()).getReports());
        log.info("Reports generated");
        return reports;
    }

    private List<Report> getAdminReports() {
        log.info("Generating admin-reports");
        List<Report> reports = new ArrayList<>();
        reports.addAll(services.get(TotalSpendPerEmployee.class.getName()).getReports());
        reports.addAll(services.get(SpendPerEmployeeByAccount.class.getName()).getReports());
        log.info("Admin-reports generated");
        return reports;
    }

    private List<Employee> orderChartsByUser(List<Report> reports) {
        List<Employee> employees = new ArrayList<>();
        for (Report report : reports) {
            if (employees.stream().noneMatch(user -> user.getUserName().equals(report.getOwner()))) {
                employees.add(new Employee(report.getOwner()));
            }
            Employee employee = findUser(employees, report.getOwner());
            if (employee != null) {
                employee.getReports().add(report);
            }
        }
        return employees;
    }

    private Employee findUser(List<Employee> employees, String owner) {
        for (Employee employee : employees) {
            if (employee.getUserName().equals(owner)) {
                return employee;
            }
        }
        return null;
    }
}
