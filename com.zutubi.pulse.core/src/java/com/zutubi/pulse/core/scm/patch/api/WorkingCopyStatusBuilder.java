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

package com.zutubi.pulse.core.scm.patch.api;

import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.api.WorkingCopyContext;

import java.io.OutputStream;

/**
 * Interface for methods used in the creation of standard patch files by the
 * {@link com.zutubi.pulse.core.scm.patch.StandardPatchFormat} implementation.  To leverage this
 * format, your {@link com.zutubi.pulse.core.scm.api.WorkingCopy} class should implement this
 * interface, and you should use patch-format="standard" in your SCM extension declaration.
 *
 * @see com.zutubi.pulse.core.scm.api.WorkingCopy
 */
public interface WorkingCopyStatusBuilder
{
    /**
     * Reports on the status of a working directory.  Used to determine what
     * local changes have been made to the files in the working copy, so that
     * these changes can be replicated in another working copy.
     * <p/>
     * The returned status need only report file statuses that are interesting,
     * as defined by {@link FileStatus#isInteresting()}.  Unchanged files may
     * be recorded if it is simpler but generally should not to reduce the size
     * of the returned data.
     * <p/>
     * As this operation may take some time, implementations should generally
     * use a {@link com.zutubi.pulse.core.ui.api.UserInterface} to report
     * feedback for an operation in progress.  The UI may also be used to query
     * for user input if necessary.  Implementations are encourage to report
     * feedback in the format used by the SCM tools themselves.
     *
     * @param context the context in which the operation is run, in particular
     *                contains the base directory
     * @param spec    implementation-defined specification of the scope of the
     *                operation.  If empty, the operation should apply to the
     *                entire working copy.  A common implementation will use
     *                any provided strings as paths of files/directories to be
     *                included in the operation.  If an SCM provides other ways
     *                of specifying a scope (e.g. pending changelists), the
     *                implementation may define and implement a way to specify
     *                such scopes.  Generally the scope should be designed to
     *                emulate the behaviour of other tools for the SCM.
     * @return status information containing a file status for at least each
     *         interesting file in the working copy
     * @throws com.zutubi.pulse.core.scm.api.ScmException on error
     */
    WorkingCopyStatus getLocalStatus(WorkingCopyContext context, String... spec) throws ScmException;

    /**
     * Indicates if the file at the given path in the working copy can be
     * diffed.  This method is called for files that are indicated as changed
     * by {@link #getLocalStatus(WorkingCopyContext, String...)}.  In most
     * cases, therefore, the file should be diffable if it is a text file, but
     * not if it is a binary file.  Specific implementations may make other
     * exceptions, however.
     * <p/>
     * Files indicated as diffable by this method will have their diffs
     * produced by {@link #diff(WorkingCopyContext, String, java.io.OutputStream)}.
     *
     * @param context the context in which the operation is run, in particular
     *                contains the base directory
     * @param path    path of the file relative to the base of the working copy
     * @return true if a unified diff can be produced for this file (presuming
     *         it has changed)
     * @throws ScmException on any error
     */
    boolean canDiff(WorkingCopyContext context, String path) throws ScmException;

    /**
     * Writes a diff for local changes to the given file to the given output
     * stream.  This method will only be called for paths previously indicated
     * as diffable by {@link #canDiff(WorkingCopyContext, String)}.  The diff
     * should be written in unified format.  It may contain extra content in
     * the header, but must have a file header in the:
     *
     * <pre>
     * --- &lt;old file&gt;
     * +++ &lt;new file&gt;
     * </pre>
     *
     * form, followed by diff hunks for changes in the file.
     *
     * @param context the context in which the operation is run, in particular
     *                contains the base directory
     * @param path    path of the file relative to the base of the working copy
     * @param output  stream to which the diff should be written
     * @throws ScmException on any error
     */
    void diff(WorkingCopyContext context, String path, OutputStream output) throws ScmException;
}
