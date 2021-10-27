import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MyThreadPool {

    private final Map<Resource, Long> availableResources;

    private final WorkerThread[] workers;

    private final LinkedList<Task> queue = new LinkedList<>();

    public MyThreadPool(int poolSize, Map<Resource, Long> availableResources) {
        this.availableResources = availableResources;

        workers = new WorkerThread[poolSize];

        for (int i = 0; i < poolSize; i++) {
            workers[i] = new WorkerThread();
            workers[i].start();
        }
    }

    public void stop() throws InterruptedException {
        stop.value = true;
        for (final Thread thread : workers)
            thread.join();
    }

    public void execute(Task task) {
        synchronized (queue) {
            System.out.println("Added task: " + task.name);
            queue.add(task);
            queue.notifyAll();
        }
    }

    private static class BooleanWrapper {
        boolean value;
    }

    BooleanWrapper stop = new BooleanWrapper() {{
        value = false;
    }};

    private class WorkerThread extends Thread {
        public void run() {

            while (true) {

                Task selectedTask = null;
                boolean selectedEnough = false;

                synchronized (queue) {

                    while (queue.isEmpty() && !stop.value) {
                        System.out.println("Queue empty");
                        try {
                            queue.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if (stop.value && queue.isEmpty()) {
                        System.out.println("Queue empty, end of program");
                        break;
                    }

                    for (Task task : queue) {
                        boolean enoughResources = true;
                        for (final Map.Entry<Resource, Long> req : task.requirements.entrySet())
                            if (availableResources.get(req.getKey()) < req.getValue()) {
                                enoughResources = false;
                                break;
                            }

                        if (selectedTask == null) {
                            if (enoughResources || task.highPriority) {
                                selectedTask = task;
                                selectedEnough = enoughResources;
                            }
                        } else {
                            if (task.highPriority) {
                                selectedTask = task;
                                selectedEnough = enoughResources;
                            }
                        }

                        if (selectedTask != null && selectedTask.highPriority && selectedEnough) {
                            System.out.println("Selected high priority task has enough resources");
                            break;
                        }
                    }

                    if (selectedTask == null || (selectedTask.highPriority && !selectedEnough)) {
                        try {
                            queue.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (stop.value && queue.isEmpty()) {
                            System.out.println("Queue empty, end of program");
                            break;
                        }
                        continue;
                    }
                    System.out.println("Task selected: " + selectedTask.name + ", priority " + selectedTask.highPriority);

                    queue.remove(selectedTask);
                    queue.notifyAll();

                    System.out.println("Tasks left: " + queue.size() + " " + queue.stream()
                            .map(it -> it.name).collect(Collectors.toList()));

                    System.out.println("Resources left: " + availableResources.entrySet().stream()
                            .map(entry -> entry.getKey() + " - " + entry.getValue())
                            .collect(Collectors.joining(", ")));
                }

                synchronized (availableResources) {
                    for (final Map.Entry<Resource, Long> req : selectedTask.requirements.entrySet())
                        availableResources.put(req.getKey(), availableResources.get(req.getKey()) - req.getValue());
                    availableResources.notifyAll();
                }

                try {
                    selectedTask.function.run();
                    selectedTask.executed = true;
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }

                synchronized (availableResources) {
                    for (final Map.Entry<Resource, Long> req : selectedTask.requirements.entrySet())
                        availableResources.put(req.getKey(), availableResources.get(req.getKey()) + req.getValue());
                    availableResources.notifyAll();
                }
            }
        }
    }
}

class Main {

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
