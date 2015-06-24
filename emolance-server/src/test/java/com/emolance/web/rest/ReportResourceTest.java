package com.emolance.web.rest;

import com.emolance.Application;
import com.emolance.domain.Report;
import com.emolance.repository.ReportRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.hasItem;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the ReportResource REST controller.
 *
 * @see ReportResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class ReportResourceTest {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private static final String DEFAULT_TYPE = "SAMPLE_TEXT";
    private static final String UPDATED_TYPE = "UPDATED_TEXT";

    private static final BigDecimal DEFAULT_VALUE = new BigDecimal(0);
    private static final BigDecimal UPDATED_VALUE = new BigDecimal(1);

    private static final DateTime DEFAULT_TIMESTAMP = new DateTime(0L, DateTimeZone.UTC);
    private static final DateTime UPDATED_TIMESTAMP = new DateTime(DateTimeZone.UTC).withMillisOfSecond(0);
    private static final String DEFAULT_TIMESTAMP_STR = dateTimeFormatter.print(DEFAULT_TIMESTAMP);
    private static final String DEFAULT_QRCODE = "SAMPLE_TEXT";
    private static final String UPDATED_QRCODE = "UPDATED_TEXT";
    private static final String DEFAULT_STATUS = "SAMPLE_TEXT";
    private static final String UPDATED_STATUS = "UPDATED_TEXT";
    private static final String DEFAULT_NAME = "SAMPLE_TEXT";
    private static final String UPDATED_NAME = "UPDATED_TEXT";
    private static final String DEFAULT_LINK = "SAMPLE_TEXT";
    private static final String UPDATED_LINK = "UPDATED_TEXT";
    private static final String DEFAULT_AGE = "SAMPLE_TEXT";
    private static final String UPDATED_AGE = "UPDATED_TEXT";
    private static final String DEFAULT_POSITION = "SAMPLE_TEXT";
    private static final String UPDATED_POSITION = "UPDATED_TEXT";
    private static final String DEFAULT_EMAIL = "SAMPLE_TEXT";
    private static final String UPDATED_EMAIL = "UPDATED_TEXT";
    private static final String DEFAULT_RESULT = "SAMPLE_TEXT";
    private static final String UPDATED_RESULT = "UPDATED_TEXT";

    @Inject
    private ReportRepository reportRepository;

    private MockMvc restReportMockMvc;

    private Report report;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ReportResource reportResource = new ReportResource();
        ReflectionTestUtils.setField(reportResource, "reportRepository", reportRepository);
        this.restReportMockMvc = MockMvcBuilders.standaloneSetup(reportResource).build();
    }

    @Before
    public void initTest() {
        report = new Report();
        report.setType(DEFAULT_TYPE);
        report.setValue(DEFAULT_VALUE);
        report.setTimestamp(DEFAULT_TIMESTAMP);
        report.setQrcode(DEFAULT_QRCODE);
        report.setStatus(DEFAULT_STATUS);
        report.setName(DEFAULT_NAME);
        report.setLink(DEFAULT_LINK);
        report.setAge(DEFAULT_AGE);
        report.setPosition(DEFAULT_POSITION);
        report.setEmail(DEFAULT_EMAIL);
        report.setResult(DEFAULT_RESULT);
    }

    @Test
    @Transactional
    public void createReport() throws Exception {
        int databaseSizeBeforeCreate = reportRepository.findAll().size();

        // Create the Report
        restReportMockMvc.perform(post("/api/reports")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(report)))
                .andExpect(status().isCreated());

        // Validate the Report in the database
        List<Report> reports = reportRepository.findAll();
        assertThat(reports).hasSize(databaseSizeBeforeCreate + 1);
        Report testReport = reports.get(reports.size() - 1);
        assertThat(testReport.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testReport.getValue()).isEqualTo(DEFAULT_VALUE);
        assertThat(testReport.getTimestamp().toDateTime(DateTimeZone.UTC)).isEqualTo(DEFAULT_TIMESTAMP);
        assertThat(testReport.getQrcode()).isEqualTo(DEFAULT_QRCODE);
        assertThat(testReport.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testReport.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testReport.getLink()).isEqualTo(DEFAULT_LINK);
        assertThat(testReport.getAge()).isEqualTo(DEFAULT_AGE);
        assertThat(testReport.getPosition()).isEqualTo(DEFAULT_POSITION);
        assertThat(testReport.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(testReport.getResult()).isEqualTo(DEFAULT_RESULT);
    }

    @Test
    @Transactional
    public void checkValueIsRequired() throws Exception {
        // Validate the database is empty
        assertThat(reportRepository.findAll()).hasSize(0);
        // set the field null
        report.setValue(null);

        // Create the Report, which fails.
        restReportMockMvc.perform(post("/api/reports")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(report)))
                .andExpect(status().isBadRequest());

        // Validate the database is still empty
        List<Report> reports = reportRepository.findAll();
        assertThat(reports).hasSize(0);
    }

    @Test
    @Transactional
    public void getAllReports() throws Exception {
        // Initialize the database
        reportRepository.saveAndFlush(report);

        // Get all the reports
        restReportMockMvc.perform(get("/api/reports"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(report.getId().intValue())))
                .andExpect(jsonPath("$.[*].type").value(hasItem(DEFAULT_TYPE.toString())))
                .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE.intValue())))
                .andExpect(jsonPath("$.[*].timestamp").value(hasItem(DEFAULT_TIMESTAMP_STR)))
                .andExpect(jsonPath("$.[*].qrcode").value(hasItem(DEFAULT_QRCODE.toString())))
                .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
                .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
                .andExpect(jsonPath("$.[*].link").value(hasItem(DEFAULT_LINK.toString())))
                .andExpect(jsonPath("$.[*].age").value(hasItem(DEFAULT_AGE.toString())))
                .andExpect(jsonPath("$.[*].position").value(hasItem(DEFAULT_POSITION.toString())))
                .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL.toString())))
                .andExpect(jsonPath("$.[*].result").value(hasItem(DEFAULT_RESULT.toString())));
    }

    @Test
    @Transactional
    public void getReport() throws Exception {
        // Initialize the database
        reportRepository.saveAndFlush(report);

        // Get the report
        restReportMockMvc.perform(get("/api/reports/{id}", report.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(report.getId().intValue()))
            .andExpect(jsonPath("$.type").value(DEFAULT_TYPE.toString()))
            .andExpect(jsonPath("$.value").value(DEFAULT_VALUE.intValue()))
            .andExpect(jsonPath("$.timestamp").value(DEFAULT_TIMESTAMP_STR))
            .andExpect(jsonPath("$.qrcode").value(DEFAULT_QRCODE.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.link").value(DEFAULT_LINK.toString()))
            .andExpect(jsonPath("$.age").value(DEFAULT_AGE.toString()))
            .andExpect(jsonPath("$.position").value(DEFAULT_POSITION.toString()))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL.toString()))
            .andExpect(jsonPath("$.result").value(DEFAULT_RESULT.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingReport() throws Exception {
        // Get the report
        restReportMockMvc.perform(get("/api/reports/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateReport() throws Exception {
        // Initialize the database
        reportRepository.saveAndFlush(report);

		int databaseSizeBeforeUpdate = reportRepository.findAll().size();

        // Update the report
        report.setType(UPDATED_TYPE);
        report.setValue(UPDATED_VALUE);
        report.setTimestamp(UPDATED_TIMESTAMP);
        report.setQrcode(UPDATED_QRCODE);
        report.setStatus(UPDATED_STATUS);
        report.setName(UPDATED_NAME);
        report.setLink(UPDATED_LINK);
        report.setAge(UPDATED_AGE);
        report.setPosition(UPDATED_POSITION);
        report.setEmail(UPDATED_EMAIL);
        report.setResult(UPDATED_RESULT);
        restReportMockMvc.perform(put("/api/reports")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(report)))
                .andExpect(status().isOk());

        // Validate the Report in the database
        List<Report> reports = reportRepository.findAll();
        assertThat(reports).hasSize(databaseSizeBeforeUpdate);
        Report testReport = reports.get(reports.size() - 1);
        assertThat(testReport.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testReport.getValue()).isEqualTo(UPDATED_VALUE);
        assertThat(testReport.getTimestamp().toDateTime(DateTimeZone.UTC)).isEqualTo(UPDATED_TIMESTAMP);
        assertThat(testReport.getQrcode()).isEqualTo(UPDATED_QRCODE);
        assertThat(testReport.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testReport.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testReport.getLink()).isEqualTo(UPDATED_LINK);
        assertThat(testReport.getAge()).isEqualTo(UPDATED_AGE);
        assertThat(testReport.getPosition()).isEqualTo(UPDATED_POSITION);
        assertThat(testReport.getEmail()).isEqualTo(UPDATED_EMAIL);
        assertThat(testReport.getResult()).isEqualTo(UPDATED_RESULT);
    }

    @Test
    @Transactional
    public void deleteReport() throws Exception {
        // Initialize the database
        reportRepository.saveAndFlush(report);

		int databaseSizeBeforeDelete = reportRepository.findAll().size();

        // Get the report
        restReportMockMvc.perform(delete("/api/reports/{id}", report.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        List<Report> reports = reportRepository.findAll();
        assertThat(reports).hasSize(databaseSizeBeforeDelete - 1);
    }
}
