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

/**
 * Holds information about project responsibility for JSON transfer.
 */
public class ProjectResponsibilityModel
{
    private String owner; // foo is responsible for...
    private String comment; // optional
    private boolean canClear;

    public ProjectResponsibilityModel(String owner, String comment)
    {
        this.owner = owner;
        this.comment = comment;
    }

    public String getOwner()
    {
        return owner;
    }

    public String getComment()
    {
        return comment;
    }

    public boolean isCanClear()
    {
        return canClear;
    }

    public void setCanClear(boolean canClear)
    {
        this.canClear = canClear;
    }
}
