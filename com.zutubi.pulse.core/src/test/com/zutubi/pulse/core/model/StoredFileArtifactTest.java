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

package com.zutubi.pulse.core.model;

import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import static java.util.Arrays.asList;

import java.util.List;

public class StoredFileArtifactTest extends PulseTestCase
{
    public void testEvictNoFeatures()
    {
        StoredFileArtifact artifact = new StoredFileArtifact();
        assertFalse(artifact.evictFeature(Feature.Level.ERROR));
        assertFalse(artifact.evictFeature(Feature.Level.WARNING));
        assertFalse(artifact.evictFeature(Feature.Level.INFO));
    }

    public void testEvictInfo()
    {
        StoredFileArtifact artifact = createArtifactWithAFeatureOfEachLevel();
        
        assertFalse(artifact.evictFeature(Feature.Level.INFO));

        assertEquals(asList(info(), warning(), error()), artifact.getFeatures());
    }

    public void testEvictWarning()
    {
        StoredFileArtifact artifact = createArtifactWithAFeatureOfEachLevel();
        
        assertTrue(artifact.evictFeature(Feature.Level.WARNING));
        
        assertEquals(asList(warning(), error()), artifact.getFeatures());
    }

    public void testEvictError()
    {
        StoredFileArtifact artifact = createArtifactWithAFeatureOfEachLevel();
        
        assertTrue(artifact.evictFeature(Feature.Level.ERROR));
        
        assertEquals(asList(warning(), error()), artifact.getFeatures());
    }

    public void testEvictErrorNoInfo()
    {
        StoredFileArtifact artifact = new StoredFileArtifact();
        artifact.addFeature(warning());
        artifact.addFeature(error());
        
        assertTrue(artifact.evictFeature(Feature.Level.ERROR));
        
        assertEquals(asList(error()), artifact.getFeatures());
    }

    public void testEvictPrefersLater()
    {
        StoredFileArtifact artifact = createArtifactWithAFeatureOfEachLevel();
        PersistentFeature later = new PersistentFeature(Feature.Level.INFO, "later");
        later.setId(4);
        artifact.addFeature(later);
        
        assertTrue(artifact.evictFeature(Feature.Level.ERROR));
        
        List<PersistentFeature> expected = createArtifactWithAFeatureOfEachLevel().getFeatures();
        expected.remove(later);
        assertEquals(expected, artifact.getFeatures());
    }

    private StoredFileArtifact createArtifactWithAFeatureOfEachLevel()
    {
        StoredFileArtifact artifact = new StoredFileArtifact();
        artifact.addFeature(info());
        artifact.addFeature(warning());
        artifact.addFeature(error());
        return artifact;
    }

    private PersistentFeature error()
    {
        PersistentFeature error = new PersistentFeature(Feature.Level.ERROR, "error");
        error.setId(1);
        return error;
    }

    private PersistentFeature warning()
    {
        PersistentFeature warning = new PersistentFeature(Feature.Level.WARNING, "warning");
        warning.setId(2);
        return warning;
    }

    private PersistentFeature info()
    {
        PersistentFeature info = new PersistentFeature(Feature.Level.INFO, "info");
        info.setId(3);
        return info;
    }

}
