package com.zutubi.tove.config.cleanup;

import com.zutubi.tove.type.record.RecordManager;

import java.util.List;
import java.util.Set;

/**
 * An action that needs to be undertaken to cleanup when a record is deleted.
 * As the action itself may lead to further records being deleted, further
 * actions may cascade.  The root action itself is always the deletion of the
 * original record.
 */
public interface RecordCleanupTask
{
    /**
     * Indicates an action that may be taken by a task on the record store.
     * The actions are used to determine which events need to be raised if a
     * task is executed.
     */
    enum CleanupAction
    {
        /**
         * The affected path will be deleted.
         */
        DELETE,
        /**
         * No externally-visible action will be taken (e.g. no change at all,
         * only changes to meta values).
         */
        NONE,
        /**
         * The <b>parent</b> of the affected path will be updated.
         */
        PARENT_UPDATE
    }
    
    /**
     * Executes the cleanup task.  Note that tasks <b>must</b> be robust to
     * paths having been deleted by previous tasks.  That is, no task can
     * assume that any record it may intend to change still exists.
     * 
     * @param recordManager manager for record access.updates
     * @return true if this task took action, false if it did not (e.g. because
     *         the affected path no longer exists)
     */
    boolean run(RecordManager recordManager);

    /**
     * @return true if this is an internal task that the user need not be
     * notified of.
     */
    boolean isInternal();

    /**
     * Indicates what type of action this task will take on the affected path
     * (or its parent) if it takes effect.
     * 
     * @return the type of action this task will take
     */
    CleanupAction getCleanupAction();
    
    /**
     * @return the path of the record or record property affected by this
     * task.
     */
    String getAffectedPath();

    /**
     * @return cleanup tasks cascaded from this one: i.e. tasks that must be
     *         carried out to cleanup after records removed by this task
     */
    List<RecordCleanupTask> getCascaded();

    /**
     * Populates the given set with the paths invalidated by this task, and
     * all cascaded tasks.  Used to identify redundant tasks from the tree
     * (e.g. no need to fix path foo/bar/baz if another task is removing
     * foo/bar).
     *
     * @param paths set to which invalidated paths are added
     */
     void getInvalidatedPaths(Set<String> paths);
}
