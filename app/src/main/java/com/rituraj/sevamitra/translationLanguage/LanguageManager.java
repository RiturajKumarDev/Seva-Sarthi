package com.rituraj.sevamitra.translationLanguage;

import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.function.Consumer;

public class LanguageManager {

    private static Translator translator;
    private static boolean isReady = false;

    public static void init(String targetLang, Runnable onReady) {
        String sourceLang = com.google.mlkit.nl.translate.TranslateLanguage.ENGLISH;
        TranslatorOptions options =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(sourceLang)
                        .setTargetLanguage(targetLang)
                        .build();

        translator = Translation.getClient(options);

        translator.downloadModelIfNeeded()
                .addOnSuccessListener(unused -> {
                    isReady = true;
                    onReady.run();
                });
    }

    private static void translate(String text, Consumer<String> callback) {
        if (!isReady || text == null || text.trim().isEmpty()) return;

        translator.translate(text)
                .addOnSuccessListener(callback::accept);
    }

    public static void translateView(View view) {
        if (view instanceof TextView) {
            TextView tv = (TextView) view;

            Object tag = tv.getTag();
            String original = tag != null ? tag.toString() : tv.getText().toString();

            translate(original, tv::setText);
        }

        if (view instanceof EditText) {
            EditText et = (EditText) view;
            CharSequence hint = et.getHint();
            if (hint != null) {
                translate(hint.toString(), et::setHint);
            }
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                translateView(group.getChildAt(i));
            }
        }
    }
}
