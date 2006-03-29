package com.cinnamonbob.api;

import com.cinnamonbob.bootstrap.ConfigurationManager;
import com.cinnamonbob.core.util.Constants;
import com.cinnamonbob.core.util.FileSystemUtils;
import com.cinnamonbob.core.util.RandomUtils;
import com.cinnamonbob.model.GrantedAuthority;
import com.cinnamonbob.model.User;
import com.cinnamonbob.model.UserManager;
import com.cinnamonbob.util.logging.Logger;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 */
public class TokenManager
{
    private static final Logger LOG = Logger.getLogger(TokenManager.class);
    private static final String TOKEN_FILE = "admin.token";

    private int loginCount = 0;
    /**
     * A random token that allows a single admin login: the token is changed
     * each time it it used.
     */
    private String adminToken;
    private Set<String> validTokens = new TreeSet<String>();
    private UserManager userManager;
    private ConfigurationManager configurationManager;


    public static File getAdminTokenFilename(ConfigurationManager configurationManager)
    {
        File configPath = configurationManager.getSystemPaths().getConfigRoot();
        return new File(configPath, TOKEN_FILE);
    }

    public synchronized String login(String username, String password) throws AuthenticationException
    {
        return login(username, password, Constants.MINUTE * 30);
    }

    public synchronized String login(String username, String password, long expiry) throws AuthenticationException
    {
        checkTokenAccessEnabled();

        if (++loginCount % 1000 == 0)
        {
            checkForExpiredTokens();
        }

        User user = userManager.getUser(username);
        if (user == null)
        {
            // This allows guessing of usernames: do we care?
            throw new AuthenticationException("Invalid username");
        }

        if (!password.equals(user.getPassword()))
        {
            throw new AuthenticationException("Invalid password");
        }

        // Generate a token which is good for 30 minutes for this user
        long expiryTime = System.currentTimeMillis() + expiry;
        String signatureValue = getTokenSignature(username, expiryTime, password);
        String tokenValue = username + ":" + expiryTime + ":" + signatureValue;
        String encoded = new String(Base64.encodeBase64(tokenValue.getBytes()));

        validTokens.add(encoded);

        return encoded;
    }

    public synchronized boolean logout(String token)
    {
        try
        {
            verifyToken(token);
        }
        catch (AuthenticationException e)
        {
            return false;
        }

        validTokens.remove(token);
        return true;
    }

    public void verifyAdmin(String token) throws AuthenticationException
    {
        verifyRoleIn(token, GrantedAuthority.ADMINISTRATOR);
    }

    public void verifyUser(String token) throws AuthenticationException
    {
        verifyRoleIn(token, GrantedAuthority.USER);
    }

    public void verifyRoleIn(String token, String... allowedAuthorities) throws AuthenticationException
    {
        if (token.equals(adminToken))
        {
            // Matches the sigle-use admin token.  Allow login and generate a
            // new token.
            newRandomToken();
            return;
        }

        User user = verifyToken(token);
        for (GrantedAuthority authority : user.getAuthorities())
        {
            for (String allowedAuthority : allowedAuthorities)
            {
                if (authority.getAuthority().equals(allowedAuthority))
                {
                    return;
                }
            }
        }

        throw new AuthenticationException("Access denied");
    }

    private synchronized User verifyToken(String token) throws AuthenticationException
    {
        checkTokenAccessEnabled();

        if (!validTokens.contains(token))
        {
            throw new AuthenticationException("Invalid token");
        }

        String username;
        try
        {
            username = verifyExpiry(token);
        }
        catch (AuthenticationException e)
        {
            validTokens.remove(token);
            throw e;
        }

        User user = userManager.getUser(username);

        // This can happen if the user is removed in the mean time
        if (user == null)
        {
            throw new AuthenticationException("Unknown user");
        }

        return user;
    }

    private synchronized void checkForExpiredTokens()
    {
        List<String> expiredTokens = new LinkedList<String>();

        for (String token : validTokens)
        {
            try
            {
                verifyExpiry(token);
            }
            catch (AuthenticationException e)
            {
                expiredTokens.add(token);
            }
        }

        for (String token : expiredTokens)
        {
            validTokens.remove(token);
        }
    }

    private String verifyExpiry(String token) throws AuthenticationException
    {
        // Token cannot have a bad format or expiry as it was found in
        // validTokens.  We don't even have to verify the signature
        // separately!
        String decoded = new String(Base64.decodeBase64(token.getBytes()));
        String parts[] = decoded.split(":");

        long expiry = Long.parseLong(parts[1]);
        if (System.currentTimeMillis() > expiry)
        {
            throw new AuthenticationException("Token expired");
        }

        return parts[0];
    }

    private String getTokenSignature(String username, long expiryTime, String password)
    {
        return DigestUtils.md5Hex(username + ":" + expiryTime + ":" + password);
    }

    private void newRandomToken()
    {
        File tokenFile = getAdminTokenFilename(configurationManager);
        adminToken = RandomUtils.randomString(128);
        try
        {
            FileSystemUtils.createFile(tokenFile, adminToken);
        }
        catch (IOException e)
        {
            LOG.severe("Unable to write admin token file: " + e.getMessage(), e);
        }
    }

    /**
     * Required resource.
     *
     * @param userManager
     */
    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    /**
     * Required resource.
     *
     * @param configurationManager
     */
    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    /**
     * Init method. Make sure that we have an admin token available for use by
     * the command line.
     */
    public void init()
    {
        newRandomToken();
    }

    private void checkTokenAccessEnabled() throws AuthenticationException
    {
        if (userManager == null)
        {
            throw new AuthenticationException("Token based login disabled.");
        }
    }
}
