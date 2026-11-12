package ua.caunt.bungeeforge;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Log4j's API has shipped with Minecraft since 1.7, so it is the one logger every version has. */
final class Log {
    private static final Logger LOGGER = LogManager.getLogger(BungeeForge.NAME);

    private Log() {
    }

    static void info(String message) {
        LOGGER.info(message);
    }

    static void warn(String message) {
        LOGGER.warn(message);
    }

    static void error(String message) {
        LOGGER.error(message);
    }

    static void error(String message, Throwable error) {
        LOGGER.error(message, error);
    }
}
