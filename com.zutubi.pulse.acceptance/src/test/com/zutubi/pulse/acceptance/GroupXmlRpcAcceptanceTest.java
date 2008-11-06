package com.zutubi.pulse.acceptance;

import java.util.Hashtable;

import static com.zutubi.pulse.master.model.UserManager.ALL_USERS_GROUP_NAME;
import static com.zutubi.pulse.master.model.UserManager.ANONYMOUS_USERS_GROUP_NAME;
import com.zutubi.pulse.master.tove.config.ConfigurationRegistry;
import com.zutubi.util.RandomUtils;
import com.zutubi.tove.type.record.PathUtils;

public class GroupXmlRpcAcceptanceTest extends BaseXmlRpcAcceptanceTest
{
    protected void setUp() throws Exception
    {
        super.setUp();

        loginAsAdmin();
    }

    protected void tearDown() throws Exception
    {
        logout();

        super.tearDown();
    }

    /**
     * Verify that the All Users group name can not be altered via the XMLRPC interface.
     *
     * @throws Exception on error
     */
    public void testAllUsersGroupNameIsReadOnly() throws Exception
    {
        assertGroupNameReadOnly(ALL_USERS_GROUP_NAME);
    }

    /**
     * Verify that the Anonymous Users group name can not be altered via the XMLRPC interface.
     *
     * @throws Exception on error
     */
    public void testAnonymousUserGroupNameIsReadOnly() throws Exception
    {
        assertGroupNameReadOnly(ANONYMOUS_USERS_GROUP_NAME);
    }

    private void assertGroupNameReadOnly(String groupName) throws Exception
    {
        String randomName = RandomUtils.randomString(10);

        Hashtable<String, Object> group = assertGroupExists(groupName);
        group.put("name", randomName);

        try
        {
            call("saveConfig", getGroupPath(groupName), group, true);
            fail("Expecting failure due to attempt to change readOnly field.");
        }
        catch (Exception e)
        {
            assertEndsWith("Attempt to change readOnly property 'name' from '"+groupName+"' " +
                    "to '"+randomName+"' is not allowed.", e.getMessage());
        }

        assertGroupExists(groupName);
        assertGroupNotExists(randomName);
    }

    private void assertGroupNotExists(String groupName) throws Exception
    {
        try
        {
            call("getConfig", getGroupPath(groupName));
        }
        catch (Exception e)
        {
            assertEndsWith("Path '"+getGroupPath(groupName)+"' does not exist", e.getMessage());
        }
    }

    private Hashtable<String, Object> assertGroupExists(String groupName) throws Exception
    {
        Hashtable<String, Object> group = call("getConfig", getGroupPath(groupName));
        assertNotNull(group);
        return group;
    }

    private void assertEndsWith(String end, String str)
    {
        assertTrue(str.endsWith(end));
    }

    private String getGroupPath(String group)
    {
        return PathUtils.getPath(ConfigurationRegistry.GROUPS_SCOPE, group);
    }
}
