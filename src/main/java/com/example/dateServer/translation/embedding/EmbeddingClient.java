package com.example.dateServer.translation.embedding;

import java.util.List;

public interface EmbeddingClient {

    float[] embed(String text);

    int dimension();
}
