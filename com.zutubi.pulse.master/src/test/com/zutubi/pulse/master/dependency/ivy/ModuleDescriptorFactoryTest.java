package com.zutubi.pulse.master.dependency.ivy;

import com.zutubi.pulse.core.dependency.ivy.IvyManager;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;

public class ModuleDescriptorFactoryTest extends PulseTestCase
{
    private int nexthandle = 1;

    private ModuleDescriptorFactory factory;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        factory = new ModuleDescriptorFactory();
    }

    public void testDefaultProjectConfiguration()
    {
        ProjectConfiguration project = newProject("organisation", "project");

        ModuleDescriptor descriptor = factory.createDescriptor(project);
        assertEquals(1, descriptor.getConfigurationsNames().length);
        assertEquals(0, descriptor.getAllArtifacts().length);
        assertEquals(0, descriptor.getDependencies().length);

        assertEquals(MasterIvyModuleRevisionId.newInstance(project, (String)null), descriptor.getModuleRevisionId());
    }

    private ProjectConfiguration newProject(String org, String name)
    {
        ProjectConfiguration project = new ProjectConfiguration(org, name);
        project.setHandle(nexthandle++);
        return project;
    }

    public BuildStageConfiguration addStage(ProjectConfiguration project, String stageName)
    {
        BuildStageConfiguration stage = new BuildStageConfiguration(stageName);
        stage.setHandle(nexthandle++);
        project.getStages().put(stage.getName(), stage);
        return stage;
    }

    public void testDependencies()
    {
        ProjectConfiguration dependentProject = newProject("", "dependent");
        DependencyConfiguration dependency = new DependencyConfiguration();
        dependency.setProject(dependentProject);

        ProjectConfiguration project = newProject("", "project");
        project.getDependencies().getDependencies().add(dependency);

        ModuleDescriptor descriptor = factory.createDescriptor(project);
        assertEquals(1, descriptor.getDependencies().length);
        DependencyDescriptor dependencyDescriptor = descriptor.getDependencies()[0];
        assertEquals(MasterIvyModuleRevisionId.newInstance(dependency), dependencyDescriptor.getDependencyRevisionId());
    }

    public void testStatus()
    {
        ProjectConfiguration project = newProject("", "project");

        ModuleDescriptor descriptor = factory.createDescriptor(project);
        assertEquals(IvyManager.STATUS_INTEGRATION, descriptor.getStatus());

        project.getDependencies().setStatus(IvyManager.STATUS_MILESTONE);
        descriptor = factory.createDescriptor(project);
        assertEquals(IvyManager.STATUS_MILESTONE, descriptor.getStatus());
    }
}
