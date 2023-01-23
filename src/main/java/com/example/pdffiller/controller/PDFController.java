package com.example.pdffiller.controller;


import com.example.pdffiller.storage.PDFService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.util.NavigableMap;

@RestController
@RequestMapping("/fillout")
public class PDFController {

    private final PDFService pdfService;

    @Autowired
    public PDFController(PDFService pdfService) {
        this.pdfService = pdfService;
    }

    @GetMapping(path = "/fields/{pdfFileName}")
    public NavigableMap<String, String> getPdfFields(@PathVariable String pdfFileName){
        return pdfService.getPdfFields(pdfFileName);
    }

    @PostMapping (path = "/mocked/{pdfFileName}")
    public String fillMockedData(@PathVariable String pdfFileName){
/*        return Stream.of(new String[][] {
                { "bankName", "bankName" },
                { "bankName.1", "bankName.1" },
                { "bankName.2", "bankName.2" },
        }).collect(Collectors.toMap(data -> data[0], data -> data[1]));*/
        //NavigableMap<String, String> map = pdfFields.getMap();
        //
        //public NavigableMap<String, String> generateMockedData(@RequestBody NavigableMap<String, String> pdfFields){
        //pdfFields.forEach((k,v) -> pdfFields.replace(k,k));
        NavigableMap<String, String> mockedFields = pdfService.getPdfFields(pdfFileName);
        mockedFields.forEach((k,v) -> mockedFields.replace(k,"[" + k + "]"));
        return fillOutPdfForm(pdfFileName,mockedFields);
    }

    @PostMapping (path = "/{pdfFileName}")
    @ResponseStatus(code=HttpStatus.CREATED)
    public String fillOutPdfForm(@PathVariable String pdfFileName, @RequestBody NavigableMap<String, String> pdfFields) {
        //return pdfService.fillOutPdfForm(pdfFileName, pdfFields);
        return MvcUriComponentsBuilder
                .fromMethodName(FileUploadController.class,
                        "serveFile",
                        pdfService.fillOutPdfForm(pdfFileName, pdfFields))
                .build().toUri().toString();
    }

    @PostMapping (path = "/flattened/{pdfFileName}")
    @ResponseStatus(code=HttpStatus.CREATED)
    public String flattenFilledOutPdfForm(@PathVariable String pdfFileName, @RequestBody NavigableMap<String, String> pdfFields) {
        //return pdfService.fillOutPdfForm(pdfFileName, pdfFields);
        return MvcUriComponentsBuilder
                .fromMethodName(FileUploadController.class,
                        "serveFile",
                        pdfService.flattenFilledOutPdfForm(pdfFileName, true, pdfFields))
                .build().toUri().toString();
    }
}

