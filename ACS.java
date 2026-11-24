
import java.util.*;

public class ACS {

    private static final int AGING_INTERVAL = 4;

    public static void runACS(
            List<Process> inputProcesses,
            int quantum,
            int contextSwitch) {

        if (quantum <= 0) {
            throw new IllegalArgumentException(
                    "Quantum must be positive.");
        }

        if (contextSwitch < 0) {
            throw new IllegalArgumentException(
                    "Context switch time cannot be negative.");
        }
        // Create fresh copies for this simulation
        List<Process> processes = new ArrayList<>();

        for (Process p : inputProcesses) {
            processes.add(new Process(p));
        }
        // Create scheduling queues
        Tier1 tier1 = new Tier1();
        Tier2 tier2 = new Tier2();
        Tier3 tier3 = new Tier3();

        int completed = 0;
        int currentTime = 0;

        Process activeProcess = null;
        Process lastExecutedProcess = null;

        int activeTier = 0;
        int quantumUsed = 0;

        int csRemaining = 0;

        System.out.println("\n========== ACS SCHEDULING ==========");
        System.out.println("Quantum = " + quantum);
        System.out.println("Context Switch = " + contextSwitch);
        System.out.println("Aging Interval = " + AGING_INTERVAL);

        // Main scheduling loop
        while (completed < processes.size()) {
            for (Process p : processes) {
                if (p.arrivalTime == currentTime) {
                    tier1.addProcess(p);
                    System.out.println(
                            "\nTime " + currentTime +
                                    ": " + p.name +
                                    " arrived -> Tier 1");
                }
            }

            // Apply Aging and print details
            tier3.applyAging(tier2, currentTime);
            tier3.printAgingDetails(tier2, currentTime);

            // Handle an ongoing Context Switch
            if (csRemaining > 0) {
                printQueuesState(currentTime, tier1, tier2);
                System.out.println(
                        "Time " + currentTime +
                                " -> " + (currentTime + 1) +
                                " : [Context Switch]");
                csRemaining--;
                currentTime++;
                if (csRemaining == 0) {
                    lastExecutedProcess = null;
                }
                continue;
            }

            // Select a process if the CPU is free
            if (activeProcess == null) {

                Process nextProcess = null;
                int nextTier = 0;

                if (!tier1.isEmpty()) {

                    nextProcess = tier1.peekProcess();
                    nextTier = 1;

                } else if (!tier2.isEmpty()) {

                    nextProcess = tier2.peekProcess();
                    nextTier = 2;
                }

                // No process is ready
                if (nextProcess == null) {
                    printQueuesState(currentTime, tier1, tier2);
                    System.out.println(
                            "Time " + currentTime +
                                    " -> " + (currentTime + 1) +
                                    " : [IDLE]");
                    lastExecutedProcess = null;
                    currentTime++;
                    continue;
                }

                // Check whether a Context Switch is needed
                if (lastExecutedProcess != null &&
                        lastExecutedProcess != nextProcess &&
                        contextSwitch > 0) {
                    System.out.println(
                            "\nTime " + currentTime +
                                    ": Context switch from " +
                                    lastExecutedProcess.name +
                                    " to " + nextProcess.name);
                    csRemaining = contextSwitch;
                    printQueuesState(currentTime, tier1, tier2);
                    System.out.println(
                            "Time " + currentTime +
                                    " -> " + (currentTime + 1) +
                                    " : [Context Switch]");
                    csRemaining--;
                    currentTime++;
                    if (csRemaining == 0) {
                        lastExecutedProcess = null;
                    }
                    continue;
                }

                // Remove the selected process from its queue
                if (nextTier == 1) {
                    activeProcess = tier1.pollNext();
                } else {
                    activeProcess = tier2.pollNext();
                }
                activeTier = nextTier;
                quantumUsed = 0;
            }

            // Record the first execution time
            if (activeProcess.startTime == -1) {
                activeProcess.startTime = currentTime;
                activeProcess.responseTime = currentTime - activeProcess.arrivalTime;
            }

            // Execute one time unit
            printQueuesState(currentTime, tier1, tier2);
            System.out.println(
                    "Time " + currentTime +
                            " -> " + (currentTime + 1) +
                            " : Running " + activeProcess.name +
                            " [Tier " + activeTier + "]" +
                            " (Remaining: " +
                            (activeProcess.remainingTime - 1) + ")");
            activeProcess.remainingTime--;
            quantumUsed++;
            lastExecutedProcess = activeProcess;
            currentTime++;

            // Check if the process has completed
            if (activeProcess.remainingTime == 0) {
                completed++;
                activeProcess.finishTime = currentTime;
                activeProcess.turnaroundTime = activeProcess.finishTime -
                        activeProcess.arrivalTime;
                activeProcess.waitingTime = activeProcess.turnaroundTime -
                        activeProcess.burstTime;
                System.out.println(
                        "   -> " + activeProcess.name +
                                " completed at Time " + currentTime);
                activeProcess = null;
                activeTier = 0;
                quantumUsed = 0;
            }

            // Demote Tier 1 process when Quantum expires
            else if (activeTier == 1 &&
                    quantumUsed == quantum) {
                System.out.println(
                        "   -> Demoting " +
                                activeProcess.name +
                                " from Tier 1 to Tier 2 at Time " +
                                currentTime);
                tier2.addProcess(
                        activeProcess,
                        currentTime);
                activeProcess = null;
                activeTier = 0;
                quantumUsed = 0;
            }
        }

        System.out.println(
                "\nTime " + currentTime +
                        " : Tier 1: [Empty] | Tier 2: [Empty]");
        printResults(processes);
    }

