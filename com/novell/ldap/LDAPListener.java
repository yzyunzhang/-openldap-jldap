/* **************************************************************************
 * $Novell: /ldap/src/jldap/com/novell/ldap/LDAPListener.java,v 1.14 2000/10/31 23:52:22 vtag Exp $
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
 
package com.novell.ldap;

import com.novell.ldap.client.*;

/**
 *
 *  The common interface for LDAPResponseListener and LDAPSearchListener.
 *  It represents a queue of incoming asynchronous messages from the server.
 */
public interface LDAPListener
{
   /**
    * Returns the message IDs for all outstanding requests. 
    *
    * <p>The last ID in the array is the messageID of the 
    * last submitted request.</p>
    *
    * @return The message IDs for all outstanding requests.
    */
   public int[] getMessageIDs();
   
   /**
    * Returns the response from an LDAP request.
    *
    * <p>The getResponse method blocks until a response is available, or until all 
    * operations associated with the object have completed or been canceled, and 
    * then returns the response. The client is responsible for processing
    * the responses returned from a listener.</p>
    *
    * @return The response.
    *
    * @exception LDAPException A general exception which includes an error
    *  message and an LDAP error code.
    */
   public LDAPMessage getResponse()
            throws LDAPException;

   /**
    * Returns the response from an LDAP request for a particular msg id.
    *
    * <p>The getResponse method blocks until a response is available
    * for a particular message id, or until all 
    * operations associated with the object have completed or been canceled, and 
    * then returns the response. The client is responsible for processing
    * the responses returned from a listener.</p>
    *
    * @return The response.
    *
    * @exception LDAPException A general exception which includes an error
    *  message and an LDAP error code.
    */
   public LDAPMessage getResponse(int msgid)
            throws LDAPException;

   /**
    * Reports whether a response has been received from the server.
    *
    * @return True if a response has been received from the server; false if
    *         a response has not been received. 
    */
   public boolean isResponseReceived();
   
   /**
    * Reports whether a response has been received from the server for
    * a particular msg id.
    *
    * @return True if a response has been received from the server; false if
    *         a response has not been received. 
    */
   public boolean isResponseReceived(int msgid);
   
   /**
    * Merges two response listeners by moving the contents from another
    * listener to this one.
    *
    * @param listener2 The listener that receives the contents from the
    *                  other listener.
    */
   public void merge(LDAPResponseListener listener2);
}
