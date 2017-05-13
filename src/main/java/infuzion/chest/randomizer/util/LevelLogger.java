package infuzion.chest.randomizer.util;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LevelLogger extends Logger {
    private Logger logger;
    private Level level;

    public LevelLogger(Logger logger) {
        super(logger.getName(), logger.getResourceBundleName());
        this.logger = logger;
        level = Level.INFO;
    }

    private String prefix(Level level, String msg) {
        return '[' + level.getName() + "] " + msg;
    }

    private boolean shouldLog(Level level) {
        return level.intValue() >= this.level.intValue();
    }

    public void log(Level level, String str) {
        if (shouldLog(level)) {
            logger.log(Level.INFO, prefix(level, str));
        }
    }

    @Override
    public void config(String msg) {
        log(Level.CONFIG, msg);
    }

    public void fine(String msg) {
        log(Level.FINE, msg);
    }

    @Override
    public void finer(String msg) {
        log(Level.FINER, msg);
    }

    @Override
    public void finest(String msg) {
        log(Level.FINEST, msg);
    }

    @Override
    public void setLevel(Level level) {
        this.level = level;
    }
}
