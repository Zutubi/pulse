package com.zutubi.pulse.core.dependency.ivy;

import com.zutubi.util.StringUtils;
import com.zutubi.util.WebUtils;
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
    /**
     * Encode the string.
     *
     * @param str   the string to be encoded.
     *
     * @return the encoded string.
     */
    public static String encode(String str)
    {
        return WebUtils.encode('$', str, AllowedCharacters.NAMES);
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
        return WebUtils.decode('$', str);
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
        String[] encoded = new String[names.length];
        for (int i = 0; i < names.length; i++)
        {
            encoded[i] = encode(names[i]);
        }
        return encoded;
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
        String[] decoded = new String[names.length];
        for (int i = 0; i < names.length; i++)
        {
            decoded[i] = decode(names[i]);
        }
        return decoded;
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
        Map<String, String> encoded = new HashMap<String, String>();
        for (String key : extraAttributes.keySet())
        {
            // Note: ivy can be inconsistent with the contents of there extra attributes maps.
            // some will have the attribute prefix stripped, others will not.
            if (key.equals(EXTRA_ATTRIBUTE_STAGE) || key.equals(STAGE))
            {
                encoded.put(key, encode(extraAttributes.get(key)));
            }
            else
            {
                encoded.put(key, extraAttributes.get(key));
            }
        }
        return encoded;
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
        Map<String, String> decoded = new HashMap<String, String>();
        for (String key : extraAttributes.keySet())
        {
            // Note: ivy can be inconsistent with the contents of there extra attributes maps.
            // some will have the attribute prefix stripped, others will not.
            if (key.equals(EXTRA_ATTRIBUTE_STAGE) || key.equals(STAGE))
            {
                decoded.put(key, decode(extraAttributes.get(key)));
            }
            else
            {
                decoded.put(key, extraAttributes.get(key));
            }
        }
        return decoded;
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
        // there are two types of artifacts that we encounter. MDArtifacts and DefaultArtifacts.
        // The MDArtifact is a special case that has a reference to the underlying descriptor.
        // We do what we can to retain that reference.
        if (artifact instanceof MDArtifact)
        {
            try
            {
                ModuleDescriptor descriptor = (ModuleDescriptor) ReflectionUtils.getFieldValue(artifact, "md");
                MDArtifact encodedArtifact = new MDArtifact(
                        descriptor,
                        encode(artifact.getName()),
                        encode(artifact.getType()),
                        encode(artifact.getExt()),
                        artifact.getUrl(),
                        encode(artifact.getExtraAttributes())
                );
                for (String conf : artifact.getConfigurations())
                {
                    encodedArtifact.addConfiguration(encode(conf));
                }
                return encodedArtifact;
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
                    encode(artifact.getModuleRevisionId()),
                    artifact.getPublicationDate(),
                    encode(artifact.getName()),
                    encode(artifact.getType()),
                    encode(artifact.getExt()),
                    encode(artifact.getExtraAttributes())
            );
        }
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
        // there are two types of artifacts that we encounter. MDArtifacts and DefaultArtifacts.
        // The MDArtifact is a special case that has a reference to the underlying descriptor.
        // We do what we can to retain that reference.
        if (artifact instanceof MDArtifact)
        {
            try
            {
                ModuleDescriptor descriptor = (ModuleDescriptor) ReflectionUtils.getFieldValue(artifact, "md");
                MDArtifact decodedArtifact = new MDArtifact(
                        descriptor,
                        decode(artifact.getName()),
                        decode(artifact.getType()),
                        decode(artifact.getExt()),
                        artifact.getUrl(),
                        decode(artifact.getExtraAttributes())
                );
                for (String conf : artifact.getConfigurations())
                {
                    decodedArtifact.addConfiguration(decode(conf));
                }
                return decodedArtifact;
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
                    decode(artifact.getModuleRevisionId()),
                    artifact.getPublicationDate(),
                    decode(artifact.getName()),
                    decode(artifact.getType()),
                    decode(artifact.getExt()),
                    decode(artifact.getExtraAttributes())
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
        DefaultDependencyDescriptor encoded = new DefaultDependencyDescriptor(null,
                encode(dependency.getDependencyRevisionId()),
                dependency.isForce(),
                dependency.isChanging(),
                dependency.isTransitive());

        for (String moduleConfiguration : dependency.getModuleConfigurations())
        {
            String[] confs = dependency.getDependencyConfigurations(moduleConfiguration);
            for (String conf : confs)
            {
                // The all stages conf is a special case that we ignore.
                if (conf.equals(ALL_STAGES))
                {
                    encoded.addDependencyConfiguration(moduleConfiguration, ALL_STAGES);
                }
                else
                {
                    encoded.addDependencyConfiguration(moduleConfiguration, encodeStageNames(STAGE_NAME_GLUE, conf));
                }
            }
        }
        return encoded;
    }

    /**
     * Decode the dpendency descriptor
     *
     * @param dependency    the dependency to be decoded.
     * @return the decoded copy of the dependency descriptor.
     */
    public static DependencyDescriptor decode(DependencyDescriptor dependency)
    {
        DefaultDependencyDescriptor decoded = new DefaultDependencyDescriptor(null,
                decode(dependency.getDependencyRevisionId()),
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
                    decoded.addDependencyConfiguration(moduleConfiguration, ALL_STAGES);
                }
                else
                {
                    decoded.addDependencyConfiguration(moduleConfiguration, decodeStageNames(STAGE_NAME_GLUE, conf));
                }
            }
        }
        return decoded;
    }

    /**
     * Decode the content of the artifact download report.
     *
     * @param report    the artifact report to be decoded.
     * @return the decoded copy of the artifact report.
     */
    public static ArtifactDownloadReport decode(ArtifactDownloadReport report)
    {
        ArtifactDownloadReport decoded = new ArtifactDownloadReport(decode(report.getArtifact()));
        decoded.setDownloadDetails(report.getDownloadDetails());
        decoded.setDownloadStatus(report.getDownloadStatus());
        decoded.setArtifactOrigin(report.getArtifactOrigin());
        decoded.setDownloadTimeMillis(report.getDownloadTimeMillis());
        decoded.setSize(report.getSize());
        decoded.setLocalFile(report.getLocalFile());
        return decoded;
    }

    /**
     * Encode the module revision id.
     *
     * @param mrid  the module revision id to be encoded.
     * @return the encoded copy of the module revision id.
     */
    public static ModuleRevisionId encode(ModuleRevisionId mrid)
    {
        return ModuleRevisionId.newInstance(
                encode(mrid.getOrganisation()),
                encode(mrid.getName()),
                mrid.getBranch(),
                mrid.getRevision(),
                encode(mrid.getExtraAttributes()));
    }

    /**
     * Decode the module revision id.
     *
     * @param mrid  the module revision id to be decoded.
     * @return the decoded copy of the module revision id
     */
    public static ModuleRevisionId decode(ModuleRevisionId mrid)
    {
        return ModuleRevisionId.newInstance(
                decode(mrid.getOrganisation()),
                decode(mrid.getName()),
                mrid.getBranch(),
                mrid.getRevision(),
                decode(mrid.getExtraAttributes()));
    }

    private static String encodeStageNames(String glue, String in)
    {
        String[] encoded;
        if (in.contains(glue))
        {
            encoded = encodeNames(StringUtils.split(in, glue.charAt(0), true));
        }
        else
        {
            encoded = new String[]{encode(in)};
        }
        return StringUtils.join(glue, encoded);
    }

    private static String decodeStageNames(String glue, String in)
    {
        String[] decoded;
        if (in.contains(glue))
        {
            decoded = decodeNames(StringUtils.split(in, glue.charAt(0), true));
        }
        else
        {
            decoded = new String[]{decode(in)};
        }
        return StringUtils.join(glue, decoded);
    }
}
