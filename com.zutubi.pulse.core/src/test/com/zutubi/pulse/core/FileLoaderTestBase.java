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

package com.zutubi.pulse.core;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.engine.ProjectRecipesConfiguration;
import com.zutubi.pulse.core.engine.marshal.PulseFileLoader;
import com.zutubi.pulse.core.engine.marshal.PulseFileLoaderFactory;
import com.zutubi.pulse.core.marshal.ImportingNotSupportedFileResolver;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.core.tove.config.CoreConfigurationRegistry;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.util.bean.WiringObjectFactory;

/**
 * Helper base class for file loader tests.
 */
public abstract class FileLoaderTestBase extends PulseTestCase
{
    protected TypeRegistry typeRegistry;
    protected PulseFileLoaderFactory fileLoaderFactory;
    protected PulseFileLoader loader;
    protected WiringObjectFactory objectFactory;

    public void setUp() throws Exception
    {
        super.setUp();

        typeRegistry = new TypeRegistry();

        objectFactory = new WiringObjectFactory();
        objectFactory.initProperties(this);

        CoreConfigurationRegistry configurationRegistry = new CoreConfigurationRegistry();
        configurationRegistry.setTypeRegistry(typeRegistry);
        configurationRegistry.init();

        fileLoaderFactory = new PulseFileLoaderFactory();
        fileLoaderFactory.setTypeRegistry(typeRegistry);
        fileLoaderFactory.setObjectFactory(objectFactory);
        fileLoaderFactory.init();
        
        loader = fileLoaderFactory.createLoader();
    }

    //-----------------------------------------------------------------------
    // Generic helpers
    //-----------------------------------------------------------------------

    protected ProjectRecipesConfiguration load(String name) throws PulseException
    {
        ProjectRecipesConfiguration prc = new ProjectRecipesConfiguration();
        loader.load(getInput(name, "xml"), prc, new ImportingNotSupportedFileResolver());
        return prc;
    }

    protected void errorHelper(String testName, String messageContent)
    {
        try
        {
            load(testName);
            fail();
        }
        catch (PulseException e)
        {
            if (!e.getMessage().contains(messageContent))
            {
                fail("Message '" + e.getMessage() + "' does not contain '" + messageContent + "'");
            }
        }
    }
}
