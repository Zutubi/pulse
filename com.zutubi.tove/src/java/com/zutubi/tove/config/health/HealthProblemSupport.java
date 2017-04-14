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

package com.zutubi.tove.config.health;

import com.zutubi.util.StringUtils;

/**
 * Abstract base to support implementation of health problems.
 */
public abstract class HealthProblemSupport implements HealthProblem
{
    private String path;
    private String message;

    protected HealthProblemSupport(String path, String message)
    {
        this.path = path;
        this.message = message;
    }

    public String getPath()
    {
        return path;
    }

    public String getMessage()
    {
        return message;
    }

    public boolean isSolvable()
    {
        return true;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        HealthProblemSupport that = (HealthProblemSupport) o;

        if (message != null ? !message.equals(that.message) : that.message != null)
        {
            return false;
        }
        if (path != null ? !path.equals(that.path) : that.path != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return (StringUtils.stringSet(path) ? path : "<root>") + ": " + message;
    }
}
