package infuzion.chest.randomizer.util.configuration;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationGroup {
    private static List<ConfigurationGroup> instances = new ArrayList<>();
    private final String name;
    private final int resetTime;
    private int currentResetTime;

    public ConfigurationGroup(String name, int resetTime) {
        this.name = name;
        if (resetTime <= 0) {
            resetTime = -1;
        }
        this.resetTime = resetTime;
        this.currentResetTime = resetTime;
    }

    public String getName() {
        return name;
    }

    public int getResetTime() {
        return resetTime;
    }

    public int getCurrentResetTime() {
        return currentResetTime;
    }

    public boolean decrementResetTime() {
        if (resetTime == -1) {
            return false;
        }
        currentResetTime--;
        if (currentResetTime <= 0) {
            currentResetTime = resetTime;
            return true;
        }
        return false;
    }
}
