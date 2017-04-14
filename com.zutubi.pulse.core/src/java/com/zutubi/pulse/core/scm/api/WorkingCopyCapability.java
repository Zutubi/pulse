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

/**
 * Capabilities that are used to indicate which optional methods an
 * implementation of {@link WorkingCopy} supports.  This allows implementations
 * to be staged, and/or functionality that makes no sense for a particular SCM
 * to be disabled.
 */
public enum WorkingCopyCapability
{
    /**
     * The working copy can retrieve the latest revision for a project from the
     * remote repository.
     */
    REMOTE_REVISION,
    /**
     * The working copy can guess the revision of the local working copy (i.e
     * the revision of the last update).
     */
    LOCAL_REVISION,
    /**
     * The working copy supports updating to a revision.
     */
    UPDATE
}
