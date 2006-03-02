package com.cinnamonbob.acceptance;

import com.cinnamonbob.core.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;

/**
 * <class-comment/>
 */
public abstract class BaseAcceptanceTest extends ExtendedWebTestCase
{
    public BaseAcceptanceTest()
    {
    }

    public BaseAcceptanceTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        getTestContext().setBaseUrl("http://localhost:8080/");
    }

    protected void removeDirectory(File dir) throws IOException
    {
        if (!FileSystemUtils.removeDirectory(dir))
        {
            throw new IOException("Failed to remove " + dir);
        }
    }

    protected void login(String user, String password)
    {
        beginAt("/login.action");
        setWorkingForm("j_acegi_security_check");
        setFormElement("j_username", user);
        setFormElement("j_password", password);
        submit("login");
    }
}
