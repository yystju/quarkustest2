package shi.quan.solver.pm2.compare;

import shi.quan.solver.pm2.vo.Task;

import java.io.Serializable;
import java.util.Comparator;

public class TaskDifficultyComparator implements Comparator<Task>, Serializable {
    @Override
    public int compare(Task task1, Task task2) {
        return Comparator.comparing(Task::getDuration).thenComparing(Task::getPrecision).thenComparing(Task::getId).compare(task1, task2);
    }
}
