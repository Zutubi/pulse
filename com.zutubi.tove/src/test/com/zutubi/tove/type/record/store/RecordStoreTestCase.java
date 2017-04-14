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

package com.zutubi.tove.type.record.store;

import com.zutubi.tove.transaction.AbstractTransactionTestCase;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.Record;

import java.util.Random;

/**
 *
 *
 */
public abstract class RecordStoreTestCase extends AbstractTransactionTestCase
{
    private static final Random RAND = new Random(System.currentTimeMillis());

    protected MutableRecord createRandomSampleRecord()
    {
        MutableRecord randomSample = new MutableRecordImpl();
        for (int i = 0; i < RAND.nextInt(10); i++)
        {
            randomSample.put(Integer.toString(i), Integer.toString(RAND.nextInt(20)));
        }
        return randomSample;
    }

    protected Record createRandomRecord()
    {
        Random rand = new Random(System.currentTimeMillis());
        return createSampleRecord(rand.nextInt(6), rand.nextInt(10));
    }

    protected Record createSampleRecord(int depth, int keys)
    {
        MutableRecordImpl random = new MutableRecordImpl();
        for (int i = 0; i < keys; i++)
        {
            random.put("key" + i, "value");
        }
        for (int i = 0; i < depth; i++)
        {
            random.put("nested" + i, random.copy(true, true));
        }
        return random;
    }

    public void assertRecordsEquals(Object expected, Object actual)
    {
        assertEquals((Record)expected, (Record)actual);
    }

    public void assertEquals(Record expected, Record actual)
    {
        assertEquals(expected.size(), actual.size());

        assertEquals(expected.keySet(), actual.keySet());
        for (String key : expected.keySet())
        {
            assertEquals(expected.get(key), actual.get(key));
        }

        assertEquals(expected.metaKeySet(), actual.metaKeySet());
        for (String key : expected.metaKeySet())
        {
            assertEquals(expected.getMeta(key), actual.getMeta(key));
        }

        assertEquals(expected.nestedKeySet(), actual.nestedKeySet());
        for (String key : expected.nestedKeySet())
        {
            assertEquals((Record) expected.get(key), (Record) actual.get(key));
        }
    }

}
