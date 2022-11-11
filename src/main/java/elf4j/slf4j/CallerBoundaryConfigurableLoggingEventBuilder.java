package elf4j.slf4j;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.KeyValuePair;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;
import org.slf4j.spi.DefaultLoggingEventBuilder;
import org.slf4j.spi.LoggingEventAware;

class CallerBoundaryConfigurableLoggingEventBuilder extends DefaultLoggingEventBuilder {

    public CallerBoundaryConfigurableLoggingEventBuilder(Logger logger, Level level) {
        super(logger, level);
    }

    @Override
    protected void log(LoggingEvent aLoggingEvent) {
        if (logger instanceof LoggingEventAware) {
            ((LoggingEventAware) logger).log(aLoggingEvent);
        } else {
            logViaPublicSLF4JLoggerAPI(aLoggingEvent);
        }
    }

    private void logViaPublicSLF4JLoggerAPI(LoggingEvent aLoggingEvent) {
        Object[] argArray = aLoggingEvent.getArgumentArray();
        int argLen = argArray == null ? 0 : argArray.length;

        Throwable t = aLoggingEvent.getThrowable();
        int tLen = t == null ? 0 : 1;

        String msg = aLoggingEvent.getMessage();

        Object[] combinedArguments = new Object[argLen + tLen];

        if (argArray != null) {
            System.arraycopy(argArray, 0, combinedArguments, 0, argLen);
        }
        if (t != null) {
            combinedArguments[argLen] = t;
        }

        msg = mergeMarkersAndKeyValuePairs(aLoggingEvent, msg);

        switch (aLoggingEvent.getLevel()) {
            case TRACE:
                logger.trace(msg, combinedArguments);
                break;
            case DEBUG:
                logger.debug(msg, combinedArguments);
                break;
            case INFO:
                logger.info(msg, combinedArguments);
                break;
            case WARN:
                logger.warn(msg, combinedArguments);
                break;
            case ERROR:
                logger.error(msg, combinedArguments);
                break;
        }
    }

    private String mergeMarkersAndKeyValuePairs(LoggingEvent aLoggingEvent, String msg) {

        StringBuilder sb = null;

        if (aLoggingEvent.getMarkers() != null) {
            sb = new StringBuilder();
            for (Marker marker : aLoggingEvent.getMarkers()) {
                sb.append(marker);
                sb.append(' ');
            }
        }

        if (aLoggingEvent.getKeyValuePairs() != null) {
            if (sb == null) {
                sb = new StringBuilder();
            }
            for (KeyValuePair kvp : aLoggingEvent.getKeyValuePairs()) {
                sb.append(kvp.key);
                sb.append('=');
                sb.append(kvp.value);
                sb.append(' ');
            }
        }

        if (sb != null) {
            sb.append(msg);
            return sb.toString();
        } else {
            return msg;
        }
    }
}
