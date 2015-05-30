package com.emolance.web.rest;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.validation.Valid;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import retrofit.RestAdapter;

import com.codahale.metrics.annotation.Timed;
import com.emolance.domain.Device;
import com.emolance.domain.ImageReport;
import com.emolance.domain.Report;
import com.emolance.repository.DeviceRepository;
import com.emolance.repository.ReportRepository;
import com.emolance.repository.UserRepository;
import com.emolance.service.ImageProcessService;
import com.emolance.service.ReportService;
import com.emolance.service.util.ParsePushUtil;
import com.emolance.web.rest.util.PaginationUtil;

/**
 * REST controller for managing Report.
 */
@RestController
@RequestMapping("/api")
public class ReportResource {

    private final Logger log = LoggerFactory.getLogger(ReportResource.class);

    @Inject
    private ReportRepository reportRepository;
    @Inject
    private UserRepository userRepository;
    @Inject
    private DeviceRepository deviceRepository;
    @Inject
    private ImageProcessService imageProcessService;


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

    @RequestMapping(value = "/reports/trigger/process",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> triggerReport() throws URISyntaxException {
    	String name = SecurityContextHolder.getContext().getAuthentication().getName();
    	log.info("Start processing photos for user: " + name);
    	Optional<com.emolance.domain.User> user = userRepository.findOneByLogin(name);

    	// trigger process
    	List<Device> devices = deviceRepository.findAllForCurrentUser();
    	Device targetDevice = devices.get(0);

    	RestAdapter restAdapter = new RestAdapter.Builder()
	        .setEndpoint("http://" + targetDevice.getSn() + ".emolance.ngrok.io")
	        .build();
    	ReportService reportService = restAdapter.create(ReportService.class);

    	ImageReport ir = reportService.triggerProcess(name);
    	log.info("Finished processing! The image is at: " + ir.getUrl());

    	// process image
    	BigDecimal res = imageProcessService.processImage(ir.getUrl());

    	if (res != null) {
	    	// report the result
	    	Report report = new Report();
	    	report.setTimestamp(new DateTime(ir.getTimestamp()));
	    	report.setType("NORMAL");
	    	report.setValue(res);
	    	report.setUserId(user.get());

			create(report);

	    	// send push notification
			ParsePushUtil.sendPushNotification(name, report);

	    	return ResponseEntity.ok().build();
    	} else {
    		log.equals("Failed to process the image and generate the report!");
    		return ResponseEntity.status(500).build();
    	}
    }
}
