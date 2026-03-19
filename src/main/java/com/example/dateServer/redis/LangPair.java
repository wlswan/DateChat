package com.example.dateServer.redis;

import com.example.dateServer.common.Lang;
import lombok.Getter;

@Getter
public class LangPair {
        Lang sourceLang;
        Lang targetLang;

        public LangPair(Lang sourceLang, Lang targetLang) {
            this.sourceLang = sourceLang;
            this.targetLang = targetLang;
        }
    }