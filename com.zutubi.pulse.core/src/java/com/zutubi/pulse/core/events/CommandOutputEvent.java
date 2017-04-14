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

package com.zutubi.pulse.core.events;

import java.util.Arrays;

/**
 * Carries a chunk of output from the currently-executing command.  These
 * events will only be sent if there is someone listening to output.
 */
public class CommandOutputEvent extends RecipeEvent implements OutputEvent
{
    private byte[] data;

    public CommandOutputEvent(Object source, long buildId, long recipeId, byte[] data)
    {
        super(source, buildId, recipeId);
        this.data = data;
    }

    public byte[] getData()
    {
        return data;
    }

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
        if (!super.equals(o))
        {
            return false;
        }

        CommandOutputEvent event = (CommandOutputEvent) o;
        return Arrays.equals(data, event.data);
    }

    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }

    public String toString()
    {
        return "Command Output Event: " + getRecipeId();
    }
}
