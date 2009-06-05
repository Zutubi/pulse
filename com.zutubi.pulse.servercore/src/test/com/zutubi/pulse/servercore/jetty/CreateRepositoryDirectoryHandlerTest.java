package com.zutubi.pulse.servercore.jetty;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.FileSystemUtils;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.util.Resource;

import java.io.File;
import java.io.IOException;

public class CreateRepositoryDirectoryHandlerTest extends PulseTestCase
{
    private File baseDir;
    private HttpContext context;
    private CreateRepositoryDirectoryHandler handler;
    private HttpResponse response;
    private HttpRequest request;

    protected void setUp() throws Exception
    {
        super.setUp();

        response = mock(HttpResponse.class);
        request = mock(HttpRequest.class);
        context = mock(HttpContext.class);
        handler = new CreateRepositoryDirectoryHandler();
        handler.initialize(context);
        baseDir = FileSystemUtils.createTempDir();
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(baseDir);
        super.tearDown();
    }

    public void testCreatesDirectory() throws IOException
    {
        String pathInContext = "/some/path.txt";
        File file = new File(baseDir, pathInContext);
        stub(context.getResource(pathInContext)).toReturn(Resource.newResource(file.toURI().toURL()));
        stub(request.getMethod()).toReturn(HttpRequest.__PUT);

        assertFalse(file.getParentFile().isDirectory());
        handler.handle(pathInContext, "", request, response);
        assertTrue(file.getParentFile().isDirectory());
    }
}
