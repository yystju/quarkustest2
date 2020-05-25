package shi.quan.solver.pm.controller;

import org.optaplanner.core.api.score.ScoreManager;
import org.optaplanner.core.api.solver.SolverJob;
import org.optaplanner.core.api.solver.SolverManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shi.quan.solver.pm.PMSolution;
import shi.quan.solver.pm.vo.Task;
import shi.quan.solver.pm.vo.Timeslot;
import shi.quan.solver.pm.vo.Workplace;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.stream.Collectors;

@SessionScoped
@Path("/pm")
public class PMController {
    private static final Logger logger = LoggerFactory.getLogger(PMController.class);

    private
    @Inject
    SolverManager<PMSolution, Long> solverManager;

    @Inject
    ScoreManager<PMSolution> scoreManager;

    private SolverJob<PMSolution, Long> job;

    private PMSolution currentBest;

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

        String tasks = currentBest != null ? String.join("\n\t", currentBest.getTaskList().stream().map(t->{
            return String.format("[%s:%d(%d)] @ %s~%s on %s(%d)", t.getName(), t.getDuration(), t.getPrecision(), t.getTimeslot().getStartTime(), t.getTimeslot().getEndTime(), t.getWorkplace().getName(), t.getWorkplace().getMaxPrecision());
        }).collect(Collectors.toList())) : "";

        return String.format("STATUS: %s\nSCORE: %s\nTASKS: %s\n", status, score, tasks);
    }

    protected PMSolution getPMById(Long id) {
        Workplace workplace1 = new Workplace(1L, "Workplace 1", 5);
        Workplace workplace2 = new Workplace(2L, "Workplace 2", 7);

        Timeslot timeslot1 = new Timeslot(1L, "20200525", "08:00", "09:00");
        Timeslot timeslot2 = new Timeslot(2L, "20200525", "09:00", "10:00");
        Timeslot timeslot3 = new Timeslot(3L, "20200525", "10:00", "11:00");

        Task task1 = new Task(1L, "Task 1", 30 * 60 * 1000L, 4, null, null);
        Task task2 = new Task(2L, "Task 2", 30 * 60 * 1000L, 6, null, null);
        Task task3 = new Task(3L, "Task 3", 30 * 60 * 1000L, 4, null, null);
        Task task4 = new Task(4L, "Task 4", 30 * 60 * 1000L, 6, null, null);
        Task task5 = new Task(5L, "Task 5", 30 * 60 * 1000L, 6, null, null);

        return new PMSolution(Arrays.asList(timeslot1, timeslot2, timeslot3), Arrays.asList(workplace1, workplace2), Arrays.asList(task1, task2, task3, task4, task5));
    }

    protected void solved(PMSolution solution) {
        currentBest = solution;
        logger.info("[SOLVED] tasks : {}", solution.getTaskList());
    }
}
