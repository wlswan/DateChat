package com.example.dateServer.translation;

import com.example.dateServer.common.Lang;

public interface Translator {

    String translate(String text, Lang sourceLang, Lang targetLang);
}
