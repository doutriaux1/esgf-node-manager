/***************************************************************************
*                                                                          *
*  Organization: Lawrence Livermore National Lab (LLNL)                    *
*   Directorate: Computation                                               *
*    Department: Computing Applications and Research                       *
*      Division: S&T Global Security                                       *
*        Matrix: Atmospheric, Earth and Energy Division                    *
*       Program: PCMDI                                                     *
*       Project: Earth Systems Grid Federation (ESGF) Data Node Software   *
*  First Author: Gavin M. Bell (gavin@llnl.gov)                            *
*                                                                          *
****************************************************************************
*                                                                          *
*   Copyright (c) 2009, Lawrence Livermore National Security, LLC.         *
*   Produced at the Lawrence Livermore National Laboratory                 *
*   Written by: Gavin M. Bell (gavin@llnl.gov)                             *
*   LLNL-CODE-420962                                                       *
*                                                                          *
*   All rights reserved. This file is part of the:                         *
*   Earth System Grid Federation (ESGF) Data Node Software Stack           *
*                                                                          *
*   For details, see http://esgf.org/esg-node/                             *
*   Please also read this link                                             *
*    http://esgf.org/LICENSE                                               *
*                                                                          *
*   * Redistribution and use in source and binary forms, with or           *
*   without modification, are permitted provided that the following        *
*   conditions are met:                                                    *
*                                                                          *
*   * Redistributions of source code must retain the above copyright       *
*   notice, this list of conditions and the disclaimer below.              *
*                                                                          *
*   * Redistributions in binary form must reproduce the above copyright    *
*   notice, this list of conditions and the disclaimer (as noted below)    *
*   in the documentation and/or other materials provided with the          *
*   distribution.                                                          *
*                                                                          *
*   Neither the name of the LLNS/LLNL nor the names of its contributors    *
*   may be used to endorse or promote products derived from this           *
*   software without specific prior written permission.                    *
*                                                                          *
*   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS    *
*   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT      *
*   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS      *
*   FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL LAWRENCE    *
*   LIVERMORE NATIONAL SECURITY, LLC, THE U.S. DEPARTMENT OF ENERGY OR     *
*   CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,           *
*   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT       *
*   LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF       *
*   USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND    *
*   ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,     *
*   OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT     *
*   OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF     *
*   SUCH DAMAGE.                                                           *
*                                                                          *
***************************************************************************/

/**
   Description:

   The web.xml entry...  (NOTE: must appear AFTER all Authorization
   Filters because they put additional information in the request that
   we need like email address and/or userid)

  <!-- Filter for token-based authorization -->
  <filter>
    <filter-name>AccessLoggingFilter</filter-name>
    <filter-class>esg.node.filters.AccessLoggingFilter</filter-class>
    <!--
    <init-param>
      <param-name>db.driver</param-name>
      <param-value>org.postgresql.Driver</param-value>
    </init-param>
    <init-param>
      <param-name>db.protocol</param-name>
      <param-value>jdbc:postgresql:</param-value>
    </init-param>
    <init-param>
      <param-name>db.host</param-name>
      <param-value>localhost</param-value>
    </init-param>
    <init-param>
      <param-name>db.port</param-name>
      <param-value>5432</param-value>
    </init-param>
    <init-param>
      <param-name>db.database</param-name>
      <param-value>esgcet</param-value>
    </init-param>
    <init-param>
      <param-name>db.user</param-name>
      <param-value>dbsuper</param-value>
    </init-param>
    <init-param>
      <param-name>db.password</param-name>
      <param-value>***</param-value>
    </init-param>
    -->
    <init-param>
      <param-name>service.name</param-name>
      <param-value>thredds</param-value>
    </init-param>
    <init-param>
      <param-name>extensions</param-name>
      <param-value>.nc,.foo,.bar</param-value>
    </init-param>
  </filter>
  <filter-mapping>
    <filter-name>AccessLoggingFilter</filter-name>
    <url-pattern>/*</url-pattern>
  </filter-mapping>

**/
package esg.node.filters;

import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Map;
import java.util.regex.*;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.*;

import esg.common.db.DatabaseResource;
import esg.common.util.ESGFProperties;

public class AccessLoggingFilter implements Filter {
    
    final static String AUTHORIZATION_REQUEST_ATTRIBUTE = "eske.model.security.AuthorizationToken"; // legacy value compatible with old TDS filter

    private static Log log = LogFactory.getLog(AccessLoggingFilter.class);
    
    FilterConfig filterConfig = null;
    AccessLoggingDAO accessLoggingDAO = null;
    Properties dbProperties = null;
    private Pattern urlPattern = null;
    
