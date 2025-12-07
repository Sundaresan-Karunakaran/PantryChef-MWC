package com.example.stepappv3.gemini;

import android.graphics.Bitmap;

import com.example.stepappv3.database.pantry.PantryItem;
import com.example.stepappv3.util.ParseResult;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
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

    public CompletableFuture<ParseResult> parseReceipt(Bitmap bitmap) {
        CompletableFuture<ParseResult> future = new CompletableFuture<>();

        try {
            // Build content with bitmap directly and text prompt
            Content content = new Content.Builder()
                    .addImage(bitmap)
                    .addText(
                            "Analyze the following receipt image. Extract each food item, its quantity, and its unit. " +
                                    "Then, classify each item into one of the following categories: " +
                                    "'Vegetables', 'Fruits', 'Dairy & Eggs', 'Meats & Seafood', 'Bakery', 'Spices','Grains and Pulses', " +
                                    "'Uncategorized'. " +
                                    "For each item, return a single line in the format: Item Name | Category | Quantity | Unit. " +
                                    "For example: 'Milk | Dairy & Eggs | 1 | ml'. Do not include headers, explanations, or any other text.Always return the Unit in metric system i.e. ml for liquids and grams for solids or none if not specified"
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
                        if (text == null || text.trim().isEmpty()) {
                            // Gemini returned nothing.
                            future.complete(new ParseResult(ParseResult.Status.NO_ITEMS_FOUND, "Could not find any items on the receipt."));
                            return;
                        }
                        List<PantryItem> items = parseTextToItems(text);
                        if (items.isEmpty()) {
                            // We got text, but our parser found no valid items.
                            // This could be because the image was not a receipt.
                            future.complete(new ParseResult(ParseResult.Status.NO_ITEMS_FOUND, "Could not recognize any items. Is this a receipt?"));
                        } else {
                            // Success!
                            future.complete(new ParseResult(items));
                        }
                    } catch (Exception e) {
                        future.completeExceptionally(e);
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    future.complete(new ParseResult(ParseResult.Status.FAILURE, "Failed to connect to the server."));
                }
            }, executor);

        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;

    }
    private List<PantryItem> parseTextToItems(String rawText) {
        List<PantryItem> items = new ArrayList<>();
        if (rawText == null || rawText.trim().isEmpty()) {
            return items;
        }

        String[] lines = rawText.split("\n");
        for (String line : lines) {
            // We now expect a format like: "Milk | Dairy & Eggs | 1 | gallon"
            String[] parts = line.split("\\|"); // Split the string by the pipe character

            if (parts.length == 4) { // We need exactly 4 parts to create an item
                try {
                    PantryItem item = new PantryItem();
                    item.name = parts[0].trim();
                    item.category = parts[1].trim();
                    item.quantity = Integer.parseInt(parts[2].trim());
                    item.unit = parts[3].trim();

                    // Create the PantryItem with the category provided by Gemini
                    items.add(item);
                } catch (NumberFormatException e) {
                    // Ignore lines where the quantity is not a valid number
                }
            }
        }
        return items;
    }
}