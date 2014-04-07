package com.speed.ob.api;

import com.speed.ob.Config;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * See LICENSE.txt for license info
 */
public abstract class ObfuscatorTransform {

    protected final Logger LOGGER;
    protected final Config config;

    public ObfuscatorTransform(final Config config) {
        this.LOGGER = Logger.getLogger(this.getClass().getName());
        this.config = config;
    }

    public abstract void run(ClassStore store, Config config);

    public abstract void results();


    public void setLogLevel(Level level, boolean consoleOnly) {
        LOGGER.setLevel(level);
        for (Handler h : LOGGER.getHandlers()) {
            if (h instanceof ConsoleHandler) {
                h.setLevel(level);
            } else if (!consoleOnly) {
                h.setLevel(level);
            }

        }
    }

    public void addLogHandler(Handler handler) {
        LOGGER.addHandler(handler);
    }

    protected void info(String msg) {
        LOGGER.info(msg);
    }

    protected void warning(String msg) {
        LOGGER.warning(msg);
    }

}
