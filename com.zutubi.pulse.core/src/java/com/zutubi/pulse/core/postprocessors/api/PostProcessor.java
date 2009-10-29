package com.zutubi.pulse.core.postprocessors.api;

import java.io.File;


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
 *
 * @see OutputPostProcessorSupport
 * @see TestReportPostProcessorSupport
 * @see StAXTestReportPostProcessorSupport
 * @see DomTestReportPostProcessorSupport
 * @see TextFilePostProcessorSupport
 * @see LineBasedPostProcessorSupport
 */
public interface PostProcessor
{
    /**
     * Called once for each artifact file to process.  Details discovered by
     * processing should be added via the given context.
     *
     * @param artifactFile the file to post process
     * @param ppContext    context in which the post processing is executing
     */
    void process(File artifactFile, PostProcessorContext ppContext);
}
