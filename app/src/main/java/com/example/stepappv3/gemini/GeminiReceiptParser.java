package com.example.stepappv3.gemini;

import android.graphics.Bitmap;
import android.util.Log;

import com.example.stepappv3.database.pantry.PantryItem;
import com.example.stepappv3.util.ParseResult;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.BlockThreshold;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.ai.client.generativeai.type.HarmCategory;
import com.google.ai.client.generativeai.type.SafetySetting;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GeminiReceiptParser {
    private final GenerativeModelFutures model;
    private final Executor executor;

    public GeminiReceiptParser(String apiKey) {
        // 1. GÜVENLİK AYARLARI (SAFETY SETTINGS)

        // 2. MODEL YAPILANDIRMASI
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
            Content content = new Content.Builder()
                    .addImage(bitmap)
                    .addText(
                            "Analyze this receipt image perfectly. \n" +
                                    "For every food item found, output a SINGLE line in this exact format:\n" +
                                    "Item Name | Category | Quantity | Unit\n\n" +
                                    "Rules:\n" +
                                    "1. Categories must be one of: Vegetables, Fruits, Dairy & Eggs, Meats & Seafood, Bakery, Spices, Grains, Uncategorized.\n" +
                                    "2. Quantity must be a number (e.g., 1, 2.5). If not found, use 1.\n" +
                                    "3. Unit must be metric (kg, g, ml, l) or 'unit' if generic.\n" +
                                    "4. Do NOT verify, do NOT add headers, do NOT use markdown formatting like ```."
                    )
                    .build();

            ListenableFuture<GenerateContentResponse> responseFuture = model.generateContent(content);

            Futures.addCallback(responseFuture, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    try {
                        String text = result.getText();
                        Log.d("GeminiResponse", "Raw AI Output: " + text);

                        if (text == null || text.trim().isEmpty()) {
                            future.complete(new ParseResult(ParseResult.Status.NO_ITEMS_FOUND, "Gemini returned empty text."));
                            return;
                        }

                        // 3. TEMİZLİK (CLEANING)
                        // Bazen AI "```" veya "Here is the list:" gibi şeyler ekler, temizleyelim.
                        text = text.replace("```", "").replace("Item Name | Category | Quantity | Unit", "");

                        List<PantryItem> items = parseTextToItems(text);

                        if (items.isEmpty()) {
                            future.complete(new ParseResult(ParseResult.Status.NO_ITEMS_FOUND, "No valid items parsed from receipt."));
                        } else {
                            future.complete(new ParseResult(items));
                        }

                    } catch (Exception e) {
                        Log.e("GeminiError", "Parsing Logic Error", e);
                        future.complete(new ParseResult(ParseResult.Status.FAILURE, "Parse Error: " + e.getMessage()));
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e("GeminiError", "API Connection Failed", t);
                    // Kullanıcıya daha net hata mesajı göster
                    String msg = t.getMessage();
                    if (msg != null && msg.contains("404")) {
                        msg = "Model not found or API Key invalid.";
                    } else if (msg != null && msg.contains("API key")) {
                        msg = "Invalid API Key.";
                    }
                    future.complete(new ParseResult(ParseResult.Status.FAILURE, "API Error: " + msg));
                }
            }, executor);

        } catch (Exception e) {
            future.complete(new ParseResult(ParseResult.Status.FAILURE, "Setup Error: " + e.getMessage()));
        }

        return future;
    }

    private List<PantryItem> parseTextToItems(String rawText) {
        List<PantryItem> items = new ArrayList<>();
        if (rawText == null) return items;

        String[] lines = rawText.split("\n");
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;

            String[] parts = line.split("\\|");
            // En az 3 parça olmalı: İsim | Kategori | Miktar
            if (parts.length >= 3) {
                try {
                    PantryItem item = new PantryItem();
                    item.name = parts[0].trim();
                    item.category = parts[1].trim();

                    // Miktarı temizle (sadece sayı ve nokta kalsın)
                    String qtyStr = parts[2].trim().replaceAll("[^0-9.]", "");
                    if (qtyStr.isEmpty()) {
                        item.quantity = 1;
                    } else {
                        // Double parse edip int'e yuvarla
                        item.quantity = (int) Math.round(Double.parseDouble(qtyStr));
                    }

                    // Birim varsa al
                    if (parts.length > 3) {
                        item.unit = parts[3].trim();
                    } else {
                        item.unit = "unit";
                    }

                    // Basit doğrulama: İsim çok uzunsa muhtemelen hata vardır
                    if (item.name.length() < 50) {
                        items.add(item);
                    }
                } catch (Exception e) {
                    Log.w("GeminiParse", "Skipping line: " + line);
                }
            }
        }
        return items;
    }
}