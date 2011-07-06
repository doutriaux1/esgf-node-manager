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
package esg.common.shell.cmds;

/**
   Description:
   ESGF's "realize" command..."

   This command takes a dataset directory and inspects its catalog to
   find missing files (files listed in the dataset catalog but not
   locally present on the filesystem) and brings them local. The
   second half of the 'replication' process - for a single dataset.
**/

import esg.common.shell.*;

import org.apache.commons.cli.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.*;

public class ESGFrealize extends ESGFCommand {

private static Log log = LogFactory.getLog(ESGFrealize.class);

    public ESGFrealize() { super(); }

    public String getCommandName() { return "realize"; }

    public void doInitOptions() {
        getOptions().addOption("a", "all", false, "realize all dataset files");
        Option dataset   = OptionBuilder.withArgName("datasetdir")
            .hasArg()
            .withLongOpt("dataset")
            .withDescription("lists the files of a particular dataset")
            .create("ds");
        getOptions().addOption(dataset);
        Option regex   = OptionBuilder.withArgName("regex")
            .hasArg()
            .withDescription("Select only dataset files that match regex")
            .create("regex");
        getOptions().addOption(regex);
    }

    public ESGFEnv doEval(CommandLine line, ESGFEnv env) {
        log.trace("inside the \"realize\" command's doEval");
        //TODO: Query for options and perform execution logic

        if(line.hasOption("all")) {
            env.getWriter().println("Realizing all datasets :-)");
        }

        String datasetdir = null;
        if(line.hasOption( "ds" )) {
            datasetdir = line.getOptionValue( "ds" );
            env.getWriter().println("dataset option value is: "+datasetdir);
        }

        String regex = null;
        if(line.hasOption( "regex" )) {
            datasetdir = line.getOptionValue( "regex" );
            env.getWriter().println("regex option value is: "+datasetdir);
        }

        int i=0;
        for(String arg : line.getArgs()) {
            log.trace("arg("+(i++)+"): "+arg);
        }
        
        return env;
    }
}
