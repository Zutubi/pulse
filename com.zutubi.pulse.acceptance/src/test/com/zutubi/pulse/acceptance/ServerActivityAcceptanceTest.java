package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.pages.browse.BuildSummaryPage;
import com.zutubi.pulse.acceptance.pages.server.ServerActivityPage;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.util.Condition;
import com.zutubi.util.FileSystemUtils;
import com.thoughtworks.selenium.Selenium;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Acceptance tests for the server/activity page.
 */
public class ServerActivityAcceptanceTest extends SeleniumTestBase
{
    private static final String ID_BUILD_QUEUE_TABLE = "server.activity.build.queue";
    private static final String ID_ACTIVITY_TABLE = "server.activity.active.builds";
    private static final String ID_RECIPE_QUEUE_TABLE = "server.activity.recipe.queue";

    private static final int TIMEOUT = 90000;

    private Map<Integer, File> waitFiles = new HashMap<Integer, File>();
    private int nextBuild = 1;

    protected void setUp() throws Exception
    {
        super.setUp();
        xmlRpcHelper.loginAsAdmin();
        loginAsAdmin();
    }

    protected void tearDown() throws Exception
    {
        // finish any builds that are left over.
        for (int i = 1; i < nextBuild; i++)
        {
            waitForBuildToComplete(i);
        }

        for (File f: waitFiles.values())
        {
            if (!f.delete())
            {
                f.deleteOnExit();
            }
        }
        
        xmlRpcHelper.logout();
        super.tearDown();
    }

    public void testEmptyActivityTables()
    {
        browser.openAndWaitFor(ServerActivityPage.class);
        assertEmptyTable(ID_BUILD_QUEUE_TABLE, "build queue", BuildQueueTable.EMPTY_MESSAGE);
        assertEmptyTable(ID_ACTIVITY_TABLE, "active builds", ActiveBuildsTable.EMPTY_MESSAGE);
        assertEmptyTable(ID_RECIPE_QUEUE_TABLE, "recipe queue", "no recipe requests queued");
    }

    private void assertEmptyTable(String id, String header, String message)
    {
        // we use contains here since the recipe queue table header gets merged with the
        // pause action when returned by selenium.
        assertTrue(new Table(id).getHeader().contains(header));
        assertEquals(message, SeleniumUtils.getCellContents(browser.getSelenium(), id, 2, 0));
    }

    /**
     * Simple verification that active builds are correctly displayed in the
     * active builds table.
     *
     * Single project.
     *
     * @throws Exception on error.
     */
    public void testActiveBuilds() throws Exception
    {
        createAndTriggerProjectBuild();

        browser.openAndWaitFor(ServerActivityPage.class);

        ActiveBuildsTable activeBuildsTable = new ActiveBuildsTable();
        assertEquals(1, activeBuildsTable.getRowCount());
        assertEquals("trigger via remote api by admin", activeBuildsTable.getReason());
        assertEquals(random, activeBuildsTable.getOwner());
        assertEquals("1", activeBuildsTable.getBuildId());
        assertEquals(random, activeBuildsTable.getProject());
        assertEquals("4", activeBuildsTable.getRevision());
        assertEquals("in progress", activeBuildsTable.getStatus());

        waitForBuildToComplete(1);
        waitForQueueCount(activeBuildsTable, 0);
    }

    public void testCancelBuild() throws Exception
    {
        createAndTriggerProjectBuild();

        browser.openAndWaitFor(ServerActivityPage.class);

        ActiveBuildsTable activeBuildsTable = new ActiveBuildsTable();
        activeBuildsTable.clickCancel(random, 1);

        xmlRpcHelper.waitForBuildToComplete(random, 1);
        browser.openAndWaitFor(BuildSummaryPage.class, random, 1L);
        assertTextPresent("Forceful termination requested by 'admin'");
    }

    /**
     * Simple verification that queued builds are correctly displayed in the
     * build queue.
     *
     * Single project.
     *
     * @throws Exception on error.
     */
    public void testBuildQueue() throws Exception
    {
        // build 1 becomes active.
        createAndTriggerProjectBuild();
        // build 2 goes into the build queue.
        triggerBuild(false);

        browser.openAndWaitFor(ServerActivityPage.class);

        ActiveBuildsTable activeBuildsTable = new ActiveBuildsTable();
        waitForQueueCount(activeBuildsTable, 1);

        final BuildQueueTable buildQueueTable = new BuildQueueTable();
        waitForQueueCount(buildQueueTable, 1);
        assertEquals("trigger via remote api by admin", buildQueueTable.getReason());
        assertEquals(random, buildQueueTable.getOwner());
        assertEquals("[pending]", buildQueueTable.getBuildId());
        assertEquals(random, buildQueueTable.getProject());

        waitForBuildToComplete(1);

        waitForQueueCount(buildQueueTable, 0);
        waitForQueueCount(activeBuildsTable, 1);

        waitForBuildToComplete(2);
    }

