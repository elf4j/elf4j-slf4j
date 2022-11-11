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
    private static final String EMPTY_MESSAGE = "";
    private static final EnumMap<Level, org.slf4j.event.Level> LEVEL_MAP = setLeveMap();
    private static final EnumMap<Level, Map<String, Slf4jLogger>> LOGGER_CACHE = initLoggerCache();
    private static final String THIS_FQCN = Slf4jLogger.class.getName();
    @NonNull private final String name;
    @NonNull private final Level level;
    @NonNull private final org.slf4j.Logger nativeLogger;

    private Slf4jLogger(@NonNull String name, @NonNull Level level) {
        this.name = name;
        this.level = level;
        this.nativeLogger = LoggerFactory.getLogger(name);
    }

    static Slf4jLogger instance() {
        return getLogger(CallStack.mostRecentCallerOf(Logger.class).getClassName());
    }

    static Slf4jLogger instance(String name) {
        return getLogger(name == null ? CallStack.mostRecentCallerOf(Logger.class).getClassName() : name);
    }

    static Slf4jLogger instance(Class<?> clazz) {
        return getLogger(clazz == null ? CallStack.mostRecentCallerOf(Logger.class).getClassName() : clazz.getName());
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

    private static EnumMap<Level, org.slf4j.event.Level> setLeveMap() {
        EnumMap<Level, org.slf4j.event.Level> levelMap = new EnumMap<>(Level.class);
        levelMap.put(TRACE, org.slf4j.event.Level.TRACE);
        levelMap.put(DEBUG, org.slf4j.event.Level.DEBUG);
        levelMap.put(INFO, org.slf4j.event.Level.INFO);
        levelMap.put(WARN, org.slf4j.event.Level.WARN);
        levelMap.put(ERROR, org.slf4j.event.Level.ERROR);
        return levelMap;
    }

    @Override
    public Logger atTrace() {
        return atLevel(TRACE);
    }

    @Override
    public Logger atDebug() {
        return atLevel(DEBUG);
    }

    @Override
    public Logger atInfo() {
        return atLevel(INFO);
    }

    @Override
    public Logger atWarn() {
        return atLevel(WARN);
    }

    @Override
    public Logger atError() {
        return atLevel(ERROR);
    }

    @Override
    public @NonNull String getName() {
        return this.name;
    }

    @Override
    public @NonNull Level getLevel() {
        return this.level;
    }

    @Override
    public boolean isEnabled() {
        if (this.level == OFF) {
            return false;
        }
        return !isLevelDisabled();
    }

    @Override
    public void log(Object message) {
        if (isLevelDisabled()) {
            return;
        }
        slf4jLog(null, Objects.toString(message), (Object[]) null);
    }

    @Override
    public void log(Supplier<?> message) {
        if (isLevelDisabled()) {
            return;
        }
        slf4jLog(null, Objects.toString(message.get()), (Object[]) null);
    }

    @Override
    public void log(String message, Object... args) {
        if (isLevelDisabled()) {
            return;
        }
        slf4jLog(null, message, args);
    }

    @Override
    public void log(String message, Supplier<?>... args) {
        if (isLevelDisabled()) {
            return;
        }
        slf4jLog(null, message, Arrays.stream(args).map(Supplier::get).toArray(Object[]::new));
    }

    @Override
    public void log(Throwable t) {
        if (isLevelDisabled()) {
            return;
        }
        slf4jLog(t, EMPTY_MESSAGE, (Object[]) null);
    }

    @Override
    public void log(Throwable t, Object message) {
        if (isLevelDisabled()) {
            return;
        }
        slf4jLog(t, Objects.toString(message), (Object[]) null);
    }

    @Override
    public void log(Throwable t, Supplier<?> message) {
        if (isLevelDisabled()) {
            return;
        }
        slf4jLog(t, message == null ? null : Objects.toString(message.get()), (Object[]) null);
    }

    @Override
    public void log(Throwable t, String message, Object... args) {
        if (isLevelDisabled()) {
            return;
        }
        slf4jLog(t, message, args);
    }

    @Override
    public void log(Throwable t, String message, Supplier<?>... args) {
        if (isLevelDisabled()) {
            return;
        }
        slf4jLog(t, message, args == null ? null : Arrays.stream(args).map(Supplier::get).toArray(Object[]::new));
    }

    private void slf4jLog(Throwable t, String message, Object... args) {
        CallerBoundaryConfigurableLoggingEventBuilder callerBoundaryConfigurableLoggingEventBuilder =
                new CallerBoundaryConfigurableLoggingEventBuilder(nativeLogger, LEVEL_MAP.get(this.level));
        callerBoundaryConfigurableLoggingEventBuilder.setCallerBoundary(THIS_FQCN);
        LoggingEventBuilder loggingEventBuilder =
                callerBoundaryConfigurableLoggingEventBuilder.setMessage(message).setCause(t);
        if (args != null) {
            for (Object arg : args) {
                loggingEventBuilder = loggingEventBuilder.addArgument(arg);
            }
        }
        loggingEventBuilder.log();
    }

    private Logger atLevel(Level level) {
        if (this.level == level) {
            return this;
        }
        return level == OFF ? NoopLogger.INSTANCE : getLogger(this.name, level);
    }

    private boolean isLevelDisabled() {
        switch (this.level) {
            case TRACE:
                return !nativeLogger.isTraceEnabled();
            case DEBUG:
                return !nativeLogger.isDebugEnabled();
            case INFO:
                return !nativeLogger.isInfoEnabled();
            case WARN:
                return !nativeLogger.isWarnEnabled();
            case ERROR:
                return !nativeLogger.isErrorEnabled();
            default:
                return true;
        }
    }

    private static class CallStack {

        static StackTraceElement mostRecentCallerOf(@NonNull Class<?> calleeClass) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String calleeClassName = calleeClass.getName();
            for (int i = 0; i < stackTrace.length; i++) {
                if (calleeClassName.equals(stackTrace[i].getClassName())) {
                    for (int j = i + 1; j < stackTrace.length; j++) {
                        if (!calleeClassName.equals(stackTrace[j].getClassName())) {
                            return stackTrace[j];
                        }
                    }
                    break;
                }
            }
            throw new NoSuchElementException("unable to locate caller class of " + calleeClass + " in call stack "
                    + Arrays.toString(stackTrace));
        }
    }
}

