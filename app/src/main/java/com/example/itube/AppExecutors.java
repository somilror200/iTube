package com.example.itube;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class AppExecutors {

    private static final ExecutorService DATABASE = Executors.newSingleThreadExecutor();

    private AppExecutors() {
    }

    static ExecutorService database() {
        return DATABASE;
    }
}
