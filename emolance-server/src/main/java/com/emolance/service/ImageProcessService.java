package com.emolance.service;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ImageProcessService {

	private ImageColorAnalyzer imageColorAnalyzer = new ImageColorAnalyzer();

    private final Logger log = LoggerFactory.getLogger(ImageProcessService.class);

    public Node processImage(String url) {
    	Node result = null;

    	// download
    	long timestamp = System.currentTimeMillis();
    	File tmpFile = null;
		try {
			tmpFile = File.createTempFile("image" + timestamp, ".jpg");
			FileUtils.copyURLToFile(new URL(url), tmpFile);

			// process
			Node node = imageColorAnalyzer.analyzeImage(tmpFile);
			log.info("SC: " + node.getSC());
			log.info("SO: " + node.getSO());
			log.info("ST: " + node.getST());
			result = node;

			// delete
			tmpFile.delete();
		} catch (IOException e) {
			log.error("Failed to download the image file");
		}

    	// return
		return result;
    }
}
