package com.zutubi.pulse;

import com.zutubi.pulse.events.build.AbstractBuildRequestEvent;
import com.zutubi.pulse.events.build.BuildRequestEvent;
import com.zutubi.pulse.events.build.PersonalBuildRequestEvent;
import com.zutubi.pulse.model.BuildSpecification;
import com.zutubi.pulse.model.BuildSpecificationNode;
import com.zutubi.pulse.model.ResourceRequirement;
import com.zutubi.pulse.test.PulseTestCase;

import java.util.List;

public class BuildControllerTest extends PulseTestCase
{

    public void testGetResourceRequirementsPersonalBuild()
    {
        BuildController buildController = createBuildController(createPersonalBuildRequestEvent());

        List<ResourceRequirement> resourceRequirements = buildController.getResourceRequirements(new BuildSpecificationNode(), true);
        assertEquals(1, resourceRequirements.size());
        ResourceRequirement requirement = resourceRequirements.get(0);
        assertEquals(BuildController.ALLOW_PERSONAL_BUILDS, requirement.getResource());
    }

    public void testGetResourceRequirementsProjectBuild()
    {
        BuildController buildController = createBuildController(createBuildRequestEvent());
        List<ResourceRequirement> resourceRequirements = buildController.getResourceRequirements(new BuildSpecificationNode(), true);
        assertEquals(0, resourceRequirements.size());
    }

    public void testNeedsPersonalBuildResource()
    {
        assertFalse(createBuildController(createBuildRequestEvent()).needsPersonalBuildResource(true));

        BuildController personalBuildController = createBuildController(createPersonalBuildRequestEvent());
        assertTrue(personalBuildController.needsPersonalBuildResource(true));
        assertFalse(personalBuildController.needsPersonalBuildResource(false));
    }

    private AbstractBuildRequestEvent createPersonalBuildRequestEvent()
    {
        return new PersonalBuildRequestEvent(null, 0, null, null, null, null, null);
    }

    private AbstractBuildRequestEvent createBuildRequestEvent()
    {
        return new BuildRequestEvent(null, null, null, null, null);
    }

    private BuildController createBuildController(AbstractBuildRequestEvent buildRequestEvent)
    {
        return new BuildController(buildRequestEvent, new BuildSpecification(), null, null, null, null, null, null, null, null, null, null);
    }
}
