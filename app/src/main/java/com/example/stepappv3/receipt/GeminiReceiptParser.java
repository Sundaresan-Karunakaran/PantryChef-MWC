package com.example.stepappv3.receipt;

import android.graphics.Bitmap;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GeminiReceiptParser {
    private final GenerativeModelFutures model;
    private final Executor executor;

    public GeminiReceiptParser(String apiKey) {
        GenerativeModel gm = new GenerativeModel(
                "gemini-2.5-flash",
                apiKey
        );
        model = GenerativeModelFutures.from(gm);
        executor = Executors.newSingleThreadExecutor();
    }

    public CompletableFuture<String> parseReceipt(Bitmap bitmap) {
        CompletableFuture<String> future = new CompletableFuture<>();

        try {
            // Build content with bitmap directly and text prompt
            Content content = new Content.Builder()
                    .addImage(bitmap)
                    .addText(
                            "Extract ONLY grocery item names from this receipt image.\n" +
                                    "Nothing except the item should be returned. No text like \"Here are the grocery item names from the receipt\" which give introduction. Only include food/edible items and just return the items as a clean bullet list. No prices. No totals. No extra text. Also if there's an indication of weight, add that info."
                    )
                    .build();

            // Generate content asynchronously
            ListenableFuture<GenerateContentResponse> responseFuture =
                    model.generateContent(content);

            // Convert ListenableFuture to CompletableFuture
            Futures.addCallback(responseFuture, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    try {
                        String text = result.getText();
                        future.complete(text != null && !text.isEmpty() ? text : "No text recognized.");
                    } catch (Exception e) {
                        future.completeExceptionally(e);
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    future.completeExceptionally(t);
                }
            }, executor);

        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }
}