    // Print the current state of Tier 1 and Tier 2
    private static void printQueuesState(
            int time,
            Tier1 tier1,
            Tier2 tier2) {

        StringBuilder t1 = new StringBuilder("[");
        List<Process> t1List = tier1.getProcesses();

        if (t1List.isEmpty()) {
            t1.append("Empty");
        } else {
            for (int i = 0; i < t1List.size(); i++) {
                Process p = t1List.get(i);
                t1.append(p.name)
                        .append("(Rem:")
                        .append(p.remainingTime)
                        .append(")");
                if (i < t1List.size() - 1) {
                    t1.append(", ");
                }
            }
        }

        t1.append("]");
        StringBuilder t2 = new StringBuilder("[");
        List<Process> t2List = tier2.getProcesses();
        if (t2List.isEmpty()) {
            t2.append("Empty");
        } else {
            for (int i = 0; i < t2List.size(); i++) {
                Process p = t2List.get(i);
                t2.append(p.name)
                        .append("(Rem:")
                        .append(p.remainingTime)
                        .append(", Priority:")
                        .append(p.priority)
                        .append(")");
                if (i < t2List.size() - 1) {
                    t2.append(", ");
                }
            }
        }

        t2.append("]");
        System.out.println(
                "Time " + time +
                        " | Tier 1: " + t1 +
                        " | Tier 2: " + t2);
    }

    // Print final scheduling metrics
    private static void printResults(List<Process> processes) {
        double totalWaiting = 0;
        double totalTurnaround = 0;
        double totalResponse = 0;
        System.out.println(
                "\n==============================================================");
        System.out.printf(
                "%-10s | %-10s | %-10s | %-10s | %-12s | %-10s | %-10s\n",
                "Process",
                "Arrival Time",
                "Burst Time",
                "Finish Time",
                "Turnaround Time",
                "Waiting Time",
                "Response Time");

        System.out.println(
                "--------------------------------------------------------------");

        for (Process p : processes) {

            totalWaiting += p.waitingTime;
            totalTurnaround += p.turnaroundTime;
            totalResponse += p.responseTime;

            System.out.printf(
                    "%-10s | %-10d | %-10d | %-10d | %-12d | %-10d | %-10d\n",
                    p.name,
                    p.arrivalTime,
                    p.burstTime,
                    p.finishTime,
                    p.turnaroundTime,
                    p.waitingTime,
                    p.responseTime);
        }

        System.out.println(
                "--------------------------------------------------------------");

        if (!processes.isEmpty()) {
            System.out.printf(
                    "Average Waiting Time    : %.2f\n",
                    totalWaiting / processes.size());
            System.out.printf(
                    "Average Turnaround Time : %.2f\n",
                    totalTurnaround / processes.size());
            System.out.printf(
                    "Average Response Time   : %.2f\n",
                    totalResponse / processes.size());
        }

        System.out.println(
                "==============================================================");
    }
}
