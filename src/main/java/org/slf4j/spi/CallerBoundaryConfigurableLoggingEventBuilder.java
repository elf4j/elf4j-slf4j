package org.slf4j.spi;

import org.slf4j.Logger;
import org.slf4j.event.Level;

public class CallerBoundaryConfigurableLoggingEventBuilder extends DefaultLoggingEventBuilder {

    public CallerBoundaryConfigurableLoggingEventBuilder(Logger logger, Level level, String callerBoundaryFqcn) {
        super(logger, level);
        DLEB_FQCN = callerBoundaryFqcn;
    }
}
