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

package com.zutubi.tove.config.health;

import com.google.common.base.Objects;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

/**
 * Indicates a template child contains a record with a different type to the corresponding record in the template
 * parent.
 */
public class IncompatibleOverrideOfComplexProblem extends MismatchedTemplateStructureProblem
{
    public IncompatibleOverrideOfComplexProblem(String path, String message, String key, String templateParentPath)
    {
        super(path, message, key, templateParentPath);
    }

    @Override
    public void solve(RecordManager recordManager)
    {
        if (parentStillHasRecord(recordManager))
        {
            String existingChildPath = PathUtils.getPath(getPath(), key);
            Record existingChild = recordManager.select(existingChildPath);
            if (existingChild != null)
            {
                Record inherited = recordManager.select(PathUtils.getPath(templateParentPath, key));
                if (!Objects.equal(inherited.getSymbolicName(), existingChild.getSymbolicName()))
                {
                    recordManager.delete(existingChildPath);
                }
            }

            // Let the super implementation add in the required skeletons.
            super.solve(recordManager);
        }
    }
}
