package com.cinnamonbob.scheduling.persistence;

import com.cinnamonbob.scheduling.Task;
import com.cinnamonbob.model.persistence.EntityDao;

import java.util.List;

/**
 * <class-comment/>
 */
public interface TaskDao extends EntityDao<Task>
{
    List<Task> findByGroup(String taskGroup);
    
    Task findByNameAndGroup(String taskName, String taskGroup);
}
