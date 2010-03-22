package com.zutubi.pulse.servercore.agent;

import com.zutubi.util.EnumUtils;

/**
 * Defines a task run during synchronisation of an agent.  Such tasks must
 * currently:
 *
 * <ul>
 *   <li>execute quickly: any slow execution should be taken out of line</li>
 *   <li>be robust to multiple execution</li>
 * </ul>
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
 *   <li>Include a constructor that takes a single {@link java.util.Properties}
 *       argument and forwards to the corresponding constructor in this class.</li>
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
        CLEANUP_DIRECTORY,
        /**
         * An instruction to rename a directory, e.g. a persistent working
         * directory that includes a name that has changed in the config.
         */
        RENAME_DIRECTORY,
        /**
         * A task used for testing only.
         */
        TEST;

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
