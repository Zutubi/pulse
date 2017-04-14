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

import com.zutubi.pulse.core.InMemoryResourceRepository;
import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.engine.ResourcesConfiguration;
import com.zutubi.pulse.core.resources.ResourceRequirement;
import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.pulse.core.resources.api.ResourcePropertyConfiguration;
import com.zutubi.pulse.core.resources.api.ResourceVersionConfiguration;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.util.bean.DefaultObjectFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class ResourceFileLoaderTest extends PulseTestCase
{
    private static final String EXTENSION_XML = "xml";

    private ResourceFileLoader loader;

    protected void setUp() throws Exception
    {
        super.setUp();

        TypeRegistry registry = new TypeRegistry();
        registry.register(ResourcesConfiguration.class);
        loader = new ResourceFileLoader();
        loader.setTypeRegistry(registry);
        loader.setObjectFactory(new DefaultObjectFactory());
        loader.init();
    }

    public void testEmptyRepo() throws Exception
    {
        InMemoryResourceRepository repo = loadRepo();
        List<String> resources = repo.getResourceNames();
        assertNotNull(resources);
        assertEquals(0, resources.size());
    }

    public void testResource() throws Exception
    {
        InMemoryResourceRepository repo = loadRepo();
        List<String> resources = repo.getResourceNames();
        assertNotNull(resources);
        assertEquals(1, resources.size());

        ResourceConfiguration resource = repo.getResource("aResource");
        assertNotNull(resource);
        Map<String, ResourcePropertyConfiguration> props = resource.getProperties();
        assertEquals(1, props.size());
        assertEquals("b", props.get("a").getValue());
    }

    public void testResourceWithVersion() throws Exception
    {
        InMemoryResourceRepository repo = loadRepo();
        List<String> resources = repo.getResourceNames();
        assertNotNull(resources);
        assertEquals(1, resources.size());

        ResourceConfiguration resource = repo.getResource("aResource");
        assertNotNull(resource);

        ResourceVersionConfiguration version = resource.getVersion("aVersion");
        assertNotNull(version);

        Map<String, ResourcePropertyConfiguration> props = version.getProperties();
        assertEquals(2, props.size());
        assertEquals("c", props.get("b").getValue());
        assertEquals("e", props.get("d").getValue());
    }

    public void testMultipleResources() throws Exception
    {
        InMemoryResourceRepository repo = loadRepo();

        List<String> resources = repo.getResourceNames();
        assertNotNull(resources);
        assertEquals(2, resources.size());

        ResourceConfiguration resource = repo.getResource("aResource");
        assertNotNull(resource);

        resource = repo.getResource("bResource");
        assertNotNull(resource);
        assertNotNull(resource.getVersion("aVersion"));
        assertNotNull(resource.getVersion("bVersion"));
    }

    public void testResourceRequirements() throws Exception
    {
        List<ResourceRequirement> requirements = loadRequirements();
        assertEquals(asList(new ResourceRequirement("basic", false, false),
                            new ResourceRequirement("versioned", "aver", false, false),
                            new ResourceRequirement("inverted", true, false),
                            new ResourceRequirement("optional-versioned", "aver", false, true)),
                     requirements);
    }

    public void testInvalidResourceRequirement() throws Exception
    {
        try
        {
            loadRequirements();
            fail("Requirements must specify a name");
        }
        catch (Exception e)
        {
            assertThat(e.getMessage(), containsString("Required attribute name not specified"));
        }
    }

    private InMemoryResourceRepository loadRepo() throws PulseException, IOException
    {
        return load().createRepository();
    }

    private List<ResourceRequirement> loadRequirements() throws PulseException, IOException
    {
        return load().createRequirements();
    }

    private ResourcesConfiguration load() throws PulseException, IOException
    {
        return loader.load(getInputFile(EXTENSION_XML));
    }
}
