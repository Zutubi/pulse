package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.core.postprocessors.api.TestStatus;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.model.persistence.TestCaseIndexDao;
import com.zutubi.util.StringUtils;
import com.zutubi.util.WebUtils;
import com.zutubi.util.logging.Logger;
import nu.xom.Attribute;
import nu.xom.Element;
import org.hibernate.SessionFactory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 */
public class DefaultTestManager implements TestManager
{
    private static final Logger LOG = Logger.getLogger(DefaultTestManager.class);

    private TestSuitePersister persister = new TestSuitePersister();
    private TestCaseIndexDao testCaseIndexDao;
    private MasterConfigurationManager configurationManager;
    private SessionFactory sessionFactory;

    public void index(BuildResult result)
    {
        if (!result.isPersonal())
        {
            for (RecipeResultNode node : result)
            {
                File testDir = new File(node.getResult().getAbsoluteOutputDir(configurationManager.getDataDirectory()), RecipeResult.TEST_DIR);
                if (testDir.isDirectory())
                {
                    indexTestsForStage(result, node.getStageHandle(), testDir);
                }
            }
        }
    }

    private void indexTestsForStage(BuildResult result, long stageNameId, File testDir)
    {
        try
        {
            persister.read(new IndexingHandler(result.getProject().getId(), result.getId(), result.getNumber(), stageNameId), null, testDir, true, false);
        }
        catch (Exception e)
        {
            LOG.severe("Unable to index test results for build: " + result.getNumber() + ": " + e.getMessage(), e);
        }
    }

    public void setTestCaseIndexDao(TestCaseIndexDao testCaseIndexDao)
    {
        this.testCaseIndexDao = testCaseIndexDao;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setSessionFactory(SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }

    private class IndexingHandler implements TestHandler
    {
        private Stack<PersistentTestSuiteResult> suites = new Stack<PersistentTestSuiteResult>();
        private Stack<Boolean> changed = new Stack<Boolean>();
        private long projectId;
        private long buildId;
        private long buildNumber;
        private long stageNameId;
        private String path;
        private Map<String, TestCaseIndex> allCases;
        private int count = 0;

        public IndexingHandler(long projectId, long buildId, long buildNumber, long stageNameId)
        {
            this.projectId = projectId;
            this.buildId = buildId;
            this.buildNumber = buildNumber;
            this.stageNameId = stageNameId;

            List<TestCaseIndex> cases = testCaseIndexDao.findByStage(stageNameId);
            allCases = new HashMap<String, TestCaseIndex>(cases.size());
            for(TestCaseIndex i: cases)
            {
                allCases.put(i.getName(), i);
            }
        }

        public void startSuite(PersistentTestSuiteResult suiteResult)
        {
            suites.push(suiteResult);
            changed.push(false);
            calculatePath();
        }

        public boolean endSuite()
        {
            suites.pop();
            calculatePath();
            return changed.pop();
        }

        private void calculatePath()
        {
            int i = 0;
            path = "";

            for (PersistentTestSuiteResult suite : suites)
            {
                if (i > 1)
                {
                    path += '/';
                }

                if (i > 0)
                {
                    path += WebUtils.formUrlEncode(suite.getName());
                }

                i++;
            }
        }

        private String getCasePath(String name)
        {
            name = WebUtils.formUrlEncode(name);
            if (StringUtils.stringSet(path))
            {
                return path + "/" + name;
            }
            else
            {
                return name;
            }
        }

        public void handleCase(PersistentTestCaseResult caseResult, Element element)
        {
            String casePath = getCasePath(caseResult.getName());
            TestCaseIndex caseIndex = allCases.get(casePath);
            boolean newIndex = false;
            if (caseIndex == null)
            {
                caseIndex = new TestCaseIndex(projectId, stageNameId, casePath);
                newIndex = true;
            }

            if (caseResult.hasBrokenTests() && !caseIndex.isHealthy())
            {
                // Broken in a previous build
                element.addAttribute(new Attribute(TestSuitePersister.ATTRIBUTE_BROKEN_SINCE, Long.toString(caseIndex.getBrokenSince())));
                element.addAttribute(new Attribute(TestSuitePersister.ATTRIBUTE_BROKEN_NUMBER, Long.toString(caseIndex.getBrokenNumber())));
                markChanged();
            }
            else if (!caseIndex.isHealthy() && caseResult.getStatus() == TestStatus.PASS)
            {
                // Fixed in this build
                element.addAttribute(new Attribute(TestSuitePersister.ATTRIBUTE_FIXED, "true"));
                markChanged();
            }

            caseIndex.recordExecution(caseResult.getStatus(), buildId, buildNumber);
            if(newIndex)
            {
                testCaseIndexDao.save(caseIndex);
            }
            if((++count % 1000) == 0)
            {
                sessionFactory.getCurrentSession().flush();
            }
        }

        private void markChanged()
        {
            if (!changed.peek())
            {
                changed.pop();
                changed.push(true);
            }
        }
    }
}
