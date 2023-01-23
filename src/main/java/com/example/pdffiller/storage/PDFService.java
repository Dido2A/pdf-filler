package com.example.pdffiller.storage;

import java.util.NavigableMap;

public interface PDFService {

    NavigableMap<String, String> getPdfFields(String filename);

    String fillOutPdfForm(String filename, NavigableMap<String, String> fillOutFieldValues);

    String flattenFilledOutPdfForm(String filename, boolean flattenFields, NavigableMap<String, String> fillOutFieldValues);
}
