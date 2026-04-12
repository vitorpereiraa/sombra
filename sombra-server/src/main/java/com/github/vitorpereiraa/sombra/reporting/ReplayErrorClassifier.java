package com.github.vitorpereiraa.sombra.reporting;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import org.springframework.web.client.ResourceAccessException;

public final class ReplayErrorClassifier {

    private ReplayErrorClassifier() {}

    public static String classify(Throwable error) {
        if (error instanceof ResourceAccessException) {
            var cause = error.getCause();
            if (cause instanceof SocketTimeoutException) {
                return "timeout";
            }
            if (cause instanceof ConnectException) {
                return "connect";
            }
            return "io";
        }
        return "other";
    }
}
