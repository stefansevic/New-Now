package com.example.demo.search.service;

import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class PdfTextExtractor {

	private final Tika tika = new Tika();

	public String extract(InputStream pdfStream) {
		if (pdfStream == null) return "";
		try {
			return tika.parseToString(pdfStream);
		} catch (Exception e) {
			System.err.println("Tika parse failed: " + e.getMessage());
			return "";
		}
	}
}