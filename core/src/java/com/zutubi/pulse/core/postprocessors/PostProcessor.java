package com.zutubi.pulse.core.postprocessors;

import com.zutubi.pulse.core.ExecutionContext;
import com.zutubi.pulse.core.Reference;
import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredFileArtifact;


/**
 * <p>
 * Post processors are used to extract information from artifacts (including
 * command output) produced by commands.  Typical post processors search for
 * information such as:
 * <ul>
 *   <li>
 *     Features: error, warning or information messages (e.g. compiler
 *     errors)
 *   </li>
 *   <li>
 *     Test results: by parsing test reports (e.g. xUnit XML report files)
 *   </li>
 * </ul>
 * </p>
 * <p>
 * Rather than implementing this interface directly, we recommend extending
 * one of the various support classes where possible.  These classes simplify
 * common cases and reduce coupling to implementation details.
 * </p>
 */
public interface PostProcessor extends Reference
{
    /**
     * Called once for each file artifact that the processor should process.
     * The processor is free to inspect the artifact file and make changes to
     * the artifact and/or command result (e.g. add features or tests).
     *
     * @param artifact the artifact to be processed
     * @param result   the result of the command which produced the artifact
     * @param context  context in which the command was executed
     * @throws com.zutubi.pulse.core.BuildException if some fatal error (one
     *         which should error the build) is encountered that prevents
     *         post processing
     */
    void process(StoredFileArtifact artifact, CommandResult result, ExecutionContext context);
}
