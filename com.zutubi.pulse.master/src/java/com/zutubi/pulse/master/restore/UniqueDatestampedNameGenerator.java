package com.zutubi.pulse.master.restore;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 *
 */
public class UniqueDatestampedNameGenerator implements ArchiveNameGenerator
{
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss");

    /**
     * Generate a unique filename for an archive file that will be placed into the target directory.
     *
     * @param target directory into which the archive file will be placed.
     *
     * @return the generated file name.
     */
    public String newName(File target)
    {
        //TODO: internationalise the 'archive' string and the calendar locale.
        String candidateName = "archive-" + DATE_FORMAT.format(Calendar.getInstance().getTime());
        File candidate = new File(target, candidateName);
        int i = 1;
        while (candidate.isFile())
        {
            candidate = new File(target, candidate + "_" + i);
            i++;
        }
        return candidate.getName();
    }
}
