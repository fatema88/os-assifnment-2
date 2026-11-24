import java.util.*;

public class Tier3 {

    public static final int AGING_INTERVAL = 4;

    public void applyAging(Tier2 tier2, int currentTime) {

        List<Process> processes = tier2.getProcesses();

        if (processes.isEmpty()) {
            return;
        }

        while (!tier2.isEmpty()) {
            tier2.pollNext();
        }

        for (Process p : processes) {

            int waited = currentTime - p.tier2EntryTime;

            int agingBonus = waited / AGING_INTERVAL;

            p.priority = p.initialPriority - agingBonus;

            tier2.addProcess(p, currentTime);
        }
    }

    public void printAgingDetails(Tier2 tier2, int currentTime) {

        List<Process> processes = tier2.getProcesses();

        if (processes.isEmpty()) {
            return;
        }

        System.out.println(
                "\n--- Tier 3 Aging at Time " + currentTime + " ---");

        for (Process p : processes) {

            int waited = currentTime - p.tier2EntryTime;

            int agingBonus = waited / AGING_INTERVAL;

            System.out.println(
                    p.name +
                            ": Waited = " + waited +
                            ", Aging Bonus = " + agingBonus +
                            ", Effective Priority = " + p.priority);
        }
    }
}
