package com.zutubi.pulse.core.dependency.ivy;

import com.zutubi.util.junit.ZutubiTestCase;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DefaultArtifact;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.DownloadStatus;
import static org.apache.ivy.core.report.DownloadStatus.SUCCESSFUL;
import static org.apache.ivy.core.report.DownloadStatus.FAILED;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class IvyRetrievalReportTest extends ZutubiTestCase
{
    public void testNoArtifacts() throws Exception
    {
        IvyRetrievalReport originalReport = new IvyRetrievalReport();

        IvyRetrievalReport report = roundTrip(originalReport);
        assertEquals(0, report.getRetrievedArtifacts().size());
    }

    public void testSingleArtifact() throws Exception
    {
        IvyRetrievalReport originalReport = new IvyRetrievalReport();
        originalReport.addDownloadReports(createDownloadReport("artifact", "jar"));

        IvyRetrievalReport report = roundTrip(originalReport);
        assertEquals(1, report.getRetrievedArtifacts().size());
    }

    public void testMultipleArtifacts() throws Exception
    {
        IvyRetrievalReport originalReport = new IvyRetrievalReport();
        originalReport.addDownloadReports(createDownloadReport("artifactA", "jar"));
        originalReport.addDownloadReports(createDownloadReport("artifactB", "jar"));
        originalReport.addDownloadReports(createDownloadReport("artifactC", "jar"));

        IvyRetrievalReport report = roundTrip(originalReport);
        assertEquals(3, report.getRetrievedArtifacts().size());
    }

    public void testMultipleModules() throws Exception
    {
        IvyRetrievalReport originalReport = new IvyRetrievalReport();
        originalReport.addDownloadReports(createDownloadReport("org", "moduleA", "revision", "artifactA", "jar"));
        originalReport.addDownloadReports(createDownloadReport("org", "moduleB", "revision", "artifactA", "jar"));

        IvyRetrievalReport report = roundTrip(originalReport);
        assertEquals(2, report.getRetrievedArtifacts().size());
    }

    public void testArtifactExtraAttributes() throws Exception
    {
        IvyRetrievalReport originalReport = new IvyRetrievalReport();
        originalReport.addDownloadReports(createDownloadReport("artifactA", "jar", map("extra", "attribute")));

        IvyRetrievalReport report = roundTrip(originalReport);
        Artifact artifact = report.getRetrievedArtifacts().get(0);
        Map<String, String> extraAttributes = artifact.getId().getExtraAttributes();
        assertEquals("attribute", extraAttributes.get("extra"));
    }

    public void testHasFailures() throws Exception
    {
        IvyRetrievalReport originalReport = new IvyRetrievalReport();
        originalReport.addDownloadReports(createDownloadReport("artifactB", "jar", SUCCESSFUL));
        originalReport.addDownloadReports(createDownloadReport("artifactA", "jar", FAILED));

        IvyRetrievalReport report = roundTrip(originalReport);
        assertTrue(report.hasFailures());
    }

    public void testGetFailures() throws Exception
    {
        IvyRetrievalReport originalReport = new IvyRetrievalReport();
        originalReport.addDownloadReports(createDownloadReport("artifactB", "jar", SUCCESSFUL));
        originalReport.addDownloadReports(createDownloadReport("artifactA", "jar", FAILED));

        IvyRetrievalReport report = roundTrip(originalReport);

        List<ArtifactDownloadReport> failures = report.getFailures();
        assertEquals(1, failures.size());
        assertEquals("artifactA", failures.get(0).getArtifact().getName());
    }

    private IvyRetrievalReport roundTrip(IvyRetrievalReport originalReport) throws Exception
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        originalReport.toXml(baos);
        return IvyRetrievalReport.fromXml(new ByteArrayInputStream(baos.toByteArray()));
    }

    private Map<String, String> map(String key, String value)
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put(key, value);
        return map;
    }

    private ArtifactDownloadReport createDownloadReport(String name, String ext)
    {
        return createDownloadReport(name, ext, new HashMap<String, String>());
    }

    private ArtifactDownloadReport createDownloadReport(String name, String ext, DownloadStatus status)
    {
        return createDownloadReport(name, ext, status, new HashMap<String, String>());
    }

    private ArtifactDownloadReport createDownloadReport(String name, String ext, Map<String, String> extraAttributes)
    {
        return createDownloadReport(name, ext, SUCCESSFUL, extraAttributes);
    }

    private ArtifactDownloadReport createDownloadReport(String name, String ext, DownloadStatus status, Map<String, String> extraAttributes)
    {
        return createDownloadReport("org", "module", "revision", name, ext, status, extraAttributes);
    }

    private ArtifactDownloadReport createDownloadReport(String org, String module, String revision, String name, String ext)
    {
        return createDownloadReport(org, module, revision, name, ext, new HashMap<String, String>());
    }
    
    private ArtifactDownloadReport createDownloadReport(String org, String module, String revision, String name, String ext, Map<String, String> extraAttributes)
    {
        return createDownloadReport(org, module, revision, name, ext, SUCCESSFUL, extraAttributes);
    }
    
    private ArtifactDownloadReport createDownloadReport(String org, String module, String revision, String name, String ext, DownloadStatus status, Map<String, String> extraAttributes)
    {
        ModuleRevisionId mrid = ModuleRevisionId.newInstance(org, module, revision);
        Artifact artifact = new DefaultArtifact(mrid, null, name, ext, ext, extraAttributes);
        ArtifactDownloadReport report = new ArtifactDownloadReport(artifact);
        report.setDownloadStatus(status);
        return report;

    }
}
