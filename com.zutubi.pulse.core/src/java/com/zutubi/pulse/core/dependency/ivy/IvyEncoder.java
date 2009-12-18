package com.zutubi.pulse.core.dependency.ivy;

import com.zutubi.util.StringUtils;
import com.zutubi.util.WebUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.reflection.ReflectionUtils;
import org.apache.ivy.core.module.descriptor.*;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;

import java.util.HashMap;
import java.util.Map;

import static com.zutubi.pulse.core.dependency.ivy.IvyModuleDescriptor.*;

/**
 * A utility class that handles the encoding and decoding needed within
 * this ivy integration.
 * <p/>
 * The dependency management process based on ivy has some special requirements with
 * regards to the characters used for configuration names.
 * <p/>
 * The Pulse stage names are used for both ivy config names / patterns and for the
 * url path at which there artifacts are stored.  To ensure that there are no problems
 * with any of these processes, all pulse stage names (which are unrestricted in what
 * characters they contain) must be encoded before being used.
 */
public class IvyEncoder
{
    private static final char ENCODING_MARKER = '$';

    private static final Mapping<String, String> ENCODE_MAPPING = new EncodeMapping();
    private static final Mapping<String, String> DECODE_MAPPING = new DecodeMapping();

    /**
     * Encode the string.
     *
     * @param str   the string to be encoded.
     *
     * @return the encoded string.
     */
    public static String encode(String str)
    {
        return WebUtils.encode(ENCODING_MARKER, str, AllowedCharacters.NAMES);
    }

    /**
     * Decode the string.
     *
     * @param str   the string to be decoded.
     *
     * @return the decoded string.
     */
    public static String decode(String str)
    {
        return WebUtils.decode(ENCODING_MARKER, str);
    }

    /**
     * Encode the array of names.
     *
     * @param names the names to be encoded.
     * @return an array of encoded names
     *
     * @see #encode(String)
     */
    public static String[] encodeNames(String... names)
    {
        return map(ENCODE_MAPPING, names);
    }

    /**
     * Decode an array of names.
     *
     * @param names    the names to be decoded.
     *
     * @return an array of encoded names
     */
    public static String[] decodeNames(String... names)
    {
        return map(DECODE_MAPPING, names);
    }

    private static String[] map(Mapping<String, String> mapping, String... names)
    {
        String[] mapped = new String[names.length];
        for (int i = 0; i < names.length; i++)
        {
            mapped[i] = mapping.map(names[i]);
        }
        return mapped;
    }

    /**
     * Encode the contents of the extra attributes map.  In particular,
     * encode the {@link IvyModuleDescriptor#EXTRA_ATTRIBUTE_STAGE} according
     * to the stage encoding rules.
     *
     * @param extraAttributes   the map to be encoded.
     *
     * @return the encoded copy of the map.
     */
    public static Map<String, String> encode(Map<String, String> extraAttributes)
    {
        return map(extraAttributes, ENCODE_MAPPING);
    }

    /**
     * Decode the contents of the extra attributes map.  In particular, decode the
     * {@link IvyModuleDescriptor#EXTRA_ATTRIBUTE_STAGE} according the stage decoding
     * rules.
     *
     * @param extraAttributes   the map to be decoded.
     * @return the decoded copy of the map.
     */
    public static Map<String, String> decode(Map<String, String> extraAttributes)
    {
        return map(extraAttributes, DECODE_MAPPING);
    }

    private static Map<String, String> map(Map<String, String> extraAttributes, Mapping<String, String> mapping)
    {
        Map<String, String> mapped = new HashMap<String, String>();
        for (String key : extraAttributes.keySet())
        {
            if (isStageName(key))
            {
                mapped.put(key, mapping.map(extraAttributes.get(key)));
            }
            else
            {
                mapped.put(key, extraAttributes.get(key));
            }
        }
        return mapped;
    }

    private static boolean isStageName(String key)
    {
        // Note: ivy can be inconsistent with the contents of there extra attributes maps.
        // some will have the attribute prefix stripped, others will not.
        return key.equals(EXTRA_ATTRIBUTE_STAGE) || key.equals(STAGE);
    }

    /**
     * Encode the contents of the artifact.
     *
     * Note that this is not an inplace encoding.
     *
     * @param artifact  the artifact to be encoded.
     * @return the encoded artifact.
     *
     * @see #decode(org.apache.ivy.core.module.descriptor.Artifact)
     */
    public static Artifact encode(Artifact artifact)
    {
        return map(artifact, ENCODE_MAPPING);
    }

    /**
     * Decode the contents of the artifact.
     *
     * Note that this is not an inplace decoding
     *
     * @param artifact  the artifact to be decoded.
     * @return the decoded artifact.
     *
     * @see #encode(org.apache.ivy.core.module.descriptor.Artifact)
     */
    public static Artifact decode(Artifact artifact)
    {
        return map(artifact, DECODE_MAPPING);
    }

