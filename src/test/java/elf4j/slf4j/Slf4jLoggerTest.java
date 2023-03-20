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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class Slf4jLoggerTest {
    public static final Logger LOGGER = Logger.instance();

    @Nested
    class levels {

        @Test
        void optToSupplyDefaultLevelAsInfo() {
            assertTrue(LOGGER.atInfo().isEnabled());
        }

        @Test
        void noArgAtHonorsLeveOnMethodName() {
            assertFalse(LOGGER.atTrace().isEnabled());
            assertTrue(LOGGER.atDebug().isEnabled());
        }
    }

    @Nested
    class log {

        @Test
        void object() {
            LOGGER.log("log message");
        }

        @Test
        void supplier() {
            LOGGER.atTrace().log((Supplier) () -> "supplier message");
        }

        @Test
        void messageAndArgs() {
            LOGGER.atDebug().log("message arg1 {}, arg2 {}", "a11111", new Object());
        }

        @Test
        void messageAndSuppliers() {
            LOGGER.atWarn()
                    .log("message supplier arg1 {}, arg2 {}, arg3 {}",
                            (Supplier) () -> "a11111",
                            "a22222",
                            (Supplier) () -> Arrays.stream(new Object[] { "a33333" }).collect(Collectors.toList()));
        }

        @Test
        void throwable() {
            LOGGER.atError().log(new Exception("ex message"));
        }

        @Test
        void throwableAndMessage() {
            LOGGER.atError().log(new Exception("ex message"), "log message");
        }

        @Test
        void throwableAndSupplier() {
            LOGGER.atError().log(new Exception("ex message"), (Supplier) () -> "supplier log message");
        }

        @Test
        void throwableAndMessageAndArgs() {
            LOGGER.atError().log(new Exception("ex message"), "log message with arg {}", "a11111");
        }

        @Test
        void throwableAndMessageAndSupplierArgs() {
            LOGGER.atError()
                    .log(new Exception("ex message"),
                            "log message with supplier arg1 {}, arg2 {}, arg3 {}",
                            "a11111",
                            "a22222",
                            (Supplier) () -> Arrays.stream(new Object[] { "a33333" }).collect(Collectors.toList()));
        }
    }

    @Nested
    class name {
        @Test
        void loggerNameForNullOrNoargInstanceCaller() {
            String thisClassName = this.getClass().getName();
            assertEquals(thisClassName, ((Slf4jLogger) Logger.instance()).getName());
        }
    }

    @Nested
    class readmeSamples {
        private final Slf4jLogger logger = (Slf4jLogger) Logger.instance();

        @Test
        void messagesArgsAndGuards() {
            logger.atWarn().log("message with arguments - arg1 {}, arg2 {}, arg3 {}", "a11111", "a22222", "a33333");
            logger.atInfo().log("info message");
            Logger debug = logger.atDebug();
            assertNotSame(logger, debug);
            assertEquals(logger.getName(), ((Slf4jLogger) debug).getName());
            assertEquals(Level.DEBUG, debug.getLevel());
            if (debug.isEnabled()) {
                debug.log("a {} guarded by a {}, so {} is created {} DEBUG level is {}",
                        "long message",
                        "level check",
                        "no message object",
                        "unless",
                        "enabled by the configuration of the logging provider");
            }
            debug.log((Supplier) () -> "alternative to the level guard, using a supplier function should achieve the same goal, pending quality of the logging provider");
        }

        @Test
        void throwableAndMessageAndArgs() {
            logger.atInfo().log("let's see immutability in action...");
            Logger error = logger.atError();
            error.log("this is an immutable logger instance whose level is Level.ERROR");
            Throwable ex = new Exception("ex message");
            error.log(ex);
            error.log(ex, "level set omitted but we know the level is Level.ERROR");
            error.atWarn()
                    .log(ex,
                            "the log level switched to WARN on the fly. that is, {} returns a {} and {} Logger {}",
                            "atWarn()",
                            "different",
                            "immutable",
                            "instance");
            error.atError()
                    .log(ex,
                            "here the {} call is {} because the {} instance is {}, and the instance's log level has and will always be Level.ERROR",
                            "atError()",
                            "unnecessary",
                            "error logger",
                            "immutable");
            error.log(ex,
                    "now at Level.ERROR, together with the exception stack trace, logging some items expensive to compute: item1 {}, item2 {}, item3 {}, item4 {}, ...",
                    "i11111",
                    "i22222",
                    "i33333",
                    (Supplier) () -> Arrays.stream(new Object[] { "i44444" }).collect(Collectors.toList()));
        }
    }
}