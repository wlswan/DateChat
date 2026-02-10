package com.example.dateServer.translation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SearchResult {
    private final String id;
    private final String original;
    private final String translated;
    private final float score;
}
