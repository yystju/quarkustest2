package shi.quan.solver.pm2.vo;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import shi.quan.solver.pm2.compare.StartTimeslotStrengthComparator;
import shi.quan.solver.pm2.compare.TaskDifficultyComparator;
import shi.quan.solver.pm2.compare.EndTimeslotStrengthComparator;
import shi.quan.solver.pm2.compare.WorkplaceStrengthComparator;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@PlanningEntity(difficultyComparatorClass = TaskDifficultyComparator.class)
public class Task {
    @PlanningId
    @NotNull
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private long duration;

    @NotBlank
    private int precision;

    @PlanningVariable(valueRangeProviderRefs = "timeslotRange", strengthComparatorClass= StartTimeslotStrengthComparator.class)
    private Timeslot timeslotStart;

    @PlanningVariable(valueRangeProviderRefs = "timeslotRange", strengthComparatorClass= EndTimeslotStrengthComparator.class)
    private Timeslot timeslotEnd;

    @PlanningVariable(valueRangeProviderRefs = "workplaceRange", strengthComparatorClass= WorkplaceStrengthComparator.class)
    private Workplace workplace;

    public Task() {
    }

    public Task(@NotNull Long id, @NotBlank String name, @NotBlank long duration, @NotBlank int precision, Timeslot timeslotStart, Timeslot timeslotEnd, Workplace workplace) {
        this.id = id;
        this.name = name;
        this.duration = duration;
        this.precision = precision;
        this.timeslotStart = timeslotStart;
        this.timeslotEnd = timeslotEnd;
        this.workplace = workplace;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
//                ", name='" + name + '\'' +
//                ", duration=" + duration +
//                ", precision=" + precision +
//                ", timeslotStart=" + timeslotStart +
//                ", timeslotEnd=" + timeslotEnd +
//                ", workplace=" + workplace +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public Timeslot getTimeslotStart() {
        return timeslotStart;
    }

    public void setTimeslotStart(Timeslot timeslotStart) {
        this.timeslotStart = timeslotStart;
    }

    public Timeslot getTimeslotEnd() {
        return timeslotEnd;
    }

    public void setTimeslotEnd(Timeslot timeslotEnd) {
        this.timeslotEnd = timeslotEnd;
    }

    public Workplace getWorkplace() {
        return workplace;
    }

    public void setWorkplace(Workplace workplace) {
        this.workplace = workplace;
    }
}
