package com.emolance.enterprise.data;


import com.emolance.enterprise.util.DateUtils;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.Date;

/**
 * A TestReport.
 */
public class TestReport implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String reportCode;

    private String resultValue;

    private String resultData;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String reportDate;

    private String status;

    private Integer level;

    public Integer getLevel() {
        return level;
    }

    public Double getPercent() {
        return percent;
    }

    private Double percent;

    private Double val1;

    private Double val2;

    private Double val3;

    private EmoUser owner;

    public void setLevel(Integer level) {
        this.level = level;
    }

    public void setPercent(Double percent) {
        this.percent = percent;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getVal1() {
        return val1;
    }

    public void setVal1(Double val1) {
        this.val1 = val1;
    }

    public Double getVal2() {
        return val2;
    }

    public void setVal2(Double val2) {
        this.val2 = val2;
    }

    public Double getVal3() {
        return val3;
    }

    public void setVal3(Double val3) {
        this.val3 = val3;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReportCode() {
        return reportCode;
    }

    public TestReport reportCode(String reportCode) {
        this.reportCode = reportCode;
        return this;
    }

    public void setReportCode(String reportCode) {
        this.reportCode = reportCode;
    }

    public String getResultValue() {
        return resultValue;
    }

    public TestReport resultValue(String resultValue) {
        this.resultValue = resultValue;
        return this;
    }

    public void setResultValue(String resultValue) {
        this.resultValue = resultValue;
    }

    public String getResultData() {
        return resultData;
    }

    public TestReport resultData(String resultData) {
        this.resultData = resultData;
        return this;
    }

    public void setResultData(String resultData) {
        this.resultData = resultData;
    }

    public String getReportDate() {
        return reportDate;
    }

    public TestReport reportDate(String reportDate) {
        this.reportDate = reportDate;
        return this;
    }

    public void setReportDate(String reportDate) {
        this.reportDate = reportDate;
    }

    public EmoUser getOwner() {
        return owner;
    }

    public TestReport owner(EmoUser emoUser) {
        this.owner = emoUser;
        return this;
    }

    public Date getReportDateinMillseconds(){ return DateUtils.getMillisecondsFromDate(reportDate); }

    public void setOwner(EmoUser emoUser) {
        this.owner = emoUser;
    }

    @Override
    public String toString() {
        return "TestReport{" +
            "id=" + id +
            ", reportCode='" + reportCode + "'" +
            ", resultValue='" + resultValue + "'" +
            ", resultData='" + resultData + "'" +
            ", reportDate='" + reportDate + "'" +
            '}';
    }
}
