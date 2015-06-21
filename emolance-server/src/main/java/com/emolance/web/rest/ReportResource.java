package com.emolance.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.emolance.domain.Report;
import com.emolance.repository.ReportRepository;
import com.emolance.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Report.
 */
@RestController
@RequestMapping("/api")
public class ReportResource {

    private final Logger log = LoggerFactory.getLogger(ReportResource.class);

    @Inject
    private ReportRepository reportRepository;

    /**
     * POST  /reports -> Create a new report.
     */
    @RequestMapping(value = "/reports",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> create(@Valid @RequestBody Report report) throws URISyntaxException {
        log.debug("REST request to save Report : {}", report);
        if (report.getId() != null) {
            return ResponseEntity.badRequest().header("Failure", "A new report cannot already have an ID").build();
        }
        reportRepository.save(report);
        return ResponseEntity.created(new URI("/api/reports/" + report.getId())).build();
    }

    /**
     * PUT  /reports -> Updates an existing report.
     */
    @RequestMapping(value = "/reports",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> update(@Valid @RequestBody Report report) throws URISyntaxException {
        log.debug("REST request to update Report : {}", report);
        if (report.getId() == null) {
            return create(report);
        }
        reportRepository.save(report);
        return ResponseEntity.ok().build();
    }

    /**
     * GET  /reports -> get all the reports.
     */
    @RequestMapping(value = "/reports",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Report>> getAll(@RequestParam(value = "page" , required = false) Integer offset,
                                  @RequestParam(value = "per_page", required = false) Integer limit)
        throws URISyntaxException {
    	Page<Report> page = reportRepository.findAll(PaginationUtil.generatePageRequest(offset, limit, true));
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/reports", offset, limit);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /reports/:id -> get the "id" report.
     */
    @RequestMapping(value = "/reports/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Report> get(@PathVariable Long id) {
        log.debug("REST request to get Report : {}", id);
        return Optional.ofNullable(reportRepository.findOne(id))
            .map(report -> new ResponseEntity<>(
                report,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /reports/:id -> delete the "id" report.
     */
    @RequestMapping(value = "/reports/{id}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public void delete(@PathVariable Long id) {
        log.debug("REST request to delete Report : {}", id);
        reportRepository.delete(id);
    }
}
