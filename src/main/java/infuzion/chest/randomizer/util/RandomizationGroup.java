package infuzion.chest.randomizer.util;

import java.util.ArrayList;
import java.util.List;

public class RandomizationGroup {
    private final static List<RandomizationGroup> instances = new ArrayList<>();
    private final String name;

    private RandomizationGroup(String name) {
        this.name = name;
        instances.add(this);
    }

    public static RandomizationGroup getGroup(String name) {
        for (RandomizationGroup e : instances) {
            if (e.getName().equals(name)) {
                return e;
            }
        }
        return new RandomizationGroup(name);
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof RandomizationGroup && ((RandomizationGroup) o).getName().equals(name);
    }
}
