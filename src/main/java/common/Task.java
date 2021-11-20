package common;

import java.util.Map;

public class Task {
    public String name;
    public boolean highPriority;
    public Runnable function;
    public Map<Resource, Long> requirements;
    public boolean executed = false;
}
