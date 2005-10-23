package com.cinnamonbob.scheduling.persistence;

import com.cinnamonbob.model.persistence.EntityDao;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.scheduling.Schedule;

import java.util.List;

/**
 * <class-comment/>
 */
public interface ScheduleDao extends EntityDao<Schedule>
{
    Schedule findBy(Project project, String name);

    List<Schedule> findByProject(Project project);
}
