package com.zutubi.pulse.core;

import com.zutubi.pulse.core.resources.ResourceRequirement;
import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.pulse.core.resources.api.ResourceVersionConfiguration;
import com.zutubi.pulse.core.test.api.PulseTestCase;

import java.util.Collections;

import static java.util.Arrays.asList;

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
        resource.addVersion(new ResourceVersionConfiguration(EXISTING_VERSION));
        repository.addResource(resource);
    }

    public void testSatisfiesNoResources()
    {
        assertTrue(repository.satisfies(Collections.<ResourceRequirement>emptyList()));
    }

    public void testSatisfiesNonExistantResource()
    {
        assertFalse(repository.satisfies(asList(new ResourceRequirement("doesnt exist", false, false))));
    }

    public void testSatisfiesNonExistantResourceOptional()
    {
        assertTrue(repository.satisfies(asList(new ResourceRequirement("doesnt exist", false, true))));
    }

    public void testSatisfiesExistingResourceDefaultVersion()
    {
        assertTrue(repository.satisfies(asList(new ResourceRequirement(EXISTING_RESOURCE, false, false))));
    }

    public void testSatisfiesExistingResourceExistingVersion()
    {
        assertTrue(repository.satisfies(asList(new ResourceRequirement(EXISTING_RESOURCE, EXISTING_VERSION, false, false))));
    }

    public void testSatisfiesExistingResourceNonExistantVersion()
    {
        assertFalse(repository.satisfies(asList(new ResourceRequirement(EXISTING_RESOURCE, "nope", false, false))));
    }

    public void testSatisfiesNonExistantResourceInverse()
    {
        assertTrue(repository.satisfies(asList(new ResourceRequirement("doesnt exist", "nope", true, false))));
    }

    public void testSatisfiesNonExistantResourceInverseOptional()
    {
        assertTrue(repository.satisfies(asList(new ResourceRequirement("doesnt exist", "nope", true, true))));
    }

    public void testSatisfiesExistingResourceInverse()
    {
        assertFalse(repository.satisfies(asList(new ResourceRequirement(EXISTING_RESOURCE, null, true, false))));
    }

    public void testSatisfiesExistingResourceExistingVersionInverse()
    {
        assertFalse(repository.satisfies(asList(new ResourceRequirement(EXISTING_RESOURCE, EXISTING_VERSION, true, false))));
    }

    public void testSatisfiesExistingResourceNonExistantVersionInverse()
    {
        assertTrue(repository.satisfies(asList(new ResourceRequirement(EXISTING_RESOURCE, "nope", true, false))));
    }

    public void testSatisfiesExistingResourceInverseOptional()
    {
        assertFalse(repository.satisfies(asList(new ResourceRequirement(EXISTING_RESOURCE, null, true, true))));
    }

    public void testSatisfiesExistingResourceNonExistantVersionOptional()
    {
        assertTrue(repository.satisfies(asList(new ResourceRequirement(EXISTING_RESOURCE, "nope", false, true))));
    }
}
