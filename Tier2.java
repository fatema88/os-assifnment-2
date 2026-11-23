
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

public class Tier2 {

    private PriorityQueue<Process> tier2Queue =
            new PriorityQueue<>(
                    Comparator.comparingInt((Process p) -> p.priority)
                            .thenComparingInt(p -> p.tier2EntryTime)
            );
    public void addProcess(Process p, int currentTime) {
        if (p.tier2EntryTime == -1) {
            p.tier2EntryTime = currentTime;
        }

        tier2Queue.add(p);
    }

    public Process executeNext(int currentTime) {

        if (tier2Queue.isEmpty()) {
            return null;
        }


        Process current = tier2Queue.poll();

        if (current.startTime == -1) {
            current.startTime = currentTime;
            current.responseTime = currentTime - current.arrivalTime;
        }

        int executionTime = current.remainingTime;
        currentTime += executionTime;

        current.remainingTime = 0;
        current.finishTime = currentTime;

        current.turnaroundTime =
                current.finishTime - current.arrivalTime;

        current.waitingTime =
                current.turnaroundTime - current.burstTime;

        return current;
    }
    public boolean isEmpty() {
        return tier2Queue.isEmpty();
    }
    public Process peekProcess() {
        return tier2Queue.peek();
    }
    public List<Process> getProcesses() {
        List<Process> list = new ArrayList<>(tier2Queue);
        list.sort(Comparator.comparingInt(p -> p.tier2EntryTime));
        return list;
    }
    public void applyAging(int currentTime) {
        List<Process> all = new ArrayList<>(tier2Queue);
        tier2Queue.clear();

        for (Process p : all) {
            int waited = currentTime - p.tier2EntryTime;
            p.priority = p.initialPriority - (waited / 4);
            tier2Queue.add(p);
        }
    }


}
