package elf4j.slf4j;

import org.slf4j.Logger;
import org.slf4j.event.Level;
import org.slf4j.spi.DefaultLoggingEventBuilder;

class CallerBoundaryConfigurableLoggingEventBuilder extends DefaultLoggingEventBuilder {
    private boolean needToInferCaller;

    public CallerBoundaryConfigurableLoggingEventBuilder(Logger logger, Level level) {
        super(logger, level);
        needToInferCaller = true;
    }

    @Override
    public void setCallerBoundary(String fqcn) {
        if (!needToInferCaller) {
            return;
        }
        super.setCallerBoundary(fqcn);
        needToInferCaller = false;
    }
}
