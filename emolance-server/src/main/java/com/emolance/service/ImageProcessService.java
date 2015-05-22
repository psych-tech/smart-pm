package com.emolance.service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ImageProcessService {

    private final Logger log = LoggerFactory.getLogger(ImageProcessService.class);

    public BigDecimal processImage(String url) {
    	BigDecimal result = null;

    	// download
    	long timestamp = System.currentTimeMillis();
    	File tmpFile = null;
		try {
			tmpFile = File.createTempFile("image" + timestamp, ".jpg");
			FileUtils.copyURLToFile(new URL(url), tmpFile);

			// process
			result = new BigDecimal(tmpFile.length());

			// delete
			tmpFile.delete();
		} catch (IOException e) {
			log.error("Failed to download the image file");
		}

    	// return
		return result;
    }
}
