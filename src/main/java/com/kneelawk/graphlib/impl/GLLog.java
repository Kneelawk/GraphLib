package com.kneelawk.graphlib.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

public final class GLLog {
    private static final String LOGS_DIR = "logs";
    private static final String GRAPHLIB_DIR = "graphlib";
    private static final String LOG_FILE_NAME = "%s.log";

    private static final DateTimeFormatter timeStampPattern = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private static final Logger log = LoggerFactory.getLogger(Constants.MOD_ID);
    private static final java.util.logging.Logger fileLogger = java.util.logging.Logger.getLogger(Constants.MOD_ID);

    static void setupLogging(Path dir) {
        Path logsDir = dir.resolve(LOGS_DIR);
        Path graphLibDir = logsDir.resolve(GRAPHLIB_DIR);
        Path logFile = graphLibDir.resolve(LOG_FILE_NAME.formatted(LocalDateTime.now().format(timeStampPattern)));
        try {
            if (!Files.exists(graphLibDir)) {
                Files.createDirectories(graphLibDir);
            }

            FileHandler fh = new FileHandler(logFile.toString());
            fileLogger.addHandler(fh);
            SimpleFormatter sf = new SimpleFormatter();
            fh.setFormatter(sf);

            fileLogger.setUseParentHandlers(false);
        } catch (IOException e) {
            log.error("Unable to initialize separate logger.", e);
        }
    }

    private GLLog() {
    }

    public static void info(String msg) {
        log.info(msg);
    }

    public static void info(String msg, Object arg1) {
        log.info(msg, arg1);
    }

    public static void info(String msg, Object arg1, Object arg2) {
        log.info(msg, arg1, arg2);
    }

    public static void info(String msg, Object... args) {
        log.info(msg, args);
    }

    public static void info(String msg, Throwable t) {
        log.info(msg, t);
    }

    public static void warn(String msg) {
        log.warn(msg);
        fileLogger.warning(msg);
    }

    public static void warn(String msg, Object arg1) {
        log.warn(msg, arg1);
        fileLogger.warning(format(msg, arg1));
    }

    public static void warn(String msg, Object arg1, Object arg2) {
        log.warn(msg, arg1, arg2);
        fileLogger.warning(format(msg, arg1, arg2));
    }

    public static void warn(String msg, Object... args) {
        log.warn(msg, args);
        fileLogger.warning(format(msg, args));
    }

    public static void warn(String msg, Throwable t) {
        log.warn(msg, t);
        fileLogger.warning(format(msg, t));
    }

    public static void error(String msg) {
        log.error(msg);
        fileLogger.severe(msg);
    }

    public static void error(String msg, Object arg1) {
        log.error(msg, arg1);
        fileLogger.severe(format(msg, arg1));
    }

    public static void error(String msg, Object arg1, Object arg2) {
        log.error(msg, arg1, arg2);
        fileLogger.severe(format(msg, arg1, arg2));
    }

    public static void error(String msg, Object... args) {
        log.error(msg, args);
        fileLogger.severe(format(msg, args));
    }

    public static void error(String msg, Throwable t) {
        log.error(msg, t);
        fileLogger.severe(format(msg, t));
    }

    private static String format(String msg, Object arg1) {
        FormattingTuple tuple = MessageFormatter.format(msg, arg1);
        return format(tuple.getMessage(), tuple.getThrowable());
    }

    private static String format(String msg, Object arg1, Object arg2) {
        FormattingTuple tuple = MessageFormatter.format(msg, arg1, arg2);
        return format(tuple.getMessage(), tuple.getThrowable());
    }

    private static String format(String msg, Object... args) {
        FormattingTuple tuple = MessageFormatter.arrayFormat(msg, args);
        return format(tuple.getMessage(), tuple.getThrowable());
    }

    private static String format(String msg, Throwable t) {
        if (t == null) {
            return msg;
        } else {
            return msg + "\n" + ExceptionUtils.getStackTrace(t);
        }
    }
}
