import java.util.Map;

public class Task {
    public boolean highPriority;
    public Runnable function;
    public Map<Resource, Long> requirements;
}
