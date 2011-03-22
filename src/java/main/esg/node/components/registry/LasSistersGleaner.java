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
package esg.node.components.registry;

import esg.common.generated.registration.*;
import esg.common.util.ESGFProperties;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.util.Properties;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.*;

/**
   Description:
   
   Encapsulates the logic for fetching and generating this local
   node's LAS Sisters file as defined by las_sisters.xsd

*/
public class LasSistersGleaner {

    private static final Log log = LogFactory.getLog(LasSistersGleaner.class);

    private Datasets datasets = null;
    private String sistersFile = "esgcet_sisters.xml";
    private Properties props = null;
    private String defaultLocation = null;

    public LasSistersGleaner() { this(null); }
    public LasSistersGleaner(Properties props) {
        try {
            if(props == null) this.props = new ESGFProperties();
            else this.props = props;

            String base = System.getenv("ESGF_HOME");
            if (base != null) {
                defaultLocation = base+"/content/las/conf/server";
            }else {
                defaultLocation = "/tmp";
                log.warn("ESGF_HOME environment var not set!");
            }

            datasets = new Datasets();
        } catch(Exception e) {
            log.error(e);
        }
    }
    
    public Datasets getMyDatasets() { return datasets; }
    
    public boolean saveDatasets() { return saveDatasets(datasets); }
    public synchronized boolean saveDatasets(Datasets datasets) {
        boolean success = false;
        if (datasets == null) {
            log.error("Datasets is ["+datasets+"]"); 
            return success;
        }
        log.info("Saving LAS Datasets information to "+props.getProperty("las.xml.config.dir",defaultLocation));
        try{
            JAXBContext jc = JAXBContext.newInstance(Datasets.class);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m.marshal(datasets, new FileOutputStream(props.getProperty("las.xml.config.dir",defaultLocation)+File.separator+this.sistersFile));
            success = true;
        }catch(Exception e) {
            log.error(e);
        }
	
        return success;
    }

    
    /**
       Looks through the current system and gathers the configured
       node service information.  Takes that information and
       creates a local representation of this node's registration.
    */
    public synchronized LasSistersGleaner appendToMyDatasetsFromRegistration(Registration registration) {
        log.info("Creating my LAS Datasets representation...");
        try{
            LASService service = null; //the LASService entry from the registration -via-> node
            LasServer sister = null;   //Local datasets xml element

            //TODO: NEED TO DEDUP ENTRIES!!!
            for(Node node : registration.getNode()) {
                service = node.getLASService();
                sister = new LasServer();
                sister.setName(node.getHostname());
                sister.setUrl(service.getEndpoint());
                datasets.getLasServer().add(sister);
            }

        } catch(Exception e) {
            log.error(e);
        }
        
        return this;
    }
    
    public void clear() {
        if(this.datasets != null) this.datasets = new Datasets();
    }

    public synchronized LasSistersGleaner loadMyDatasets() {
        log.info("Loading my LAS Datasets info from "+sistersFile);
        try{
            JAXBContext jc = JAXBContext.newInstance(Datasets.class);
            Unmarshaller u = jc.createUnmarshaller();
            JAXBElement<Datasets> root = u.unmarshal(new StreamSource(new File(props.getProperty("las.xml.config.dir",defaultLocation)+File.separator+this.sistersFile)),Datasets.class);
            datasets = root.getValue();
        }catch(Exception e) {
            log.error(e);
        }
        return this;
    }

    //****************************************************************
    // Not really used methods but here for completeness
    //****************************************************************

    public Datasets createDatasetsFromString(String sistersContent) {
        log.info("Loading my LAS Datasets info from \n"+sistersContent+"\n");
        Datasets fromContentDatasets = null;
        try{
            JAXBContext jc = JAXBContext.newInstance(Datasets.class);
            Unmarshaller u = jc.createUnmarshaller();
            JAXBElement<Datasets> root = u.unmarshal(new StreamSource(new StringReader(sistersContent)),Datasets.class);
            fromContentDatasets = root.getValue();
        }catch(Exception e) {
            log.error(e);
        }
        return fromContentDatasets;
    }

}