import common.Resource;
import common.Task;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import v1.Forest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PriorityTest {
    static final String HIGH_PRIORITY_MESSAGE = "6 dogs are sleeping for 2s";
    static final String LOW_PRIORITY_MESSAGE = "42 cats are sleeping for 2s";

    String output = "";

    Resource resource;

    public final static HashMap<Resource, Long> resources = new HashMap<Resource, Long>() {{
        put(Resource.CATS, 42L);
        put(Resource.DOGS, 6L);
    }};

    final ArrayList<Task> tasks = new ArrayList<Task>() {
        {
            add(new Task() {{
                highPriority = false;
                requirements = new HashMap<Resource, Long>() {{
                    put(Resource.CATS, 42L);
                }};
                function = () -> {
                    output += LOW_PRIORITY_MESSAGE;
                    System.out.println(LOW_PRIORITY_MESSAGE);
                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException ignored) {
                    }
                };
            }});
            add(new Task() {{
                highPriority = true;
                requirements = new HashMap<Resource, Long>() {{
                    put(Resource.DOGS, 6L);
                }};
                function = () -> {
                    output += HIGH_PRIORITY_MESSAGE;
                    System.out.println(HIGH_PRIORITY_MESSAGE);
                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException ignored) {
                    }
                };
            }});
        }
    };

    @DisplayName("Test that high priority task is executed first")
    @Test
    public void testPriority() throws InterruptedException {
        Forest forest = new Forest();

        forest.makeResources(resources);
        forest.makeWorkers(1);
        forest.addTasks(tasks);
        forest.stop();
        forest.killWorkers();

        assertEquals(HIGH_PRIORITY_MESSAGE + LOW_PRIORITY_MESSAGE, output);
    }
}
