package com.example.dateServer.translation;

import java.util.List;

public interface EmbeddingClient {

    float[] embed(String text);

    float[] embedWithContext(String text, List<String> context);

    int dimension();
}
