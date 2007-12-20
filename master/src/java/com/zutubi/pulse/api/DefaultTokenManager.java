package com.zutubi.pulse.api;

import com.zutubi.pulse.model.AcegiUser;
import com.zutubi.pulse.model.GrantedAuthority;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.UserManager;
import com.zutubi.pulse.prototype.config.group.ServerPermission;
import com.zutubi.pulse.security.AcegiUtils;
import com.zutubi.util.Constants;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 */
public class DefaultTokenManager implements TokenManager
{
    private int loginCount = 0;
    private Set<String> validTokens = new TreeSet<String>();
    private UserManager userManager;
    private AuthenticationManager authenticationManager;
    private AdminTokenManager adminTokenManager;

    public synchronized String login(String username, String password) throws AuthenticationException
    {
        return login(username, password, Constants.MINUTE * 30);
    }

    /**
     * Init method used to initialise any post creation resource requirements.
     */
    public void init()
    {

    }

    public synchronized String login(String username, String password, long expiry) throws AuthenticationException
    {
        checkTokenAccessEnabled();

        if (++loginCount % 1000 == 0)
        {
            checkForExpiredTokens();
        }

        try
        {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            logoutUser();

            // Generate a token which is good for 30 minutes for this user
            long expiryTime = System.currentTimeMillis() + expiry;
            String signatureValue = getTokenSignature(username, expiryTime, password);
            String tokenValue = username + ":" + expiryTime + ":" + signatureValue;
            String encoded = new String(Base64.encodeBase64(tokenValue.getBytes()));

            validTokens.add(encoded);

            return encoded;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new AuthenticationException(e.getMessage());
        }
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

    public User verifyAdmin(String token) throws AuthenticationException
    {
        return verifyRoleIn(token, ServerPermission.ADMINISTER.toString());
    }

    public User verifyUser(String token) throws AuthenticationException
    {
        return verifyRoleIn(token, GrantedAuthority.USER);
    }

    public User verifyRoleIn(String token, String... allowedAuthorities) throws AuthenticationException
    {
        // if the token is the admin token, then we are happy.
        if(checkAdminToken(token))
        {
            return null;
        }

        User user = verifyToken(token);
        AcegiUser principle = userManager.getPrinciple(user);
        for (org.acegisecurity.GrantedAuthority authority : principle.getAuthorities())
        {
            for (String allowedAuthority : allowedAuthorities)
            {
                if (authority.getAuthority().equals(allowedAuthority))
                {
                    return user;
                }
            }
        }

        throw new AuthenticationException("Access denied");
    }

    private boolean checkAdminToken(String token)
    {
        return adminTokenManager != null && adminTokenManager.checkAdminToken(token);
    }

    public User loginUser(String token) throws AuthenticationException
    {
        User user = verifyToken(token);
        AcegiUser principle = userManager.getPrinciple(user);
        AcegiUtils.loginAs(principle);
        return user;
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

    private void checkTokenAccessEnabled() throws AuthenticationException
    {
        if (userManager == null)
        {
            throw new AuthenticationException("Token based login disabled.");
        }
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    public void setAuthenticationManager(AuthenticationManager authenticationManager)
    {
        this.authenticationManager = authenticationManager;
    }

    public void setAdminTokenManager(AdminTokenManager adminTokenManager)
    {
        this.adminTokenManager = adminTokenManager;
    }
}
