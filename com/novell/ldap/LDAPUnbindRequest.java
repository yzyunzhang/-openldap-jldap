/* Generated by Together */

package com.novell.ldap.message;

import com.novell.ldap.*;
import com.novell.ldap.asn1.*;
import com.novell.ldap.client.Debug;
import com.novell.ldap.rfc2251.*;

import java.util.Enumeration;

/* 
 *       UnbindRequest ::= [APPLICATION 2] NULL
 */
public class LDAPUnbindRequest extends LDAPMessage
{
    /**
     */
    public LDAPUnbindRequest( LDAPConstraints cons)
        throws LDAPException
    {
        super( LDAPMessage.UNBIND_REQUEST,
               new RfcUnbindRequest(),
               (cons != null) ? cons.getControls() : null);
        return;
    }
}
