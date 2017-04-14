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

package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.master.model.BuildResult;
import flexjson.JSON;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * The viewport is the data representing the build navigation UI.
 */
public class Viewport
{
    private List<Data> builds = new LinkedList<Data>();

    private Data nextSuccessful;
    private Data nextBroken;
    private Data previousSuccessful;
    private Data previousBroken;
    private Data latest;

    public void addAll(Collection<Data> builds)
    {
        this.builds.addAll(builds);
    }

    public void add(Data build)
    {
        this.builds.add(build);
    }

    @JSON
    public List<Data> getBuilds()
    {
        return builds;
    }

    public Data getNextSuccessful()
    {
        return nextSuccessful;
    }

    public Data getNextBroken()
    {
        return nextBroken;
    }

    public Data getPreviousSuccessful()
    {
        return previousSuccessful;
    }

    public Data getPreviousBroken()
    {
        return previousBroken;
    }

    public Data getLatest()
    {
        return latest;
    }

    public void setNextSuccessful(Data nextSuccessful)
    {
        this.nextSuccessful = nextSuccessful;
    }

    public void setNextBroken(Data nextBroken)
    {
        this.nextBroken = nextBroken;
    }

    public void setPreviousSuccessful(Data previousSuccessful)
    {
        this.previousSuccessful = previousSuccessful;
    }

    public void setPreviousBroken(Data previousBroken)
    {
        this.previousBroken = previousBroken;
    }

    public void setLatest(Data latest)
    {
        this.latest = latest;
    }

    public static class Data
    {
        private String status;
        private String id;
        private String number;
        private String name;

        public Data(BuildResult r)
        {
            status = r.getState().getString();
            id = String.valueOf(r.getId());
            number = String.valueOf(r.getNumber());
            name = r.getProject().getName();
        }

        public String getNumber()
        {
            return number;
        }

        public String getStatus()
        {
            return status;
        }

        public String getId()
        {
            return id;
        }

        public String getName()
        {
            return name;
        }
    }
}

