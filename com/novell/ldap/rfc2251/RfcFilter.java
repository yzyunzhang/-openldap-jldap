/* **************************************************************************
 * $Novell: /ldap/src/jldap/ldap/src/com/novell/asn1/ldap/Filter.java,v 1.10 2000/08/29 06:36:06 smerrill Exp $
 *
 * Copyright (C) 1999, 2000 Novell, Inc. All Rights Reserved.
 ***************************************************************************/

package com.novell.asn1.ldap;

import java.util.*;
import java.io.*;

import com.novell.asn1.*;
import com.novell.ldap.LDAPException;

/**
 *       Filter ::= CHOICE {
 *               and             [0] SET OF Filter,
 *               or              [1] SET OF Filter,
 *               not             [2] Filter,
 *               equalityMatch   [3] AttributeValueAssertion,
 *               substrings      [4] SubstringFilter,
 *               greaterOrEqual  [5] AttributeValueAssertion,
 *               lessOrEqual     [6] AttributeValueAssertion,
 *               present         [7] AttributeDescription,
 *               approxMatch     [8] AttributeValueAssertion,
 *               extensibleMatch [9] MatchingRuleAssertion }
 */
public class Filter extends ASN1Choice {

   //*************************************************************************
   // Public variables for Filter
   //*************************************************************************

   /**
    * Context-specific TAG for AND component.
    */
   public final static int AND = 0;
   /**
    * Context-specific TAG for OR component.
    */
   public final static int OR = 1;
   /**
    * Context-specific TAG for NOT component.
    */
   public final static int NOT = 2;
   /**
    * Context-specific TAG for EQUALITY_MATCH component.
    */
   public final static int EQUALITY_MATCH = 3;
   /**
    * Context-specific TAG for SUBSTRINGS component.
    */
   public final static int SUBSTRINGS = 4;
   /**
    * Context-specific TAG for GREATER_OR_EQUAL component.
    */
   public final static int GREATER_OR_EQUAL = 5;
   /**
    * Context-specific TAG for LESS_OR_EQUAL component.
    */
   public final static int LESS_OR_EQUAL = 6;
   /**
    * Context-specific TAG for PRESENT component.
    */
   public final static int PRESENT = 7;
   /**
    * Context-specific TAG for APPROX_MATCH component.
    */
   public final static int APPROX_MATCH = 8;
   /**
    * Context-specific TAG for EXTENSIBLE_MATCH component.
    */
   public final static int EXTENSIBLE_MATCH = 9;

   /**
    * Context-specific TAG for INITIAL component.
    */
   public final static int INITIAL = 0;
   /**
    * Context-specific TAG for ANY component.
    */
   public final static int ANY = 1;
   /**
    * Context-specific TAG for FINAL component.
    */
   public final static int FINAL = 2;

   //*************************************************************************
   // Private variables for Filter
   //*************************************************************************

   private FilterTokenizer ft;

   //*************************************************************************
   // Constructor for Filter
   //*************************************************************************

   /**
    * Constructs a Filter object by parsing an RFC 2254 Search Filter String.
    */
   public Filter(String filter)
      throws LDAPException
   {
      setContent(parse(filter));
   }

   //*************************************************************************
   // Helper methods for RFC 2254 Search Filter parsing.
   //*************************************************************************

	/**
	 * Parses an RFC 2251 filter string into an ASN.1 LDAP Filter object.
	 */
   private ASN1Tagged parse(String filterExpr)
      throws LDAPException
   {
      if(filterExpr == null || filterExpr.equals("")) {
         throw new LDAPException("Invalid filter",
                                 LDAPException.FILTER_ERROR);
      }

      if(filterExpr.charAt(0) != '(')
        filterExpr = "(" + filterExpr + ")";

      ft = new FilterTokenizer(filterExpr);

      return parseFilter();
   }

   /**
    * Will parse an RFC 2254 filter
    */
   private ASN1Tagged parseFilter()
      throws LDAPException
   {
      ft.getLeftParen();

      ASN1Tagged filter = parseFilterComp();
      
      ft.getRightParen();

      return filter;
   }

