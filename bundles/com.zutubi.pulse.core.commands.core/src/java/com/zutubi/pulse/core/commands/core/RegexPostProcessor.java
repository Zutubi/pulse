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

package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.RegexPatternConfiguration;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.postprocessors.api.LineBasedPostProcessorSupport;
import com.zutubi.util.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A post processor that does line-by-line searching with regular expressions
 * to detect features.
 */
public class RegexPostProcessor extends LineBasedPostProcessorSupport
{
    public RegexPostProcessor(RegexPostProcessorConfiguration config)
    {
        super(config);
    }

    public RegexPostProcessorConfiguration getConfig()
    {
        return (RegexPostProcessorConfiguration) super.getConfig();
    }

    protected List<Feature> findFeatures(String line)
    {
        List<Feature> features = new LinkedList<Feature>();
        for (RegexPatternConfiguration p : getConfig().getPatterns())
        {
            String summary = match(p, line);
            if (summary != null)
            {
                features.add(new Feature(p.getCategory(), summary));
            }
        }

        return features;
    }

    private String match(RegexPatternConfiguration patternConfiguration, String line)
    {
        String result = null;

        Pattern pattern = Pattern.compile(patternConfiguration.getExpression());
        Matcher matcher = pattern.matcher(line);
        if (matcher.find())
        {
            for (String e : patternConfiguration.getExclusions())
            {
                if (Pattern.compile(e).matcher(line).find())
                {
                    return null;
                }
            }

            String summary = patternConfiguration.getSummary();
            if (StringUtils.stringSet(summary))
            {
                result = matcher.replaceAll(summary);
            }
            else
            {
                result = line;
            }
        }

        return result;
    }
}
