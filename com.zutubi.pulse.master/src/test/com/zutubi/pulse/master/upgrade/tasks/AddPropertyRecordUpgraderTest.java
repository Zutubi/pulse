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

package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.tove.type.record.MutableRecordImpl;
import static org.mockito.Mockito.*;

public class AddPropertyRecordUpgraderTest extends PulseTestCase
{
    private static final String PROPERTY_NAME = "test name";
    private static final String PROPERTY_VALUE = "test value";
    private static final String SCOPE = "scope";
    private static final String PATH = SCOPE + "/path";
    private static final String TEMPLATED_SCOPE = "templated scope";
    private static final String TEMPLATED_ANCESTOR_PATH = TEMPLATED_SCOPE + "/ancestor";
    private static final String TEMPLATED_NO_ANCESTOR_PATH = TEMPLATED_SCOPE + "/noancestor";

    private AddPropertyRecordUpgrader upgrader;

    protected void setUp() throws Exception
    {
        super.setUp();
        upgrader = new AddPropertyRecordUpgrader(PROPERTY_NAME, PROPERTY_VALUE);

        TemplatedScopeDetails templatedScopeDetails = mock(TemplatedScopeDetails.class);
        doReturn(true).when(templatedScopeDetails).hasAncestor(TEMPLATED_ANCESTOR_PATH);
        doReturn(false).when(templatedScopeDetails).hasAncestor(TEMPLATED_NO_ANCESTOR_PATH);

        PersistentScopes scopes = mock(PersistentScopes.class);
        doReturn(new ScopeDetails(SCOPE)).when(scopes).findByPath(startsWith(SCOPE));
        doReturn(templatedScopeDetails).when(scopes).findByPath(startsWith(TEMPLATED_SCOPE));

        upgrader.setPersistentScopes(scopes);
    }

    public void testSimpleScope()
    {
        MutableRecordImpl mutable = new MutableRecordImpl();
        upgrader.upgrade(PATH, mutable);
        assertEquals(PROPERTY_VALUE, mutable.get(PROPERTY_NAME));
    }

    public void testTemplatedScopeNoAncestor()
    {
        MutableRecordImpl mutable = new MutableRecordImpl();
        upgrader.upgrade(TEMPLATED_NO_ANCESTOR_PATH, mutable);
        assertEquals(PROPERTY_VALUE, mutable.get(PROPERTY_NAME));
    }

    public void testTemplatedScopeHasAncestor()
    {
        MutableRecordImpl mutable = new MutableRecordImpl();
        upgrader.upgrade(TEMPLATED_ANCESTOR_PATH, mutable);
        assertNull(mutable.get(PROPERTY_NAME));
    }

    public void testPropertyAlreadyExists()
    {
        final String EXISTING_VALUE = "another value";

        MutableRecordImpl mutable = new MutableRecordImpl();
        mutable.put(PROPERTY_NAME, EXISTING_VALUE);
        upgrader.upgrade(PATH, mutable);
        assertEquals(EXISTING_VALUE, mutable.get(PROPERTY_NAME));
    }

    public void testInvalidValue()
    {
        try
        {
            new AddPropertyRecordUpgrader("name", new Object());
            fail("Should not be able to create an upgrader with a non-simple value");
        }
        catch(IllegalArgumentException e)
        {
            assertTrue(e.getMessage().contains("only simple properties"));
        }
    }
}
