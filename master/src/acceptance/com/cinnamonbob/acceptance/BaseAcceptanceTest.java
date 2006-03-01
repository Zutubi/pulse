package com.cinnamonbob.acceptance;

/**
 * <class-comment/>
 */
public class BaseAcceptanceTest extends ExtendedWebTestCase
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

    public void test()
    {
        
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
