package v1;

import common.Resource;
import common.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class ForestMain {

    public static void main(String[] args) throws InterruptedException {
        final HashMap<Resource, Long> resources = new HashMap<Resource, Long>() {{
            put(Resource.CATS, 42L);
            put(Resource.DOGS, 23L);
            put(Resource.OWLS, 10L);
            put(Resource.MICE, 99L);
        }};

        Forest forest = new Forest();

        forest.makeResources(resources);

        forest.makeWorkers(4);

        forest.addTasks(new ArrayList<Task>() {
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
        });

        forest.stop();

        forest.killWorkers();
    }
}
