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

package com.zutubi.pulse.dev.expand;

import com.google.common.io.Resources;
import com.zutubi.pulse.core.NoopCommandConfiguration;
import com.zutubi.pulse.core.NoopPostProcessorConfiguration;
import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.engine.marshal.PulseFileLoaderFactory;
import com.zutubi.pulse.core.engine.marshal.ResourceFileLoader;
import com.zutubi.pulse.core.resources.ResourceRequirement;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.core.tove.config.CoreConfigurationRegistry;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.util.bean.WiringObjectFactory;
import com.zutubi.util.io.FileSystemUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class PulseFileExpanderTest extends PulseTestCase
{
    private File tempDir;
    private PulseFileExpander expander;

    private TypeRegistry typeRegistry;
    private PulseFileLoaderFactory fileLoaderFactory;
    private WiringObjectFactory objectFactory;
    
    public void setUp() throws Exception
    {
        super.setUp();

        typeRegistry = new TypeRegistry();

        fileLoaderFactory = new PulseFileLoaderFactory();

        objectFactory = new WiringObjectFactory();
        objectFactory.initProperties(this);

        CoreConfigurationRegistry configurationRegistry = new CoreConfigurationRegistry();
        configurationRegistry.setTypeRegistry(typeRegistry);
        configurationRegistry.init();

        typeRegistry.register(NoopCommandConfiguration.class);
        typeRegistry.register(NoopPostProcessorConfiguration.class);
        
        fileLoaderFactory.setTypeRegistry(typeRegistry);
        fileLoaderFactory.setObjectFactory(objectFactory);
        fileLoaderFactory.init();
        fileLoaderFactory.register("noop", NoopCommandConfiguration.class);
        fileLoaderFactory.register("noop.pp", NoopPostProcessorConfiguration.class);
        
        ResourceFileLoader resourceFileLoader = new ResourceFileLoader();
        resourceFileLoader.setObjectFactory(objectFactory);
        resourceFileLoader.setTypeRegistry(typeRegistry);
        resourceFileLoader.init();
        
        expander = new PulseFileExpander();
        expander.setFileLoaderFactory(fileLoaderFactory);
        expander.setResourceFileLoader(resourceFileLoader);
        
        tempDir = FileSystemUtils.createTempDir(getName(), ".tmp");
    }

    @Override
    protected void tearDown() throws Exception
    {
        removeDirectory(tempDir);
        super.tearDown();
    }

    public void testTrivial() throws IOException, PulseException
    {
        expandAndCompare();
    }

    public void testSimpleRecipe() throws IOException, PulseException
    {
        expandAndCompare();
    }
    
    public void testExpandProperty() throws IOException, PulseException
    {
        expandAndCompare();
    }

    public void testExpandMacro() throws IOException, PulseException
    {
        expandAndCompare();
    }
    
    public void testProcessImports() throws IOException, PulseException
    {
        copyInputToDirectory("fragment.xml", tempDir);
        PulseFileExpanderOptions options = new PulseFileExpanderOptions();
        options.setBaseDir(tempDir);
        expandAndCompare(options);
    }

    public void testRelativeImportPulseFileNotInBaseDir() throws IOException, PulseException
    {
        multiDirectoryHelper();
    }

    public void testAbsoluteImportPulseFileNotInBaseDir() throws IOException, PulseException
    {
        multiDirectoryHelper();
    }

    private void multiDirectoryHelper() throws IOException, PulseException
    {
        File pulseFileDir = new File(tempDir, "pulse");
        File includeDir = new File(tempDir, "include");
        assertTrue(pulseFileDir.mkdirs());
        assertTrue(includeDir.mkdirs());
        File pulseFile = copyInputToDirectory("xml", pulseFileDir);
        copyInputToDirectory("fragment.xml", includeDir);

        PulseFileExpanderOptions options = new PulseFileExpanderOptions();
        options.setBaseDir(tempDir);
        options.setPulseFile(pulseFileDir.getName() + "/" + pulseFile.getName());

        expandAndCompare(options);
    }

    public void testIsolateRecipe() throws IOException, PulseException
    {
        PulseFileExpanderOptions options = new PulseFileExpanderOptions();
        options.setRecipe("two");
        expandAndCompare(options);
    }

    public void testResources() throws IOException, PulseException
    {
        File resourcesFile = copyInputToDirectory("resources.xml", tempDir);
        PulseFileExpanderOptions options = new PulseFileExpanderOptions();
        options.setResourcesFile(resourcesFile.getAbsolutePath());
        options.getResourceRequirements().add(new ResourceRequirement("my-resource", false, false));
        expandAndCompare(options);
    }

    public void testDefines() throws IOException, PulseException
    {
        PulseFileExpanderOptions options = new PulseFileExpanderOptions();
        options.getDefines().put("defined-property", "defined-value");
        expandAndCompare(options);
    }

    public void testScopedProperties() throws IOException, PulseException
    {
        expandAndCompare();
    }
    
    public void testPostProcessorReference() throws IOException, PulseException
    {
        expandAndCompare();
    }
    
    private void expandAndCompare() throws IOException, PulseException
    {
        expandAndCompare(new PulseFileExpanderOptions());
    }

    private void expandAndCompare(PulseFileExpanderOptions options) throws IOException, PulseException
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        if (options.getBaseDir() == null)
        {
            options.setBaseDir(tempDir);
        }
        if (options.getPulseFile() == null)
        {
            File input = copyInputToDirectory("xml", tempDir);
            options.setPulseFile(input.getName());
        }
        options.setOutputStream(os);
        expander.expand(options);

        String got = new String(os.toByteArray());
        String expected = getExpectedOutput();
        assertEquals(normalise(expected), normalise(got));
    }

    private String normalise(String s)
    {
        return s.trim().replaceAll("\\t", "    ").replaceAll("\\r\\n", "\n");
    }

    private String getExpectedOutput()  throws IOException
    {
        return Resources.asCharSource(getInputURL("out.xml"), Charset.defaultCharset()).read();
    }

}
