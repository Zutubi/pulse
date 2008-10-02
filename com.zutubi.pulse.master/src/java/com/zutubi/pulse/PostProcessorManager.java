package com.zutubi.pulse;

import com.zutubi.pulse.master.tove.config.project.types.PostProcessorFragment;

import java.util.Map;

/**
 */
public interface PostProcessorManager
{
    PostProcessorFragment getProcessor(String name);
    Map<String, PostProcessorFragment> getAvailableProcessors();
}
