import java.util.*;
import java.io.PrintStream;

public class PreemptiveSJF {

    public static void runSJF(List<Process> inputProcesses, int contextSwitch) {
        runSJF(inputProcesses, contextSwitch, System.out);
    }

    public static void runSJF(List<Process> inputProcesses, int contextSwitch, PrintStream out) {
        if (contextSwitch < 0) {
            throw new IllegalArgumentException("Context switch time cannot be negative.");
        }

        List<Process> processes = new ArrayList<>();
        for (Process p : inputProcesses) {
            processes.add(new Process(p));
        }

        int n = processes.size();
        int completed = 0;
        int currentTime = 0;
        Process lastExecutedProcess = null;

        int csRemaining = 0;

        boolean justFinishedContextSwitch = false;

        out.println("=== Execution Timeline (CS = " + contextSwitch + ") ===");

        while (completed < n) {

            // Account for every unit of context-switch time.
            if (csRemaining > 0) {
                out.println("Time " + currentTime + " -> " + (currentTime + 1)
                        + " : [Context Switch]");
                csRemaining--;
                currentTime++;

                if (csRemaining == 0) {
                    justFinishedContextSwitch = true;
                }
                continue;
            }

            Process shortest = null;
            for (Process p : processes) {
                if (p.arrivalTime <= currentTime && p.remainingTime > 0) {
                    if (shortest == null
                            || p.remainingTime < shortest.remainingTime
                            || (p.remainingTime == shortest.remainingTime
                                    && p.arrivalTime < shortest.arrivalTime)) {
                        shortest = p;
                    }
                }
            }

            if (shortest == null) {
                out.println("Time " + currentTime + " -> " + (currentTime + 1) + " : [IDLE]");
                currentTime++;
                lastExecutedProcess = null;
                justFinishedContextSwitch = false;
                continue;
            }

            if (!justFinishedContextSwitch
                    && lastExecutedProcess != null
                    && lastExecutedProcess != shortest
                    && contextSwitch > 0) {
                out.println("Time " + currentTime + " : Starting context switch");
                csRemaining = contextSwitch;
                continue;
            }

            justFinishedContextSwitch = false;
            lastExecutedProcess = shortest;

            if (shortest.startTime == -1) {
                shortest.startTime = currentTime;
                shortest.responseTime = shortest.startTime - shortest.arrivalTime;
            }

            out.println("Time " + currentTime + " -> " + (currentTime + 1)
                    + " : Running " + shortest.name
                    + " (Remaining: " + (shortest.remainingTime - 1) + ")");

            shortest.remainingTime--;
            currentTime++;

            if (shortest.remainingTime == 0) {
                completed++;
                shortest.finishTime = currentTime;
                shortest.turnaroundTime = shortest.finishTime - shortest.arrivalTime;
                shortest.waitingTime = shortest.turnaroundTime - shortest.burstTime;
            }
        }

        printResults(processes, out);
    }

    private static void printResults(List<Process> processes, PrintStream out) {
        double totalWaiting = 0;
        double totalTurnaround = 0;
        double totalResponse = 0;

        out
                .println("\n-----------------------------------------------------------------------------------------");
        out.printf("%-10s | %-12s | %-10s | %-11s | %-15s | %-12s | %-13s\n",
                "Process", "Arrival Time", "Burst Time", "Finish Time", "Turnaround Time",
                "Waiting Time", "Response Time");
        out.println("-----------------------------------------------------------------------------------------");

        for (Process p : processes) {
            totalWaiting += p.waitingTime;
            totalTurnaround += p.turnaroundTime;
            totalResponse += p.responseTime;

            out.printf("%-10s | %-12d | %-10d | %-11d | %-15d | %-12d | %-13d\n",
                    p.name, p.arrivalTime, p.burstTime, p.finishTime,
                    p.turnaroundTime, p.waitingTime, p.responseTime);
        }

        out.println("-----------------------------------------------------------------------------------------");
        if (!processes.isEmpty()) {
            out.printf("Average Waiting Time    : %.2f\n", totalWaiting / processes.size());
            out.printf("Average Turnaround Time : %.2f\n", totalTurnaround / processes.size());
            out.printf("Average Response Time   : %.2f\n", totalResponse / processes.size());
        }
        out
                .println("=========================================================================================\n");
    }
}
