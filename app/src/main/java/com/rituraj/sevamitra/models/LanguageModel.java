package com.rituraj.sevamitra.models;


public class LanguageModel {
    public String name;
    public String code;

    public LanguageModel() {
    }

    public LanguageModel(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String toString() {
        return name;
    }
}