    private String serviceName = null;


    public void init(FilterConfig filterConfig) throws ServletException {
        log.debug("Initializing filter: "+this.getClass().getName());
        this.filterConfig = filterConfig;
        ESGFProperties esgfProperties = null;
        try{
            esgfProperties = new ESGFProperties();
        }catch (java.io.IOException e) { e.printStackTrace(); log.error(e); }
        String value = null;
        dbProperties = new Properties();
        log.debug("FilterConfig is : ["+filterConfig+"]");
        log.debug("db.protocol is  : ["+filterConfig.getInitParameter("db.protocol")+"]");
        dbProperties.put("db.protocol",((null != (value = filterConfig.getInitParameter("db.protocol"))) ? value : esgfProperties.getProperty("db.protocol"))); value = null;
        dbProperties.put("db.host",((null != (value = filterConfig.getInitParameter("db.host"))) ? value : esgfProperties.getProperty("db.host"))); value = null;
        dbProperties.put("db.port",((null != (value = filterConfig.getInitParameter("db.port"))) ? value : esgfProperties.getProperty("db.port"))); value = null;
        dbProperties.put("db.database",((null != (value = filterConfig.getInitParameter("db.database"))) ? value : esgfProperties.getProperty("db.database"))); value = null;
        dbProperties.put("db.user",((null != (value = filterConfig.getInitParameter("db.user"))) ? value : esgfProperties.getProperty("db.user"))); value = null;
        dbProperties.put("db.password",((null != (value = filterConfig.getInitParameter("db.password"))) ? value : esgfProperties.getDatabasePassword())); value = null;
        dbProperties.put("db.driver",((null != (value = filterConfig.getInitParameter("db.driver"))) ? value : esgfProperties.getProperty("db.driver","org.postgresql.Driver"))); value = null;
        
        serviceName = (null != (value = filterConfig.getInitParameter("service.name"))) ? value : "thredds"; value = null;
        
        log.debug("Database parameters: "+dbProperties);

        DatabaseResource.init(dbProperties.getProperty("db.driver","org.postgresql.Driver")).setupDataSource(dbProperties);
        DatabaseResource.getInstance().showDriverStats();
        accessLoggingDAO = new AccessLoggingDAO(DatabaseResource.getInstance().getDataSource());
        
        
        String extensionsParam = filterConfig.getInitParameter("extensions");
        if (extensionsParam == null) { extensionsParam=""; } //defensive program against null for this param
        String[] extensions = (".nc,"+extensionsParam.toString()).split(",");
            
        StringBuffer sb = new StringBuffer();
        for(int i=0 ; i<extensions.length; i++) { 
            sb.append(extensions[i].trim());
            if(i<extensions.length-1) sb.append("|");
        }
        System.out.println("looking for extensions: "+sb.toString());
        String regex = "http.*(?:"+sb.toString()+")$";
        System.out.println("Regex = "+regex);
        
        urlPattern = Pattern.compile(regex,Pattern.CASE_INSENSITIVE);
        
        log.trace(accessLoggingDAO.toString());
    }

    public void destroy() { 
        this.filterConfig = null; 
        this.dbProperties.clear();
        this.accessLoggingDAO = null;
        
        //Shutting down this resource under the assuption that no one
        //else is using this resource but us
        DatabaseResource.getInstance().shutdownResource();
    }

