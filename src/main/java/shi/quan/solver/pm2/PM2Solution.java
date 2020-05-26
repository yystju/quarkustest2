package shi.quan.solver.pm2;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import shi.quan.solver.pm2.vo.Task;
import shi.quan.solver.pm2.vo.Timeslot;
import shi.quan.solver.pm2.vo.Workplace;

import java.util.List;

@PlanningSolution
@XStreamAlias("plan2")
public class PM2Solution {
    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "timeslotRange")
    private List<Timeslot> timeslotList;

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "workplaceRange")
    private List<Workplace> workplaceList;

    @PlanningEntityCollectionProperty
    private List<Task> taskList;

    @PlanningScore
    private HardSoftScore score;

    public PM2Solution() {
    }

    public PM2Solution(List<Timeslot> timeslotList, List<Workplace> workplaceList, List<Task> taskList) {
        this.timeslotList = timeslotList;
        this.workplaceList = workplaceList;
        this.taskList = taskList;
    }

    @Override
    public String toString() {
        return "PMSolution{" +
                "timeslotList=" + timeslotList +
                ", workplaceList=" + workplaceList +
                ", taskList=" + taskList +
                ", score=" + score +
                '}';
    }

    public List<Timeslot> getTimeslotList() {
        return timeslotList;
    }

    public void setTimeslotList(List<Timeslot> timeslotList) {
        this.timeslotList = timeslotList;
    }

    public List<Workplace> getWorkplaceList() {
        return workplaceList;
    }

    public void setWorkplaceList(List<Workplace> workplaceList) {
        this.workplaceList = workplaceList;
    }

    public List<Task> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<Task> taskList) {
        this.taskList = taskList;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }
}