    private static Artifact map(Artifact artifact, Mapping<String, String> mapping)
    {
        // there are two types of artifacts that we encounter. MDArtifacts and DefaultArtifacts.
        // The MDArtifact is a special case that has a reference to the underlying descriptor.
        // We do what we can to retain that reference.
        if (artifact instanceof MDArtifact)
        {
            try
            {
                ModuleDescriptor descriptor = (ModuleDescriptor) ReflectionUtils.getFieldValue(artifact, "md");
                MDArtifact mappedArtifact = new MDArtifact(
                        descriptor,
                        mapping.map(artifact.getName()),
                        mapping.map(artifact.getType()),
                        mapping.map(artifact.getExt()),
                        artifact.getUrl(),
                        map((Map<String, String>)artifact.getExtraAttributes(), mapping)
                );
                for (String conf : artifact.getConfigurations())
                {
                    mappedArtifact.addConfiguration(mapping.map(conf));
                }
                return mappedArtifact;
            }
            catch (Exception e)
            {
                // Something went wrong, most likely with extracting the descriptor.
                // Have we upgraded ivy recently?
                throw new RuntimeException(e);
            }
        }
        else
        {
            return new DefaultArtifact(
                    map(artifact.getModuleRevisionId(), mapping),
                    artifact.getPublicationDate(),
                    mapping.map(artifact.getName()),
                    mapping.map(artifact.getType()),
                    mapping.map(artifact.getExt()),
                    map((Map<String, String>)artifact.getExtraAttributes(), mapping)
            );
        }
    }

    /**
     * Encode the dependency descriptor.
     * 
     * @param dependency    the dependency to be encoded.
     * @return  the encoded copy of the dependency.
     */
    public static DependencyDescriptor encode(DependencyDescriptor dependency)
    {
        return map(dependency, ENCODE_MAPPING);
    }

    /**
     * Decode the dpendency descriptor
     *
     * @param dependency    the dependency to be decoded.
     * @return the decoded copy of the dependency descriptor.
     */
    public static DependencyDescriptor decode(DependencyDescriptor dependency)
    {
        return map(dependency, DECODE_MAPPING);
    }

    private static DependencyDescriptor map(DependencyDescriptor dependency, Mapping<String, String> mapping)
    {
        DefaultDependencyDescriptor mapped = new DefaultDependencyDescriptor(null,
                map(dependency.getDependencyRevisionId(), mapping),
                dependency.isForce(),
                dependency.isChanging(),
                dependency.isTransitive());

        for (String moduleConfiguration : dependency.getModuleConfigurations())
        {
            String[] confs = dependency.getDependencyConfigurations(moduleConfiguration);
            for (String conf : confs)
            {
                if (conf.equals(ALL_STAGES))
                {
                    mapped.addDependencyConfiguration(moduleConfiguration, ALL_STAGES);
                }
                else
                {
                    mapped.addDependencyConfiguration(moduleConfiguration, mapNames(STAGE_NAME_GLUE, conf, mapping));
                }
            }
        }
        return mapped;
    }

    /**
     * Decode the content of the artifact download report.
     *
     * @param report    the artifact report to be decoded.
     * @return the decoded copy of the artifact report.
     */
    public static ArtifactDownloadReport decode(ArtifactDownloadReport report)
    {
        return map(report, DECODE_MAPPING);
    }

    /**
     * Encode the content of the artifact download report.
     *
     * @param report    the artifact report to be encoded.
     * @return an encoded copy of the artifact report.
     */
    public static ArtifactDownloadReport encode(ArtifactDownloadReport report)
    {
        return map(report, ENCODE_MAPPING);
    }

    private static ArtifactDownloadReport map(ArtifactDownloadReport report, Mapping<String, String> mapping)
    {
        ArtifactDownloadReport mapped = new ArtifactDownloadReport(map(report.getArtifact(), mapping));
        mapped.setDownloadDetails(report.getDownloadDetails());
        mapped.setDownloadStatus(report.getDownloadStatus());
        mapped.setArtifactOrigin(report.getArtifactOrigin());
        mapped.setDownloadTimeMillis(report.getDownloadTimeMillis());
        mapped.setSize(report.getSize());
        mapped.setLocalFile(report.getLocalFile());
        return mapped;
    }

    /**
     * Encode the module revision id.
     *
     * @param mrid  the module revision id to be encoded.
     * @return the encoded copy of the module revision id.
     */
    public static ModuleRevisionId encode(ModuleRevisionId mrid)
    {
        return map(mrid, new EncodeMapping());
    }

    /**
     * Decode the module revision id.
     *
     * @param mrid  the module revision id to be decoded.
     * @return the decoded copy of the module revision id
     */
    public static ModuleRevisionId decode(ModuleRevisionId mrid)
    {
        return map(mrid, new DecodeMapping());
    }

    private static ModuleRevisionId map(ModuleRevisionId mrid, Mapping<String, String> mapping)
    {
        return ModuleRevisionId.newInstance(
                mapping.map(mrid.getOrganisation()),
                mapping.map(mrid.getName()),
                mrid.getBranch(),
                mrid.getRevision(),
                map(mrid.getExtraAttributes(), mapping));
    }

    private static String mapNames(String glue, String in, Mapping<String, String> mapping)
    {
        String[] mapped;
        if (in.contains(glue))
        {
            mapped = map(mapping, StringUtils.split(in, glue.charAt(0), true));
        }
        else
        {
            mapped = new String[]{mapping.map(in)};
        }
        return StringUtils.join(glue, mapped);
    }

    private static class EncodeMapping implements Mapping<String, String>
    {
        public String map(String decodedString)
        {
            return encode(decodedString);
        }
    }

    private static class DecodeMapping implements Mapping<String, String>
    {
        public String map(String encodedString)
        {
            return decode(encodedString);
        }
    }
}
