package infuzion.chest.randomizer.util.randomize;

import java.util.ArrayList;
import java.util.List;

public class RandomizationGroup {
    private static final List<RandomizationGroup> instances = new ArrayList<>();
    private final String name;

    private RandomizationGroup(String name) {
        this.name = name;
        instances.add(this);
    }

    public static RandomizationGroup getGroup(String name) {
        for (RandomizationGroup e : instances) {
            if (e.name.equals(name)) {
                return e;
            }
        }
        return new RandomizationGroup(name);
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof RandomizationGroup) && ((RandomizationGroup) o).name.equals(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
