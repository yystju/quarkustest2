package shi.quan.solver.pm.vo;

import org.optaplanner.core.api.domain.lookup.PlanningId;

import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.LocalTime;

public class Timeslot {
    @PlanningId
    @NotNull
    private Long id;

    @NotNull
    private String date;

    @NotNull
    private String startTime;

    @NotNull
    private String endTime;

    public Timeslot() {
    }

    public Timeslot(@NotNull Long id, @NotNull String date, @NotNull String startTime, @NotNull String endTime) {
        this.id = id;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "Timeslot{" +
                "id=" + id +
                ", date='" + date + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public long getDuration() {
        LocalTime start = LocalTime.parse(this.startTime);
        LocalTime end = LocalTime.parse(this.endTime);
        return Duration.between(start, end).toMillis();
    }
}
