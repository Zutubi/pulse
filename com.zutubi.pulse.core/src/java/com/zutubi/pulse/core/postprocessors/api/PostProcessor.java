/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
