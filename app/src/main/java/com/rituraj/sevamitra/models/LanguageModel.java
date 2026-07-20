package com.rituraj.sevamitra.models;


import java.util.ArrayList;

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

    public static ArrayList<LanguageModel> getLanguageModelArrayList() {
        ArrayList<LanguageModel> languages = new ArrayList<>();
        languages.add(new LanguageModel("English", "en"));
        languages.add(new LanguageModel("Afrikaans", "af"));
        languages.add(new LanguageModel("Albanian", "sq"));
        languages.add(new LanguageModel("Arabic", "ar"));
        languages.add(new LanguageModel("Belarusian", "be"));
        languages.add(new LanguageModel("Bengali", "bn"));
        languages.add(new LanguageModel("Bulgarian", "bg"));
        languages.add(new LanguageModel("Catalan", "ca"));
        languages.add(new LanguageModel("Chinese", "zh"));
        languages.add(new LanguageModel("Croatian", "hr"));
        languages.add(new LanguageModel("Czech", "cs"));
        languages.add(new LanguageModel("Danish", "da"));
        languages.add(new LanguageModel("Dutch", "nl"));
        languages.add(new LanguageModel("Esperanto", "eo"));
        languages.add(new LanguageModel("Estonian", "et"));
        languages.add(new LanguageModel("Finnish", "fi"));
        languages.add(new LanguageModel("French", "fr"));
        languages.add(new LanguageModel("Galician", "gl"));
        languages.add(new LanguageModel("Georgian", "ka"));
        languages.add(new LanguageModel("German", "de"));
        languages.add(new LanguageModel("Greek", "el"));
        languages.add(new LanguageModel("Gujarati", "gu"));
        languages.add(new LanguageModel("Haitian Creole", "ht"));
        languages.add(new LanguageModel("Hebrew", "he"));
        languages.add(new LanguageModel("Hindi", "hi"));
        languages.add(new LanguageModel("Hungarian", "hu"));
        languages.add(new LanguageModel("Icelandic", "is"));
        languages.add(new LanguageModel("Indonesian", "id"));
        languages.add(new LanguageModel("Irish", "ga"));
        languages.add(new LanguageModel("Italian", "it"));
        languages.add(new LanguageModel("Japanese", "ja"));
        languages.add(new LanguageModel("Kannada", "kn"));
        languages.add(new LanguageModel("Korean", "ko"));
        languages.add(new LanguageModel("Latvian", "lv"));
        languages.add(new LanguageModel("Lithuanian", "lt"));
        languages.add(new LanguageModel("Macedonian", "mk"));
        languages.add(new LanguageModel("Malay", "ms"));
        languages.add(new LanguageModel("Maltese", "mt"));
        languages.add(new LanguageModel("Marathi", "mr"));
        languages.add(new LanguageModel("Norwegian", "no"));
        languages.add(new LanguageModel("Persian", "fa"));
        languages.add(new LanguageModel("Polish", "pl"));
        languages.add(new LanguageModel("Portuguese", "pt"));
        languages.add(new LanguageModel("Romanian", "ro"));
        languages.add(new LanguageModel("Russian", "ru"));
        languages.add(new LanguageModel("Slovak", "sk"));
        languages.add(new LanguageModel("Slovenian", "sl"));
        languages.add(new LanguageModel("Spanish", "es"));
        languages.add(new LanguageModel("Swahili", "sw"));
        languages.add(new LanguageModel("Swedish", "sv"));
        languages.add(new LanguageModel("Tagalog", "tl"));
        languages.add(new LanguageModel("Tamil", "ta"));
        languages.add(new LanguageModel("Telugu", "te"));
        languages.add(new LanguageModel("Thai", "th"));
        languages.add(new LanguageModel("Turkish", "tr"));
        languages.add(new LanguageModel("Ukrainian", "uk"));
        languages.add(new LanguageModel("Urdu", "ur"));
        languages.add(new LanguageModel("Vietnamese", "vi"));
        languages.add(new LanguageModel("Welsh", "cy"));
        return languages;
    }
}
