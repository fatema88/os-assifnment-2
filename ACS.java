import java.util.*;
import java.io.PrintStream;

public class ACS {

    public static void runACS(
            List<Process> inputProcesses,
            int quantum,
            int contextSwitch) {
        runACS(inputProcesses, quantum, contextSwitch, System.out);
    }

    public static void runACS(
            List<Process> inputProcesses,
            int quantum,
            int contextSwitch,
            PrintStream out) {

        if (quantum <= 0) {
            throw new IllegalArgumentException("Quantum must be positive.");
        }

        if (contextSwitch < 0) {
            throw new IllegalArgumentException("Context switch time cannot be negative.");
        }

        for (Process p : inputProcesses) {
            if (p.burstTime <= 0) {
                throw new IllegalArgumentException(
                        "Burst time must be positive for " + p.name);
            }
        }

        List<Process> processes = new ArrayList<>();
        for (Process p : inputProcesses) {
            processes.add(new Process(p));
        }

        Tier1 tier1 = new Tier1();
        Tier2 tier2 = new Tier2();
        Tier3 tier3 = new Tier3();

        int completed = 0;
        int currentTime = 0;

        Process activeProcess = null;
        Process lastExecutedProcess = null;

        int activeTier = 0;
        int sliceRemaining = 0;
        boolean isFinished = false;
        boolean isDemoted = false;

        int csRemaining = 0;

        out.println("\n========== ACS SCHEDULING ==========");
        out.println("Quantum = " + quantum);
        out.println("Context Switch = " + contextSwitch);
        out.println("Aging Interval = " + Tier3.AGING_INTERVAL);

        while (completed < processes.size()) {
            for (Process p : processes) {
                if (p.arrivalTime == currentTime) {
                    tier1.addProcess(p);
                    out.println(
                            "\nTime " + currentTime +
                                    ": " + p.name +
                                    " arrived -> Tier 1");
                }
            }

            tier3.applyAging(tier2, currentTime);
            tier3.printAgingDetails(tier2, currentTime);

            if (activeProcess == null && csRemaining == 0) {
                Process nextProcess = null;
                int nextTier = 0;

                if (!tier1.isEmpty()) {
                    nextProcess = tier1.peekProcess();
                    nextTier = 1;
                } else if (!tier2.isEmpty()) {
                    nextProcess = tier2.peekProcess();
                    nextTier = 2;
                }

                if (nextProcess == null) {
                    printHeuristicDemotionMatrix(
                            currentTime, processes, tier1, tier2,
                            null, 0, 0, false, out);
                    printQueuesState(currentTime, tier1, tier2, out);
                    out.println(
                            "Time " + currentTime +
                                    " -> " + (currentTime + 1) +
                                    " : [IDLE]");
                    lastExecutedProcess = null;
                    currentTime++;
                    continue;
                }

                if (lastExecutedProcess != null &&
                        lastExecutedProcess != nextProcess &&
                        contextSwitch > 0) {
                    out.println(
                            "\nTime " + currentTime +
                                    ": Context switch from " +
                                    lastExecutedProcess.name +
                                    " to " + nextProcess.name);
                    csRemaining = contextSwitch;
                } else {
                    if (nextTier == 1) {
                        Tier1.SliceResult result = tier1.executeNext(quantum, currentTime);
                        activeProcess = result.process;
                        sliceRemaining = result.executionTime;
                        isFinished = result.finished;
                        isDemoted = result.demoted;
                    } else {
                        activeProcess = tier2.executeNext(currentTime);
                        sliceRemaining = activeProcess.finishTime - currentTime;
                        isFinished = true;
                        isDemoted = false;
                    }
                    activeTier = nextTier;
                }
            }

            if (csRemaining > 0) {
                printHeuristicDemotionMatrix(
                        currentTime, processes, tier1, tier2,
                        null, 0, 0, true, out);
                printQueuesState(currentTime, tier1, tier2, out);
                out.println(
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

            printHeuristicDemotionMatrix(
                    currentTime, processes, tier1, tier2,
                    activeProcess, activeTier, sliceRemaining, false, out);
            printQueuesState(currentTime, tier1, tier2, out);
            int displayRemaining = activeProcess.remainingTime + sliceRemaining - 1;
            out.println(
                    "Time " + currentTime +
                            " -> " + (currentTime + 1) +
                            " : Running " + activeProcess.name +
                            " [Tier " + activeTier + "]" +
                            " (Remaining: " + displayRemaining + ")");

            sliceRemaining--;
            lastExecutedProcess = activeProcess;
            currentTime++;

            if (sliceRemaining == 0) {
                if (isFinished) {
                    completed++;
                    out.println(
                            "   -> " + activeProcess.name +
                                    " completed at Time " + currentTime);
                } else if (isDemoted) {
                    out.println(
                            "   -> Demoting " +
                                    activeProcess.name +
                                    " from Tier 1 to Tier 2 at Time " +
                                    currentTime);
                    tier2.addProcess(activeProcess, currentTime);
                }
                activeProcess = null;
                activeTier = 0;
            }
        }

        printHeuristicDemotionMatrix(
                currentTime, processes, tier1, tier2,
                null, 0, 0, false, out);
        out.println(
                "\nTime " + currentTime +
                        " : Tier 1: [Empty] | Tier 2: [Empty]");
        printResults(processes, out);
    }

    private static void printHeuristicDemotionMatrix(
            int time,
            List<Process> processes,
            Tier1 tier1,
            Tier2 tier2,
            Process activeProcess,
            int activeTier,
            int sliceRemaining,
            boolean isContextSwitch,
            PrintStream out) {

        out.println("\n=== Heuristic Demotion Matrix (Time Step = " + time + ") ===");
        out.printf(
                "%-10s | %-15s | %-12s | %-18s | %-12s | %-55s\n",
                "Process",
                "Remaining Burst",
                "Current Tier",
                "Effective Priority",
                "Status",
                "Demotion & Aging Info");
        out.println(
                "---------------------------------------------------------------------------------------------------------------------------------------");

        for (Process p : processes) {
            int remBurst;
            String currentTierStr;
            String priorityStr;
            String statusStr;
            List<String> notes = new ArrayList<>();

            boolean isCompleted = (p.finishTime > 0 && p.finishTime <= time);
            boolean isNotArrived = (p.arrivalTime > time);

            if (isNotArrived) {
                remBurst = p.burstTime;
                currentTierStr = "-";
                priorityStr = "-";
                statusStr = "Not Arrived";
            } else if (isCompleted) {
                remBurst = 0;
                currentTierStr = "-";
                priorityStr = "-";
                statusStr = "Completed";
            } else if (p == activeProcess) {
                remBurst = p.remainingTime + sliceRemaining;
                currentTierStr = "Tier " + activeTier;
                priorityStr = String.valueOf(p.priority);
                statusStr = "Running";

                if (activeTier == 2) {
                    notes.add("Demoted");
                    int appliedBonus = p.initialPriority - p.priority;
                    if (appliedBonus > 0) {
                        notes.add("Aged (+" + appliedBonus + ", Base: " + p.initialPriority + ")");
                    }
                }
            } else if (tier1.containsProcess(p)) {
                remBurst = p.remainingTime;
                currentTierStr = "Tier 1";
                priorityStr = String.valueOf(p.priority);
                statusStr = "Ready";
                if (isContextSwitch) {
                    notes.add("Context Switch");
                }
            } else if (tier2.containsProcess(p)) {
                remBurst = p.remainingTime;
                currentTierStr = "Tier 2";
                priorityStr = String.valueOf(p.priority);
                statusStr = "Waiting";

                if (p.tier2EntryTime == time) {
                    notes.add("Demoted (T1 -> T2)");
                } else {
                    notes.add("Demoted");
                }

                int appliedBonus = p.initialPriority - p.priority;
                if (appliedBonus > 0) {
                    notes.add("Aged (+" + appliedBonus + ", Base: " + p.initialPriority + ")");
                } else {
                    notes.add("Tier 3 Aging (+0)");
                }

                if (isContextSwitch) {
                    notes.add("Context Switch");
                }
            } else {
                remBurst = p.remainingTime;
                currentTierStr = "-";
                priorityStr = String.valueOf(p.priority);
                statusStr = "Waiting";
            }

            String notesStr = notes.isEmpty() ? "-" : String.join(" | ", notes);

            out.printf(
                    "%-10s | %-15d | %-12s | %-18s | %-12s | %-55s\n",
                    p.name,
                    remBurst,
                    currentTierStr,
                    priorityStr,
                    statusStr,
                    notesStr);
        }

        out.println(
                "---------------------------------------------------------------------------------------------------------------------------------------");
    }

    private static void printQueuesState(
            int time,
            Tier1 tier1,
            Tier2 tier2,
            PrintStream out) {

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
        out.println(
                "Time " + time +
                        " | Tier 1: " + t1 +
                        " | Tier 2: " + t2);
    }

    private static void printResults(List<Process> processes, PrintStream out) {
        double totalWaiting = 0;
        double totalTurnaround = 0;
        double totalResponse = 0;
        out.println(
                "\n==============================================================");
        out.printf(
                "%-10s | %-10s | %-10s | %-10s | %-12s | %-10s | %-10s\n",
                "Process",
                "Arrival Time",
                "Burst Time",
                "Finish Time",
                "Turnaround Time",
                "Waiting Time",
                "Response Time");

        out.println(
                "--------------------------------------------------------------");

        for (Process p : processes) {
            totalWaiting += p.waitingTime;
            totalTurnaround += p.turnaroundTime;
            totalResponse += p.responseTime;

            out.printf(
                    "%-10s | %-10d | %-10d | %-10d | %-12d | %-10d | %-10d\n",
                    p.name,
                    p.arrivalTime,
                    p.burstTime,
                    p.finishTime,
                    p.turnaroundTime,
                    p.waitingTime,
                    p.responseTime);
        }

        out.println(
                "--------------------------------------------------------------");

        if (!processes.isEmpty()) {
            out.printf(
                    "Average Waiting Time    : %.2f\n",
                    totalWaiting / processes.size());
            out.printf(
                    "Average Turnaround Time : %.2f\n",
                    totalTurnaround / processes.size());
            out.printf(
                    "Average Response Time   : %.2f\n",
                    totalResponse / processes.size());
        }

        out.println(
                "==============================================================");
    }
}