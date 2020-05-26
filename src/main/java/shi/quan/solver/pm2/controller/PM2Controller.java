package shi.quan.solver.pm2.controller;

import org.optaplanner.core.api.score.ScoreManager;
import org.optaplanner.core.api.solver.SolverJob;
import org.optaplanner.core.api.solver.SolverManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shi.quan.solver.pm2.PM2Solution;
import shi.quan.solver.pm2.vo.Task;
import shi.quan.solver.pm2.vo.Timeslot;
import shi.quan.solver.pm2.vo.Workplace;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SessionScoped
@Path("/pm2")
public class PM2Controller {
    private static final Logger logger = LoggerFactory.getLogger(PM2Controller.class);

    @Inject
    SolverManager<PM2Solution, Long> solverManager;

    private SolverJob<PM2Solution, Long> job;

    private PM2Solution currentBest;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/start")
    public String start() {
        Long id = 1L;

        job = solverManager.solveAndListen(id, this::getPMById, this::solved);

        return job.getSolverStatus().toString();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/stop")
    public String stop() {
        if(job != null) {
            job.terminateEarly();
        }

        return "DONE";
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/current")
    public String current() {
        String status = job != null ? job.getSolverStatus().toString() : "";
        String score = currentBest != null ? currentBest.getScore().toString() : "";

        String tasks = currentBest != null ? String.join("\n\t", currentBest.getTaskList().stream().map(t-> String.format("[%s:%d(%d)] @ <%s~%s - %s~%s> on %s(%d)", t.getName(), t.getDuration(), t.getPrecision(), t.getTimeslotStart().getStartTime(), t.getTimeslotStart().getEndTime(), t.getTimeslotEnd().getStartTime(), t.getTimeslotEnd().getEndTime(), t.getWorkplace().getName(), t.getWorkplace().getMaxPrecision())).collect(Collectors.toList())) : "";

        return String.format("STATUS: %s\nSCORE: %s\nTASKS: %s\n", status, score, tasks);
    }

    protected PM2Solution getPMById(Long id) {
        List<Workplace> workplaceList = new ArrayList<>();
        workplaceList.add(new Workplace(1L, "Workplace 1", 5));
        workplaceList.add(new Workplace(2L, "Workplace 2", 7));
        workplaceList.add(new Workplace(3L, "Workplace 3", 7));
        workplaceList.add(new Workplace(4L, "Workplace 4", 9));

        List<Timeslot> timeslotList = new ArrayList<>();

//        timeslotList.add(new Timeslot(1L, "20200525", "08:00", "09:00"));
//        timeslotList.add(new Timeslot(2L, "20200525", "09:00", "10:00"));
//        timeslotList.add(new Timeslot(3L, "20200525", "10:00", "11:00"));
//        timeslotList.add(new Timeslot(4L, "20200525", "11:00", "11:30"));
//        timeslotList.add(new Timeslot(5L, "20200525", "13:00", "14:00"));
//        timeslotList.add(new Timeslot(6L, "20200525", "14:00", "15:00"));
//        timeslotList.add(new Timeslot(7L, "20200525", "15:00", "16:00"));
//        timeslotList.add(new Timeslot(8L, "20200525", "16:00", "17:00"));
//        timeslotList.add(new Timeslot(9L, "20200525", "17:00", "17:30"));

        timeslotList.add(new Timeslot(1L, "20200525", "08:00", "08:15"));
        timeslotList.add(new Timeslot(2L, "20200525", "08:15", "08:30"));
        timeslotList.add(new Timeslot(3L, "20200525", "08:30", "08:45"));
        timeslotList.add(new Timeslot(4L, "20200525", "08:45", "09:00"));
        timeslotList.add(new Timeslot(5L, "20200525", "09:00", "09:15"));
        timeslotList.add(new Timeslot(6L, "20200525", "09:15", "09:30"));
        timeslotList.add(new Timeslot(7L, "20200525", "09:30", "09:45"));
        timeslotList.add(new Timeslot(8L, "20200525", "09:45", "10:00"));
        timeslotList.add(new Timeslot(9L, "20200525", "10:00", "10:15"));
        timeslotList.add(new Timeslot(10L, "20200525", "10:15", "10:30"));
        timeslotList.add(new Timeslot(11L, "20200525", "10:30", "10:45"));
        timeslotList.add(new Timeslot(12L, "20200525", "10:45", "11:00"));
        timeslotList.add(new Timeslot(13L, "20200525", "11:00", "11:15"));
        timeslotList.add(new Timeslot(14L, "20200525", "11:15", "11:30"));
        timeslotList.add(new Timeslot(15L, "20200525", "13:00", "13:15"));
        timeslotList.add(new Timeslot(16L, "20200525", "13:15", "13:30"));
        timeslotList.add(new Timeslot(17L, "20200525", "13:30", "13:45"));
        timeslotList.add(new Timeslot(18L, "20200525", "13:45", "14:00"));
        timeslotList.add(new Timeslot(19L, "20200525", "14:00", "14:15"));
        timeslotList.add(new Timeslot(20L, "20200525", "14:15", "14:30"));
        timeslotList.add(new Timeslot(21L, "20200525", "14:30", "14:45"));
        timeslotList.add(new Timeslot(22L, "20200525", "14:45", "15:00"));
        timeslotList.add(new Timeslot(23L, "20200525", "15:00", "15:15"));
        timeslotList.add(new Timeslot(24L, "20200525", "15:15", "15:30"));
        timeslotList.add(new Timeslot(25L, "20200525", "15:30", "15:45"));
        timeslotList.add(new Timeslot(26L, "20200525", "15:45", "16:00"));
        timeslotList.add(new Timeslot(27L, "20200525", "16:00", "16:15"));
        timeslotList.add(new Timeslot(28L, "20200525", "16:15", "16:30"));
        timeslotList.add(new Timeslot(29L, "20200525", "16:30", "16:45"));
        timeslotList.add(new Timeslot(30L, "20200525", "16:45", "17:00"));

        List<Task> taskList = new ArrayList<>();

        for(int i = 1; i <= 30; ++i) {
            taskList.add(new Task((long)i, String.format("Task %d", i), 30 * 60 * 1000L, (i % 8), null, null, null));
        }

//        taskList.add(new Task(1L, "Task 1", 90 * 60 * 1000L, 4, null, null, null));
//        taskList.add(new Task(2L, "Task 2", 10 * 60 * 1000L, 4, null, null, null));
//        taskList.add(new Task(3L, "Task 3", 10 * 60 * 1000L, 4, null, null, null));
//        taskList.add(new Task(4L, "Task 4", 20 * 60 * 1000L, 6, null, null, null));
//        taskList.add(new Task(5L, "Task 5", 20 * 60 * 1000L, 6, null, null, null));
//        taskList.add(new Task(6L, "Task 6", 30 * 60 * 1000L, 6, null, null, null));
//        taskList.add(new Task(7L, "Task 7", 30 * 60 * 1000L, 6, null, null, null));
//        taskList.add(new Task(8L, "Task 8", 40 * 60 * 1000L, 8, null, null, null));
//        taskList.add(new Task(9L, "Task 9", 40 * 60 * 1000L, 8, null, null, null));
//        taskList.add(new Task(10L, "Task 10", 60 * 60 * 1000L, 6, null, null, null));

        return new PM2Solution(timeslotList, workplaceList, taskList);
    }

    protected void solved(PM2Solution solution) {
        currentBest = solution;

        logger.info("[SOLVED] score : {}", solution.getScore());

        for(Task task : solution.getTaskList()) {
            logger.info("\ttask ({}/{}/{}) @ {} ~ {} on {}", task.getId(), task.getDuration(), task.getPrecision(), task.getTimeslotStart(), task.getTimeslotEnd(), task.getWorkplace());
        }
    }
}
