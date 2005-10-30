package com.cinnamonbob.schedule.persistence;

import com.cinnamonbob.model.persistence.EntityDao;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.schedule.Schedule;
import com.cinnamonbob.schedule.Trigger;

import java.util.List;

/**
 * <class-comment/>
 */
public interface ScheduleDao extends EntityDao<Schedule>
{
    Schedule findBy(Project project, String name);

    List<Schedule> findByProject(Project project);

    List<Trigger> findAllTriggers();

    Schedule findByTrigger(long id);
}
