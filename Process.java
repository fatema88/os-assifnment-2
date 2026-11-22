public class Process {
    String name;
    int arrivalTime;
    int burstTime;
    int remainingTime;
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

    public Process(Process other) {
        this.name = other.name;
        this.arrivalTime = other.arrivalTime;
        this.burstTime = other.burstTime;
        this.remainingTime = other.burstTime;
    }
}