    private void createAndTriggerProjectBuild() throws Exception
    {
        Hashtable<String, Object> svn = xmlRpcHelper.getSubversionConfig(Constants.WAIT_ANT_REPOSITORY);
        Hashtable<String,Object> ant = xmlRpcHelper.getAntConfig();
        ant.put(Constants.Project.AntCommand.ARGUMENTS, getFileArgument());
        xmlRpcHelper.insertSingleCommandProject(random, ProjectManager.GLOBAL_PROJECT_NAME, false, svn, ant);

        triggerBuild(true);
    }

    private void triggerBuild(boolean waitForInProgress) throws Exception
    {
        int thisBuild = nextBuild++;

        File waitFile = new File(FileSystemUtils.getSystemTempDir(), random + nextBuild);
        if (waitFile.exists() && !waitFile.delete())
        {
            throw new RuntimeException("Wait file '" + waitFile.getAbsolutePath() + "' already exists and can't be removed");
        }
        waitFiles.put(thisBuild, waitFile);

        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put("wait.file", waitFile.getAbsolutePath().replace("\\", "/"));
        xmlRpcHelper.triggerBuild(random, "", properties);

        if (waitForInProgress)
        {
            xmlRpcHelper.waitForBuildInProgress(random, thisBuild, TIMEOUT);
        }
    }

    private void waitForQueueCount(final Table queueTable, final int count)
    {
        SeleniumUtils.refreshUntil(browser.getSelenium(), TIMEOUT, new Condition()
        {
            public boolean satisfied()
            {
                return queueTable.getRowCount() == count;
            }
        }, "queue to have " + count + " entries");
    }

    private void waitForBuildToComplete(int buildId) throws Exception
    {
        File waitFile = waitFiles.get(buildId);
        if (!waitFile.isFile())
        {
            FileSystemUtils.createFile(waitFile, "test");
        }

        xmlRpcHelper.waitForBuildToComplete(random, buildId);
    }

    private String getFileArgument()
    {
        return "-Dfile=${wait.file}";
    }

    public class Table
    {
        protected String id;
        protected String emptyMessage;

        protected Selenium selenium;

        public Table(String id)
        {
            this(id, null);
        }

        public Table(String id, String emptyMessage)
        {
            this.id = id;
            this.emptyMessage = emptyMessage;
            this.selenium = browser.getSelenium();
        }

        public String getHeader()
        {
            return SeleniumUtils.getCellContents(this.selenium, id, 0, 0);
        }

        /**
         * Get the content of the table cell based at the specified row / column.
         * @param row     row identifier, starting at 1 for the first data row.
         * @param column  column identifier, starting at 1 for the left hand most column
         * @return the content of the selected cell.
         */
        public String getCell(int row, int column)
        {
            try
            {
                return SeleniumUtils.getCellContents(this.selenium, id, row + 1, column - 1);
            }
            catch (Exception e)
            {
                return null;
            }
        }

        public int getRowCount()
        {
            int count = 0;
            while (true)
            {
                try
                {
                    SeleniumUtils.getCellContents(this.selenium, id, count + 2, 0);
                    count++;
                }
                catch (Exception e)
                {
                    break;
                }
            }
            if (count == 1 && isTableEmpty())
            {
                return 0;
            }
            return count;
        }

        public boolean isTableEmpty()
        {
            return getCell(1, 1).equals(emptyMessage);
        }
    }

    /**
     * The build queue table.
     */
    public class BuildQueueTable extends Table
    {
        private int row = 1;

        private static final String EMPTY_MESSAGE = "no builds queued";

        public BuildQueueTable()
        {
            super(ID_BUILD_QUEUE_TABLE, EMPTY_MESSAGE);
        }

        public String getReason()
        {
            return getCell(row, 6);
        }

        public String getOwner()
        {
            return getCell(row, 1);
        }

        public String getBuildId()
        {
            return getCell(row, 2);
        }

        public String getProject()
        {
            return getCell(row, 3);
        }
    }

    /**
     * The active builds table.
     */
    public class ActiveBuildsTable extends Table
    {
        private int row = 1;

        public static final String EMPTY_MESSAGE = "no builds active";

        public ActiveBuildsTable()
        {
            super(ID_ACTIVITY_TABLE, EMPTY_MESSAGE);
        }

        public String getOwner()
        {
            return getCell(row, 1);
        }

        public String getBuildId()
        {
            return getCell(row, 2);
        }

        public String getProject()
        {
            return getCell(row, 3);
        }

        public String getRevision()
        {
            return getCell(row, 4);
        }

        public String getStatus()
        {
            return getCell(row, 6);
        }

        public String getReason()
        {
            // column 7 because the status column has two cells.
            return getCell(row, 8);
        }

        public void clickCancel(String owner, long number)
        {
            this.selenium.click("cancel.active." + owner + "." + Long.toString(number));
        }
    }
}
