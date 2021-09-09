import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {

    private static class BooleanWrapper {
        boolean value;
    }

    private static void addTask(LinkedList<Task> queue, Task task) {
        queue.add(task);
    }

    public static void main(String[] args) throws InterruptedException {
        final Map<Resource, Long> availableResources = new HashMap<Resource, Long>() {{
            put(Resource.CATS, 42L);
            put(Resource.DOGS, 23L);
            put(Resource.OWLS, 10L);
            put(Resource.MICE, 99L);
        }};

        LinkedList<Task> queue = new LinkedList<>();

        BooleanWrapper stop = new BooleanWrapper() {{
            value = false;
        }};

        Lock lock = new ReentrantLock();
        Condition checkQueue = lock.newCondition();

        ArrayList<Thread> workers = new ArrayList<>();
        for (int i = 0; i < 4; ++i) {
            workers.add(new Thread(() -> {
                try {
                    while (true) {
                        lock.lock();

                        if (queue.isEmpty() && !stop.value)
                            checkQueue.await();

                        if (stop.value)
                            break;

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

                            if (stop.value)
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

        lock.lock();

        addTask(queue, new Task() {{
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

        addTask(queue, new Task() {{
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

        addTask(queue, new Task() {{
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

        addTask(queue, new Task() {{
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

        addTask(queue, new Task() {{
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

        addTask(queue, new Task() {{
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

        addTask(queue, new Task() {{
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

        addTask(queue, new Task() {{
            highPriority = false;
            requirements = new HashMap<Resource, Long>() {{
                put(Resource.OWLS, 99L);
            }};
            function = () -> System.out.println("This task is never executed. Not enough owls");
        }});

        addTask(queue, new Task() {{
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

        addTask(queue, new Task() {{
            highPriority = false;
            requirements = new HashMap<Resource, Long>() {{
                put(Resource.CATS, 42L);
                put(Resource.DOGS, 23L);
                put(Resource.OWLS, 10L);
                put(Resource.MICE, 99L);
            }};
            function = () -> {
                System.out.println("This is the last task");
                lock.lock();
                stop.value = true;
                checkQueue.signalAll();
                lock.unlock();
            };
        }});

        checkQueue.signalAll();
        lock.unlock();

        for (final Thread thread : workers)
            thread.join();
    }
}
