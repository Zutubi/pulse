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

package com.zutubi.pulse.master.model;

import com.google.common.base.Supplier;
import com.zutubi.pulse.core.scm.api.Revision;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the revision to be built.  The revision of the build can be initialised
 * at any time before the build starts, but once initialised, it can not be changed.
 */
public class BuildRevision
{
    /**
     * Lazy source of the revision, used when the revision should float until we are ready.
     */
    private Supplier<Revision> revisionSupplier;
    /**
     * The revision to build, which may be null if it has not yet been initialised.
     */
    private Revision revision;
    /**
     * True if this is a revision explicitly specified by the user, when triggering the build.
     */
    private boolean user = false;
    /**
     * Revisions from downstream builds that should be fixed ASAP after this revision is fixed.
     * This is used for "fix with upstream" revision handling (not for revision propagation,
     * where builds share a single BuildRevision instance).
     */
    private List<BuildRevision> dependentRevisions = new ArrayList<BuildRevision>();

    // For Hessian.
    private BuildRevision()
    {

    }

    /**
     * Construct a new revision that will be determined lazily.
     *
     * @param revisionSupplier used to retrieve the revision when we are ready
     */
    public BuildRevision(Supplier<Revision> revisionSupplier)
    {
        if (revisionSupplier == null)
        {
            throw new NullPointerException("Revision supplier may not be null");
        }

        this.revisionSupplier = revisionSupplier;
    }

    /**
     * Create a new revision that will stay fixed at the given revision.
     *
     * @param revision  the revision to build, which will not change.
     * @param user      if true, this is a user-specified revision (as opposed to a fixed revision
     *                  decided on by Pulse itself, as happens e.g. when isolating changes).
     */
    public BuildRevision(Revision revision, boolean user)
    {
        if (revision == null)
        {
            throw new NullPointerException("Revision may not be null");
        }

        this.revision = revision;
        this.user = user;
    }

    /**
     * @return the underlying revision to use for the build, may be null if this revision has not
     * been initialised.
     */
    public synchronized Revision getRevision()
    {
        return revision;
    }

    /**
     * Check if this revision has been initialised.  The revision is initialised at the latest when
     * the build commences.
     *
     * @return true if this revision has been initialised.
     */
    public synchronized boolean isInitialised()
    {
        return getRevision() != null;
    }

    /**
     * @return true if this is a revision explicitly specified by the user
     */
    public synchronized boolean isUser()
    {
        return user;
    }

    /**
     * @return all revisions that are chained downstream from this, and will be initialised when
     *         this revision is initialised
     */
    public List<BuildRevision> getDependentRevisions()
    {
        return Collections.unmodifiableList(dependentRevisions);
    }

    /**
     * Adds a revision for a downstream build that should be initialised when this revision is
     * initialised. This is used for "fix with upstream" revision handling.
     *
     * @param downstream the dependent revision
     */
    public synchronized void addDependentRevision(BuildRevision downstream)
    {
        dependentRevisions.add(downstream);
    }

    /**
     * Initialise the revision.  Obtains the revision from the supplier provided on construction.
     * All dependent revisions will also be initialised.
     */
    public synchronized void initialiseRevision()
    {
        if (isInitialised())
        {
            throw new IllegalStateException("Attempt to update the revision");
        }

        this.revision = revisionSupplier.get();
        for (BuildRevision downstream : dependentRevisions)
        {
            if (!downstream.isInitialised())
            {
                downstream.initialiseRevision();
            }
        }
    }
}
