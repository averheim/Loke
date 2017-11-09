package loke;

import loke.db.athena.AthenaClient;
import loke.model.Employee;
import loke.model.Report;
import loke.service.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CostReportGenerator {
    private Logger log = LogManager.getLogger(CostReportGenerator.class);
    private List<Service> employeeServices;
    private List<Service> adminServices;

    public CostReportGenerator(AthenaClient athena, String userOwnerRegExp, double showAccountThreshold, Map<String, String> csvAccounts, VelocityEngine velocityEngine) {
        this.employeeServices = new ArrayList<>();
        this.adminServices = new ArrayList<>();

        TotalSpendPerEmployee totalSpendPerEmployee = new TotalSpendPerEmployee(athena, userOwnerRegExp, showAccountThreshold);
        SpendPerEmployeeByResource spendPerEmployeeByResource = new SpendPerEmployeeByResource(athena, userOwnerRegExp, velocityEngine);
        SpendPerEmployeeByAccount spendPerEmployeeByAccount = new SpendPerEmployeeByAccount(athena, userOwnerRegExp, showAccountThreshold, csvAccounts, velocityEngine);
        ResourceStartedLastWeek resourceStartedLastWeek = new ResourceStartedLastWeek(athena, userOwnerRegExp, csvAccounts, velocityEngine);

        this.employeeServices.add(spendPerEmployeeByResource);
        this.employeeServices.add(spendPerEmployeeByAccount);
        this.employeeServices.add(resourceStartedLastWeek);

        this.adminServices.add(totalSpendPerEmployee);
        this.adminServices.add(spendPerEmployeeByAccount);
    }

    public List<Employee> generateReports() {
        return orderChartsByUser(getReports());
    }

    public List<Employee> generateAdminReports() {
        return orderChartsByUser(getAdminReports());
    }

    private List<Report> getReports() {
        log.info("Generating reports");
        List<Report> employeeReports = new ArrayList<>();
        for (Service service : employeeServices) {
            employeeReports.addAll(service.getReports());
        }
        log.info("Reports generated");
        return employeeReports;
    }

    private List<Report> getAdminReports() {
        log.info("Generating admin-reports");
        List<Report> adminReports = new ArrayList<>();
        for (Service service : adminServices) {
            adminReports.addAll(service.getReports());
        }
        log.info("Admin-reports generated");
        return adminReports;
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
