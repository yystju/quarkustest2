package shi.quan.solver.pm2.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shi.quan.solver.pm2.vo.Task;
import shi.quan.solver.pm2.vo.Timeslot;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {
    private static final Logger logger = LoggerFactory.getLogger(DateTimeUtil.class);

    public static long duration(Timeslot timeslotStart, Timeslot timeslotEnd) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        LocalDate startDate = LocalDate.parse(timeslotStart.getDate(), dateFormatter);
        LocalTime startTime = LocalTime.parse(timeslotStart.getStartTime(), timeFormatter);

        LocalDate endDate = LocalDate.parse(timeslotEnd.getDate(), dateFormatter);
        LocalTime endTime = LocalTime.parse(timeslotEnd.getEndTime(), timeFormatter);

        LocalDateTime startDateTime = startDate.atTime(startTime);
        LocalDateTime endDateTime = endDate.atTime(endTime);

        return Duration.between(startDateTime, endDateTime).toMillis();
    }

    public static boolean isAfter(Timeslot timeslotStart, Timeslot timeslotEnd) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        LocalDate startDate = LocalDate.parse(timeslotStart.getDate(), dateFormatter);
        LocalTime startTime = LocalTime.parse(timeslotStart.getStartTime(), timeFormatter);

        LocalDate endDate = LocalDate.parse(timeslotEnd.getDate(), dateFormatter);
        LocalTime endTime = LocalTime.parse(timeslotEnd.getEndTime(), timeFormatter);

        LocalDateTime startDateTime = startDate.atTime(startTime);
        LocalDateTime endDateTime = endDate.atTime(endTime);

        return startDateTime.isEqual(endDateTime) || startDateTime.isAfter(endDateTime);
    }

    public static boolean isBefore(Timeslot timeslotStart, Timeslot timeslotEnd) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        LocalDate startDate = LocalDate.parse(timeslotStart.getDate(), dateFormatter);
        LocalTime startTime = LocalTime.parse(timeslotStart.getStartTime(), timeFormatter);

        LocalDate endDate = LocalDate.parse(timeslotEnd.getDate(), dateFormatter);
        LocalTime endTime = LocalTime.parse(timeslotEnd.getEndTime(), timeFormatter);

        LocalDateTime startDateTime = startDate.atTime(startTime);
        LocalDateTime endDateTime = endDate.atTime(endTime);

        return startDateTime.isEqual(endDateTime) || startDateTime.isBefore(endDateTime);
    }

    public static boolean isTimeOverlap(Timeslot timeslot, Task task) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        LocalDate startDate1 = LocalDate.parse(timeslot.getDate(), dateFormatter);
        LocalTime startTime1 = LocalTime.parse(timeslot.getStartTime(), timeFormatter);

        LocalDate endDate1 = LocalDate.parse(timeslot.getDate(), dateFormatter);
        LocalTime endTime1 = LocalTime.parse(timeslot.getEndTime(), timeFormatter);

        LocalDateTime startDateTime1 = startDate1.atTime(startTime1);
        LocalDateTime endDateTime1 = endDate1.atTime(endTime1);

        LocalDate startDate2 = LocalDate.parse(task.getTimeslotStart().getDate(), dateFormatter);
        LocalTime startTime2 = LocalTime.parse(task.getTimeslotStart().getStartTime(), timeFormatter);

        LocalDate endDate2 = LocalDate.parse(task.getTimeslotEnd().getDate(), dateFormatter);
        LocalTime endTime2 = LocalTime.parse(task.getTimeslotEnd().getEndTime(), timeFormatter);

        LocalDateTime startDateTime2 = startDate2.atTime(startTime2);
        LocalDateTime endDateTime2 = endDate2.atTime(endTime2);

        LocalDateTime startMax = startDateTime1.isAfter(startDateTime2) ? startDateTime1 : startDateTime2;
        LocalDateTime endMin = endDateTime1.isBefore(endDateTime2) ? endDateTime1 : endDateTime2;

        return startMax.isBefore(endMin);
    }

    public static boolean isTimeOverlap(Task task1, Task task2) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        LocalDate startDate1 = LocalDate.parse(task1.getTimeslotStart().getDate(), dateFormatter);
        LocalTime startTime1 = LocalTime.parse(task1.getTimeslotStart().getStartTime(), timeFormatter);

        LocalDate endDate1 = LocalDate.parse(task1.getTimeslotEnd().getDate(), dateFormatter);
        LocalTime endTime1 = LocalTime.parse(task1.getTimeslotEnd().getEndTime(), timeFormatter);

        LocalDateTime startDateTime1 = startDate1.atTime(startTime1);
        LocalDateTime endDateTime1 = endDate1.atTime(endTime1);

        LocalDate startDate2 = LocalDate.parse(task2.getTimeslotStart().getDate(), dateFormatter);
        LocalTime startTime2 = LocalTime.parse(task2.getTimeslotStart().getStartTime(), timeFormatter);

        LocalDate endDate2 = LocalDate.parse(task2.getTimeslotEnd().getDate(), dateFormatter);
        LocalTime endTime2 = LocalTime.parse(task2.getTimeslotEnd().getEndTime(), timeFormatter);

        LocalDateTime startDateTime2 = startDate2.atTime(startTime2);
        LocalDateTime endDateTime2 = endDate2.atTime(endTime2);

        LocalDateTime startMax = startDateTime1.isAfter(startDateTime2) ? startDateTime1 : startDateTime2;
        LocalDateTime endMin = endDateTime1.isBefore(endDateTime2) ? endDateTime1 : endDateTime2;

        return startMax.isBefore(endMin);
    }

    public static long intersectionMillis(Timeslot timeslot, Task task, boolean verbose) {
        if(verbose) {
            logger.info("[intersectionMillis] timeslot : {}, task : ({} ~ {})", timeslot, task.getTimeslotStart(), task.getTimeslotEnd());
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        LocalDate startDate1 = LocalDate.parse(timeslot.getDate(), dateFormatter);
        LocalTime startTime1 = LocalTime.parse(timeslot.getStartTime(), timeFormatter);

        LocalDate endDate1 = LocalDate.parse(timeslot.getDate(), dateFormatter);
        LocalTime endTime1 = LocalTime.parse(timeslot.getEndTime(), timeFormatter);

        LocalDateTime startDateTime1 = startDate1.atTime(startTime1);
        LocalDateTime endDateTime1 = endDate1.atTime(endTime1);

        LocalDate startDate2 = LocalDate.parse(task.getTimeslotStart().getDate(), dateFormatter);
        LocalTime startTime2 = LocalTime.parse(task.getTimeslotStart().getStartTime(), timeFormatter);

        LocalDate endDate2 = LocalDate.parse(task.getTimeslotEnd().getDate(), dateFormatter);
        LocalTime endTime2 = LocalTime.parse(task.getTimeslotEnd().getEndTime(), timeFormatter);

        LocalDateTime startDateTime2 = startDate2.atTime(startTime2);
        LocalDateTime endDateTime2 = endDate2.atTime(endTime2);

        LocalDateTime startMax = startDateTime1.isAfter(startDateTime2) ? startDateTime1 : startDateTime2;
        LocalDateTime endMin = endDateTime1.isBefore(endDateTime2) ? endDateTime1 : endDateTime2;

        if(verbose) {
            logger.info("[intersectionMillis] startMax : {}, endMin : {}", startMax, endMin);
        }

        long intersectionDuration = Duration.between(startMax, endMin).toMillis();
        long taskDuration = task.getDuration();

        if(verbose) {
            logger.info("[intersectionMillis] intersectionDuration : {}, taskDuration : {}", intersectionDuration, taskDuration);
        }

        return startMax.isBefore(endMin) ? Math.min(intersectionDuration, taskDuration) : 0;
    }

    public static long millis(Timeslot timeslot) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        LocalDate date = LocalDate.parse(timeslot.getDate(), dateFormatter);
        LocalTime time = LocalTime.parse(timeslot.getStartTime(), timeFormatter);

        LocalDateTime dateTime = date.atTime(time);

        return Timestamp.valueOf(dateTime).getTime();
    }
}
