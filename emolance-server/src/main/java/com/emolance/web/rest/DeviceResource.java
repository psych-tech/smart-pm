package com.emolance.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.emolance.domain.Device;
import com.emolance.repository.DeviceRepository;
import com.emolance.web.rest.util.PaginationUtil;

/**
 * REST controller for managing Device.
 */
@RestController
@RequestMapping("/api")
public class DeviceResource {

    private final Logger log = LoggerFactory.getLogger(DeviceResource.class);

    @Inject
    private DeviceRepository deviceRepository;

    /**
     * POST  /devices -> Create a new device.
     */
    @RequestMapping(value = "/devices",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> create(@RequestBody Device device) throws URISyntaxException {
        log.debug("REST request to save Device : {}", device);
        if (device.getId() != null) {
            return ResponseEntity.badRequest().header("Failure", "A new device cannot already have an ID").build();
        }
        deviceRepository.save(device);
        return ResponseEntity.created(new URI("/api/devices/" + device.getId())).build();
    }

    /**
     * PUT  /devices -> Updates an existing device.
     */
    @RequestMapping(value = "/devices",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> update(@RequestBody Device device) throws URISyntaxException {
        log.debug("REST request to update Device : {}", device);
        if (device.getId() == null) {
            return create(device);
        }
        deviceRepository.save(device);
        return ResponseEntity.ok().build();
    }

    /**
     * GET  /devices -> get all the devices.
     */
    @RequestMapping(value = "/devices",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Device>> getAll(@RequestParam(value = "page" , required = false) Integer offset,
                                  @RequestParam(value = "per_page", required = false) Integer limit)
        throws URISyntaxException {
        Page<Device> page = deviceRepository.findAll(PaginationUtil.generatePageRequest(offset, limit));
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/devices", offset, limit);
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /devices/:id -> get the "id" device.
     */
    @RequestMapping(value = "/devices/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Device> get(@PathVariable Long id) {
        log.debug("REST request to get Device : {}", id);
        return Optional.ofNullable(deviceRepository.findOne(id))
            .map(device -> new ResponseEntity<>(
                device,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /devices/:id -> delete the "id" device.
     */
    @RequestMapping(value = "/devices/{id}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public void delete(@PathVariable Long id) {
        log.debug("REST request to delete Device : {}", id);
        deviceRepository.delete(id);
    }

    @RequestMapping(value = "/devices/register/{sn}",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> registerDevice(@PathVariable(value = "sn") String sn)
        throws URISyntaxException {
    	List<Device> page = deviceRepository.findBySn(sn);
    	if (page.size() > 0) {
    		Device device = page.get(0);
    		device.setLastUpdate(new DateTime());
    		update(device);
    	} else {
    		Device device = new Device();
    		device.setRegTime(new DateTime());
    		device.setLastUpdate(device.getRegTime());
    		device.setSn(sn);
    		create(device);
    	}

        return ResponseEntity.ok().build();
    }
}
