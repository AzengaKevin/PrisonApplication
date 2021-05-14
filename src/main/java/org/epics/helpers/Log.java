package org.epics.helpers;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Log {

    public static void info(final String TAG, final String message) {
        Logger.getLogger(TAG).log(Level.INFO, message);
    }

    public static void error(final String TAG, final String message, Throwable throwable) {
        Logger.getLogger(TAG).log(Level.SEVERE, message, throwable);
    }
}
