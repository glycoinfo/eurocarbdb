/*
*   EuroCarbDB, a framework for carbohydrate bioinformatics
*
*   Copyright (c) 2006-2009, Eurocarb project, or third-party contributors as
*   indicated by the @author tags or express copyright attribution
*   statements applied by the authors.  
*
*   This copyrighted material is made available to anyone wishing to use, modify,
*   copy, or redistribute it subject to the terms and conditions of the GNU
*   Lesser General Public License, as published by the Free Software Foundation.
*   A copy of this license accompanies this distribution in the file LICENSE.txt.
*
*   This program is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
*   or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
*   for more details.
*
*   Last commit: $Rev: 1549 $ by $Author: glycoslave $ on $Date:: 2009-07-19 #$  
*/
package org.eurocarbdb.action.user;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
//import org.openid4java.server.RealmVerifier;

import com.opensymphony.webwork.views.util.UrlHelper;

import org.apache.log4j.Logger;

/**
* Utility class for authenticating a user's OpenId credentials.
* 
* <i>The meat of this code was taken from http://code.google.com/p/openid4java/wiki/SampleConsumer.
* I only modified it to fix some syntax errors and make it suit my needs.</i>
* 
* @author zechariahs
*/
public class OpenIdAuthenticator
{

    /** Logging handle. */
    static final Logger log = Logger.getLogger( OpenIdAuthenticator.class );

    /**
     * The manager used to authenticate a user.  The same manager 
     * must be used when making the authentication request, and 
     * authenticating the result.
     */
    private ConsumerManager m_oManager;
    
    /**
     * The URL the OpenID provider should send its response to.
     */
    public static final String RETURN_ACTION = "/acceptLogin.action";
    
    // Name of this openId user
    private String name = null;

    // Email address of openId user
    private String email = null;
    
