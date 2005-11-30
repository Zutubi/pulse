package com.cinnamonbob;

import com.cinnamonbob.core.model.BuildResult;
import com.cinnamonbob.services.MasterService;

/**
 */
public class MasterServiceImpl implements MasterService
{
    public void buildComplete(BuildResult result)
    {
        System.out.println(result.getId());
    }
}
