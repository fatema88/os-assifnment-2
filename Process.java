public class Process {
    String name;
    int arrivalTime;
    int burstTime;
    int remainingTime;

    int tier2EntryTime = -1;

    int initialPriority;
    int priority;

    int startTime = -1;
    int finishTime;
    int waitingTime;
    int turnaroundTime;
    int responseTime;

    public Process(String name, int arrivalTime, int burstTime) {
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
    }

    public Process(String name, int arrivalTime, int burstTime, int priority) {
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;

        this.initialPriority = priority;
        this.priority = priority;
    }

    public Process(Process other) {
        this.name = other.name;
        this.arrivalTime = other.arrivalTime;
        this.burstTime = other.burstTime;
        this.remainingTime = other.burstTime;
        this.initialPriority = other.initialPriority;
        this.priority = other.priority;
    }
}
// Creates a fresh process copy for a new scheduling simulation.