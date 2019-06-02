package logging;

import java.util.Collection;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class Log {
    private static final int MAX_LOG_ENTRIES = 1_000_000;

    private final BlockingDeque<LogRecord> log = new LinkedBlockingDeque<>(MAX_LOG_ENTRIES);

    public int drainTo(Collection<? super LogRecord> collection) {
        return log.drainTo(collection);
    }

    public void offer(LogRecord record) {
        log.offer(record);
    }
}
