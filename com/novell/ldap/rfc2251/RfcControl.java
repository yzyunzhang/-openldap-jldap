/* **************************************************************************
 * $Novell: /ldap/src/jldap/com/novell/ldap/protocol/RfcControl.java,v 1.9 2000/11/09 18:27:19 vtag Exp $
 *
 * Copyright (C) 1999, 2000 Novell, Inc. All Rights Reserved.
 ***************************************************************************/

package com.novell.ldap.protocol;

import java.io.*;
import com.novell.ldap.asn1.*;

/**
 *        Control ::= SEQUENCE {
 *               controlType             LDAPOID,
 *               criticality             BOOLEAN DEFAULT FALSE,
 *               controlValue            OCTET STRING OPTIONAL }
 */
public class RfcControl extends ASN1Sequence {

	//*************************************************************************
	// Constructors for Control
	//*************************************************************************

	/**
	 *
	 */
	public RfcControl(RfcLDAPOID controlType)
	{
		this(controlType, new ASN1Boolean(false), null);
	}

	/**
	 *
	 */
	public RfcControl(RfcLDAPOID controlType, ASN1Boolean criticality)
	{
		this(controlType, criticality, null);
	}

	/**
	 *
	 * Note: criticality is only added if true, as per RFC 2251 sec 5.1 part
	 *       (4): If a value of a type is its default value, it MUST be
	 *       absent.
	 */
	public RfcControl(RfcLDAPOID controlType, ASN1Boolean criticality,
		            ASN1OctetString controlValue)
	{
		super(3);
		add(controlType);
		if(criticality.getContent() == true)
			add(criticality);
		if(controlValue != null)
			add(controlValue);
	}

	/**
	 * Constructs a Control object by decoding it from an InputStream.
	 */
	public RfcControl(ASN1Decoder dec, InputStream in, int len)
		throws IOException
	{
		super(dec, in, len);
	}

	/**
	 * Constructs a Control object by decoding from an ASN1Sequence
	 */
	public RfcControl(ASN1Sequence seqObj)
		throws IOException
	{
	    super(3);
		int len = seqObj.size();
		for (int i = 0; i < len; i++)
		    add(seqObj.get(i));
	}

	//*************************************************************************
	// Accessors
	//*************************************************************************

	/**
	 *
	 */
	public ASN1OctetString getControlType()
	{
		return (ASN1OctetString)get(0);
	}

	/**
	 * Returns criticality.
	 *
	 * If no value present, return the default value of FALSE.
	 */
	public ASN1Boolean getCriticality()
	{
		if(size() > 1) { // MAY be a criticality
			ASN1Object obj = get(1);
			if(obj instanceof ASN1Boolean)
				return (ASN1Boolean)obj;
		}

		return new ASN1Boolean(false);
	}

	/**
	 * Since controlValue is an OPTIONAL component, we need to check
	 * to see if one is available. Remember that if criticality is of default
	 * value, it will not be present.
	 */
	public ASN1OctetString getControlValue()
	{
		if(size() > 2) { // MUST be a control value
			return (ASN1OctetString)get(2);
		}
		else if(size() > 1) { // MAY be a control value
			ASN1Object obj = get(1);
			if(obj instanceof ASN1OctetString)
				return (ASN1OctetString)obj;
		}
		return null;

	}

   /**
	 * Called to set/replace the ControlValue.  Will normally be called by
	 * the child classes after the parent has been instantiated.
	 */
	public void setControlValue(ASN1OctetString controlValue)
	{

		if (controlValue == null)
			return;

		if(size() == 3) { 
			// We already have a control value, replace it
			set(2, controlValue);
			return;

		} 
		
		if (size() == 2) {
			
			// Get the second element
			ASN1Object obj = get(1);

			// Is this a control value
			if(obj instanceof ASN1OctetString) {
			
				// replace this one
				set(1, controlValue);
				return;
			}
			else {
				// add a new one at the end
				add(controlValue);
				return;
			}
		}
	}

}

