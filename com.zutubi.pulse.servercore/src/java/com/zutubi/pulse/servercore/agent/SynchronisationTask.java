package com.zutubi.pulse.servercore.agent;

import com.zutubi.util.EnumUtils;

/**
 * Defines a task run during synchronisation of an agent.  Such tasks must
 * currently be robust to multiple execution, as they may be retried despite
 * getting through the first time.
 *
 * To implement a task, add a new type in the enum here, add a corresponding
 * entry to register the type in the static block of the {@link com.zutubi.pulse.servercore.agent.SynchronisationTaskFactory}
 * and in the implementing class:
 *
 * <ul>
 *   <li>Declare all arguments required for the task as fields.  These will be
 *       handled by the {@link SynchronisationTaskFactory}.</li>
 *   <li>Use only field types that have corresponding {@link com.zutubi.tove.squeezer.Squeezers}.</li>
 *   <li>Mark any fields that should not be sent in messages as transient.
 *       (Note that static and final fields are also ignored during binding.)</li>
 *   <li>Avoid initialising fields, as this will overwrite changes made by the
 *       binding implementation.</li>
 * </ul>
 */
public interface SynchronisationTask
{
    /**
     * Types of tasks, linked to the classes that implement them.  These types
     * are used to allow simple messages sent to the agent to be turned into
     * executable tasks.
     */
    enum Type
    {
        /**
         * An instruction to remove a specific directory, e.g. a directory that
         * corresponds to a project that has been removed.
         */
        CLEANUP_DIRECTORY(true),
        /**
         * An instruction to rename a directory, e.g. a persistent working
         * directory that includes a name that has changed in the config.
         */
        RENAME_DIRECTORY(true),
        /**
         * A task used for testing only.
         */
        TEST(true),
        /**
         * A task used for testing an asynchronous message only.
         */
        TEST_ASYNC(false);
        
        private boolean synchronous;

        Type(boolean synchronous)
        {
            this.synchronous = synchronous;
        }

        /**
         * Indicates if the task should have its execute method called
         * synchronously.  This is preferrable for tasks where execute is
         * guaranteed to be fast.  Note that the task need not necessarily
         * perform all of its work in execute, that could be farmed off to
         * another asynchronous process, provided it is OK for the task to
         * say it succeeded without all this work being complete.
         * <p/>
         * When a task needs to do a non-trivial amount of work before its
         * success can be decided, it should be asynchronous, and should
         * raise a SynchronisationTaskCompleteEvent when done.
         * 
         * @return true if tasks of this type should be executed synchronously
         */
        public boolean isSynchronous()
        {
            return synchronous;
        }

        public String getPrettyString()
        {
            return EnumUtils.toPrettyString(this);
        }

        @Override
        public String toString()
        {
            return EnumUtils.toString(this);
        }
    }

    /**
     * Executes this task.  Implementations of this method:
     *
     * <ul>
     *   <li>Must be fast: these tasks are synchronously executed during agent
     *       synchronisation.  Anything lengthy should be done asynchronously
     *       where possible.</li>
     *   <li>Must allow multiple calls: as messages may be retried, even though
     *       they got through the first time (because the master may not
     *       realise this).</li>
     *   <li>Should throw a {@link RuntimeException} on any error, with a
     *       detail message that is human-readable.</li>
     * </ul>
     * 
     * @throws RuntimeException on any error
     */
    void execute();
}
