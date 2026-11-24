
import java.util.*;

public class Main {
        public static void main(String[] args) {

                // ==================================================
                // SJF TEST CASES
                // ==================================================

                // ---------------- TEST CASE 1: SJF ----------------

                System.out.println(">>> TEST CASE 1 SJF<<<");

                List<Process> testCase1sjf = Arrays.asList(
                                new Process("P1", 0, 4),
                                new Process("P2", 1, 7),
                                new Process("P3", 3, 2));

                PreemptiveSJF.runSJF(testCase1sjf, 0);

                // ---------------- TEST CASE 2: SJF ----------------

                System.out.println(">>> TEST CASE 2 SJF<<<");

                List<Process> testCase2sjf = Arrays.asList(
                                new Process("P1", 0, 5),
                                new Process("P2", 1, 6),
                                new Process("P3", 4, 4));

                PreemptiveSJF.runSJF(testCase2sjf, 1);

                // ==================================================
                // ACS TEST CASES
                // ==================================================

                // ---------------- TEST CASE 1: TIER 1, 2, 3 + AGING ----------------

                System.out.println(">>> TEST CASE 1 ACS<<<");

                List<Process> testCase1acs = new ArrayList<>();

                testCase1acs.add(new Process("P1", 0, 4, 2));
                testCase1acs.add(new Process("P2", 1, 7, 3));
                testCase1acs.add(new Process("P3", 3, 2, 1));

                System.out.println("Testing Tier 1, Tier 2 and Aging");

                ACS.runACS(testCase1acs, 3, 0);

                // ---------------- TEST CASE 2: CONTEXT SWITCHING + AGING ----------------

                System.out.println(">>> TEST CASE 2 ACS<<<");

                List<Process> testCase2acs = new ArrayList<>();

                testCase2acs.add(new Process("P1", 0, 5, 4));
                testCase2acs.add(new Process("P2", 1, 6, 2));
                testCase2acs.add(new Process("P3", 4, 4, 3));

                System.out.println("Testing Context Switch and Aging");

                ACS.runACS(testCase2acs, 3, 1);

                // ---------------- TEST CASE 3: SINGLE PROCESS ----------------

                System.out.println(">>> TEST CASE 3 ACS<<<");

                List<Process> testCase3acs = new ArrayList<>();

                testCase3acs.add(new Process("P1", 0, 5, 2));

                System.out.println("Testing Single Process");

                ACS.runACS(testCase3acs, 3, 0);

                // ---------------- TEST CASE 4: DIFFERENT ARRIVAL TIMES ----------------

                System.out.println(">>> TEST CASE 4 ACS<<<");

                List<Process> testCase4acs = new ArrayList<>();

                testCase4acs.add(new Process("P1", 0, 8, 3));
                testCase4acs.add(new Process("P2", 5, 3, 1));
                testCase4acs.add(new Process("P3", 7, 5, 2));
                testCase4acs.add(new Process("P4", 10, 4, 4));

                System.out.println("Testing Different Arrival Times");

                ACS.runACS(testCase4acs, 2, 1);

        }
}
