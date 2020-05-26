package shi.quan.solver.pm2.vo;

import org.optaplanner.core.api.domain.lookup.PlanningId;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class Workplace implements Comparable<Workplace> {
    @PlanningId
    @NotNull
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private int maxPrecision;

    public Workplace() {
    }

    public Workplace(@NotNull Long id, @NotBlank String name, @NotBlank int maxPrecision) {
        this.id = id;
        this.name = name;
        this.maxPrecision = maxPrecision;
    }

    @Override
    public String toString() {
        return "Workplace{" +
                "id=" + id +
//                ", name='" + name + '\'' +
                ", maxPrecision=" + maxPrecision +
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

    public int getMaxPrecision() {
        return maxPrecision;
    }

    public void setMaxPrecision(int maxPrecision) {
        this.maxPrecision = maxPrecision;
    }

    @Override
    public int compareTo(Workplace o) {
        return this.getId().compareTo(o.getId());
    }
}
