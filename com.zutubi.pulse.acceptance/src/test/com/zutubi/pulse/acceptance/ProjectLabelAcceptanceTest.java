/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.forms.admin.LabelForm;
import com.zutubi.pulse.acceptance.forms.admin.NewLabelForm;
import com.zutubi.pulse.acceptance.pages.admin.ListPage;
import com.zutubi.pulse.acceptance.pages.browse.BrowsePage;
import com.zutubi.pulse.master.tove.config.NewLabelConfiguration;
import com.zutubi.tove.type.record.PathUtils;

import java.util.Hashtable;

/**
 * Acceptance tests that verify categorisation of projects using labels.
 */
public class ProjectLabelAcceptanceTest extends AcceptanceTestBase
{
    protected void setUp() throws Exception
    {
        super.setUp();
        rpcClient.loginAsAdmin();
    }

    protected void tearDown() throws Exception
    {
        rpcClient.logout();
        super.tearDown();
    }

    public void testSimpleGroups() throws Exception
    {
        String p1 = random + "-p1";
        String p2 = random + "-p2";
        String p3 = random + "-p3";
        String g1 = random + "-g1";
        String g2 = random + "-g2";

        rpcClient.RemoteApi.insertSimpleProject(p1,  false);
        rpcClient.RemoteApi.insertSimpleProject(p2,  false);
        rpcClient.RemoteApi.insertSimpleProject(p3,  false);

        rpcClient.RemoteApi.addLabel(p1, g1);
        rpcClient.RemoteApi.addLabel(p1, g2);
        rpcClient.RemoteApi.addLabel(p2, g1);

        getBrowser().loginAsAdmin();
        BrowsePage browsePage = getBrowser().openAndWaitFor(BrowsePage.class);
        assertGroupPresent(browsePage, g1, p1, p2);
        assertGroupPresent(browsePage, g2, p1);
        assertGroupPresent(browsePage, null, p3);
        assertFalse(browsePage.isProjectPresent(g1, p3));
        assertFalse(browsePage.isProjectPresent(g2, p2));
        assertFalse(browsePage.isProjectPresent(g2, p3));
        assertFalse(browsePage.isProjectPresent(null, p1));
        assertFalse(browsePage.isProjectPresent(null, p2));
    }

    public void testDisableGroupByLabel() throws Exception
    {
        String projectName = random + "-project";
        String labelName = random + "-label";
        String userLogin = random + "-user";

        rpcClient.RemoteApi.insertSimpleProject(projectName,  false);
        rpcClient.RemoteApi.addLabel(projectName, labelName);

        String userPath = rpcClient.RemoteApi.insertTrivialUser(userLogin);
        getBrowser().loginAndWait(userLogin, "");

        // Default is group by label
        BrowsePage browsePage = getBrowser().openAndWaitFor(BrowsePage.class);
        assertGroupPresent(browsePage, labelName, projectName);
        assertFalse(browsePage.isProjectPresent(null, projectName));

        // Uncheck option and ensure grouping disappears
        String prefsPath = PathUtils.getPath(userPath, "preferences", "browseView");
        Hashtable<String, Object> browsePreferences = rpcClient.RemoteApi.getConfig(prefsPath);
        browsePreferences.put("groupsShown", false);
        rpcClient.RemoteApi.saveConfig(prefsPath, browsePreferences, false);

        browsePage.openAndWaitFor();
        assertFalse(browsePage.isGroupPresent(labelName));
        assertTrue(browsePage.isProjectPresent(null, projectName));
    }

    public void testAddProjectToGroup() throws Exception
    {
        String p1 = random + "-1";
        String p2 = random + "-2";
        String group = random + "-group";

        rpcClient.RemoteApi.insertSimpleProject(p1,  false);
        rpcClient.RemoteApi.insertSimpleProject(p2,  false);

        rpcClient.RemoteApi.addLabel(p1, group);

        getBrowser().loginAsAdmin();
        BrowsePage browsePage = getBrowser().openAndWaitFor(BrowsePage.class);
        assertGroupPresent(browsePage, group, p1);
        assertFalse(browsePage.isProjectPresent(group, p2));

        rpcClient.RemoteApi.addLabel(p2, group);

        browsePage.openAndWaitFor();
        assertGroupPresent(browsePage, group, p1, p2);
    }

    public void testRemoveProjectFromGroup() throws Exception
    {
        String p1 = random + "-1";
        String p2 = random + "-2";
        String group = random + "-group";

        rpcClient.RemoteApi.insertSimpleProject(p1,  false);
        rpcClient.RemoteApi.insertSimpleProject(p2,  false);

        rpcClient.RemoteApi.addLabel(p1, group);
        String path = rpcClient.RemoteApi.addLabel(p2, group);

        getBrowser().loginAsAdmin();
        BrowsePage browsePage = getBrowser().openAndWaitFor(BrowsePage.class);
        assertGroupPresent(browsePage, group, p1, p2);

        rpcClient.RemoteApi.call("deleteConfig", path);

        browsePage.openAndWaitFor();
        assertGroupPresent(browsePage, group, p1);
        assertFalse(browsePage.isProjectPresent(group, p2));
    }

