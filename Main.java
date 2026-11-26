import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.swing.SwingUtilities;

public class Main {

        private static Scanner scanner;

        public static void main(String[] args) {
                if (args.length == 0 || !"--console".equals(args[0])) {
                        SwingUtilities.invokeLater(() -> new SimulatorGUI().setVisible(true));
                        return;
                }
                scanner = new Scanner(System.in);

                System.out.println("======================================");
                System.out.println("     CLOUD-CLUSTER CPU SCHEDULER");
                System.out.println("======================================");

                while (true) {

                        System.out.println("\n========== MAIN MENU ==========");
                        System.out.println("1. Run Preemptive SJF");
                        System.out.println("2. Run Adaptive Container Scheduler (ACS)");
                        System.out.println("3. Run Both Algorithms");
                        System.out.println("0. Exit");
                        System.out.print("Choose an option: ");

                        int choice = readInt();

                        if (choice == 0) {
                                System.out.println("Program terminated.");
                                break;
                        }

                        if (choice < 1 || choice > 3) {
                                System.out.println("Invalid choice. Try again.");
                                continue;
                        }

                        // Read processes
                        List<Process> processes = readProcesses();

                        // Read Context Switching
                        System.out.print("\nEnter Context Switching Time: ");
                        int contextSwitch = readNonNegativeInt();

                        if (choice == 1) {

                                System.out.println("\nRunning Preemptive SJF...");
                                PreemptiveSJF.runSJF(processes, contextSwitch);

                        } else if (choice == 2) {

                                System.out.print("Enter Time Quantum: ");
                                int quantum = readPositiveInt();

                                System.out.println("\nRunning ACS...");
                                ACS.runACS(processes, quantum, contextSwitch);

                        } else {

                                System.out.print("\nEnter Time Quantum for ACS: ");
                                int quantum = readPositiveInt();

                                System.out.println("\n========== SJF RESULTS ==========");
                                PreemptiveSJF.runSJF(processes, contextSwitch);

                                System.out.println("\n========== ACS RESULTS ==========");
                                ACS.runACS(processes, quantum, contextSwitch);
                        }
                }

                scanner.close();
        }

        // Read process information from the user
        private static List<Process> readProcesses() {

                List<Process> processes = new ArrayList<>();

                System.out.print("\nEnter number of processes: ");
                int numberOfProcesses = readPositiveInt();

                for (int i = 0; i < numberOfProcesses; i++) {

                        System.out.println("\nProcess P" + (i + 1));

                        String name = "P" + (i + 1);

                        System.out.print("Arrival Time: ");
                        int arrivalTime = readNonNegativeInt();

                        System.out.print("Burst Time: ");
                        int burstTime = readPositiveInt();

                        System.out.print("Initial Priority: ");
                        int priority = readPositiveInt();

                        processes.add(
                                        new Process(name, arrivalTime, burstTime, priority));
                }

                return processes;
        }

        // Read any integer
        private static int readInt() {

                while (!scanner.hasNextInt()) {
                        System.out.print("Please enter a valid integer: ");
                        scanner.next();
                }

                return scanner.nextInt();
        }

        // Read a non-negative integer
        private static int readNonNegativeInt() {

                int value;

                while (true) {

                        value = readInt();

                        if (value >= 0) {
                                return value;
                        }

                        System.out.print("Value cannot be negative. Try again: ");
                }
        }

        // Read a positive integer
        private static int readPositiveInt() {

                int value;

                while (true) {

                        value = readInt();

                        if (value > 0) {
                                return value;
                        }

                        System.out.print("Value must be greater than zero. Try again: ");
                }
        }
}
