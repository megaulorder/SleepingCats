package v2;

import common.Resource;
import common.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

class MyThreadPoolMain {

    public static void main(String[] args) throws InterruptedException {

        final HashMap<Resource, Long> resources = new HashMap<Resource, Long>() {{
            put(Resource.CATS, 42L);
            put(Resource.DOGS, 23L);
            put(Resource.OWLS, 10L);
            put(Resource.MICE, 99L);
        }};

        final LinkedList<Task> tasks = new LinkedList<>(new ArrayList<Task>() {
            {
                add(new Task() {{
                    name = "20 cats are sleeping for 5s";
                    highPriority = false;
                    requirements = new HashMap<Resource, Long>() {{
                        put(Resource.CATS, 20L);
                    }};
                    function = () -> {
                        System.out.println(name + "\n");
                        try {
                            TimeUnit.SECONDS.sleep(5);
                        } catch (InterruptedException ignored) {
                        }
                    };
                }});
                add(new Task() {{
                    name = "22 cats are sleeping for 3s";
                    highPriority = true;
                    requirements = new HashMap<Resource, Long>() {{
                        put(Resource.CATS, 22L);
                    }};
                    function = () -> {
                        System.out.println(name + "\n");
                        try {
                            TimeUnit.SECONDS.sleep(3);
                        } catch (InterruptedException ignored) {
                        }
                    };
                }});
                add(new Task() {{
                    name = "42 cats are sleeping for 5s";
                    highPriority = true;
                    requirements = new HashMap<Resource, Long>() {{
                        put(Resource.CATS, 42L);
                    }};
                    function = () -> {
                        System.out.println(name + "\n");
                        try {
                            TimeUnit.SECONDS.sleep(5);
                        } catch (InterruptedException ignored) {
                        }
                    };
                }});
                add(new Task() {{
                    name = "10 dogs are sleeping for 10s";
                    highPriority = false;
                    requirements = new HashMap<Resource, Long>() {{
                        put(Resource.DOGS, 10L);
                    }};
                    function = () -> {
                        System.out.println(name + "\n");
                        try {
                            TimeUnit.SECONDS.sleep(10);
                        } catch (InterruptedException ignored) {
                        }
                    };
                }});
                add(new Task() {{
                    name = "13 dogs are sleeping for 5s";
                    highPriority = false;
                    requirements = new HashMap<Resource, Long>() {{
                        put(Resource.DOGS, 13L);
                    }};
                    function = () -> {
                        System.out.println(name + "\n");
                        try {
                            TimeUnit.SECONDS.sleep(5);
                        } catch (InterruptedException ignored) {
                        }
                    };
                }});
                add(new Task() {{
                    name = "23 dogs are sleeping for 15s";
                    highPriority = true;
                    requirements = new HashMap<Resource, Long>() {{
                        put(Resource.DOGS, 23L);
                    }};
                    function = () -> {
                        System.out.println(name + "\n");
                        try {
                            TimeUnit.SECONDS.sleep(15);
                        } catch (InterruptedException ignored) {
                        }
                    };
                }});
                add(new Task() {{
                    name = "10 owls are watching for 7s";
                    highPriority = false;
                    requirements = new HashMap<Resource, Long>() {{
                        put(Resource.OWLS, 10L);
                    }};
                    function = () -> {
                        System.out.println(name + "\n");
                        try {
                            TimeUnit.SECONDS.sleep(7);
                        } catch (InterruptedException ignored) {
                        }
                    };
                }});
                add(new Task() {{
                    name = "99 mice are running for 7s";
                    highPriority = false;
                    requirements = new HashMap<Resource, Long>() {{
                        put(Resource.MICE, 99L);
                    }};
                    function = () -> {
                        System.out.println(name + "\n");
                        try {
                            TimeUnit.SECONDS.sleep(7);
                        } catch (InterruptedException ignored) {
                        }
                    };
                }});
                add(new Task() {{
                    name = "This is the last task";
                    highPriority = false;
                    requirements = new HashMap<>(resources);
                    function = () -> {
                        System.out.println(name + "\n");
                    };
                }});
            }
        });

        MyThreadPool myThreadPool = new MyThreadPool(4, resources);

        LinkedList<Task> test = new LinkedList<Task>();

        Thread taskThread = new Thread(() -> {
            for (Task task : tasks) {
                myThreadPool.execute(task);
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        taskThread.start();
        taskThread.join();

        myThreadPool.stop();
    }
}
