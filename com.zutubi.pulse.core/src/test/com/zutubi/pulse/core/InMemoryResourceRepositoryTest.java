package com.zutubi.pulse.core;

import com.zutubi.pulse.core.config.ResourceConfiguration;
import com.zutubi.pulse.core.config.ResourceRequirement;
import com.zutubi.pulse.core.config.ResourceVersionConfiguration;
import com.zutubi.pulse.core.test.api.PulseTestCase;

import static java.util.Arrays.asList;
import java.util.Collections;

public class InMemoryResourceRepositoryTest extends PulseTestCase
{
    private static final String EXISTING_RESOURCE = "existingresource";
    private static final String EXISTING_VERSION = "existingversion";

    private InMemoryResourceRepository repository;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        repository = new InMemoryResourceRepository();
        ResourceConfiguration resource = new ResourceConfiguration(EXISTING_RESOURCE);
        resource.add(new ResourceVersionConfiguration(EXISTING_VERSION));
        repository.addResource(resource);
    }

    public void testSatisfiesNoResources()
    {
        assertTrue(repository.satisfies(Collections.<ResourceRequirement>emptyList()));
    }

    public void testSatisfiesNonExistantResource()
    {
        assertFalse(repository.satisfies(asList(new ResourceRequirement("doesnt exist", false))));
    }

    public void testSatisfiesNonExistantResourceOptional()
    {
        assertTrue(repository.satisfies(asList(new ResourceRequirement("doesnt exist", true))));
    }

    public void testSatisfiesExistingResourceDefaultVersion()
    {
        assertTrue(repository.satisfies(asList(new ResourceRequirement(EXISTING_RESOURCE, false))));
    }

    public void testSatisfiesExistingResourceExistingVersion()
    {
        assertTrue(repository.satisfies(asList(new ResourceRequirement(EXISTING_RESOURCE, EXISTING_VERSION, false))));
    }

    public void testSatisfiesExistingResourceNonExistantVersion()
    {
        assertFalse(repository.satisfies(asList(new ResourceRequirement(EXISTING_RESOURCE, "nope", false))));
    }

    public void testSatisfiesExistingResourceNonExistantVersionOptional()
    {
        assertTrue(repository.satisfies(asList(new ResourceRequirement(EXISTING_RESOURCE, "nope", true))));
    }
}
