import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Forest {
    private final LinkedList<Task> queue;
    private final Lock lock;
    private final Condition checkQueue;
    private Map<Resource, Long> availableResources;
    private ArrayList<Thread> workers;


    public Forest() {
        this.queue = new LinkedList<>();
        this.lock = new ReentrantLock();
        this.checkQueue = lock.newCondition();
        this.availableResources = new HashMap<>();
        this.workers = new ArrayList<>();
    }


    private static class BooleanWrapper {
        boolean value;
    }

    BooleanWrapper stop = new BooleanWrapper() {{
        value = false;
    }};

    public void makeResources(Map<Resource, Long> resources) {
        lock.lock();
        availableResources = new HashMap<>(resources);
        checkQueue.signalAll();
        lock.unlock();
    }

    public void setResource(Resource resource, Long amount) {
        lock.lock();
        availableResources.put(resource, amount);
        checkQueue.signalAll();
        lock.unlock();
    }

    public void addTasks(ArrayList<Task> tasks) {
        lock.lock();
        queue.addAll(tasks);
        checkQueue.signalAll();
        lock.unlock();
    }

    public void makeWorkers(final int nWorkers) {
        for (int i = 0; i < nWorkers; ++i) {
            workers.add(new Thread(() -> {
                try {
                    while (true) {
                        lock.lock();

                        if (stop.value && queue.isEmpty())
                            break;

                        if (queue.isEmpty())
                            checkQueue.await();

                        Task selectedTask = null;
                        boolean selectedEnough = false;
                        for (final Task task : queue) {
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

                            if (selectedTask != null && selectedTask.highPriority && selectedEnough)
                                break;
                        }

                        if (selectedTask == null || (selectedTask.highPriority && !selectedEnough)) {
                            checkQueue.await();

                            if (stop.value && queue.isEmpty())
                                break;

                            lock.unlock();
                            continue;
                        }

                        queue.remove(selectedTask);
                        for (final Map.Entry<Resource, Long> req : selectedTask.requirements.entrySet())
                            availableResources.put(req.getKey(), availableResources.get(req.getKey()) - req.getValue());

                        checkQueue.signalAll();

                        lock.unlock();

                        selectedTask.function.run();

                        lock.lock();

                        for (final Map.Entry<Resource, Long> req : selectedTask.requirements.entrySet())
                            availableResources.put(req.getKey(), availableResources.get(req.getKey()) + req.getValue());

                        lock.unlock();
                    }
                } catch (InterruptedException ignored) {
                } finally {
                    lock.unlock();
                }
            }));

            workers.get(workers.size() - 1).start();
        }
    }

    public void stop() {
        lock.lock();
        stop.value = true;
        checkQueue.signalAll();
        lock.unlock();
    }

    public void killWorkers() throws InterruptedException {
        for (final Thread thread : workers)
            thread.join();
    }
}