    @SuppressWarnings("unchecked")
    public void doFilter(ServletRequest request,
                         ServletResponse response, 
                         FilterChain chain) throws IOException, ServletException {
        
        if(filterConfig == null) return;
        
        boolean success = false;
        int id=-1;
        long duration = -1;

        //Record identifying tuple
        String userID = null;
        String email = null;
        String url = null;
        String fileID = null;
        String remoteAddress = null;
        String userAgent = null;
        long   dateFetched = 0L;
        long   batchUpdateTime = 0L;
        //(note: serviceName defined in global scope)

        //firewall off any errors so that nothing stops the show...
        try {
            if(accessLoggingDAO != null) {
                
                //This filter should only appy to specific requests
                //in particular requests for data files (*.nc)
                
                HttpServletRequest req = (HttpServletRequest)request;
                url = req.getRequestURL().toString().trim();
                Matcher m = urlPattern.matcher(url);
                
                if(m.matches()) {

                    // only proceed if the request has been authorized
                    final Boolean requestIsAuthorized = (Boolean)request.getAttribute(AUTHORIZATION_REQUEST_ATTRIBUTE);
                    log.debug("AUTHORIZATION_REQUEST_ATTRIBUTE="+requestIsAuthorized);
                    if (requestIsAuthorized==null || requestIsAuthorized==false) {
                        log.warn("UnAuthorized Request for: "+req.getRequestURL().toString().trim());
                        chain.doFilter(request, response);
                        return;
                    }

                    log.debug("Executing filter on: "+url);

                    //------------------------------------------------------------------------------------------
                    //For Token authentication there is a Validation Map present with user and email information
                    //------------------------------------------------------------------------------------------
                    Map<String,String> validationMap = (Map<String,String>)req.getAttribute("validationMap");
                    if(validationMap != null) {
                        
                        userID = validationMap.get("user");
                        email = validationMap.get("email");
                        
                        //Want to make sure that any snooping filters
                        //behind this one does not have access to this
                        //information (posted by the
                        //authorizationTokenValidationFilter, which should
                        //immediately preceed this one).  This is in
                        //effort to limit information exposure the
                        //best we can.
                        req.removeAttribute("validationMap");
                        
                    }else{
                        log.debug("Validation Map is ["+validationMap+"] - (not a token based request)");
                    }
                    //------------------------------------------------------------------------------------------
                    
                    
                    
                    //------------------------------------------------------------------------------------------
                    //For TokenLESS authentication the userid information is in a parameter called "esg.openid"
                    //------------------------------------------------------------------------------------------
                    if (userID == null || userID.isEmpty()) {
                        userID = ((req.getAttribute("esg.openid") == null) ? "<no-id>" : req.getAttribute("esg.openid").toString());
                        if(userID == null || userID.isEmpty()) { log.warn("This request is apparently not a \"tokenless\" request either - no openid attribute!!!!!"); }
                        log.debug("AccessLoggingFilter - Tokenless: UserID = ["+userID+"]");
                    }
                    //------------------------------------------------------------------------------------------
                    
                    
                    
                    fileID = "0A";
                    remoteAddress = req.getRemoteAddr();
                    userAgent = (String)req.getAttribute("userAgent");
                    dateFetched = System.currentTimeMillis()/1000;
                    batchUpdateTime = dateFetched; //For the life of my I am not sure why this is there, something from the gridftp metrics collection. -gmb
                    
                    success = ((id = accessLoggingDAO.logIngressInfo(userID,email,url,fileID,remoteAddress,userAgent,serviceName,batchUpdateTime,dateFetched)) > 0);

                }else {
                    log.debug("No match against: "+url);
                }
                
            }else{
                log.error("DAO is null :["+accessLoggingDAO+"]");
                HttpServletResponse resp = (HttpServletResponse)response;
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Invalid State Of ESG Access Logging Filter: DAO=["+accessLoggingDAO+"]");
            }
                    
        }catch(Throwable t) {
            log.error(t);
            HttpServletResponse resp = (HttpServletResponse)response;
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Caught unforseen Exception in ESG Access Logging Filter");
        }
        
        long startTime = System.currentTimeMillis();
        chain.doFilter(request, response);
        duration = System.currentTimeMillis() - startTime;
        //NOTE: I Don't think duration means what Nate thinks it means...

        try{
            if((accessLoggingDAO != null) && success) {
                if(success) {success = feasibilityHeuristic(duration); }
                accessLoggingDAO.logEgressInfo(id, success, duration);
            }
        }catch(Throwable t) {
            log.error(t);
            HttpServletResponse resp = (HttpServletResponse)response;
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Caught unforseen Exception in ESG Access Logging Filter");
        }
    }

    //The issue: We are getting quite a few "false positives" in the
    //database indicating that the number of successfull downloads is
    //larger than reality.  This is because we have no signal to
    //detect a download failure or partial download (i.e. failure).
    //The only signal at our disposal is the duration value that
    //indicates how long this filter has been engaged in the service
    //of file transfer.  This problem is certainly under constrained.
    //Because we know we have files that average in size of over a GB
    //and in general bandwidth for a node is about around 15MB/sec, we
    //assert that if a duration is one second (1000ms) or less it is
    //suspicious and thus deem it a failure.  Clearly this is a
    //heuristic, however, when dealing with statistics over petabytes
    //of data the small files that may get spuriously caught in this
    //heuristic won't impact much on the overall size. Thus for the
    //moment, until we have a better way, we will apply this heuristic
    //to clean up the reporting so it is closer to reality.

    //(time given in milliseconds)
    private boolean feasibilityHeuristic(long time) {
        //The heuristic is that it takes more than a second to transfer any file in the corpus
        //(modulo super tiny fx files... which because they are super tiny won't have a huge effect on the gross value that is on the order of tera/peta bytes)
        return (time > 1000);
    }
    
}
