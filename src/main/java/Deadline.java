/**
 * Deadline Class.
 *
 * Represents the deadline-type task.
 *
 * @author Marcus Ong
 */
public class Deadline extends Task {

    protected String by;

    public Deadline(String description, String by) {
        super(description, "D");
        this.by = by;
    }

    public Deadline(String description, boolean isDone, String by) {
        super(description, isDone, "D");
    }

    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + by + ")";
    }
}