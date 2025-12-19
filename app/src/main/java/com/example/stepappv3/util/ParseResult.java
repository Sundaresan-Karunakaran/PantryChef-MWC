package com.example.stepappv3.util;

import com.example.stepappv3.database.pantry.PantryItem;
import com.example.stepappv3.database.pantry.ParsedReceiptItem;

import java.util.List;

public class ParseResult {

    public enum Status {
        SUCCESS,
        FAILURE, // For network/API errors
        NO_ITEMS_FOUND // For valid images that have no items
    }

    public final Status status;
    public final List<ParsedReceiptItem> items;
    public final String errorMessage;

    // Constructor for SUCCESS
    public ParseResult(List<ParsedReceiptItem> items) {
        this.status = Status.SUCCESS;
        this.items = items;
        this.errorMessage = null;
    }

    // Constructor for FAILURE or NO_ITEMS_FOUND
    public ParseResult(Status status, String errorMessage) {
        this.status = status;
        this.items = null;
        this.errorMessage = errorMessage;
    }
}