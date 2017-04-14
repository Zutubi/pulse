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

package com.zutubi.pulse.core.engine.marshal;

import com.zutubi.pulse.core.api.PulseRuntimeException;
import com.zutubi.pulse.core.commands.CommandGroupConfiguration;
import com.zutubi.pulse.core.commands.api.DirectoryArtifactConfiguration;
import com.zutubi.pulse.core.commands.api.FileArtifactConfiguration;
import com.zutubi.pulse.core.commands.api.LinkArtifactConfiguration;
import com.zutubi.pulse.core.engine.RecipeConfiguration;
import com.zutubi.pulse.core.engine.api.PropertyConfiguration;
import com.zutubi.pulse.core.marshal.ToveFileStorer;
import com.zutubi.pulse.core.marshal.TypeDefinitions;
import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.util.bean.ObjectFactory;

/**
 * A factory for creating PulseFileLoader objects that are aware of the
 * current plugins.
 */
public class PulseFileLoaderFactory
{
    public static final String ROOT_ELEMENT = "project";

    protected TypeDefinitions typeDefinitions = new TypeDefinitions();
    protected ObjectFactory objectFactory;
    protected TypeRegistry typeRegistry;

    public void init()
    {
        register("property", PropertyConfiguration.class);
        register("recipe", RecipeConfiguration.class);
        register("command", CommandGroupConfiguration.class);
        register("dir-artifact", DirectoryArtifactConfiguration.class);
        register("link-artifact", LinkArtifactConfiguration.class);
        register("artifact", FileArtifactConfiguration.class);
    }

    public PulseFileLoader createLoader()
    {
        PulseFileLoader loader = objectFactory.buildBean(PulseFileLoader.class);
        loader.setTypeDefinitions(typeDefinitions);
        return loader;
    }

    public ToveFileStorer createStorer()
    {
        ToveFileStorer storer = objectFactory.buildBean(ToveFileStorer.class);
        storer.setTypeDefinitions(typeDefinitions);
        return storer;
    }

    public TypeDefinitions getTypeDefinitions()
    {
        return typeDefinitions;
    }

    public CompositeType register(String name, Class clazz)
    {
        CompositeType type = typeRegistry.getType(clazz);
        if (type == null)
        {
            throw new PulseRuntimeException("Attempt to register unknown type with file loader: " + clazz.getName());
        }

        typeDefinitions.register(name, type);
        return type;
    }

    public CompositeType unregister(String name)
    {
        return typeDefinitions.unregister(name);
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry)
    {
        this.typeRegistry = typeRegistry;
    }
}
