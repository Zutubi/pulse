package com.zutubi.pulse.master.notifications;

import com.zutubi.pulse.master.build.log.LogFile;
import com.zutubi.util.Predicate;
import com.zutubi.util.StringUtils;
import com.zutubi.util.WebUtils;
import org.apache.commons.io.input.AutoCloseInputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An attachment on a build result notification.
 */
public class NotificationAttachment
{
    private String filename;
    private LogFile logFile;
    private int lineLimit;

    /**
     * Creates a new attachment for the given log.
     *
     * @param filename  filename to give the attachment (may contain unsafe
     *                  characters, they will be escaped if necessary)
     * @param logFile   the log file to attach
     * @param lineLimit the maximum number of lines to attach (if the log is
     *                  larger only the tail is used)
     */
    public NotificationAttachment(String filename, LogFile logFile, int lineLimit)
    {
        this.filename = filename;
        this.logFile = logFile;
        this.lineLimit = lineLimit;
    }

    /**
     * Returns this attachment as a MIME-multipart body part, suitable for e.g.
     * email.
     *
     * @return this attachement in MIME form
     * @throws IOException if the attachment data cannot be opened
     * @throws MessagingException on any other error
     */
    public MimeBodyPart asBodyPart() throws IOException, MessagingException
    {
        MimeBodyPart bodyPart = new MimeBodyPart();
        if (lineLimit == 0)
        {
            bodyPart.setDataHandler(new DataHandler(new DataSource()
            {
                public InputStream getInputStream() throws IOException
                {
                    return new AutoCloseInputStream(logFile.openInputStream());
                }

                public OutputStream getOutputStream() throws IOException
                {
                    return null;
                }

                public String getContentType()
                {
                    return "text/plain";
                }

                public String getName()
                {
                    return null;
                }
            }));
        }
        else
        {
            bodyPart.setContent(logFile.getTail(lineLimit), "text/plain");
        }

        String safeFileName = WebUtils.encode('_', filename, new Predicate<Character>()
        {
            public boolean satisfied(Character c)
            {
                switch (c)
                {
                    case '-':
                    case '.':
                        return true;
                    default:
                        return StringUtils.isAsciiAlphaNumeric(c);
                }
            }
        });
        bodyPart.setFileName(safeFileName);
        return bodyPart;
    }
}
