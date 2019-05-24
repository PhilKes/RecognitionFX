package logging;

import java.util.Date;

class LogRecord {
    private Date timestamp;
    private Level  level;
    private String context;
    private String message;

    public LogRecord(Level level, String context, String message) {
        this.timestamp = new Date();
        this.level     = level;
        this.context   = context;
        this.message   = message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Level getLevel() {
        return level;
    }

    public String getContext() {
        return level.name();
    }

    public String getMessage() {
        return message;
    }
}