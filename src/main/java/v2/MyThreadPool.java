package v2;

import common.Resource;
import common.Task;

import java.util.LinkedList;
import java.util.Map;
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
