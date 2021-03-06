package com.tngtech.archunit.example.cycle.simplescenario.report;

import com.tngtech.archunit.example.cycle.simplescenario.importer.ImportService;

public class ReportService {
    private ImportService importService;

    public Report getReport(String customer) {
        if (isUnknown(customer)) {
            importService.process(customer);
        }
        return new Report();
    }

    private boolean isUnknown(String customer) {
        return true;
    }
}
