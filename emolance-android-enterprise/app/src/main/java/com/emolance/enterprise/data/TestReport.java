package com.emolance.enterprise.data;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

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

    private EmoUser owner;

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

    @JsonIgnore
    public int getLevel() {
        try {
            int value = Integer.parseInt(getResultValue());
            return value / 10;
        } catch (Exception e) {
            return 1;
        }
    }

    @JsonIgnore
    public int getPercent() {
        try {
            return Integer.parseInt(getResultValue());
        } catch (Exception e) {
            return 3;
        }
    }
}