    public void testEmptyOutGroup() throws Exception
    {
        String p1 = random + "-1";
        String group = random + "-group";

        rpcClient.RemoteApi.insertSimpleProject(p1,  false);

        String path = rpcClient.RemoteApi.addLabel(p1, group);

        getBrowser().loginAsAdmin();
        BrowsePage browsePage = getBrowser().openAndWaitFor(BrowsePage.class);
        assertGroupPresent(browsePage, group, p1);

        rpcClient.RemoteApi.call("deleteConfig", path);

        browsePage.openAndWaitFor();
        assertFalse(browsePage.isGroupPresent(group));
    }

    public void testRenameLabelAction() throws Exception
    {
        String p1 = random + "-p1";
        String p2 = random + "-p2";
        String originalLabel = random + "-label";
        String editedLabel = originalLabel + "-edited";
        
        rpcClient.RemoteApi.insertSimpleProject(p1,  false);
        rpcClient.RemoteApi.insertSimpleProject(p2,  false);

        String l1Path = rpcClient.RemoteApi.addLabel(p1, originalLabel);
        String l2Path = rpcClient.RemoteApi.addLabel(p2, originalLabel);

        Hashtable<String, Object> newLabel = rpcClient.RemoteApi.createEmptyConfig(NewLabelConfiguration.class);
        newLabel.put("label", editedLabel);
        
        rpcClient.RemoteApi.doConfigActionWithArgument(l1Path, "rename", newLabel);

        assertLabelName(editedLabel, l1Path);
        assertLabelName(editedLabel, l2Path);
    }

    public void testRenameLabelCustomWorkflow() throws Exception
    {
        String p1 = random + "-p1";
        String p2 = random + "-p2";
        String originalName = random + "-label";
        String secondName = originalName + "-edited";
        String thirdName = secondName + "-edited";
        String finalName = thirdName + "-edited";

        rpcClient.RemoteApi.insertSimpleProject(p1,  false);
        rpcClient.RemoteApi.insertSimpleProject(p2,  false);

        // Begin with two differently named labels, rename one so it matches the other, then rename
        // again to test custom workflow will prompt to change both.
        
        String l1Path = rpcClient.RemoteApi.addLabel(p1, originalName);
        String l2Path = rpcClient.RemoteApi.addLabel(p2, secondName);

        getBrowser().loginAsAdmin();
        // View then cancel, nothing changes.
        ListPage labelsPage = getBrowser().openAndWaitFor(ListPage.class, PathUtils.getParentPath(l1Path));
        labelsPage.clickView(PathUtils.getBaseName(l1Path));        
        LabelForm labelForm = getBrowser().createForm(LabelForm.class);
        labelForm.waitFor();
        labelForm.cancelFormElements("");
        
        labelsPage.waitFor();
        assertLabelName(originalName, l1Path);
        assertLabelName(secondName, l2Path);

        // View and edit, only first label changes.
        labelsPage.clickView(PathUtils.getBaseName(l1Path));
        labelForm.waitFor();
        labelForm.saveFormElements(secondName);
        
        labelsPage.waitFor();
        assertLabelName(secondName, l1Path);
        assertLabelName(secondName, l2Path);

        // View then edit, prompted for both labels, cancel and nothing changes.
        labelsPage.clickView(PathUtils.getBaseName(l1Path));
        labelForm.waitFor();
        labelForm.saveFormElements(thirdName);

        NewLabelForm renameForm = getBrowser().createForm(NewLabelForm.class);
        renameForm.waitFor();
        getBrowser().waitForTextPresent("rename all labels?");
        renameForm.cancelFormElements("");
        
        labelsPage.waitFor();
        assertLabelName(secondName, l1Path);
        assertLabelName(secondName, l2Path);

        // View then edit, prompted for both labels, edit and change both.
        labelsPage.clickView(PathUtils.getBaseName(l1Path));
        labelForm.waitFor();
        labelForm.saveFormElements(thirdName);

        renameForm.waitFor();
        getBrowser().waitForTextPresent("rename all labels?");
        renameForm.submitFormElements("all", thirdName);

        labelsPage.waitFor();
        assertLabelName(thirdName, l1Path);
        assertLabelName(thirdName, l2Path);
        
        // View then edit, prompted for both labels, edit one and only that one changes.
        labelsPage.clickView(PathUtils.getBaseName(l1Path));
        labelForm.waitFor();
        labelForm.saveFormElements(finalName);

        renameForm.waitFor();
        getBrowser().waitForTextPresent("rename all labels?");
        renameForm.submitFormElements("one", finalName);

        labelsPage.waitFor();
        assertLabelName(finalName, l1Path);
        assertLabelName(thirdName, l2Path);
    }

    private void assertLabelName(String expectedName, String path) throws Exception
    {
        Hashtable<String, Object> label = rpcClient.RemoteApi.getConfig(path);
        assertEquals(expectedName, label.get("label"));        
    }
    
    private void assertGroupPresent(BrowsePage browsePage, String group, String... projects)
    {
        assertTrue("Group '" + group + "' not found", browsePage.isGroupPresent(group));
        for (String project: projects)
        {
            assertTrue("Project '" + project + "' not found in group '" + group + "'", browsePage.isProjectPresent(group, project));
        }
    }
}
