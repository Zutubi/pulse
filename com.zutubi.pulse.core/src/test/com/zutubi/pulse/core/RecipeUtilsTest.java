package com.zutubi.pulse.core;

import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.core.config.ResourceRequirement;
import com.zutubi.pulse.core.config.ResourceVersion;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.engine.api.ResourceProperty;
import com.zutubi.pulse.core.test.api.PulseTestCase;

import static java.util.Arrays.asList;

public class RecipeUtilsTest extends PulseTestCase
{
    private static final String PROPERTY_SUFFIX = "-prop";
    private static final String VALUE_SUFFIX = "-val";

    private static final String RESOURCE_WITH_DEFAULT = "with-default";
    private static final String VERSION_DEFAULT = "default";
    private static final String VERSION_NON_DEFAULT = "non-default";

    private static final String RESOURCE_NO_DEFAULT = "no-default";
    private static final String VERSION_ONLY = "oly";

    private InMemoryResourceRepository resourceRepository;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        resourceRepository = new InMemoryResourceRepository();

        Resource withDefaultResource = new Resource(RESOURCE_WITH_DEFAULT);
        withDefaultResource.addProperty(property(RESOURCE_WITH_DEFAULT));

        ResourceVersion defaultVersion = new ResourceVersion(VERSION_DEFAULT);
        defaultVersion.addProperty(property(VERSION_DEFAULT));
        withDefaultResource.add(defaultVersion);

        ResourceVersion nonDefaultVersion = new ResourceVersion(VERSION_NON_DEFAULT);
        nonDefaultVersion.addProperty(property(VERSION_NON_DEFAULT));
        withDefaultResource.add(nonDefaultVersion);

        withDefaultResource.setDefaultVersion(VERSION_DEFAULT);
        resourceRepository.add(withDefaultResource);

        Resource noDefaultResource = new Resource(RESOURCE_NO_DEFAULT);
        noDefaultResource.addProperty(property(RESOURCE_NO_DEFAULT));

        ResourceVersion onlyVersion = new ResourceVersion(VERSION_ONLY);
        onlyVersion.addProperty(property(VERSION_ONLY));
        noDefaultResource.add(onlyVersion);
        resourceRepository.add(noDefaultResource);
    }

    private ResourceProperty property(String name)
    {
        return new ResourceProperty(propertyName(name), propertyValue(name));
    }

    private String propertyName(String name)
    {
        return name + PROPERTY_SUFFIX;
    }

    private String propertyValue(String name)
    {
        return propertyName(name) + VALUE_SUFFIX;
    }

    public void testImportNoVersion()
    {
        ExecutionContext context = populateContext(new ResourceRequirement(RESOURCE_WITH_DEFAULT, null, false));
        assertPropertyAdded(context, RESOURCE_WITH_DEFAULT);
        assertPropertyNotAdded(context, VERSION_DEFAULT);
        assertPropertyNotAdded(context, VERSION_NON_DEFAULT);
    }

    public void testImportDefaultVersion()
    {
        ExecutionContext context = populateContext(new ResourceRequirement(RESOURCE_WITH_DEFAULT, null, true));
        assertPropertyAdded(context, RESOURCE_WITH_DEFAULT);
        assertPropertyAdded(context, VERSION_DEFAULT);
        assertPropertyNotAdded(context, VERSION_NON_DEFAULT);
    }

    public void testImportSpecificVersion()
    {
        ExecutionContext context = populateContext(new ResourceRequirement(RESOURCE_WITH_DEFAULT, VERSION_NON_DEFAULT, false));
        assertPropertyAdded(context, RESOURCE_WITH_DEFAULT);
        assertPropertyNotAdded(context, VERSION_DEFAULT);
        assertPropertyAdded(context, VERSION_NON_DEFAULT);
    }

    public void testImportDefaultVersionNoDefault()
    {
        ExecutionContext context = populateContext(new ResourceRequirement(RESOURCE_NO_DEFAULT, null, true));
        assertPropertyAdded(context, RESOURCE_NO_DEFAULT);
        assertPropertyNotAdded(context, VERSION_ONLY);
    }

    public void testImportUnknownResource()
    {
        ExecutionContext context = populateContext(new ResourceRequirement("unknown", null, true));
        assertPropertyNotAdded(context, RESOURCE_WITH_DEFAULT);
        assertPropertyNotAdded(context, RESOURCE_NO_DEFAULT);
    }

    public void testImportUnknownVersion()
    {
        ExecutionContext context = populateContext(new ResourceRequirement(RESOURCE_WITH_DEFAULT, "unknown", false));
        assertPropertyAdded(context, RESOURCE_WITH_DEFAULT);
        assertPropertyNotAdded(context, VERSION_DEFAULT);
        assertPropertyNotAdded(context, VERSION_NON_DEFAULT);
    }

    public void testImportMultipleResources()
    {
        ExecutionContext context = populateContext(new ResourceRequirement(RESOURCE_WITH_DEFAULT, null, false), new ResourceRequirement(RESOURCE_NO_DEFAULT, null, false));
        assertPropertyAdded(context, RESOURCE_WITH_DEFAULT);
        assertPropertyNotAdded(context, VERSION_DEFAULT);
        assertPropertyNotAdded(context, VERSION_NON_DEFAULT);
        assertPropertyAdded(context, RESOURCE_NO_DEFAULT);
        assertPropertyNotAdded(context, VERSION_ONLY);
    }

    public void testImportResourceExistingAfterUnknown()
    {
        ExecutionContext context = populateContext(new ResourceRequirement("unknown", null, false), new ResourceRequirement(RESOURCE_WITH_DEFAULT, null, false));
        assertPropertyAdded(context, RESOURCE_WITH_DEFAULT);
        assertPropertyNotAdded(context, RESOURCE_NO_DEFAULT);
    }

    public void testImportResourceExistingAfterUnknownVerison()
    {
        ExecutionContext context = populateContext(new ResourceRequirement(RESOURCE_WITH_DEFAULT, "unknown", false), new ResourceRequirement(RESOURCE_NO_DEFAULT, null, false));
        assertPropertyAdded(context, RESOURCE_WITH_DEFAULT);
        assertPropertyAdded(context, RESOURCE_NO_DEFAULT);
    }
    
    private ExecutionContext populateContext(ResourceRequirement... requirements)
    {
        ExecutionContext context = new PulseExecutionContext();
        RecipeUtils.addResourceProperties(context, asList(requirements), resourceRepository);
        return context;
    }

    private void assertPropertyAdded(ExecutionContext context, String name)
    {
        assertEquals(propertyValue(name), context.getString(propertyName(name)));
    }

    private void assertPropertyNotAdded(ExecutionContext context, String name)
    {
        assertNull("Property '" + name + "' should not have been added to the context", context.getString(propertyName(name)));
    }
}
