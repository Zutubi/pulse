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

package com.zutubi.pulse.master.scm.polling;

import com.google.common.base.Predicate;
import com.zutubi.pulse.master.model.Project;

/**
 * A predicate that is satisfied if and only if the project has been
 * initialised.
 *
 * @see com.zutubi.pulse.master.model.Project#isInitialised() 
 */
public class IsInitialisedPredicate implements Predicate<Project>
{
    public boolean apply(Project project)
    {
        // CIB-2987: there's usually no point polling a project that is due to reinitialise (and
        // indeed it may be counter-productive).  Reinitialisation is commonly due to non-contiguous
        // change history.
        return project.isInitialised() && project.getState() != Project.State.INITIALISE_ON_IDLE;
    }

    @Override
    public String toString()
    {
        return "IsInitialised";
    }
}
