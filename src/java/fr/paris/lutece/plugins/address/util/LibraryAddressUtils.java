/*
 * Copyright (c) 2002-2011, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.address.util;

import fr.paris.lutece.plugins.address.business.jaxb.Adresse;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;


/**
 * Utils methods for library address
 */
public final class LibraryAddressUtils
{
    public static final String CONSTANT_COMA = ",";
    public static final String CONSTANT_MULTI_SPACE = "\\s+";
    public static final String CONSTANT_ONE_SPACE = " ";
    public static final String CONSTANT_OPEN_PARENTHESIS = "(";
    public static final String CONSTANT_CLOSE_PARENTHESIS = ")";
    public static final String VALID_GEOMETRY_REGEX = ".*\\(.+ .+\\)";
    private static final String PROPERTY_VALUES = "address.endTypeVoie.values";
    private static final String PROPERTY_SEPARATOR = "address.endTypeVoie.separator";

    /**
     *
     *
     */
    private LibraryAddressUtils(  )
    {
    }

    /**
     * Replace a sequence of space by one space
     * @param strChaine the chaine
     * @return the chaine with one space beetween words
     */
    public static String removeMultiSpace( String strChaine )
    {
        return strChaine.replaceAll( CONSTANT_MULTI_SPACE, CONSTANT_ONE_SPACE );
    }

    /**
     * Test strTypeVoie is termitate by  a value of list strListValuesTerminator
     * @param strTypeVoie the type voie to test
     * @return true if a value of strTypeVoie is terminate by a value of list strListValuesTerminator
     */
    public static boolean isTerminateByApostrophe( String strTypeVoie )
    {
        boolean bReturn = false;

        String strListValues = AppPropertiesService.getProperty( PROPERTY_VALUES );
        String strSeparator = AppPropertiesService.getProperty( PROPERTY_SEPARATOR );

        if ( ( strListValues == null ) || ( strSeparator == null ) )
        {
            return bReturn;
        }

        String[] arrayValues = strListValues.split( strSeparator );

        for ( String strCurrentValue : arrayValues )
        {
            if ( ( strTypeVoie != null ) &&
                    strTypeVoie.substring( strTypeVoie.length(  ) - 2 ).equalsIgnoreCase( strCurrentValue ) )
            {
                bReturn = true;
            }
        }

        return bReturn;
    }

    /**
     * Parse a long
     * @param strValue the value to parse
     * @return the long value of <code>strValue</code>, <code>-1</code> otherwise.
     */
    public static long parseLong( String strValue )
    {
        return parseLong( strValue, -1 );
    }

    /**
     * Parse a long
     * @param strValue the value to parse
     * @param nDefaultValue the default value
     * @return the long value of <code>strValue</code>, <code>nDefaultValue</code> otherwise.
     */
    public static long parseLong( String strValue, long nDefaultValue )
    {
        try
        {
            return Long.parseLong( strValue );
        }
        catch ( NumberFormatException nfe )
        {
            return nDefaultValue;
        }
    }

    /**
     * Parse a long
     * @param strValue the value to parse
     * @return the int value of <code>strValue</code>, <code>-1</code> otherwise.
     */
    public static int parseInt( String strValue )
    {
        return parseInt( strValue, -1 );
    }

    /**
     * Parse a long
     * @param strValue the value to parse
     * @param nDefaultValue the default value
     * @return the int value of <code>strValue</code>, <code>nDefaultValue</code> otherwise.
     */
    public static int parseInt( String strValue, int nDefaultValue )
    {
        try
        {
            return Integer.parseInt( strValue );
        }
        catch ( NumberFormatException nfe )
        {
            return nDefaultValue;
        }
    }

    /**
     * Fills the address with x and y geolocation using strGeometry.
     * <code>POINT (123.456789 987.654321)</code> will give <code>x = 123.456789</code> and <code>y = 987.654321</code>.
     * Set x and y to 0 if x or y is not a number.
     * @param adresse the address to fill
     * @param strGeometry the geometry string
     */
    public static void fillAddressGeolocation( Adresse adresse, String strGeometry )
    {
        if ( StringUtils.isNotBlank( strGeometry ) && strGeometry.matches( VALID_GEOMETRY_REGEX ) )
        {
            String strCleanedGeometry = strGeometry.substring( strGeometry.lastIndexOf( CONSTANT_OPEN_PARENTHESIS ) +
                    1, strGeometry.length(  ) - 1 );

            try
            {
                adresse.setGeoX( Float.parseFloat( strCleanedGeometry.substring( 0,
                            strCleanedGeometry.lastIndexOf( CONSTANT_ONE_SPACE ) ) ) );
                adresse.setGeoY( Float.parseFloat( strCleanedGeometry.substring( strCleanedGeometry.lastIndexOf( 
                                CONSTANT_ONE_SPACE ), strCleanedGeometry.length(  ) ) ) );
            }
            catch ( NumberFormatException nfe )
            {
                // set to 0
                AppLogService.error( "LibraryAddressUtils.fillAddressGeolocation failed for " + strGeometry + " " +
                    nfe.getLocalizedMessage(  ) );
                adresse.setGeoX( 0 );
                adresse.setGeoY( 0 );
            }
        }
    }

    /**
     * String representation of the adresse
     * @param adresse the adresse
     * @return the string
     */
    public static String normalizeAddress( Adresse adresse )
    {
        StringBuilder sbAddress = new StringBuilder(  );

        sbAddress.append( ObjectUtils.toString( adresse.getDunumero(  ) ) );
        sbAddress.append( CONSTANT_ONE_SPACE );

        if ( StringUtils.isNotBlank( adresse.getDubis(  ) ) )
        {
            sbAddress.append( ObjectUtils.toString( adresse.getDubis(  ) ) );
            sbAddress.append( CONSTANT_ONE_SPACE );
        }

        sbAddress.append( ObjectUtils.toString( adresse.getTypeVoie(  ) ) );

        if ( !LibraryAddressUtils.isTerminateByApostrophe( adresse.getTypeVoie(  ) ) )
        {
            sbAddress.append( CONSTANT_ONE_SPACE );
        }

        sbAddress.append( ObjectUtils.toString( adresse.getLibelleVoie(  ) ) );
        sbAddress.append( CONSTANT_COMA );
        sbAddress.append( CONSTANT_ONE_SPACE );

        if ( StringUtils.isNotBlank( adresse.getComplement1Adresse(  ) ) )
        {
            sbAddress.append( adresse.getComplement1Adresse(  ) );
            sbAddress.append( CONSTANT_COMA );
            sbAddress.append( CONSTANT_ONE_SPACE );
        }

        if ( StringUtils.isNotBlank( adresse.getComplement2Adresse(  ) ) )
        {
            sbAddress.append( adresse.getComplement2Adresse(  ) );
            sbAddress.append( CONSTANT_COMA );
            sbAddress.append( CONSTANT_ONE_SPACE );
        }

        sbAddress.append( ObjectUtils.toString( adresse.getVille(  ) ) );

        return sbAddress.toString(  );
    }
}
