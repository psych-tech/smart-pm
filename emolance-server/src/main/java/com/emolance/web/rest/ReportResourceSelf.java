package com.emolance.web.rest;

import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import retrofit.RestAdapter;

import com.codahale.metrics.annotation.Timed;
import com.emolance.domain.Device;
import com.emolance.domain.ImageReport;
import com.emolance.domain.Report;
import com.emolance.domain.ReportStatus;
import com.emolance.domain.User;
import com.emolance.repository.DeviceRepository;
import com.emolance.repository.ReportRepository;
import com.emolance.repository.UserRepository;
import com.emolance.service.ImageProcessService;
import com.emolance.service.ReportService;
import com.emolance.service.util.ParsePushUtil;

/**
 * REST controller for managing Report.
 */
@RestController
@RequestMapping("/api")
public class ReportResourceSelf {

	private final Logger log = LoggerFactory.getLogger(ReportResourceSelf.class);

	@Inject
	private ReportRepository reportRepository;
	@Inject
	private UserRepository userRepository;
	@Inject
	private DeviceRepository deviceRepository;
	@Inject
	private ImageProcessService imageProcessService;

	@RequestMapping(value = "/reports/user/create/{qrcode}",
			method = RequestMethod.POST,
			produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<Void> userCreateReport(
			@PathVariable("qrcode") String qrcode,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "link", required = false) String link) throws URISyntaxException {

		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		log.info("Received the qrcode report from user: " + username + " qr: " + qrcode);
		Optional<com.emolance.domain.User> user = userRepository.findOneByLogin(username);

		// report the result
		Report report = new Report();
		report.setTimestamp(new DateTime());
		report.setName(name);
		report.setLink(link);
		report.setType("NORMAL");
		report.setQrcode(qrcode);
		report.setStatus(ReportStatus.READY.toString());
		report.setUserId(user.get());
		report.setValue(new BigDecimal(-1));

		reportRepository.save(report);

		return ResponseEntity.ok().build();
	}

	@RequestMapping(value = "/reports/test/first/report",
			method = RequestMethod.GET,
			produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<List<Report>> getFirstReadyReport() throws URISyntaxException {
		return new ResponseEntity<List<Report>>(reportRepository.findFirstReadyReport(), HttpStatus.OK);
	}

	@RequestMapping(value = "/reports/device/trigger/process/{sn}",
			method = RequestMethod.POST,
			produces = MediaType.APPLICATION_JSON_VALUE)
	@Timed
	public ResponseEntity<Void> deviceReturnReport(
			@PathVariable(value = "sn") String sn,
			@RequestParam(value = "qrcode", required = false) String qrcode) throws URISyntaxException {
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		log.info("Trigger the report from device sn: " + sn);
		Optional<com.emolance.domain.User> user = userRepository.findOneByLogin(name);

		Report report = null;
		List<Report> reports = qrcode == null ?
				reportRepository.findFirstReadyReport()
				:
				reportRepository.findByQrcode(qrcode);

		if (reports == null || reports.size() == 0) {
			log.warn("No report is available");
			report = new Report();
			report.setTimestamp(new DateTime());
			report.setQrcode("SELF_GEN");
			report.setType("NO_USER_REPORT");
			report.setUserId(null);
		} else {
			report = reports.get(0);
		}

		try {
			// report the result
			RestAdapter restAdapter = new RestAdapter.Builder()
					.setEndpoint("http://" + sn + ".emolance.ngrok.io")
					.build();
			ReportService reportService = restAdapter.create(ReportService.class);

			ImageReport ir = reportService.triggerProcess(name);
			log.info("Finished processing! The image is at: " + ir.getUrl());

			// process image
			BigDecimal res = imageProcessService.processImage(ir.getUrl());

			// update result
			report.setValue(res);
			report.setStatus(ReportStatus.DONE.toString());
			report.setUserId(user.get());
			reportRepository.save(report);

			// send notification
			User userToNotify = report.getUserId();
			if (userToNotify != null) {
				String login = userToNotify.getLogin();
				ParsePushUtil.sendPushNotification(login, report);
			} else {
				log.warn("Unknow user. Didn't send push notificaiton!");
			}
		} catch (Exception e) {
			log.error("Failed to trigger the report from the device", e);
			report.setStatus(ReportStatus.ERROR.toString());
			report.setUserId(user.get());
			reportRepository.save(report);
		}

		return ResponseEntity.ok().build();
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
			report.setStatus(ReportStatus.DONE.toString());
			report.setUserId(user.get());

			reportRepository.save(report);

			// send push notification
			ParsePushUtil.sendPushNotification(name, report);

			return ResponseEntity.ok().build();
		} else {
			log.equals("Failed to process the image and generate the report!");
			return ResponseEntity.status(500).build();
		}
	}

}
