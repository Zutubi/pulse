package com.zutubi.pulse.master;

import com.zutubi.pulse.master.tove.config.project.types.PostProcessorFragment;

import java.util.Map;

/**
 */
public interface PostProcessorManager
{
    PostProcessorFragment getProcessor(String name);
    Map<String, PostProcessorFragment> getAvailableProcessors();
}
