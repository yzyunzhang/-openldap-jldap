/* **************************************************************************
 * $Id: NamingContextEntryCountRequest.java,v 1.6 2000/08/21 18:35:47 vtag Exp $
 *
 * Copyright (C) 1999, 2000 Novell, Inc. All Rights Reserved.
 * 
 * THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND
 * TREATIES. USE, MODIFICATION, AND REDISTRIBUTION OF THIS WORK IS SUBJECT
 * TO VERSION 2.0.1 OF THE OPENLDAP PUBLIC LICENSE, A COPY OF WHICH IS
 * AVAILABLE AT HTTP://WWW.OPENLDAP.ORG/LICENSE.HTML OR IN THE FILE "LICENSE"
 * IN THE TOP-LEVEL DIRECTORY OF THE DISTRIBUTION. ANY USE OR EXPLOITATION
 * OF THIS WORK OTHER THAN AS AUTHORIZED IN VERSION 2.0.1 OF THE OPENLDAP
 * PUBLIC LICENSE, OR OTHER PRIOR WRITTEN CONSENT FROM NOVELL, COULD SUBJECT
 * THE PERPETRATOR TO CRIMINAL AND CIVIL LIABILITY. 
 ***************************************************************************/
package com.novell.ldap.ext; 

import com.novell.ldap.*;
import com.novell.asn1.*;
import java.io.*;
 
/**
 *
 *      This class returns a count of the number of entries (objects) in the
 *  specified naming context.<br><br>
 *
 *      To get this count create an instance of this 
 *  class and then call the extendedOperation method with this
 *  object as the required LDAPExtendedOperation parameter.<br><br>
 *
 *  The returned LDAPExtendedResponse object can then be converted to
 *  a NamingContextEntryCountResponse object.  This object contains
 *  methods for retreiving the returned count.<br><br>
 *
 *  The OID used for this extended operation is:
 *      "2.16.840.1.113719.1.27.100.13"<br><br>
 *
 *  The RequestValue has the folling ASN:<br><br>
 *
 *  requestValue ::=<br><br>
 *  &nbsp;&nbsp;&nbsp;&nbsp;    dn          LDAPDN
 */
 public class NamingContextEntryCountRequest extends LDAPExtendedOperation {
 
    /**
    *      The constructor takes one parameters:<br><br>
    *
    * @param dn  This parameter identifies the naming context.<br>
    *
    */  

    public NamingContextEntryCountRequest(String dn) 
                throws LDAPException {
        
        super(NamingContextConstants.NAMING_CONTEXT_COUNT_REQ, null);
        
        try {
            
            if ( (dn == null) )
                throw new LDAPException("Invalid parameter",
				                        LDAPException.PARAM_ERROR);
				                        
            ByteArrayOutputStream encodedData = new ByteArrayOutputStream();
			BEREncoder encoder  = new BEREncoder();

		    ASN1OctetString asn1_dn = new ASN1OctetString(dn);

            asn1_dn.encode(encoder, encodedData);
            
            setValue(encodedData.toByteArray());
            
        }
		catch(IOException ioe) {
			throw new LDAPException("Encoding Error",
				                     LDAPException.ENCODING_ERROR);
		}
     }
}
