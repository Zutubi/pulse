package com.zutubi.pulse.api;

import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.model.GrantedAuthority;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.UserManager;
import com.zutubi.pulse.util.Constants;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.RandomUtils;
import com.zutubi.pulse.util.logging.Logger;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.providers.encoding.PasswordEncoder;
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
public class TokenManager extends AdminTokenManager
{
    private static final Logger LOG = Logger.getLogger(TokenManager.class);

    private int loginCount = 0;
    private Set<String> validTokens = new TreeSet<String>();
    private UserManager userManager;
    private PasswordEncoder passwordEncoder;

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

        if (!passwordEncoder.isPasswordValid(user.getPassword(), password,  null))
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
        if(checkAdminToken(token))
        {
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

    public void loginUser(String token) throws AuthenticationException
    {
        User user = verifyToken(token);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities()));
    }

    public void logoutUser()
    {
        SecurityContextHolder.getContext().setAuthentication(null);
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
     * @param passwordEncoder
     */
    public void setPasswordEncoder(PasswordEncoder passwordEncoder)
    {
        this.passwordEncoder = passwordEncoder;
    }

    private void checkTokenAccessEnabled() throws AuthenticationException
    {
        if (userManager == null)
        {
            throw new AuthenticationException("Token based login disabled.");
        }
    }

}
