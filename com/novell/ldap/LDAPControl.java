/* **************************************************************************
 * $Novell: /ldap/src/jldap/com/novell/ldap/LDAPControl.java,v 1.29 2001/07/25 23:42:03 vtag Exp $
 *
 * Copyright (C) 1999, 2000, 2001 Novell, Inc. All Rights Reserved.
 *
 * THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND
 * TREATIES. USE, MODIFICATION, AND REDISTRIBUTION OF THIS WORK IS SUBJECT
 * TO VERSION 2.0.7 OF THE OPENLDAP PUBLIC LICENSE, A COPY OF WHICH IS
 * AVAILABLE AT HTTP://WWW.OPENLDAP.ORG/LICENSE.HTML OR IN THE FILE "LICENSE"
 * IN THE TOP-LEVEL DIRECTORY OF THE DISTRIBUTION. ANY USE OR EXPLOITATION
 * OF THIS WORK OTHER THAN AS AUTHORIZED IN VERSION 2.0.7 OF THE OPENLDAP
 * PUBLIC LICENSE, OR OTHER PRIOR WRITTEN CONSENT FROM NOVELL, COULD SUBJECT
 * THE PERPETRATOR TO CRIMINAL AND CIVIL LIABILITY.
 ******************************************************************************/

package com.novell.ldap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;
import com.novell.ldap.controls.*;
import com.novell.ldap.client.*;
import com.novell.ldap.asn1.*;
import com.novell.ldap.rfc2251.*;

/**
 *  Encapsulates optional additional parameters or constraints for an
 *  LDAP operation, either on the server or on the client.
 *
 * <p>If set as a server control, it is sent to the server along with the operation
 * request. If set as a client control, it is not sent to the server, but
 * rather interpreted locally by the client. LDAPControl is an LDAPv3 extension,
 * and is not supported in an LDAPv2 environment.</p>
 *
 * @see LDAPConnection#getResponseControls
 * @see LDAPSearchConstraints#getClientControls
 * @see LDAPSearchConstraints#getServerControls
 * @see LDAPSearchConstraints#setClientControls
 * @see LDAPSearchConstraints#setServerControls 
 */
public class LDAPControl implements Cloneable {

    private static RespControlVector registeredControls = new RespControlVector(5, 5);

    private RfcControl control; // An RFC 2251 Control

    /**
     * Constructs a new LDAPControl object using the specified values.
     *
     *  @param id     The ID of the control, as a dotted string.
     *<br><br>
     *  @param critical   True if the LDAP operation should be discarded if
     *                    the control is not supported. False if
     *                    the operation can be processed without the control.
     *<br><br>
     *  @param vals     The control-specific data.
     */
    public LDAPControl(String id, boolean critical, byte[] vals)
    {
        control = new RfcControl(new RfcLDAPOID(id), new ASN1Boolean(critical),
                              new ASN1OctetString(vals));
        return;
    }

    /**
     * Create an LDAPControl from an existing control.
     */
    protected LDAPControl(RfcControl control)
    {
        this.control = control;
        return;
    }

    /**
     * Returns a copy of the current LDAPControl object.
     *
     * @return A copy of the current LDAPControl object.
     */
    public Object clone()
    {
       byte[] vals = this.getValue();
       byte[] twin = new byte[vals.length];
       for(int i = 0; i < vals.length; i++){
         twin[i]=vals[i];
       }//is this necessary?  Yes even though the contructor above allocates a
       //new ASN1OctetString.  vals in that constuctor is only copied by reference

       return (Object)( new LDAPControl(this.getID(), this.isCritical(), twin));
    }

    /**
     * Returns the identifier of the control.
     *
     * @return The object ID of the control.
     */
    public String getID()
    {
        return new String(control.getControlType().getContent());
    }

    /**
     * Returns the control-specific data of the object.
     *
     * @return The control-specific data of the object as a byte array.
     */
    public byte[] getValue()
    {
        return control.getControlValue().getContent();
    }


    /**
     * Sets the control-specific data of the object.  This method is for
     * use by extension of LDAPControl.
     */
    protected void setValue(byte[] controlValue)
    {
        control.setControlValue(new ASN1OctetString(controlValue));
        return;
    }


    /**
     * Returns whether the control is critical for the operation.
     *
     * @return Returns true if the control must be supported for an associated
     * operation to be executed, and false if the control is not required for
     * the operation.
     */
    public boolean isCritical()
    {
        return control.getCriticality().getContent();
    }

    /**
     * Instantiates an LDAPControl.  We search through our list of
     * registered controls.  If we find a matchiing OID we instantiate
     * that control by calling its contructor.  Otherwise we default to
     * returning a regular LDAPControl object
     *
     * @depreacted.  Not to be used by application programs.
     *
     * @param data A RfcControl object that encodes the returned control.
     */
    public static LDAPControl newInstance(RfcControl rfcCtl)
    {
        String oid = rfcCtl.getControlType().getString();

        try {
            /* search through the registered extension list to find the response control class */
            Class responseControlClass = registeredControls.findResponseControl(oid);

            // Did not find a match so return default LDAPControl
            if ( responseControlClass == null)
                return new LDAPControl(rfcCtl);

            /* If found get default 1 parameter LDAPControl constructor */
            Class[] ArgsClass = new Class[] {rfcCtl.getClass()};
            Object[] Args = new Object[] {rfcCtl};
            try {
                Constructor defaultConstructor = responseControlClass.getConstructor(ArgsClass);

                try {
                    /* Call the default constructor for registered Class*/
                    Object ctl = null;
                    ctl = defaultConstructor.newInstance(Args);
                    return (LDAPControl) ctl;
                }
                // Could not create the ResponseControl object
                // All possible exceptions are ignored. We fall through
                // and create a default LDAPControl object
                catch (InstantiationException e) {
                    ;
                } catch (IllegalAccessException e) {
                    ;
                } catch (InvocationTargetException e) {
                    ;
                }
            } catch (NoSuchMethodException e) {
                // bad class was specified, fall through and return a
                // default LDAPControl object
                if( Debug.LDAP_DEBUG) {
                    Debug.trace( Debug.controls, "Could not find default constructor in registered LDAPControl class");
                }

            }

        } catch (NoSuchFieldException ex) {
            ; // Do nothing. Fall through and construct a default LDAPControl object.

        }
        // If we get here we did not have a registered response control
        // for this oid.  Return a default LDAPControl object.
        if( Debug.LDAP_DEBUG) {
            Debug.trace( Debug.controls, "Could not instantiate child LDAPControl. Returning default LDAPControl class");
        }
        return new LDAPControl(rfcCtl);
    }

    /**
     * Registers a class to be instantiated on receipt of a control with the
     * given OID.
     *
     * <p>Any previous registration for the OID is overridden. The
     * controlClass must be an extension of LDAPControl.</p>
     *
     *  @param oid            The object identifier of the control.
     *<br><br>
     *  @param controlClass   A class which can instantiate an LDAPControl.
     */
    public static void register(String oid, Class controlClass)
    {
        registeredControls.registerResponseControl(oid, controlClass);
        return;
    }

    /**
     * Returns the RFC 2251 Control object.
     *
     * @return An ASN.1 RFC 2251 Control.
     */
    /*package*/ RfcControl getASN1Object()
    {
        return control;
    }
}
