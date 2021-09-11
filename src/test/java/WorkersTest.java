import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class WorkersTest {
    static final int ONE_WORKER_TIME = 45000;
    static final int TWO_WORKERS_TIME = 26000;
    static final int FIVE_WORKERS_TIME = 14000;

    final HashMap<Resource, Long> resources = new HashMap<Resource, Long>() {{
        put(Resource.CATS, 42L);
        put(Resource.DOGS, 23L);
        put(Resource.OWLS, 10L);
        put(Resource.MICE, 99L);
    }};

    final ArrayList<Task> tasks = new ArrayList<Task>() {
        {
            add(new Task() {{
                highPriority = false;
                requirements = new HashMap<Resource, Long>() {{
                    put(Resource.CATS, 20L);
                }};
                function = () -> {
                    System.out.println("20 cats are sleeping for 5s");
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException ignored) {
                    }
                };
            }});
            add(new Task() {{
                highPriority = true;
                requirements = new HashMap<Resource, Long>() {{
                    put(Resource.CATS, 22L);
                }};
                function = () -> {
                    System.out.println("22 cats are sleeping for 3s");
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException ignored) {
                    }
                };
            }});
            add(new Task() {{
                highPriority = true;
                requirements = new HashMap<Resource, Long>() {{
                    put(Resource.CATS, 42L);
                }};
                function = () -> {
                    System.out.println("42 cats are sleeping for 5s");
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException ignored) {
                    }
                };
            }});
            add(new Task() {{
                highPriority = false;
                requirements = new HashMap<Resource, Long>() {{
                    put(Resource.DOGS, 10L);
                }};
                function = () -> {
                    System.out.println("10 dogs are sleeping for 10s");
                    try {
                        TimeUnit.SECONDS.sleep(10);
                    } catch (InterruptedException ignored) {
                    }
                };
            }});
            add(new Task() {{
                highPriority = false;
                requirements = new HashMap<Resource, Long>() {{
                    put(Resource.DOGS, 13L);
                }};
                function = () -> {
                    System.out.println("13 dogs are sleeping for 5s");
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException ignored) {
                    }
                };
            }});
            add(new Task() {{
                highPriority = true;
                requirements = new HashMap<Resource, Long>() {{
                    put(Resource.DOGS, 23L);
                }};
                function = () -> {
                    System.out.println("23 dogs are sleeping for 1s");
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException ignored) {
                    }
                };
            }});
            add(new Task() {{
                highPriority = false;
                requirements = new HashMap<Resource, Long>() {{
                    put(Resource.OWLS, 10L);
                }};
                function = () -> {
                    System.out.println("10 owls are watching for 7s");
                    try {
                        TimeUnit.SECONDS.sleep(7);
                    } catch (InterruptedException ignored) {
                    }
                };
            }});
            add(new Task() {{
                highPriority = false;
                requirements = new HashMap<Resource, Long>() {{
                    put(Resource.MICE, 99L);
                }};
                function = () -> {
                    System.out.println("99 mice are running for 7s");
                    try {
                        TimeUnit.SECONDS.sleep(7);
                    } catch (InterruptedException ignored) {
                    }
                };
            }});
            add(new Task() {{
                highPriority = false;
                requirements = new HashMap<>(resources);
                function = () -> {
                    System.out.println("This is the last task");
                };
            }});
        }
    };

    @DisplayName("Test with 1 worker")
    @Test
    public void testOneWorker() throws InterruptedException {
        Forest forest = new Forest();

        long start = System.currentTimeMillis();

        forest.makeResources(resources);

        forest.makeWorkers(1);

        forest.addTasks(tasks);

        forest.stop();

        forest.killWorkers();

        long finish = System.currentTimeMillis();

        Assertions.assertTrue(finish - start <= ONE_WORKER_TIME);
    }

    @DisplayName("Test with 2 workers")
    @Test
    public void testTwoWorkers() throws InterruptedException {
        Forest forest = new Forest();

        long start = System.currentTimeMillis();

        forest.makeResources(resources);
        forest.makeWorkers(2);
        forest.addTasks(tasks);
        forest.stop();
        forest.killWorkers();

        long finish = System.currentTimeMillis();

        Assertions.assertTrue(finish - start <= TWO_WORKERS_TIME);
    }

    @DisplayName("Test with 5 workers")
    @Test
    public void testFiveWorkers() throws InterruptedException {
        Forest forest = new Forest();

        long start = System.currentTimeMillis();

        forest.makeResources(resources);
        forest.makeWorkers(5);
        forest.addTasks(tasks);
        forest.stop();
        forest.killWorkers();

        long finish = System.currentTimeMillis();

        Assertions.assertTrue(finish - start <= FIVE_WORKERS_TIME);
    }
}