    /**
     * Creates a new instance of OpenIdAuthenticator.
     */
    public OpenIdAuthenticator()
    {
        try
        {
            m_oManager = new ConsumerManager();
        } 
        catch (ConsumerException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * Creates a new Authentication Request.
     * @param a_sOpenIdUrl The user's Open ID URL.
     * @param a_oRequest The request to use
     * @return The ActionForward used to forward a user to their OpenID provider.
     * @throws OpenIDException
     * @author zschwenk
     */
    public String createAuthenticationRequest(String a_sOpenIdUrl, HttpServletRequest a_oRequest) 
        throws OpenIDException
    {
        
        // Custom realm verifier - http://groups.google.com/group/openid4java/browse_thread/thread/ccd874fee1c0720c
        //RealmVerifier oRealmVerifier = new RealmVerifier();
        //m_oManager.setRealmVerifier(oRealmVerifier);
        
        // configure the return_to URL where your application will receive
        // the authentication responses from the OpenID provider
        String sReturnURL = UrlHelper.buildUrl(RETURN_ACTION, a_oRequest,null,null,a_oRequest.getScheme(),false,false,true);


        // --- Forward proxy setup (only if needed) ---
        // ProxyProperties proxyProps = new ProxyProperties();
        // proxyProps.setProxyName("proxy.example.com");
        // proxyProps.setProxyPort(8080);
        // HttpClientFactory.setProxyProperties(proxyProps);

        // perform discovery on the user-supplied identifier
        List discoveries = m_oManager.discover(a_sOpenIdUrl);

        // attempt to associate with the OpenID provider
        // and retrieve one service endpoint for authentication
        DiscoveryInformation discovered = m_oManager.associate(discoveries);

        // store the discovery information in the user's session
        a_oRequest.getSession().setAttribute("openid-disc", discovered);

        // obtain a AuthRequest message to be sent to the OpenID provider
        AuthRequest authReq = m_oManager.authenticate(discovered, sReturnURL);
        
        // Attribute Exchange example: fetching the 'email' attribute
        FetchRequest fetch = FetchRequest.createFetchRequest();
        fetch.addAttribute("email","http://axschema.org/contact/email",true);
        fetch.addAttribute("altemail","http://schema.openid.net/contact/email",true);
        fetch.addAttribute("first","http://axschema.org/namePerson/first",true);
        fetch.addAttribute("last","http://axschema.org/namePerson/last",true);
        fetch.addAttribute("username","http://axschema.org/namePerson/friendly",true);

        // attach the extension to the authentication request
        authReq.addExtension(fetch);

        // 
        // if (! discovered.isVersion2() )
        // {
        //     // Option 1: GET HTTP-redirect to the OpenID Provider endpoint
        //     // The only method supported in OpenID 1.x
        //     // redirect-URL usually limited ~2048 bytes
        //     
        //  afOpenId = new ActionForward(authReq.getDestinationUrl(true), true);
        // }
        // else
        // {
        //     // Option 2: HTML FORM Redirection (Allows payloads >2048 bytes)
        // 
        //  afOpenId = new ActionForward(authReq.getDestinationUrl(true), true);
        //     a_oRequest.getSession().setAttribute("parameterMap", authReq.getParameterMap());
        //     a_oRequest.getSession().setAttribute("destinationUrl", authReq.getDestinationUrl(true));
        // }

        return authReq.getDestinationUrl(true);
    }
    
    /**
     * Validates the authentication.
     * 
     * @param a_oRequest The request containing the authentication information.
     * @return true if the authentication is valid, otherwise false.
     * @throws OpenIDException
     * @author zschwenk
     */
    public String isAuthenticationValid(HttpServletRequest a_oRequest) 
        throws OpenIDException
    {
        
        String identifier = null;
        
        // extract the parameters from the authentication response
        // (which comes in as a HTTP request from the OpenID provider)
        ParameterList lstResponse =
                new ParameterList(a_oRequest.getParameterMap());

        // retrieve the previously stored discovery information
        DiscoveryInformation discovered = (DiscoveryInformation)a_oRequest.getSession().getAttribute("openid-disc");

        // extract the receiving URL from the HTTP request
        StringBuffer receivingURL = a_oRequest.getRequestURL();
        String queryString = a_oRequest.getQueryString();
        if (queryString != null && queryString.length() > 0)
            receivingURL.append("?").append(a_oRequest.getQueryString());

        // verify the response; ConsumerManager needs to be the same
        // (static) instance used to place the authentication request
        VerificationResult verification = m_oManager.verify(
                receivingURL.toString(),
                lstResponse, discovered);

        // examine the verification result and extract the verified identifier
        Identifier verified = verification.getVerifiedId();
        
        if (verified != null)
        {
            AuthSuccess authSuccess =
                    (AuthSuccess) verification.getAuthResponse();

            if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX))
            {
                FetchResponse fetchResp = (FetchResponse) authSuccess
                        .getExtension(AxMessage.OPENID_NS_AX);
                
                if (fetchResp.getAttributeValue("email") != null) {
                    this.email = fetchResp.getAttributeValue("email");
                }
                if (fetchResp.getAttributeValue("altemail") != null && this.email == null) {
                    this.email = fetchResp.getAttributeValue("altemail");                    
                }
                
                if (fetchResp.getAttributeValue("first") != null) {
                    this.name = fetchResp.getAttributeValue("first");                    
                }
                
                String userName = (this.name == null) ? "" : this.name+" ";
                if (fetchResp.getAttributeValue("last") != null) {
                    userName += fetchResp.getAttributeValue("last");
                    this.name = userName;
                }                
            }
            
            identifier = verified.getIdentifier();
        }
        
        return identifier;
    }


    /**
     *  Get accessor for name
     *  Name of this openId user
     */
    public String getName()
    {
        return this.name;
    }

    /**
     *  Set accessor for name
     *  @param name Data to set
     *  Name of this openId user
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    /**
     *  Get accessor for email
     *  Email address of openId user
     */
    public String getEmail()
    {
        return this.email;
    }

    /**
     *  Set accessor for email
     *  @param email Data to set
     *  Email address of openId user
     */
    public void setEmail(String email)
    {
        this.email = email;
    }
    
}