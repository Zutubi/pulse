package com.zutubi.pulse.core.model;

import com.zutubi.util.Sort;

import java.util.Comparator;

/**
 */
public class TestResultComparator implements Comparator<PersistentTestResult>
{
    private Comparator<String> packageComparator = new Sort.PackageComparator();

    public int compare(PersistentTestResult r1, PersistentTestResult r2)
    {
        if(r1.isSuite())
        {
            if(r2.isSuite())
            {
                return packageComparator.compare(r1.getName(), r2.getName());
            }
            else
            {
                return -1;
            }
        }
        else
        {
            if(r2.isSuite())
            {
                return 1;
            }
            else
            {
                return packageComparator.compare(r1.getName(), r2.getName());
            }
        }
    }
}
