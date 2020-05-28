package shi.quan.rcpsp.vo;

public class Relationship<TaskType, DurationType> {
    private DurationType lagDuration;

    private PrecedenceDiagrammingConstraint precedenceDiagrammingConstraint = PrecedenceDiagrammingConstraint.FS;

    public DurationType getLagDuration() {
        return lagDuration;
    }

    public Relationship<TaskType, DurationType> setLagDuration(DurationType lagDuration) {
        this.lagDuration = lagDuration;
        return this;
    }

    public PrecedenceDiagrammingConstraint getPrecedenceDiagrammingConstraint() {
        return precedenceDiagrammingConstraint;
    }

    public Relationship<TaskType, DurationType> setPrecedenceDiagrammingConstraint(PrecedenceDiagrammingConstraint precedenceDiagrammingConstraint) {
        this.precedenceDiagrammingConstraint = precedenceDiagrammingConstraint;
        return this;
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", precedenceDiagrammingConstraint.getName(), String.valueOf(lagDuration));
    }
}
