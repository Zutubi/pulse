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

package com.zutubi.pulse.core.scm.api;

import java.util.Set;

/**
 * An interface for interaction with a checked-out working copy of a project.
 * This interface must be implemented to support personal builds for an SCM.
 * <p/>
 * Note that not all methods need to be implemented, for early implementations
 * or for those where not all operations make sense.  The supported methods are
 * communicated via the {@link #getCapabilities()} method.  All other methods
 * have documentation indicating which capabilities they are required for.
 * <p/>
 * Implementations of this interface should also consider using the
 * {@link com.zutubi.pulse.core.ui.api.UserInterface} instance provided by
 * the working copy context to enable querying and/or providing feedback to
 * the user during working copy operations.
 */
public interface WorkingCopy
{
    /**
     * A placeholder revision value that indicates to the Pulse server that it
     * should leave the revision floating for a personal build.
     */
    public static final Revision REVISION_FLOATING = new Revision("__floating__");
    /**
     * A placeholder revision value that indicates to the Pulse server that it
     * should use the latest known good revision for a personal build.
     */
    public static final Revision REVISION_LAST_KNOWN_GOOD = new Revision("__last_known_good__");

    Set<WorkingCopyCapability> getCapabilities();

    /**
     * Used to test if the working copy matches the SCM location of a Pulse
     * project.  When the user requests a personal build of a project, some
     * sanity checks should be made to ensure that their working copy is from
     * the same project.  This check is made prior to querying the status of
     * the working copy.
     * <p/>
     * Required for all implementations, however a valid implementation of this
     * method can always return true.  Generally speaking, the more
     * verification that can be done the better, to provide the user with
     * timely and accurate feedback.
     *
     * @param context the context in which the operation is run, in particular
     *                contains the base directory
     * @param location the location string for the SCM configured for the Pulse
     *                 project - this is the value returned from {@link ScmClient#getLocation(ScmContext)}
     *                 using the ScmClient implementation for that project
     * @return true if the working copy appears to match the SCM project
     *         configuration identified by the location
     * @throws ScmException on error
     */
    boolean matchesLocation(WorkingCopyContext context, String location) throws ScmException;

    /**
     * Returns the latest revision committed to the remote repository that
     * affects this working copy (i.e. later revisions affecting only files
     * outside the scope of this working copy are not reported).
     * <p/>
     * Required for {@link WorkingCopyCapability#REMOTE_REVISION}.
     *
     * @param context the context in which the operation is run, in particular
     *                contains the base directory
     * @return the latest revision within to the remote repository
     * @throws ScmException on error
     */
    Revision getLatestRemoteRevision(WorkingCopyContext context) throws ScmException;

    /**
     * Returns a best guess of the local revision for this working copy.  This
     * is the latest revision that the working copy has been updated to.  Note
     * that for some SCMs this can be difficult to determine - and indeed
     * impossible if the workspace has a mixture of revisions.  If no
     * reasonable guess can be made, the implementation may just throw an
     * {@link ScmException}.  The implementation may also issue warnings via
     * the UI in the context if it is uncertain of the returned guess.
     * <p/>
     * Required for {@link WorkingCopyCapability#LOCAL_REVISION}.
     *
     * @param context the context in which the operation is run, in particular
     *                contains the base directory
     * @return a best guess of the last revision this workspace was updated to
     * @throws ScmException on error, including when no reasonable guess is
     *                      possible
     */
    Revision guessLocalRevision(WorkingCopyContext context) throws ScmException;

    /**
     * Updates the working copy to the revision specified by synchronising with
     * changes in the SCM server.
     * <p/>
     * As this operation may take some time, implementations should generally
     * use a {@link com.zutubi.pulse.core.ui.api.UserInterface} to report
     * feedback for an operation in progress.  The UI may also be used to query
     * for user input if necessary.  Implementations are encouraged to report
     * feedback in the format used by the SCM tools themselves.
     * <p/>
     * Required for {@link WorkingCopyCapability#UPDATE}.
     *
     * @param context  the context in which the operation is run, in particular
     *                 contains the base directory
     * @param revision the revision to update to, which may be null to indicate
     *                 that the update should be to the latest revision
     * @return the revision which was actually updated to, which may not be
     *         null - if the latest revision is requested the implementation
     *         must return the actual revision that turned out to be
     * @throws ScmException on error
     */
    Revision update(WorkingCopyContext context, Revision revision) throws ScmException;
}
