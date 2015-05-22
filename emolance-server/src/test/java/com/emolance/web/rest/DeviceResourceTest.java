package com.emolance.web.rest;

import com.emolance.Application;
import com.emolance.domain.Device;
import com.emolance.repository.DeviceRepository;

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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the DeviceResource REST controller.
 *
 * @see DeviceResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class DeviceResourceTest {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private static final String DEFAULT_SN = "SAMPLE_TEXT";
    private static final String UPDATED_SN = "UPDATED_TEXT";

    private static final DateTime DEFAULT_REG_TIME = new DateTime(0L, DateTimeZone.UTC);
    private static final DateTime UPDATED_REG_TIME = new DateTime(DateTimeZone.UTC).withMillisOfSecond(0);
    private static final String DEFAULT_REG_TIME_STR = dateTimeFormatter.print(DEFAULT_REG_TIME);

    private static final DateTime DEFAULT_LAST_UPDATE = new DateTime(0L, DateTimeZone.UTC);
    private static final DateTime UPDATED_LAST_UPDATE = new DateTime(DateTimeZone.UTC).withMillisOfSecond(0);
    private static final String DEFAULT_LAST_UPDATE_STR = dateTimeFormatter.print(DEFAULT_LAST_UPDATE);
    private static final String DEFAULT_STATUS = "SAMPLE_TEXT";
    private static final String UPDATED_STATUS = "UPDATED_TEXT";

    @Inject
    private DeviceRepository deviceRepository;

    private MockMvc restDeviceMockMvc;

    private Device device;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        DeviceResource deviceResource = new DeviceResource();
        ReflectionTestUtils.setField(deviceResource, "deviceRepository", deviceRepository);
        this.restDeviceMockMvc = MockMvcBuilders.standaloneSetup(deviceResource).build();
    }

    @Before
    public void initTest() {
        device = new Device();
        device.setSn(DEFAULT_SN);
        device.setRegTime(DEFAULT_REG_TIME);
        device.setLastUpdate(DEFAULT_LAST_UPDATE);
        device.setStatus(DEFAULT_STATUS);
    }

    @Test
    @Transactional
    public void createDevice() throws Exception {
        int databaseSizeBeforeCreate = deviceRepository.findAll().size();

        // Create the Device
        restDeviceMockMvc.perform(post("/api/devices")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(device)))
                .andExpect(status().isCreated());

        // Validate the Device in the database
        List<Device> devices = deviceRepository.findAll();
        assertThat(devices).hasSize(databaseSizeBeforeCreate + 1);
        Device testDevice = devices.get(devices.size() - 1);
        assertThat(testDevice.getSn()).isEqualTo(DEFAULT_SN);
        assertThat(testDevice.getRegTime().toDateTime(DateTimeZone.UTC)).isEqualTo(DEFAULT_REG_TIME);
        assertThat(testDevice.getLastUpdate().toDateTime(DateTimeZone.UTC)).isEqualTo(DEFAULT_LAST_UPDATE);
        assertThat(testDevice.getStatus()).isEqualTo(DEFAULT_STATUS);
    }

    @Test
    @Transactional
    public void getAllDevices() throws Exception {
        // Initialize the database
        deviceRepository.saveAndFlush(device);

        // Get all the devices
        restDeviceMockMvc.perform(get("/api/devices"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*].id").value(hasItem(device.getId().intValue())))
                .andExpect(jsonPath("$.[*].sn").value(hasItem(DEFAULT_SN.toString())))
                .andExpect(jsonPath("$.[*].regTime").value(hasItem(DEFAULT_REG_TIME_STR)))
                .andExpect(jsonPath("$.[*].lastUpdate").value(hasItem(DEFAULT_LAST_UPDATE_STR)))
                .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())));
    }

    @Test
    @Transactional
    public void getDevice() throws Exception {
        // Initialize the database
        deviceRepository.saveAndFlush(device);

        // Get the device
        restDeviceMockMvc.perform(get("/api/devices/{id}", device.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(device.getId().intValue()))
            .andExpect(jsonPath("$.sn").value(DEFAULT_SN.toString()))
            .andExpect(jsonPath("$.regTime").value(DEFAULT_REG_TIME_STR))
            .andExpect(jsonPath("$.lastUpdate").value(DEFAULT_LAST_UPDATE_STR))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingDevice() throws Exception {
        // Get the device
        restDeviceMockMvc.perform(get("/api/devices/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateDevice() throws Exception {
        // Initialize the database
        deviceRepository.saveAndFlush(device);

		int databaseSizeBeforeUpdate = deviceRepository.findAll().size();

        // Update the device
        device.setSn(UPDATED_SN);
        device.setRegTime(UPDATED_REG_TIME);
        device.setLastUpdate(UPDATED_LAST_UPDATE);
        device.setStatus(UPDATED_STATUS);
        restDeviceMockMvc.perform(put("/api/devices")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(device)))
                .andExpect(status().isOk());

        // Validate the Device in the database
        List<Device> devices = deviceRepository.findAll();
        assertThat(devices).hasSize(databaseSizeBeforeUpdate);
        Device testDevice = devices.get(devices.size() - 1);
        assertThat(testDevice.getSn()).isEqualTo(UPDATED_SN);
        assertThat(testDevice.getRegTime().toDateTime(DateTimeZone.UTC)).isEqualTo(UPDATED_REG_TIME);
        assertThat(testDevice.getLastUpdate().toDateTime(DateTimeZone.UTC)).isEqualTo(UPDATED_LAST_UPDATE);
        assertThat(testDevice.getStatus()).isEqualTo(UPDATED_STATUS);
    }

    @Test
    @Transactional
    public void deleteDevice() throws Exception {
        // Initialize the database
        deviceRepository.saveAndFlush(device);

		int databaseSizeBeforeDelete = deviceRepository.findAll().size();

        // Get the device
        restDeviceMockMvc.perform(delete("/api/devices/{id}", device.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        List<Device> devices = deviceRepository.findAll();
        assertThat(devices).hasSize(databaseSizeBeforeDelete - 1);
    }
}
