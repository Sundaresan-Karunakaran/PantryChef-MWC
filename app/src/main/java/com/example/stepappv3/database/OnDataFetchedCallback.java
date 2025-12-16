package com.example.stepappv3.database;

public interface OnDataFetchedCallback<T> {
    void onDataFetched(T data);
}