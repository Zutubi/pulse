package com.cinnamonbob;

import com.cinnamonbob.core.model.BuildResult;

/**
 */
public class MasterServiceImpl implements MasterService
{
    public void buildComplete(BuildResult result)
    {
        System.out.println(result.getId());
    }
}
