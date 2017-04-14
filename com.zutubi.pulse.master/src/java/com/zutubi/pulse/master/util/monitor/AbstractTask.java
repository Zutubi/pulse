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

package com.zutubi.pulse.master.util.monitor;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractTask implements Task
{
    private String name;
    private List<String> errors;

    protected AbstractTask(String name)
    {
        this.name = name;
        this.errors = new LinkedList<String>();
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return getName();
    }

    public List<String> getErrors()
    {
        return errors;
    }

    public boolean haltOnFailure()
    {
        return false;
    }

    public boolean hasFailed()
    {
        return errors.size() > 0;
    }

    public void addError(String msg)
    {
        errors.add(msg);
    }

    public void execute() throws TaskException
    {
        
    }
}
