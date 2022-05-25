package com.kneelawk.graphlib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GLLog {
    public static final Logger log = LoggerFactory.getLogger(Constants.MOD_ID);

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
    }

    public static void warn(String msg, Object arg1) {
        log.warn(msg, arg1);
    }

    public static void warn(String msg, Object arg1, Object arg2) {
        log.warn(msg, arg1, arg2);
    }

    public static void warn(String msg, Object... args) {
        log.warn(msg, args);
    }

    public static void warn(String msg, Throwable t) {
        log.warn(msg, t);
    }

    public static void error(String msg) {
        log.error(msg);
    }

    public static void error(String msg, Object arg1) {
        log.error(msg, arg1);
    }

    public static void error(String msg, Object arg1, Object arg2) {
        log.error(msg, arg1, arg2);
    }

    public static void error(String msg, Object... args) {
        log.error(msg, args);
    }

    public static void error(String msg, Throwable t) {
        log.error(msg, t);
    }
}
