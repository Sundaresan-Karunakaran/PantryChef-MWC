package com.example.stepappv3.database;

public interface OnWeekDataFetchedCallback {
    // 7 elemanlı dizi: [Mon, Tue, Wed, Thu, Fri, Sat, Sun]
    void onWeekDataFetched(int[] dailySteps);
}
