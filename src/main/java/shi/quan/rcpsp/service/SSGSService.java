package shi.quan.rcpsp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shi.quan.common.Duo;
import shi.quan.common.exception.BuzzException;
import shi.quan.rcpsp.vo.Task;

import java.util.Set;

public class SSGSService {
    private static final Logger logger = LoggerFactory.getLogger(SSGSService.class);


    public <TimeType extends Comparable<TimeType>, PayloadType> void ssgs() throws BuzzException {
        timeCalculation();
        resourceAdjustment();

        Set<Task<TimeType, PayloadType>> lastAvailableTasks = null;

        for(;;) {
            Set<Task<TimeType, PayloadType>> availableTasks = getCurrentAvailableTasks();

            if((lastAvailableTasks != null && lastAvailableTasks.equals(availableTasks)) || (lastAvailableTasks == null && availableTasks == null)) {
                logger.error("availableTasks : {}", availableTasks);
                throw new BuzzException("Endless loop detected...");
            }

            if(availableTasks == null || availableTasks.isEmpty()) {
                return;
            }

            for (var task : availableTasks) {
                task = resourceConstraintCheck(task);

                if (task != null) {
                    Duo<TimeType, TimeType> timeDuo = startEndTimeCalculate(task);

                    var resourceHandler = resourceOccupationCalculation(task);

                    task = choosingResource(task, resourceHandler);

                    if (task != null) {
                        updateResource(task, resourceHandler);
                        updateTask(task);
                    }
                }
            }

            lastAvailableTasks = availableTasks;
        }
    }

    private void timeCalculation() {
        throw new UnsupportedOperationException();
    }

    private void resourceAdjustment() {
        throw new UnsupportedOperationException();
    }

    private <TimeType, PayloadType> Set<Task<TimeType, PayloadType>> getCurrentAvailableTasks() {
        throw new UnsupportedOperationException();
    }

    private <TimeType, PayloadType> Task<TimeType, PayloadType> resourceConstraintCheck(Task<TimeType, PayloadType> task) {
        throw new UnsupportedOperationException();
    }

    private <TimeType extends Comparable<TimeType>, PayloadType> Duo<TimeType, TimeType> startEndTimeCalculate(Task<TimeType, PayloadType> task) {
        throw new UnsupportedOperationException();
    }

    private <TimeType extends Comparable<TimeType>, PayloadType> Object resourceOccupationCalculation(Task<TimeType, PayloadType> task) {
        throw new UnsupportedOperationException();
    }

    private <TimeType extends Comparable<TimeType>, PayloadType> Task<TimeType, PayloadType> choosingResource(Task<TimeType, PayloadType> task, Object resource) {
        throw new UnsupportedOperationException();
    }

    private <TimeType extends Comparable<TimeType>, PayloadType> void updateResource(Task<TimeType, PayloadType> task, Object resource) {
        throw new UnsupportedOperationException();
    }

    private <PayloadType, TimeType extends Comparable<TimeType>> void updateTask(Task<TimeType,PayloadType> task) {
        throw new UnsupportedOperationException();
    }
}
