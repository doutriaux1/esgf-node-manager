/***************************************************************************
*                                                                          *
*  Organization: Lawrence Livermore National Lab (LLNL)                    *
*   Directorate: Computation                                               *
*    Department: Computing Applications and Research                       *
*      Division: S&T Global Security                                       *
*        Matrix: Atmospheric, Earth and Energy Division                    *
*       Program: PCMDI                                                     *
*       Project: Earth Systems Grid (ESG) Data Node Software Stack         *
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

       This is a very simple object to encapsulate information between
       remote client and service

**/
package esg.common.service;

public class ESGRemoteEvent implements java.io.Serializable {

    //Okay, I know ideally these should be ENUMS but I am not sure on
    //how enums serialize with hessian, so I am going to play it safe
    //before I start experimenting.  Just something to get this setup
    //rolling. Besides this is old school flava right here! :-)

    public static final int NOOP       = 1;
    public static final int REGISTER   = 2;
    public static final int UNREGISTER = 4;
    public static final int NOTIFY     = 8;
    public static final int HEALTH     = 16;
    public static final int METRICS    = 32;
    public static final int APPLICATION = 64;

    private String source  = null; //This value is the complete URL of the originating node service
    private int    messageType = -1;
    private Object payload = null;
    private String checksum = null;
    private long   seqNum = 0L;
    private int    ttl = 1; //a good spread in a network about the size of 2^5ish nodes

    public ESGRemoteEvent(String source, int messageType, Object payload, String checksum, Long seqNum, int ttl) {
        this.source  = source;
        this.messageType = messageType;
        this.payload = payload;
        this.checksum = checksum;
        this.seqNum = seqNum;
        this.ttl = ttl;
    }

    public ESGRemoteEvent(String source, int messageType, Object payload, long seqNum, int ttl) {
        this(source,messageType,payload,null,seqNum,ttl);
    }
    
    public ESGRemoteEvent(String source, int messageType, Object payload, long seqNum) {
        this(source,messageType,payload,null,seqNum,1);
    }

    public ESGRemoteEvent(String source, int messageType, long seqNum) {
        this(source,messageType,null,null,seqNum,1);
    }

    public ESGRemoteEvent(String source) { this(source,NOOP,null,null,-1L,1); }

    //Accessors...
    public String getSource()  { return source;  }
    public int    getMessageType() { return messageType; }
    public Object getPayload() { return payload; }
    public String getPayloadChecksum() { return checksum; }
    public long   getSeqNum() { return seqNum; }
    public int    getTTL() { return ttl; }

    //call me before sending on to next hop
    public void decTTL() { ttl--; }
    public boolean isValid() { return (this.ttl > 0); }

    public String toString() { return "RE - s:["+source+"] m:["+messageType+"] n:["+seqNum+"] t:["+ttl+"] p:["+payload+"]"; }

}

