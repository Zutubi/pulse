package com.zutubi.pulse.master.dependency.ivy;

import com.zutubi.pulse.core.dependency.ivy.IvyManager;
import com.zutubi.pulse.core.dependency.ivy.IvyUtils;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import com.zutubi.pulse.master.tove.config.project.DependencyConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.PublicationConfiguration;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;

public class ModuleDescriptorFactoryTest extends PulseTestCase
{
    private ModuleDescriptorFactory factory;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        factory = new ModuleDescriptorFactory();
    }

    public void testDefaultProjectConfiguration()
    {
        ProjectConfiguration project = new ProjectConfiguration("organisation", "project");
        
        ModuleDescriptor descriptor = factory.createDescriptor(project);
        assertEquals(1, descriptor.getConfigurationsNames().length);
        assertEquals(0, descriptor.getAllArtifacts().length);
        assertEquals(0, descriptor.getDependencies().length);

        assertEquals(ModuleRevisionId.newInstance("organisation", "project", null), descriptor.getModuleRevisionId());
    }

    public void testBuildStage()
    {
        BuildStageConfiguration stage = new BuildStageConfiguration("stage");
        ProjectConfiguration project = new ProjectConfiguration("project");
        project.getStages().put(stage.getName(), stage);

        ModuleDescriptor descriptor = factory.createDescriptor(project);
        assertEquals(1, descriptor.getConfigurationsNames().length);

        // stages only change the descriptor if they have publications.
        PublicationConfiguration publication = new PublicationConfiguration();
        publication.setName("artifact");
        publication.setExt("jar");
        stage.getPublications().add(publication);

        descriptor = factory.createDescriptor(project);
        assertEquals(2, descriptor.getConfigurationsNames().length);
        assertEquals(1, descriptor.getArtifacts("stage").length);
    }

    public void testBuildStageNameEncoding()
    {
        BuildStageConfiguration stage = new BuildStageConfiguration("#$%*");
        stage.getPublications().add(new PublicationConfiguration("artifact", "jar"));
        ProjectConfiguration project = new ProjectConfiguration("project");
        project.getStages().put(stage.getName(), stage);

        ModuleDescriptor descriptor = factory.createDescriptor(project);
        assertNotNull(descriptor.getConfiguration(IvyUtils.ivyEncodeStageName(stage.getName())));
    }

    public void testDependencies()
    {
        ProjectConfiguration dependentProject = new ProjectConfiguration("dependent");
        DependencyConfiguration dependency = new DependencyConfiguration();
        dependency.setProject(dependentProject);

        ProjectConfiguration project = new ProjectConfiguration("project");
        project.getDependencies().getDependencies().add(dependency);

        ModuleDescriptor descriptor = factory.createDescriptor(project);
        assertEquals(1, descriptor.getDependencies().length);
        DependencyDescriptor dependencyDescriptor = descriptor.getDependencies()[0];
        assertEquals(ModuleRevisionId.newInstance("", dependentProject.getName(), dependency.getRevision()), dependencyDescriptor.getDependencyRevisionId());
    }

    public void testStatus()
    {
        ProjectConfiguration project = new ProjectConfiguration("project");

        ModuleDescriptor descriptor = factory.createDescriptor(project);
        assertEquals(IvyManager.STATUS_INTEGRATION, descriptor.getStatus());

        project.getDependencies().setStatus(IvyManager.STATUS_MILESTONE);
        descriptor = factory.createDescriptor(project);
        assertEquals(IvyManager.STATUS_MILESTONE, descriptor.getStatus());
    }
}
