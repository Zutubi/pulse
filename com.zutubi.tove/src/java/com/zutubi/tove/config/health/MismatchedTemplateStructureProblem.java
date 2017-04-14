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

import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.tove.type.record.RecordUtils;

/**
 * Base for all problems that indicate a template parent and child have different typed items at the same subpath.  This
 * includes the case where the template child is completely missing a subpath from the parent.
 */
public abstract class MismatchedTemplateStructureProblem extends HealthProblemSupport
{
    protected String templateParentPath;
    protected String key;

    public MismatchedTemplateStructureProblem(String path, String message, String key, String templateParentPath)
    {
        super(path, message);
        this.key = key;
        this.templateParentPath = templateParentPath;
    }

    protected boolean parentStillHasRecord(RecordManager recordManager)
    {
        String inheritedPath = PathUtils.getPath(templateParentPath, key);
        return recordManager.containsRecord(getPath()) && recordManager.containsRecord(inheritedPath);
    }

    public void solve(RecordManager recordManager)
    {
        // If there is nothing where the skeletons should appear, create a new
        // skeleton structure from the template parent and insert it.
        if (parentStillHasRecord(recordManager))
        {
            Record existingChild = recordManager.select(getPath());
            if (!existingChild.containsKey(key))
            {
                Record inherited = recordManager.select(PathUtils.getPath(templateParentPath, key));
                recordManager.insert(PathUtils.getPath(getPath(), key), RecordUtils.createSkeletonOf(inherited));
            }
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        MismatchedTemplateStructureProblem that = (MismatchedTemplateStructureProblem) o;

        if (key != null ? !key.equals(that.key) : that.key != null)
        {
            return false;
        }
        if (templateParentPath != null ? !templateParentPath.equals(that.templateParentPath) : that.templateParentPath != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (templateParentPath != null ? templateParentPath.hashCode() : 0);
        result = 31 * result + (key != null ? key.hashCode() : 0);
        return result;
    }
}
