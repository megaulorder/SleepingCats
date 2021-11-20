package v3;

import common.Resource;
import common.Task;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Model {

    private final ThreadPoolExecutor threadPoolExecutor;
    private final Map<Resource, Long> availableResources;
    private final LinkedList<Task> queue = new LinkedList<>();

    private static class BooleanWrapper {
        boolean value;
    }

    private final BooleanWrapper stopExecution = new BooleanWrapper() {{
        value = false;
    }};

    public Model(int corePoolSize, Map<Resource, Long> availableResources) {
        threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(corePoolSize);
        this.availableResources = availableResources;
    }

    public void push(Task task) {
        synchronized (stopExecution) {
            if (stopExecution.value)
                return;
        }

        queue.push(task);
        executeNext();
    }

    public void stop() throws InterruptedException {
        synchronized (stopExecution) {
            stopExecution.value = true;
        }

        while (!threadPoolExecutor.awaitTermination(1, TimeUnit.DAYS));
    }

    private void executeNext() {
        Task selectedTask = null;
        boolean selectedEnough = false;

        synchronized (queue) {
            if (queue.isEmpty()) {
                synchronized (stopExecution) {
                    if (stopExecution.value)
                        threadPoolExecutor.shutdown();
                }

                return;
            }

            synchronized (availableResources) {
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

                if (selectedTask == null || (selectedTask.highPriority && !selectedEnough))
                    return;

                queue.remove(selectedTask);

                for (final Map.Entry<Resource, Long> req : selectedTask.requirements.entrySet())
                    availableResources.put(req.getKey(), availableResources.get(req.getKey()) - req.getValue());

                final Task taskToExecute = selectedTask;
                threadPoolExecutor.execute(() -> {
                    taskToExecute.function.run();
                    taskToExecute.executed = true;

                    synchronized (availableResources) {
                        for (final Map.Entry<Resource, Long> req : taskToExecute.requirements.entrySet())
                            availableResources.put(req.getKey(), availableResources.get(req.getKey()) + req.getValue());
                    }

                    executeNext();
                });
            }
        }
    }
}
