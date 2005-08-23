package com.cinnamonbob.model.persistence;

import java.util.List;

import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.Schedule;

/**
 * 
 *
 */
public interface ScheduleDao extends EntityDao<Schedule>
{
    List<Schedule> findByProject(Project project);
}