   /**
    * RFC 2254 filter helper method. Will Parse a filter component.
    */
   private ASN1Tagged parseFilterComp()
      throws LDAPException
   {
      ASN1Tagged tag = null;
      int filterComp = ft.getOpOrAttr();

      switch(filterComp) {
         case AND:
         case OR:
            tag = new ASN1Tagged(
               new ASN1Identifier(ASN1Identifier.CONTEXT, true, filterComp),
               parseFilterList(),
               false);
				break;
         case NOT:
            tag = new ASN1Tagged(
               new ASN1Identifier(ASN1Identifier.CONTEXT, true, filterComp),
               parseFilter(),
               true);
				break;
         default:
            int filterType = ft.getFilterType();
            String value = ft.getValue();

            switch(filterType) {
               case GREATER_OR_EQUAL:
               case LESS_OR_EQUAL:
               case APPROX_MATCH:
                  tag = new ASN1Tagged(
                     new ASN1Identifier(ASN1Identifier.CONTEXT, true,
								                filterType),
                     new AttributeValueAssertion(
                        new AttributeDescription(ft.getAttr()),
                        new AssertionValue(escaped2unicode(value))),
                     false);
                  break;
               case EQUALITY_MATCH:
                  if(value.equals("*")) { // present
                     tag = new ASN1Tagged(
                        new ASN1Identifier(ASN1Identifier.CONTEXT, false,
									                PRESENT),
                        new AttributeDescription(ft.getAttr()),
                        false);
                  }
                  else if(value.indexOf('*') != -1) { // substring
                     // parse: [initial], *any*, [final] into an
							// ASN1SequenceOf
                     StringTokenizer sub =
								new StringTokenizer(value, "*", true);
                     ASN1SequenceOf seq = new ASN1SequenceOf(5);
                     int tokCnt = sub.countTokens();
                     int cnt = 0;

                     while(sub.hasMoreTokens()) {
                        String subTok = sub.nextToken();
                        cnt++;
                        if(subTok.equals("*")) { // delimiter
                        }
                        else { // value (LDAPString)
                           if(cnt == 1) { // initial
                              seq.add(
                                 new ASN1Tagged(
                                    new ASN1Identifier(ASN1Identifier.CONTEXT,
                                                       false, INITIAL),
                                    new LDAPString(escaped2unicode(subTok)),
                                    false));
                           }
                           else if(cnt < tokCnt) { // any
                              seq.add(
                                 new ASN1Tagged(
                                    new ASN1Identifier(ASN1Identifier.CONTEXT,
                                                       false, ANY),
                                    new LDAPString(escaped2unicode(subTok)),
                                    false));
                           }
                           else { // final
                              seq.add(
                                 new ASN1Tagged(
                                    new ASN1Identifier(ASN1Identifier.CONTEXT,
                                                       false, FINAL),
                                    new LDAPString(escaped2unicode(subTok)),
                                    false));
                           }
                        }
                     }

                     tag = new ASN1Tagged(
                        new ASN1Identifier(ASN1Identifier.CONTEXT, true,
                                           SUBSTRINGS),
                        new SubstringFilter(
                           new AttributeDescription(ft.getAttr()),
                           seq),
                        false);
                  }
                  else { // simple
                     tag = new ASN1Tagged(
                        new ASN1Identifier(ASN1Identifier.CONTEXT, true,
                                           EQUALITY_MATCH),
                        new AttributeValueAssertion(
                           new AttributeDescription(ft.getAttr()),
                           new AssertionValue(escaped2unicode(value))),
                        false);
                  }
            }
      }
      return tag;

   }

   /**
    * Must have 1 or more Filters
    */
   private ASN1SetOf parseFilterList()
      throws LDAPException
   {
      ASN1SetOf set = new ASN1SetOf();

      set.add(parseFilter()); // must have at least 1 filter

      while(ft.peekChar() == '(') { // check for more filters
         set.add(parseFilter());
      }

      return set;
   }

   /**
    * Convert hex character to an integer. Return -1 if char is something
    * other than a hex char.
    */
   private int hex2int(char c)
   {
      return
         (c >= '0' && c <= '9') ? c - '0'      :
         (c >= 'A' && c <= 'F') ? c - 'A' + 10 :
         (c >= 'a' && c <= 'f') ? c - 'a' + 10 :
            -1;
   }

   /**
    * Replace escaped hex digits with the equivalent unicode representation.
    * Assume either V2 or V3 escape mechanisms:
    * V2: \*,  \(,  \),  \\.
    * V3: \2A, \28, \29, \5C, \00.
    */
   private String escaped2unicode(String value)
      throws LDAPException
   {
      StringBuffer sb = new StringBuffer();
      boolean escape = false, escStart = false;
      int ival;
      char ch, temp = 0;

      for(int i = 0; i < value.length(); i++) {
         ch = value.charAt(i);
         if(escape) {
            // Try LDAP V3 escape (\\xx)
            if((ival = hex2int(ch)) < 0) {
               if(escStart) {
                  // V2 escaped "*()" chars differently: \*, \(, \)
                  escape = false;
                  sb.append(ch);
               }
               else {
                  throw new LDAPException("Invalid escape value",
                                          LDAPException.FILTER_ERROR);
               }
            }
            else {
               if(escStart) {
                  temp = (char)(ival<<4);
                  escStart = false;
               }
               else {
                  temp |= (char)(ival);
                  sb.append(temp);
                  escape = false;
               }
            }
         }
         else if(ch != '\\') {
            sb.append(ch);
            escape = false;
         }
         else {
            escStart = escape = true;
         }
      }

      return sb.toString();
   }

}

