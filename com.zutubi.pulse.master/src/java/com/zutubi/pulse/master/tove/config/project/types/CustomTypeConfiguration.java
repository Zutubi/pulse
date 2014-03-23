package com.zutubi.pulse.master.tove.config.project.types;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.zutubi.pulse.core.PulseScope;
import com.zutubi.pulse.core.engine.FixedPulseFileProvider;
import com.zutubi.pulse.core.engine.ProjectRecipesConfiguration;
import com.zutubi.pulse.core.engine.PulseFileProvider;
import com.zutubi.pulse.core.engine.marshal.PulseFileLoader;
import com.zutubi.pulse.core.engine.marshal.PulseFileLoaderFactory;
import com.zutubi.pulse.core.marshal.ImportingNotSupportedFileResolver;
import com.zutubi.pulse.core.marshal.ParseException;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.TextArea;
import com.zutubi.tove.annotations.Transient;
import com.zutubi.tove.annotations.Wire;
import com.zutubi.util.StringUtils;
import com.zutubi.util.bean.DefaultObjectFactory;
import com.zutubi.util.logging.Logger;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * A pulse file project where the pulse file is edited directly in Pulse
 * itself.
 */
@SymbolicName("zutubi.customTypeConfig")
@Wire
public class CustomTypeConfiguration extends TypeConfiguration implements Validateable
{
    private static final Logger LOG = Logger.getLogger(CustomTypeConfiguration.class);

    private static final String VALIDATION_CACHE_SPEC = System.getProperty("pulse.custom.type.validation.cache.spec", "maximumSize=8192");
    private static final Cache<HashCode, List<String>> VALIDATION_CACHE = CacheBuilder.from(VALIDATION_CACHE_SPEC).build();
    private static final HashFunction VALIDATION_CACHE_HASH_FUNCTION = Hashing.goodFastHash(32);

    @TextArea(rows = 30, cols = 80)
    private String pulseFileString;
    @Transient
    private PulseFileLoaderFactory fileLoaderFactory;

    public PulseFileProvider getPulseFile()
    {
        return new FixedPulseFileProvider(pulseFileString);
    }

    public String getPulseFileString()
    {
        return pulseFileString;
    }

    public void setPulseFileString(String pulseFileString)
    {
        this.pulseFileString = pulseFileString;
    }

    public void validate(ValidationContext context)
    {
        if (!StringUtils.stringSet(pulseFileString))
        {
            context.addFieldError("pulseFileString", "pulse file is required");
            return;
        }

        if (fileLoaderFactory == null)
        {
            // This happens when we are validated during startup.  Although
            // it feels a bit sloppy, actually the only time it is really
            // important to do the next validation is when the user is making
            // changes.
            return;
        }

        // CIB-3100: Custom pulse file validation is expensive.  It's also likely (via templating,
        // and instance cache refreshing) that we'll re-validate the same Pulse file XML many
        // times.  So we cache the results of a validate, keyed by a hash of the Pulse file string
        // (to keep memory requirements down).
        final byte[] bytes = pulseFileString.getBytes();
        try
        {
            List<String> errors = VALIDATION_CACHE.get(VALIDATION_CACHE_HASH_FUNCTION.hashBytes(bytes), new Callable<List<String>>()
            {
                public List<String> call() throws Exception
                {
                    List<String> result = new ArrayList<String>();
                    try
                    {
                        PulseFileLoader loader = fileLoaderFactory.createLoader();
                        loader.setObjectFactory(new DefaultObjectFactory());

                        loader.load(new ByteArrayInputStream(bytes), new ProjectRecipesConfiguration(), new PulseScope(), new ImportingNotSupportedFileResolver(), new CustomProjectValidationInterceptor());
                    }
                    catch (ParseException pe)
                    {
                        result.add(pe.getMessage());
                        if (pe.getLine() > 0)
                        {
                            String line = StringUtils.getLine(pulseFileString, pe.getLine());
                            if (line != null)
                            {
                                result.add("First line of offending element: " + line);
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        result.add(e.getMessage());
                    }

                    if (result.size() == 0)
                    {
                        // The expected case, so share the single empty list to save memory.
                        return Collections.emptyList();
                    }
                    else
                    {
                        return result;
                    }
                }
            });

            for (String error: errors)
            {
                context.addActionError(error);
            }
        }
        catch (ExecutionException e)
        {
            LOG.severe(e);
            context.addActionError("Unable to validate: " + e.getMessage());
        }
    }

    public void setFileLoaderFactory(PulseFileLoaderFactory fileLoaderFactory)
    {
        this.fileLoaderFactory = fileLoaderFactory;
    }
}
