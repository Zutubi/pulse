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

package com.zutubi.pulse.core.scm.hg;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.scm.PersistentContextImpl;
import com.zutubi.pulse.core.scm.RecordingScmFeedbackHandler;
import com.zutubi.pulse.core.scm.ScmContextImpl;
import com.zutubi.pulse.core.scm.api.PersistentContext;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.core.util.PulseZipUtils;

import java.io.File;
import java.net.URL;

public abstract class MercurialTestBase extends PulseTestCase
{
    protected static final String REVISION_DEFAULT_LATEST = "fe4571fd8bad5d556b26d1a05806074e67bbfa97";
    protected static final String REVISION_DEFAULT_PREVIOUS = "60d7c7d4e20cbb29e5bf9b56651fd9cd0255e3be";
    protected static final String REVISION_DEFAULT_TWO_PREVIOUS = "0040f780ba9a5905d059d26d777bb6cd78cdb96f";
    protected static final String REVISION_BRANCH_LATEST = "04010fd8851efebac7f36ad39d246a4970806109";
    protected static final String REVISION_BRANCH_PREVIOUS = "867f406a6a399c66e7f6a16e3a0a292b03484404";
    protected static final String REVISION_BRANCH_TWO_PREVIOUS = "805df7c8e75b6d9e2739a74fb5c69f35bdf9be3d";
    protected static final String REVISION_MULTILINE = "7e57b6bda144ef2688363ad58d250460cf6a422c";
    protected static final String REVISION_MULTILINE_PREVIOUS = "5ee4cf0fdf08fedc05a483253b087243481728dd";
    
    protected static final String BRANCH = "1.0";
    
    protected static final String CONTENT_FILE_PATH = "exercise1/hello.c";
    
    protected static final String CONTENT_DEFAULT_LATEST = "#include <stdio.h>\n" +
            "\n" +
            "int main(int argc, char **argv)\n" +
            "{\n" +
            "    int i;\n" +
            "    for (i = 1; i < argc; i++)\n" +
            "    {\n" +
            "        printf(\"Why hello there, %s!\\n\", argv[i]);\n" +
            "    }\n" +
            "\n" +
            "    return 0;\n" +
            "}\n";
    protected static final String CONTENT_DEFAULT_PREVIOUS = "#include <stdio.h>\n" +
            "\n" +
            "int main(int argc, char **argv)\n" +
            "{\n" +
            "    if (argc < 2)\n" +
            "    {\n" +
            "        fprintf(stderr, \"Usage: %s <name>\\n\", argv[0]);\n" +
            "        return 1;\n" +
            "    }\n" +
            "\n" +
            "    printf(\"Why hello there, %s!\\n\", argv[1]);\n" +
            "    return 0;\n" +
            "}\n";
    protected static final String CONTENT_DEFAULT_TWO_PREVIOUS = "#include <stdio.h>\n" +
            "\n" +
            "int main(int argc, char *argv[])\n" +
            "{\n" +
            "    if (argc < 2)\n" +
            "    {\n" +
            "        fprintf(stderr, \"Usage: %s <name>\\n\", argv[0]);\n" +
            "        return 1;\n" +
            "    }\n" +
            "\n" +
            "    printf(\"Why hello there, %s!\\n\", argv[1]);\n" +
            "    return 0;\n" +
            "}\n";
    protected static final String CONTENT_BRANCH_LATEST = "#include <stdio.h>\n" +
            "\n" +
            "int main(int argc, char **argv)\n" +
            "{\n" +
            "    if (argc < 2)\n" +
            "    {\n" +
            "        fprintf(stderr, \"Usage: %s <name>\\n\", argv[0]);\n" +
            "        return 1;\n" +
            "    }\n" +
            "\n" +
            "    printf(\"Hello, %s!\\n\", argv[1]);\n" +
            "    return 0;\n" +
            "}\n";
    protected static final String CONTENT_BRANCH_PREVIOUS = "#include <stdio.h>\n" +
            "\n" +
            "int main(int argc, char *argv[])\n" +
            "{\n" +
            "    if (argc < 2)\n" +
            "    {\n" +
            "        fprintf(stderr, \"Usage: %s <name>\\n\", argv[0]);\n" +
            "        return 1;\n" +
            "    }\n" +
            "\n" +
            "    printf(\"Hello, %s!\\n\", argv[1]);\n" +
            "    return 0;\n" +
            "}\n";
    
    protected static final String OUTPUT_NO_UPDATES = "0 files updated, 0 files merged, 0 files removed, 0 files unresolved";
    protected static final String OUTPUT_ONE_UPDATE = "1 files updated, 0 files merged, 0 files removed, 0 files unresolved";
    
    protected File tmp;
    protected File repositoryBase;
    protected File baseDir;
    protected String repository;
    protected PulseExecutionContext buildContext;
    protected RecordingScmFeedbackHandler handler;
    protected ScmContextImpl scmContext;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = createTempDirectory();

        URL url = getClass().getResource("MercurialTestBase.repo.zip");
        PulseZipUtils.extractZip(new File(url.toURI()), new File(tmp, "repo"));

        repositoryBase = new File(tmp, "repo");
        repository = repositoryBase.getCanonicalPath();

        baseDir = new File(tmp, "base");
        buildContext = new PulseExecutionContext();
        buildContext.setWorkingDir(baseDir);

        File persistentWorkingDir = new File(tmp, "scm");
        assertTrue(persistentWorkingDir.mkdir());
        PersistentContext persistentContext = new PersistentContextImpl(persistentWorkingDir);
        scmContext = new ScmContextImpl(persistentContext, new PulseExecutionContext());

        handler = new RecordingScmFeedbackHandler();
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(tmp);
        super.tearDown();
    }
}
