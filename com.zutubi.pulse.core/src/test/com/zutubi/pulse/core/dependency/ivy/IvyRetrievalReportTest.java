package com.zutubi.pulse.core.dependency.ivy;

import com.zutubi.util.junit.ZutubiTestCase;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DefaultArtifact;
import org.apache.ivy.core.module.id.ModuleRevisionId;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.HashMap;

public class IvyRetrievalReportTest extends ZutubiTestCase
{
    public void testNoArtifacts() throws Exception
    {
        IvyRetrievalReport originalReport = new IvyRetrievalReport();

        IvyRetrievalReport report = roundTrip(originalReport);
        assertEquals(0, report.getArtifacts().size());
    }

    public void testSingleArtifact() throws Exception
    {
        IvyRetrievalReport originalReport = new IvyRetrievalReport();
        originalReport.addArtifact(createArtifact("artifact", "jar"));

        IvyRetrievalReport report = roundTrip(originalReport);
        assertEquals(1, report.getArtifacts().size());
    }

    public void testMultipleArtifacts() throws Exception
    {
        IvyRetrievalReport originalReport = new IvyRetrievalReport();
        originalReport.addArtifact(createArtifact("artifactA", "jar"));
        originalReport.addArtifact(createArtifact("artifactB", "jar"));
        originalReport.addArtifact(createArtifact("artifactC", "jar"));

        IvyRetrievalReport report = roundTrip(originalReport);
        assertEquals(3, report.getArtifacts().size());
    }

    public void testMultipleModules() throws Exception
    {
        IvyRetrievalReport originalReport = new IvyRetrievalReport();
        originalReport.addArtifact(createArtifact("org", "moduleA", "revision", "artifactA", "jar"));
        originalReport.addArtifact(createArtifact("org", "moduleB", "revision", "artifactA", "jar"));

        IvyRetrievalReport report = roundTrip(originalReport);
        assertEquals(2, report.getArtifacts().size());
    }

    public void testArtifactExtraAttributes() throws Exception
    {
        IvyRetrievalReport originalReport = new IvyRetrievalReport();
        originalReport.addArtifact(createArtifact("artifactA", "jar", map("extra", "attribute")));

        IvyRetrievalReport report = roundTrip(originalReport);
        Artifact artifact = report.getArtifacts().get(0);
        Map<String, String> extraAttributes = artifact.getId().getExtraAttributes();
        assertEquals("attribute", extraAttributes.get("extra"));
    }

    private IvyRetrievalReport roundTrip(IvyRetrievalReport originalReport) throws Exception
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        originalReport.toXml(baos);
        System.out.println(baos.toString());
        return IvyRetrievalReport.fromXml(new ByteArrayInputStream(baos.toByteArray()));
    }

    private Map<String, String> map(String key, String value)
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put(key, value);
        return map;
    }

    private Artifact createArtifact(String name, String ext)
    {
        ModuleRevisionId mrid = ModuleRevisionId.newInstance("org", "module", "revision");
        return new DefaultArtifact(mrid, null, name, ext, ext);
    }

    private Artifact createArtifact(String name, String ext, Map<String, String> extraAttributes)
    {
        ModuleRevisionId mrid = ModuleRevisionId.newInstance("org", "module", "revision");
        return new DefaultArtifact(mrid, null, name, ext, ext, extraAttributes);
    }

    private Artifact createArtifact(String org, String module, String revision, String name, String ext)
    {
        ModuleRevisionId mrid = ModuleRevisionId.newInstance(org, module, revision);
        return new DefaultArtifact(mrid, null, name, ext, ext);
    }

    private Artifact createArtifact(String org, String module, String revision, String name, String ext, Map<String, String> extraAttributes)
    {
        ModuleRevisionId mrid = ModuleRevisionId.newInstance(org, module, revision);
        return new DefaultArtifact(mrid, null, name, ext, ext, extraAttributes);
    }
}
