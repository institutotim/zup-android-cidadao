package br.com.lfdb.zup.api.model;

import java.io.Serializable;
import java.util.List;

public class ReportCategory implements Serializable {

    private List<ReportCategoryStatus> statuses;
    private List<ReportCategory> subcategories;

    public List<ReportCategoryStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<ReportCategoryStatus> statuses) {
        this.statuses = statuses;
    }

    public List<ReportCategory> getSubcategories() {
        return subcategories;
    }

    public void setSubcategories(List<ReportCategory> subcategories) {
        this.subcategories = subcategories;
    }
}
