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

import com.google.common.base.Strings;
import com.zutubi.pulse.core.engine.api.Feature;
import com.zutubi.pulse.core.test.api.PulseTestCase;

/**
 */
public class FeatureTest extends PulseTestCase
{
    public void testSummaryBangOn()
    {
        String s = Strings.repeat("x", 4095);
        PersistentFeature f = new PersistentFeature(Feature.Level.WARNING, s);
        assertEquals(s, f.getSummary());
    }

    public void testSummaryTooLong()
    {
        PersistentFeature f = new PersistentFeature(Feature.Level.WARNING, Strings.repeat("x", 4096));
        assertEquals(getTrimmedSummary(), f.getSummary());
    }

    public void testAppendUnderLimit()
    {
        PersistentFeature f = new PersistentFeature(Feature.Level.ERROR, "yay");
        f.appendToSummary("bird");
        assertEquals("yaybird", f.getSummary());
    }

    public void testAppendHitsLimit()
    {
        PersistentFeature f = new PersistentFeature(Feature.Level.ERROR, Strings.repeat("x", 4094));
        f.appendToSummary("x");
        assertEquals(Strings.repeat("x", 4095), f.getSummary());
    }
    
    public void testAppendMakesSummaryTooLong()
    {
        String s = Strings.repeat("x", 4090);
        PersistentFeature f = new PersistentFeature(Feature.Level.WARNING, s);
        assertEquals(s, f.getSummary());
        f.appendToSummary("this is long enough");
        assertEquals(getTrimmedSummary(), f.getSummary());
    }

    public void testAppendToExactMakesSummaryTooLong()
    {
        String s = Strings.repeat("x", 4095);
        PersistentFeature f = new PersistentFeature(Feature.Level.WARNING, s);
        assertEquals(s, f.getSummary());
        f.appendToSummary("w00t");
        assertEquals(getTrimmedSummary(), f.getSummary());
    }

    public void testAppendToAlreadyTrimmedSummary()
    {
        PersistentFeature f = new PersistentFeature(Feature.Level.WARNING, Strings.repeat("x", 4096));
        String trimmed = getTrimmedSummary();
        assertEquals(trimmed, f.getSummary());
        f.appendToSummary("again, again!");
        assertEquals(trimmed, f.getSummary());
    }
            
    private String getTrimmedSummary()
    {
        return Strings.repeat("x", 4082) + "... [trimmed]";
    }
}
