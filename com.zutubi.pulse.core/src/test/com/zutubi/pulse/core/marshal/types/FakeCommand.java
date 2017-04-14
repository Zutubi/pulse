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

import com.zutubi.pulse.core.engine.api.Addable;
import com.zutubi.tove.annotations.Reference;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;

import java.util.LinkedList;
import java.util.List;

@SymbolicName("fakeCommand")
public abstract class FakeCommand extends AbstractNamedConfiguration
{
    @Reference
    @Addable(value = "process", attribute = "processor")
    private List<FakePostProcessor> postProcessors = new LinkedList<FakePostProcessor>();

    protected FakeCommand()
    {
    }

    public FakeCommand(String name)
    {
        super(name);
    }

    public List<FakePostProcessor> getPostProcessors()
    {
        return postProcessors;
    }

    public void setPostProcessors(List<FakePostProcessor> postProcessors)
    {
        this.postProcessors = postProcessors;
    }

    public void addPostProcessor(FakePostProcessor postProcessor)
    {
        postProcessors.add(postProcessor);
    }
}
