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

package com.zutubi.pulse.servercore.dependency.ivy;

import com.zutubi.pulse.core.dependency.ivy.IvyManager;
import com.zutubi.pulse.servercore.hessian.CustomSerialiserFactory;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;

/**
 * This class initialises the ModuleDescriptor instance serialisation/deserialisation
 * support within Hessian.
 */
public class HessianModuleDescriptorSupportInitialiser
{
    private CustomSerialiserFactory customSerialiserFactory = null;

    public void init()
    {
        ModuleDescriptorDeserialiser deserialiser = new ModuleDescriptorDeserialiser();
        ModuleDescriptorSerialiser serialiser = new ModuleDescriptorSerialiser();

        customSerialiserFactory.register(ModuleDescriptor.class, serialiser, deserialiser);
    }

    public void setCustomSerialiserFactory(CustomSerialiserFactory customSerialiserFactory)
    {
        this.customSerialiserFactory = customSerialiserFactory;
    }

    public void setIvyManager(IvyManager ivyManager)
    {
        // for backward patching compatibility.
    }
}
