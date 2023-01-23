package com.example.pdffiller.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("fill-out")
public class PDFProperties {


    public enum FillWith {
        NOTHING, VALUES, DEFAULTS
    }
    private FillWith fillWith = FillWith.VALUES;

    // resources/fonts/FreeSans.ttf
    // resources/fonts/arialunicodems.ttf
    private String font = "fonts/FreeSans.ttf";

    private float globalFontSize = 0;

    private boolean flattenFields = false;

    public boolean getFlattenFields() {
        return flattenFields;
    }

    public void setFlattenFields(boolean flattenFields) {
        this.flattenFields = flattenFields;
    }

    public FillWith getFillWith(){
        return fillWith;
    }

    public void setFillWith(FillWith fillWith){
        this.fillWith = fillWith;
    }

    public String getFont() {
        return font;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public float getGlobalFontSize() {
        return globalFontSize;
    }

    public void setGlobalFontSize(float globalFontSize) {
        this.globalFontSize = globalFontSize;
    }

}