/**
 * This class will tokenize the components of an RFC 2254 search filter.
 */
class FilterTokenizer {

   //*************************************************************************
   // Private variables
   //*************************************************************************

   private String filter; // The filter string to parse
   private String attr;   // Name of the attribute just parsed
   private int i;         // Offset pointer into the filter string
   private int len;       // Length of the filter string to parse

   //*************************************************************************
   // Constructor
   //*************************************************************************

	/**
	 * Constructs a FilterTokenizer for a filter.
	 */
   public FilterTokenizer(String filter) {
      this.filter = filter;
      this.i = 0;
      this.len = filter.length();
   }

   //*************************************************************************
   // Tokenizer methods
   //*************************************************************************

	/**
	 * Reads the current char and throws an Exception if it is not a left
	 * parenthesis.
	 */
   public void getLeftParen()
      throws LDAPException
   {
      if(i >= len)
         throw new LDAPException("Unexpected end of filter",
                                 LDAPException.FILTER_ERROR);

      if(filter.charAt(i++) != '(')
         throw new LDAPException("Missing left paren",
                                 LDAPException.FILTER_ERROR);
   }

	/**
	 * Reads the current char and throws an Exception if it is not a right
	 * parenthesis.
	 */
   public void getRightParen()
      throws LDAPException
   {
      if(i >= len)
         throw new LDAPException("Unexpected end of filter",
                                 LDAPException.FILTER_ERROR);

      if(filter.charAt(i++) != ')')
         throw new LDAPException("Missing right paren",
                                 LDAPException.FILTER_ERROR);
   }

	/**
	 * Reads either an operator (&, |, !), or an attribute, whichever is
	 * next in the filter string. If the next component is an attribute, it
	 * is read and stored in the attr field of this class which may be
	 * retrieved with getAttr() and a -1 is returned. Otherwise, the int
	 * value of the operator read is returned.
	 */
   public int getOpOrAttr()
      throws LDAPException
   {
      if(i >= len)
         throw new LDAPException("Unexpected end of filter",
                                 LDAPException.FILTER_ERROR);

      if(filter.charAt(i) == '&') {
			i++;
         return Filter.AND;
      }
      if(filter.charAt(i) == '|') {
			i++;
         return Filter.OR;
      }
      if(filter.charAt(i) == '!') {
			i++;
         return Filter.NOT;
      }

      String delims = "=~<>:()";
      StringBuffer sb = new StringBuffer();
      while(delims.indexOf(filter.charAt(i)) == -1) {
         sb.append(filter.charAt(i++));
      }

      attr = sb.toString().trim();
      return -1;
   }

	/**
	 * Reads an RFC 2251 filter type from the filter string and returns its
	 * int value.
	 */
   public int getFilterType()
      throws LDAPException
   {
      if(i >= len)
         throw new LDAPException("Unexpected end of filter",
                                 LDAPException.FILTER_ERROR);

      if(filter.startsWith(">=", i)) {
         i+=2;
         return Filter.GREATER_OR_EQUAL;
      }
      if(filter.startsWith("<=", i)) {
         i+=2;
         return Filter.LESS_OR_EQUAL;
      }
      if(filter.startsWith("~=", i)) {
         i+=2;
         return Filter.APPROX_MATCH;
      }
      if(filter.charAt(i) == '=') {
         i++;
         return Filter.EQUALITY_MATCH;
      }
      throw new LDAPException("Invalid filter type",
                              LDAPException.FILTER_ERROR);
   }

	/**
	 * Reads a value from a filter string and returns it after trimming any
	 * superfluous spaces from the beginning or end of the value.
	 */
   public String getValue()
		throws LDAPException
   {
      if(i >= len)
         throw new LDAPException("Unexpected end of filter",
                                 LDAPException.FILTER_ERROR);

      StringBuffer sb = new StringBuffer();
      while(i < len && filter.charAt(i) != ')') {
         sb.append(filter.charAt(i++));
      }

      return sb.toString().trim();
   }

	/**
	 * Returns the current attribute identifier.
	 */
   public String getAttr()
   {
      return attr;
   }

	/**
	 * Return the current char without advancing the offset pointer. This is
	 * used by ParseFilterList when determining if there are any more
	 * Filters in the list.
	 */
   public char peekChar()
      throws LDAPException
   {
      if(i >= len)
         throw new LDAPException("Unexpected end of filter",
                                 LDAPException.FILTER_ERROR);

      return filter.charAt(i);
   }

}

