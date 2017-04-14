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

package com.zutubi.pulse.core.marshal;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import static com.zutubi.tove.type.record.PathUtils.getPath;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;

public class RelativeFileResolverTest extends PulseTestCase
{
    private static final String DIRNAME_NESTED = "d1";
    private static final String DIRNAME_NESTED_NESTED = "d2";

    private static final String DIR_NESTED = DIRNAME_NESTED;
    private static final String DIR_NESTED_NESTED = getPath(DIRNAME_NESTED, DIRNAME_NESTED_NESTED);

    private static final String FILENAME_TOP = "top.xml";
    private static final String FILENAME_NESTED = "nested.xml";
    private static final String FILENAME_NESTED_NESTED = "nestednested.xml";

    private static final String PATH_TOP = FILENAME_TOP;
    private static final String PATH_NESTED = getPath(DIR_NESTED, FILENAME_NESTED);
    private static final String PATH_NESTED_NESTED = getPath(DIR_NESTED_NESTED, FILENAME_NESTED_NESTED);

    private static final InputStream INPUT_TOP = mock(InputStream.class);
    private static final InputStream INPUT_NESTED = mock(InputStream.class);
    private static final InputStream INPUT_NESTED_NESTED = mock(InputStream.class);

    private FileResolver delegate;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        delegate = mock(FileResolver.class);
        stub(delegate.resolve(PATH_TOP)).toReturn(INPUT_TOP);
        stub(delegate.resolve("/" + PATH_TOP)).toReturn(INPUT_TOP);
        stub(delegate.resolve(PATH_NESTED)).toReturn(INPUT_NESTED);
        stub(delegate.resolve("/" + PATH_NESTED)).toReturn(INPUT_NESTED);
        stub(delegate.resolve(PATH_NESTED_NESTED)).toReturn(INPUT_NESTED_NESTED);
        stub(delegate.resolve("/" + PATH_NESTED_NESTED)).toReturn(INPUT_NESTED_NESTED);
        stub(delegate.resolve(argThat(new BaseMatcher<String>()
        {
            public boolean matches(Object o)
            {
                return !Arrays.<Object>asList(PATH_TOP, "/"+ PATH_TOP, PATH_NESTED, "/" + PATH_NESTED, PATH_NESTED_NESTED, "/" + PATH_NESTED_NESTED).contains(o);
            }

            public void describeTo(Description description)
            {
                description.appendText("a non-mapped path");
            }
        }))).toAnswer(new Answer<Object>()
        {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                throw new FileNotFoundException((String) invocationOnMock.getArguments()[0]);
            }
        });
    }

    public void testTopRelativeToNull() throws Exception
    {
        RelativeFileResolver relative = new RelativeFileResolver(null, delegate);
        assertSame(INPUT_TOP, relative.resolve(PATH_TOP));
    }

    public void testNestedRelativeToNull() throws Exception
    {
        RelativeFileResolver relative = new RelativeFileResolver(null, delegate);
        assertSame(INPUT_NESTED, relative.resolve(PATH_NESTED));
    }

    public void testTopRelativeToTop() throws Exception
    {
        RelativeFileResolver relative = new RelativeFileResolver(PATH_TOP, delegate);
        assertSame(INPUT_TOP, relative.resolve(PATH_TOP));
    }

    public void testNestedRelativeToTop() throws Exception
    {
        RelativeFileResolver relative = new RelativeFileResolver(PATH_TOP, delegate);
        assertSame(INPUT_NESTED, relative.resolve(PATH_NESTED));
    }

    public void testNestedNestedRelativeToTop() throws Exception
    {
        RelativeFileResolver relative = new RelativeFileResolver(PATH_TOP, delegate);
        assertSame(INPUT_NESTED_NESTED, relative.resolve(PATH_NESTED_NESTED));
    }

    public void testTopRelativeToNested() throws Exception
    {
        RelativeFileResolver relative = new RelativeFileResolver(PATH_NESTED, delegate);
        assertSame(INPUT_TOP, relative.resolve(getPath("..", PATH_TOP)));
    }

    public void testNestedRelativeToNested() throws Exception
    {
        RelativeFileResolver relative = new RelativeFileResolver(PATH_NESTED, delegate);
        assertSame(INPUT_NESTED, relative.resolve(FILENAME_NESTED));
    }

    public void testNestedNestedRelativeToNested() throws Exception
    {
        RelativeFileResolver relative = new RelativeFileResolver(PATH_NESTED, delegate);
        assertSame(INPUT_NESTED_NESTED, relative.resolve(getPath(DIRNAME_NESTED_NESTED, FILENAME_NESTED_NESTED)));
    }

    public void testTopRelativeToNestedNested() throws Exception
    {
        RelativeFileResolver relative = new RelativeFileResolver(PATH_NESTED_NESTED, delegate);
        assertSame(INPUT_TOP, relative.resolve(getPath("..", "..", PATH_TOP)));
    }

    public void testNestedRelativeToNestedNested() throws Exception
    {
        RelativeFileResolver relative = new RelativeFileResolver(PATH_NESTED_NESTED, delegate);
        assertSame(INPUT_NESTED, relative.resolve(getPath("..", FILENAME_NESTED)));
    }

    public void testNestedNestedRelativeToNestedNested() throws Exception
    {
        RelativeFileResolver relative = new RelativeFileResolver(PATH_NESTED_NESTED, delegate);
        assertSame(INPUT_NESTED_NESTED, relative.resolve(FILENAME_NESTED_NESTED));
    }

    public void testSameDirectoryNormalised() throws Exception
    {
        RelativeFileResolver relative = new RelativeFileResolver(PATH_NESTED, delegate);
        assertSame(INPUT_NESTED_NESTED, relative.resolve(getPath(DIRNAME_NESTED_NESTED, ".", ".", FILENAME_NESTED_NESTED)));
    }

    public void testCombiningRelativeResolvers() throws Exception
    {
        RelativeFileResolver r1 = new RelativeFileResolver(PATH_NESTED, delegate);
        RelativeFileResolver r2 = new RelativeFileResolver(getPath(DIRNAME_NESTED_NESTED, FILENAME_NESTED_NESTED), r1);
        assertSame(INPUT_NESTED, r2.resolve(getPath("..", FILENAME_NESTED)));
    }

    public void testAbsolutePathToTop() throws Exception
    {
        RelativeFileResolver relative = new RelativeFileResolver(PATH_NESTED, delegate);
        assertSame(INPUT_TOP, relative.resolve("/" + PATH_TOP));
    }

    public void testAbsolutePathToNestedNested() throws Exception
    {
        RelativeFileResolver relative = new RelativeFileResolver(PATH_NESTED, delegate);
        assertSame(INPUT_NESTED_NESTED, relative.resolve("/" + PATH_NESTED_NESTED));
    }
}
