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

import com.zutubi.pulse.core.engine.ResourcesConfiguration;
import com.zutubi.pulse.core.engine.marshal.ResourceFileLoader;
import com.zutubi.pulse.core.marshal.doc.ToveFileDocManager;

/**
 * Master version of {@link com.zutubi.pulse.core.engine.marshal.ResourceFileLoader},
 * extends the core version by adding generation of documentation.
 */
public class MasterResourceFileLoader extends ResourceFileLoader
{
    private ToveFileDocManager toveFileDocManager;

    @Override
    public void init()
    {
        super.init();
        toveFileDocManager.registerRoot(ROOT_ELEMENT, typeRegistry.getType(ResourcesConfiguration.class), typeDefinitions);
    }

    public void setToveFileDocManager(ToveFileDocManager toveFileDocManager)
    {
        this.toveFileDocManager = toveFileDocManager;
    }
}
