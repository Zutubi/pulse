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

import com.zutubi.pulse.core.model.Entity;

/**
 * Abstract parent for classes that can describe why a build occured (i.e.
 * why the request was triggered).
 */
public abstract class AbstractBuildReason extends Entity implements BuildReason, Cloneable
{
    public boolean isUser()
    {
        return false;
    }

    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    public String getTriggerName()
    {
        return null;
    }
}
