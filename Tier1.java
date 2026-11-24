
import java.util.*;

public class Tier1 {

    Queue<Process> queue = new LinkedList<>();

    public void addProcess(Process p) {
        queue.add(p);
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public Process peekProcess() {
        return queue.peek();
    }

    public SliceResult executeNext(int quantum, int currentTime) {

        if (quantum <= 0) {
            throw new IllegalArgumentException("Quantum must be positive.");
        }

        if (queue.isEmpty()) {
            return null;
        }

        Process p = queue.poll();

        if (p.startTime == -1) {
            p.startTime = currentTime;
            p.responseTime = currentTime - p.arrivalTime;
        }

        int executionTime = Math.min(quantum, p.remainingTime);

        p.remainingTime -= executionTime;

        boolean finished = p.remainingTime == 0;
        boolean demoted = p.remainingTime > 0;

        if (finished) {
            p.finishTime = currentTime + executionTime;
            p.turnaroundTime = p.finishTime - p.arrivalTime;
            p.waitingTime = p.turnaroundTime - p.burstTime;
        }

        return new SliceResult(p, executionTime, finished, demoted);
    }

    public static class SliceResult {

        Process process;
        int executionTime;
        boolean finished;
        boolean demoted;

        public SliceResult(Process process, int executionTime,
                boolean finished, boolean demoted) {

            this.process = process;
            this.executionTime = executionTime;
            this.finished = finished;
            this.demoted = demoted;
        }
    }

    public List<Process> getProcesses() {
        return new ArrayList<>(queue);
    }
}
