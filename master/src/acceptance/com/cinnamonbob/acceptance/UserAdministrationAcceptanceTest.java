package com.cinnamonbob.acceptance;

/**
 * <class-comment/>
 */
public class UserAdministrationAcceptanceTest extends BaseAcceptanceTest
{
    public UserAdministrationAcceptanceTest()
    {
    }

    public UserAdministrationAcceptanceTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

    }

    protected void tearDown() throws Exception
    {

        super.tearDown();
    }

    public void testCreateUser()
    {
        // navigate to user admin tab.

        // create random login name.
        // assert user does not exist.
        // select create form.
        // fill in details
        // save
        // assert user does exist.
        // assert form is reset.

    }

    public void testCreateUserValidation()
    {
        // test create user validation.
        // check validation - login is required.
        // check validation - password is required.
        // check validation - password and confirmation mismatch
    }
}
