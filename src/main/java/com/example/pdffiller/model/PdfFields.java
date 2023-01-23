package com.example.pdffiller.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.*;
import java.util.stream.Collectors;

public class PdfFields {

    private NavigableMap<String,String> map = new TreeMap<>();

    @JsonAnyGetter
    public NavigableMap<String,String> getMap() {
        return this.map;
    }
    public void setMap(NavigableMap<String, String> map) {
        this.map = map;
    }

    @JsonAnySetter
    public void addValue(String key, String value) {
        this.map.put(key, value);
    }

    @Override
    public int hashCode(){
        return Objects.hash(this.map);
    }

    @Override
    public String toString() {
        return map.keySet().stream()
                .map(key -> key + "=" + map.get(key))
                .collect(Collectors.joining(", ", "{", "}"));
    }

}
