package logging;

public class Logger {
    private final Log log;
    private final String context;

    public Logger(Log log, String context) {
        this.log = log;
        this.context = context;
    }

    public void log(LogRecord record) {
        log.offer(record);
    }

    /**
     * Print Debug Message in Logger
     * @param msg Log Message
     */
    public void debug(String msg) {
        log(new LogRecord(Level.DEBUG, context, msg));
    }
    /**
     * Print Info Message in Logger
     * @param msg Log Message
     */
    public void info(String msg) {
        log(new LogRecord(Level.INFO, context, msg));
    }
    /**
     * Print Warn Message in Logger
     * @param msg Log Message
     */
    public void warn(String msg) {
        log(new LogRecord(Level.WARN, context, msg));
    }
    /**
     * Print Error Message in Logger
     * @param msg Log Message
     */
    public void error(String msg) {
        log(new LogRecord(Level.ERROR, context, msg));
    }

    public Log getLog() {
        return log;
    }
}
