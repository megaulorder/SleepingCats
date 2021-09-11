import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResourcesTest {
    static final String MESSAGE = "43 cats are sleeping for 2s";
    static final long nCats = 42L;

    String output = "";

    public final static HashMap<Resource, Long> resources = new HashMap<Resource, Long>() {{
        put(Resource.CATS, nCats);
    }};

    final ArrayList<Task> tasks = new ArrayList<Task>() {
        {
            add(new Task() {{
                highPriority = false;
                requirements = new HashMap<Resource, Long>() {{
                    put(Resource.CATS, nCats + 1L);
                }};
                function = () -> {
                    output += MESSAGE;
                    System.out.println(MESSAGE);
                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException ignored) {
                    }
                };
            }});
        }
    };

    @DisplayName("Test that task is executed when there are enough resources")
    @Test
    public void testResources() throws InterruptedException {
        Forest forest = new Forest();

        forest.makeResources(resources);
        forest.makeWorkers(1);
        forest.addTasks(tasks);

        sleep(5000);

        forest.setResource(Resource.CATS, nCats + 1L);

        forest.stop();
        forest.killWorkers();

        assertEquals(MESSAGE, output);
    }
}
