package shi.quan.solver.pm.vo;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@PlanningEntity
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

    @PlanningVariable(valueRangeProviderRefs = "timeslotRange")
    private Timeslot timeslot;

    @PlanningVariable(valueRangeProviderRefs = "workplaceRange")
    private Workplace workplace;

    public Task() {
    }

    public Task(@NotNull Long id, @NotBlank String name, @NotBlank long duration, @NotBlank int precision, Timeslot timeslot, Workplace workplace) {
        this.id = id;
        this.name = name;
        this.duration = duration;
        this.precision = precision;
        this.timeslot = timeslot;
        this.workplace = workplace;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", duration=" + duration +
                ", precision=" + precision +
                ", timeslot=" + timeslot +
                ", workplace=" + workplace +
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

    public Timeslot getTimeslot() {
        return timeslot;
    }

    public void setTimeslot(Timeslot timeslot) {
        this.timeslot = timeslot;
    }

    public Workplace getWorkplace() {
        return workplace;
    }

    public void setWorkplace(Workplace workplace) {
        this.workplace = workplace;
    }
}
