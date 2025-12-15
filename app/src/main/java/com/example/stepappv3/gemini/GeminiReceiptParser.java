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
                "AIzaSyAP17WHFQe6Y_o7mmQgX8HQF3Y1IGazBLo"
        );
        model = GenerativeModelFutures.from(gm);
        executor = Executors.newSingleThreadExecutor();
    }

    public CompletableFuture<ParseResult> parseReceipt(Bitmap bitmap) {
        CompletableFuture<ParseResult> future = new CompletableFuture<>();

        try {
            // Build content with bitmap and prompt
            Content content = new Content.Builder()
                    .addImage(bitmap)
                    .addText(
                            "Analyze the following receipt image. Extract each food item, its quantity, and its unit. " +
                                    "Then, classify each item into one of the following categories: " +
                                    "'Vegetables', 'Fruits', 'Dairy & Eggs', 'Meats & Seafood', 'Bakery', 'Spices','Grains and Pulses', " +
                                    "'Uncategorized'. " +
                                    "For each item, return a single line in the format: Item Name | Category | Quantity | Unit. " +
                                    "For example: 'Milk | Dairy & Eggs | 1 | ml'. Do not include headers, explanations, or any other text. " +
                                    "Always return the Unit in metric system i.e. ml for liquids and grams for solids or none if not specified"
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
                            // No text recognized
                            future.complete(new ParseResult(ParseResult.Status.NO_ITEMS_FOUND, "No text recognized in receipt image."));
                            return;
                        }

                        // Parse text into PantryItem list
                        List<PantryItem> items = parseTextToItems(text);

                        if (items.isEmpty()) {
                            future.complete(new ParseResult(ParseResult.Status.NO_ITEMS_FOUND, "No items found in receipt text."));
                        } else {
                            // Wrap parsed items in a SUCCESS ParseResult
                            future.complete(new ParseResult(items));
                        }

                    } catch (Exception e) {
                        future.complete(new ParseResult(ParseResult.Status.FAILURE, e.getMessage()));
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    future.complete(new ParseResult(ParseResult.Status.FAILURE, t.getMessage()));
                }
            }, executor);

        } catch (Exception e) {
            future.complete(new ParseResult(ParseResult.Status.FAILURE, e.getMessage()));
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