package elf4j.slf4j;

import org.slf4j.Logger;
import org.slf4j.event.Level;
import org.slf4j.spi.DefaultLoggingEventBuilder;

class CallerBoundaryCachingLoggingEventBuilder extends DefaultLoggingEventBuilder {
    private boolean callerBoundaryCached;

    public CallerBoundaryCachingLoggingEventBuilder(Logger logger, Level level) {
        super(logger, level);
    }

    @Override
    public void setCallerBoundary(String fqcn) {
        if (callerBoundaryCached) {
            return;
        }
        super.setCallerBoundary(fqcn);
        callerBoundaryCached = true;
    }
}
