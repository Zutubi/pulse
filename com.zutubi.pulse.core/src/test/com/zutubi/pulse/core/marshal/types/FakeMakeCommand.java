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

package com.zutubi.pulse.core.marshal.types;

import com.zutubi.tove.annotations.SymbolicName;

import java.util.LinkedList;
import java.util.List;

@SymbolicName("fakeMakeCommand")
public class FakeMakeCommand extends FakeCommand
{
    private String makefile;
    private List<String> targets = new LinkedList<String>();

    public FakeMakeCommand()
    {
    }

    public FakeMakeCommand(String name)
    {
        super(name);
    }

    public String getMakefile()
    {
        return makefile;
    }

    public void setMakefile(String makefile)
    {
        this.makefile = makefile;
    }

    public List<String> getTargets()
    {
        return targets;
    }

    public void setTargets(List<String> targets)
    {
        this.targets = targets;
    }
}