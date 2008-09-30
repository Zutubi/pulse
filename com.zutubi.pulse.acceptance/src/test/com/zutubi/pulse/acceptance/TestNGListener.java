package com.zutubi.pulse.acceptance;

import org.testng.TestListenerAdapter;
import org.testng.ITestResult;

/**
 *
 *
 */
public class TestNGListener extends TestListenerAdapter
{
    private int m_count = 0;

    @Override
    public void onTestFailure(ITestResult tr)
    {
        log(tr.getTestClass().getName() + "." + tr.getName() + " Failure");
    }

    @Override
    public void onTestSkipped(ITestResult tr)
    {
        log(tr.getTestClass().getName() + "." + tr.getName() + " Skipped");
    }

    @Override
    public void onTestSuccess(ITestResult tr)
    {
        log(tr.getTestClass().getName() + "." + tr.getName() + " Success");
    }

    private void log(String string)
    {
        System.out.println(string);
/*
        if (++m_count % 40 == 0)
        {
            System.out.println("");
        }
*/
    }
}
