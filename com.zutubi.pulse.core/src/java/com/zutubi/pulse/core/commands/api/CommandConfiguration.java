package com.zutubi.pulse.core.commands.api;

import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.tove.annotations.Ordered;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Table;
import com.zutubi.tove.annotations.Wizard;
import com.zutubi.tove.config.api.NamedConfiguration;

import java.util.Map;

/**
 * Base interface for all command configuration types.  Defines common
 * properties, and a method returning the type of {@link Command} instances
 * to build from the configuration.
 */
@SymbolicName("zutubi.commandConfig")
@Table(columns = {"name", "force", "enabled"})
public interface CommandConfiguration extends NamedConfiguration
{
    /**
     * Callback to allow for customised configuration of a command that is part
     * of a single-command project.  This is invoked when a user creates such a
     * project (or template project) via the wizard, and allows the command to
     * be configured with extra bits and pieces that could be useful by default.
     * <p/>
     * For example, the command could have default captures added with post-
     * processing if the likely location of such captures is known.  Note that
     * only the passed in processors are available - it is not possible to
     * create a processor and add it to a capture as they must be added by
     * reference (they are defined at the project level).
     *
     * @param availableProcessors mapping from processor name to configuration
     *                            for all post-processors available in the
     *                            created project.  If no suitable processor
     *                            exists then it is best to just omit any
     *                            configuration which may have used it.
     */
    void initialiseSingleCommandProject(Map<String, PostProcessorConfiguration> availableProcessors);

    /**
     * Indicates if the command should be executed even if the recipe has
     * already failed.
     *
     * @return true if the command is forced to execute
     */
    @Wizard.Ignore
    boolean isForce();

    /**
     * Set to true to force the command to execute even though the recipe has
     * already failed, false to skip this command in that case.
     *
     * @param force the new value of the force flag
     */
    void setForce(boolean force);

    /**
     * Indicates if the command is enabled. An enabled command will be executed
     * during recipe execution.
     *
     * @return  true if the command is enabled, false otherwise.
     */
    @Wizard.Ignore
    boolean isEnabled();
    
    /**
     * Set to true to enable this command.  An enabled command will be executed
     * during recipe execution.
     *
     * @param enabled the new value of the enabled flag.
     */
    void setEnabled(boolean enabled);

    /**
     * The artifacts that should be captured when this command completes.  These
     * are typically files or reports created by running the command.
     *
     * @return artifacts to capture when the command completes
     */
    @Ordered
    Map<String, ArtifactConfiguration> getArtifacts();

    /**
     * Sets the artifacts to be captured when this command completes.
     *
     * @param artifacts artifacts to capture when the command completes
     */
    void setArtifacts(Map<String, ArtifactConfiguration> artifacts);

    /**
     * Indicates the type of command to build from this configuration.  The
     * command type must have a single-argument constructor that accepts this
     * configuration as an argument.
     * 
     * @return the type of command to build for this configuration
     */
    Class<? extends Command> commandType();
}
