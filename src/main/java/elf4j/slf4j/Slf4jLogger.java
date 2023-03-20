/*
 * MIT License
 *
 * Copyright (c) 2022 Easy Logging Facade for Java (ELF4J)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package elf4j.slf4j;

import elf4j.Level;
import elf4j.Logger;
import elf4j.util.NoopLogger;
import lombok.NonNull;
import lombok.ToString;
import net.jcip.annotations.Immutable;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LoggingEventBuilder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static elf4j.Level.*;

@Immutable
@ToString
class Slf4jLogger implements Logger {
    private static final Level DEFAULT_LEVEL = INFO;
    private static final EnumMap<Level, org.slf4j.event.Level> LEVEL_MAP = setLevelMap();
    private static final EnumMap<Level, Map<String, Slf4jLogger>> LOGGER_CACHE = initLoggerCache();
    private static final String THIS_FQCN = Slf4jLogger.class.getName();
    @NonNull private final String name;
    @NonNull private final Level level;
    @NonNull private final org.slf4j.Logger delegateLogger;
    private final boolean enabled;

    private Slf4jLogger(@NonNull String name, @NonNull Level level) {
        this.name = name;
        this.level = level;
        this.delegateLogger = LoggerFactory.getLogger(name);
        switch (this.level) {
            case TRACE:
                enabled = delegateLogger.isTraceEnabled();
                break;
            case DEBUG:
                enabled = delegateLogger.isDebugEnabled();
                break;
            case INFO:
                enabled = delegateLogger.isInfoEnabled();
                break;
            case WARN:
                enabled = delegateLogger.isWarnEnabled();
                break;
            case ERROR:
                enabled = delegateLogger.isErrorEnabled();
                break;
            default:
                enabled = false;
        }
    }

    static Slf4jLogger instance() {
        return getLogger(CallStack.mostRecentCallerOf(Logger.class).getClassName());
    }

    private static Slf4jLogger getLogger(@NonNull String name, @NonNull Level level) {

        return LOGGER_CACHE.get(level).computeIfAbsent(name, k -> new Slf4jLogger(k, level));
    }

    private static Slf4jLogger getLogger(String name) {
        return getLogger(name, DEFAULT_LEVEL);
    }

    private static EnumMap<Level, Map<String, Slf4jLogger>> initLoggerCache() {
        EnumMap<Level, Map<String, Slf4jLogger>> loggerCache = new EnumMap<>(Level.class);
        EnumSet.allOf(Level.class).forEach(level -> loggerCache.put(level, new ConcurrentHashMap<>()));
        return loggerCache;
    }

    private static EnumMap<Level, org.slf4j.event.Level> setLevelMap() {
        EnumMap<Level, org.slf4j.event.Level> levelMap = new EnumMap<>(Level.class);
        levelMap.put(TRACE, org.slf4j.event.Level.TRACE);
        levelMap.put(DEBUG, org.slf4j.event.Level.DEBUG);
        levelMap.put(INFO, org.slf4j.event.Level.INFO);
        levelMap.put(WARN, org.slf4j.event.Level.WARN);
        levelMap.put(ERROR, org.slf4j.event.Level.ERROR);
        return levelMap;
    }

    private static Object supply(Object o) {
        return o instanceof Supplier<?> ? ((Supplier<?>) o).get() : o;
    }

    @Override
    public Logger atLevel(Level level) {
        if (this.level == level) {
            return this;
        }
        return level == OFF ? NoopLogger.OFF : getLogger(this.name, level);
    }

    @Override
    public @NonNull Level getLevel() {
        return this.level;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void log(Object message) {
        slf4jLog(null, message, (Object[]) null);
    }

    @Override
    public void log(String message, Object... args) {
        slf4jLog(null, message, args);
    }

    @Override
    public void log(Throwable t) {
        slf4jLog(t, t.getMessage(), (Object[]) null);
    }

    @Override
    public void log(Throwable t, Object message) {
        slf4jLog(t, message, (Object[]) null);
    }

    @Override
    public void log(Throwable t, String message, Object... args) {
        slf4jLog(t, message, args);
    }

    String getName() {
        return this.name;
    }

    private void slf4jLog(Throwable t, Object message, Object... args) {
        if (!isEnabled()) {
            return;
        }
        LoggingEventBuilder loggingEventBuilder = new CallerBoundaryImmutableLoggingEventBuilder(delegateLogger,
                LEVEL_MAP.get(this.level),
                THIS_FQCN).setMessage(Objects.toString(supply(message))).setCause(t);
        if (args != null) {
            for (Object arg : args) {
                loggingEventBuilder = loggingEventBuilder.addArgument(supply(arg));
            }
        }
        loggingEventBuilder.log();
    }

    private static class CallStack {

        static StackTraceElement mostRecentCallerOf(@NonNull Class<?> calleeClass) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String calleeClassName = calleeClass.getName();
            int i = 0;
            for (; i < stackTrace.length; i++) {
                if (calleeClassName.equals(stackTrace[i].getClassName())) {
                    break;
                }
            }
            for (i++; i < stackTrace.length; i++) {
                if (!calleeClassName.equals(stackTrace[i].getClassName())) {
                    return stackTrace[i];
                }
            }
            throw new NoSuchElementException("unable to locate caller class of " + calleeClass + " in call stack "
                    + Arrays.toString(stackTrace));
        }
    }
}

