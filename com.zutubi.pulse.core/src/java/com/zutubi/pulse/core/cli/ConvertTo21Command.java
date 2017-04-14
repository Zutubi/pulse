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

package com.zutubi.pulse.core.cli;

import com.google.common.io.Files;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.command.BootContext;
import com.zutubi.pulse.command.Command;
import com.zutubi.pulse.core.upgrade.PulseFileToToveFile;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

/**
 * Commmand line interface for the 2.0->2.1 pulse file converter.
 */
public class ConvertTo21Command implements Command
{
    private static final Messages I18N = Messages.getInstance(ConvertTo21Command.class);

    public int execute(BootContext bootContext) throws Exception
    {
        String[] argv = bootContext.getCommandArgv();
        if (argv.length != 2)
        {
            fatal("Usage: convertTo21 <input file> <output file>");
        }

        File inputFile = new File(argv[0]);
        if (!inputFile.isFile())
        {
            fatal("Input file '" + inputFile.getPath() + "' does not exist");
        }

        String converted = PulseFileToToveFile.convert(new FileInputStream(inputFile));
        File outputFile = new File(argv[1]);
        Files.write(converted, outputFile, Charset.defaultCharset());

        return 0;
    }

    private void fatal(String message)
    {
        System.err.println(message);
        System.exit(1);
    }

    public String getHelp()
    {
        return I18N.format("command.help");
    }

    public String getDetailedHelp()
    {
        return I18N.format("command.detailed.help");
    }

    public List<String> getUsages()
    {
        return asList("<input file> <output file>");
    }

    public List<String> getAliases()
    {
        return asList("to21");
    }

    public Map<String, String> getOptions()
    {
        return Collections.emptyMap();
    }

    public boolean isDefault()
    {
        return false;
    }
}
