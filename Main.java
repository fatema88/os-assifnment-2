
import java.util.*;

public class Main {
        public static void main(String[] args) {
                // ---------------- TEST CASE 1: SJF ----------------

                System.out.println(">>> TEST CASE 1 SJF<<<");

                List<Process> testCase1 = Arrays.asList(
                                new Process("P1", 0, 4),
                                new Process("P2", 1, 7),
                                new Process("P3", 3, 2));

                PreemptiveSJF.runSJF(testCase1, 0);

                // ---------------- TEST CASE 2: SJF ----------------

                System.out.println(">>> TEST CASE 2 SJF<<<");

                List<Process> testCase2 = Arrays.asList(
                                new Process("P1", 0, 5),
                                new Process("P2", 1, 6),
                                new Process("P3", 4, 4));

                PreemptiveSJF.runSJF(testCase2, 1);
        }
}
