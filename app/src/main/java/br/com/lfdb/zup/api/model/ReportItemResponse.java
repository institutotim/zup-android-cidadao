package br.com.lfdb.zup.api.model;

import java.io.Serializable;

public class ReportItemResponse implements Serializable {

    private ReportItem report;

    public ReportItem getReport() {
        return report;
    }

    public void setReport(ReportItem report) {
        this.report = report;
    }
}
