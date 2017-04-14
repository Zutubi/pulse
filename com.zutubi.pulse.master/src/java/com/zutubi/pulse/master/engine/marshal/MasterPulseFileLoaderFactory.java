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

package com.zutubi.pulse.master.engine.marshal;

import com.zutubi.pulse.core.engine.ReferenceCollectingProjectRecipesConfiguration;
import com.zutubi.pulse.core.engine.marshal.PulseFileLoaderFactory;
import com.zutubi.pulse.core.marshal.doc.ToveFileDocManager;
import com.zutubi.tove.type.CompositeType;

/**
 * Master version of {@link com.zutubi.pulse.core.engine.marshal.PulseFileLoaderFactory},
 * extends the core version by adding generation of documentation.
 */
public class MasterPulseFileLoaderFactory extends PulseFileLoaderFactory
{
    private ToveFileDocManager toveFileDocManager;

    @Override
    public void init()
    {
        super.init();
        toveFileDocManager.registerRoot(ROOT_ELEMENT, typeRegistry.getType(ReferenceCollectingProjectRecipesConfiguration.class), typeDefinitions);
    }

    @Override
    public CompositeType register(String name, Class clazz)
    {
        CompositeType type = super.register(name, clazz);
        toveFileDocManager.registerType(name, type, typeDefinitions);
        return type;
    }

    @Override
    public CompositeType unregister(String name)
    {
        CompositeType type = super.unregister(name);
        if (type != null)
        {
            toveFileDocManager.unregisterType(name, type);
        }

        return type;
    }

    public void setToveFileDocManager(ToveFileDocManager toveFileDocManager)
    {
        this.toveFileDocManager = toveFileDocManager;
    }
}
