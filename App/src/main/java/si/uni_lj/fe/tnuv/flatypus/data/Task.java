package si.uni_lj.fe.tnuv.flatypus.data;

public class Task {
    private String taskName;
    private User assigned;
    private int repeatability;
    private boolean done;
    private int timeSinceDone;

    private Task(String taskName, User assigned, int repeatability, boolean done, int timeSinceDone) {
        this.taskName = taskName;
        this.assigned = assigned;
        this.repeatability = repeatability;
        this.done = done;
        this.timeSinceDone = timeSinceDone;
    }
}
