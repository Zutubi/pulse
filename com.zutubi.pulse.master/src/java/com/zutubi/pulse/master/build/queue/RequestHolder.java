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

package com.zutubi.pulse.master.build.queue;

import com.zutubi.pulse.master.events.build.BuildRequestEvent;

/**
 * The base class for queued and activated requests that are
 * tracked within the scheduling system.
 */
public abstract class RequestHolder
{
    private BuildRequestEvent request;

    protected RequestHolder(BuildRequestEvent request)
    {
        this.request = request;
    }

    /**
     * The id that defines the meta build this request is
     * associated with.
     *
     * @return the request meta build id.
     */
    public long getMetaBuildId()
    {
        return request.getMetaBuildId();
    }

    /**
     * Get the request instance itself.
     *
     * @return the build request event.
     */
    public BuildRequestEvent getRequest()
    {
        return request;
    }

    /**
     * Convenience method that provides access to the held requests owner.
     *
     * @return the owner of the request
     *
     * @see com.zutubi.pulse.master.events.build.BuildRequestEvent#getOwner()
     */
    public Object getOwner()
    {
        return getRequest().getOwner();
    }

    /**
     * Convenience method that provides access to the held requests is personal flag.
     *
     * @return the requests isPersonal flag
     *
     * @see com.zutubi.pulse.master.events.build.BuildRequestEvent#isPersonal()
     */
    public boolean isPersonal()
    {
        return getRequest().isPersonal();
    }

    /**
     * Convenience method that provides access to held requests project id.
     *
     * @return the requests project id.
     *
     * @see com.zutubi.pulse.master.events.build.BuildRequestEvent#getProjectId() 
     */
    public long getProjectId()
    {
        return getRequest().getProjectId();
    }
}
