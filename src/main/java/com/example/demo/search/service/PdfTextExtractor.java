package com.example.demo.search.service;

import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class PdfTextExtractor {

	private static final Logger log = LoggerFactory.getLogger(PdfTextExtractor.class);

	private final Tika tika = new Tika();

	public String extract(InputStream pdfStream) {
		if (pdfStream == null) return "";
		try {
			return tika.parseToString(pdfStream);
		} catch (Exception e) {
			log.error("Tika PDF parse failed: {}", e.getMessage());
			return "";
		}
	}
}