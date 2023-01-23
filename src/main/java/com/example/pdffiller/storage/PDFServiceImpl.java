package com.example.pdffiller.storage;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfButtonFormField;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

@Service
public class PDFServiceImpl implements PDFService {
    private final Logger log = LoggerFactory.getLogger(PDFServiceImpl.class);
    //private final AtomicLong atomicLong = new AtomicLong();

    private final StorageService storageService;
    private final String font;
    private final float globalFontSize;
    private final boolean globalFlattenFields;
    private final PDFProperties.FillWith fillWith;

    @Autowired
    public PDFServiceImpl(StorageService storageService, PDFProperties pdfProperties) {
        this.storageService = storageService;
        this.font = pdfProperties.getFont();
        this.globalFontSize = pdfProperties.getGlobalFontSize();
        this.globalFlattenFields = pdfProperties.getFlattenFields();
        this.fillWith = pdfProperties.getFillWith();
    }

    private NavigableMap<String, String> getPdfFields(InputStream is) throws IOException {
        NavigableMap<String, String> fieldNamesAndTypes = new TreeMap<>();
        PdfReader reader = new PdfReader(is);
        PdfDocument pdfDoc = new PdfDocument(reader);
        // Get the fields from the reader (read-only!!!)
        PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDoc, true);
        // Loop over the fields and get info about them
        Set<String> fields = form.getFormFields().keySet();
        for (String key : fields) {
            String fieldType;
            PdfName type = form.getField(key).getFormType();
            if (0 == PdfName.Btn.compareTo(type)) {
                if(((PdfButtonFormField)form.getField(key)).isPushButton()){
                    fieldType="Pushbutton";
                } else {
                    if(((PdfButtonFormField)form.getField(key)).isRadio()){
                        fieldType="Radiobutton";
                    }else {
                        fieldType="Checkbox";
                    }
                }
            } else if (0 == PdfName.Ch.compareTo(type)) {
                fieldType = "Choicebox";
            } else if (0 == PdfName.Sig.compareTo(type)) {
                fieldType="Signature";
            } else if (0 == PdfName.Tx.compareTo(type)) {
                fieldType="Text";
            }else {
                fieldType="?";
            }
            fieldNamesAndTypes.put(key,fieldType);
        }
        log.debug("Keys found:{}", fieldNamesAndTypes);
        return fieldNamesAndTypes;
    }

    @Override
    public NavigableMap<String, String> getPdfFields(String filename){
        //log.debug("#{}, Getting fields of {}",atomicLong.incrementAndGet(), filename);
        log.debug("Getting fields of {}", filename);
        //List<String> fieldNamesAndTypes = new ArrayList<>();
        NavigableMap<String, String> fieldNamesAndTypes = null;
        try {
            Resource resource = storageService.loadAsResource(filename);
            try (InputStream is = resource.getInputStream()) {
                fieldNamesAndTypes = getPdfFields(is);
            } catch (IOException e) {
                log.error("Error:", e);
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            log.error("Error:", e);
        }
        return fieldNamesAndTypes;
    }

    private void debugField(String state, PdfFormField field){
        log.debug("Field \"{}\" - {} state:",field.getFieldName(),state);
        log.debug("       Color:{}",Arrays.toString(field.getColor().getColorValue()));
        log.debug("       Font:{}",field.getFont());
        log.debug("       Font size:{}",field.getFontSize());
        log.debug("       Default value:{}",field.getDefaultValue());
        log.debug("       Value:{}",field.getValue().toString());
        log.debug("       Value as String:{}",field.getValueAsString());
    }

    @Override
    public String fillOutPdfForm(String filename, NavigableMap<String, String> fillOutFieldValues) {
        return flattenFilledOutPdfForm(filename,globalFlattenFields,fillOutFieldValues);
    }

    @Override
    public String flattenFilledOutPdfForm(String filename, boolean flattenFields, NavigableMap<String, String> fillOutFieldValues) {
        //log.debug("#{}, Filling out the form {}",atomicLong.incrementAndGet(), filename);
        log.debug("Filling out the form {}", filename);
        // Font from resources
        /*
        //alternative:
        URL font_path = Thread.currentThread().getContextClassLoader().getResource("font/font1.ttf");
        try (InputStream is = font_path.openStream()) {
            byte[] bytes = StreamUtil.inputStreamToArray(is);
            PdfFont font = PdfFontFactory.createFont(bytes, PdfEncodings.WINANSI, EmbeddingStrategy.PREFER_EMBEDDED);
        }*/
        ClassPathResource fontPathResource = new ClassPathResource(font);
        String fontPath = fontPathResource.getPath();

        Resource resource = storageService.loadAsResource(filename);
        WritableResource tempResource = storageService.tempResource();
        try (InputStream is = resource.getInputStream();
             OutputStream os = tempResource.getOutputStream();
             PdfReader reader = new PdfReader(is);
             PdfWriter writer = new PdfWriter(os);
             PdfDocument pdfDoc = new PdfDocument(reader, writer))
        {
            PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDoc, false);

            // Being set as true, this parameter is responsible to generate an appearance Stream
            // while flattening for all form fields that don't have one. Generating appearances will
            // slow down form flattening, but otherwise Acrobat might render the pdf on its own rules.
            form.setGenerateAppearance(true);

            PdfFont font = PdfFontFactory.createFont(fontPath, PdfEncodings.IDENTITY_H,
                    PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED,true);
            if (!flattenFields){
                // Only a font subset is needed if the file is being flattened
                // However, this value will be ignored in case you use the encoding Identity-H because that's how it's
                // described in ISO-32000-1. iText will only embed full fonts that are stored inside the PDF as a simple
                // font (256 characters); iText will never embed fonts that are stored as a composite font (up to
                // 65,535 characters).
                font.setSubset(false);
            }

            //form.getField("bankName.1").setValue(" 1", font, 10f);
            //form.getField("bankName.2").setValue("Банка 2", font, 10f);

            Map<String, PdfFormField> formFields = form.getFormFields();

            formFields.forEach((k,v) -> {
                if (0 == PdfName.Tx.compareTo(v.getFormType())) {
                    // Text field
                    debugField("initials state",v);
                    float fontSize;
                    if(globalFontSize>0){
                        fontSize = globalFontSize;
                    }else{
                        fontSize = v.getFontSize();
                    }
                    if(fillOutFieldValues.containsKey(k)){
                        log.debug("Setting to \"{}\"",fillOutFieldValues.get(k));
                        v.setValue(fillOutFieldValues.get(k), font, fontSize);
                    } else {
                        log.debug("Performing {} ...", fillWith);
                        switch (fillWith) {
                            case VALUES -> v.setValue(v.getValueAsString());
                            case DEFAULTS -> v.setValue(v.getDefaultValue().toString());
                            //case NOTHING -> {}
                            default -> {}
                        }
                    }
                    debugField("modified state",v);
                }
            });

            if (flattenFields){
                form.flattenFields();
            }
        } catch (IOException e) {
            log.error("Error:", e);
            throw new RuntimeException(e);
        }
        return tempResource.getFilename();

    }

}

