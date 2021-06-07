/*
 * Copyright (c) 2002-2021, City of Paris
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
package fr.paris.lutece.plugins.address.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import fr.paris.lutece.plugins.address.business.jaxb.Adresse;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.ReferenceItem;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.httpaccess.HttpAccess;
import fr.paris.lutece.util.httpaccess.HttpAccessException;
import fr.paris.lutece.util.string.StringUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import javassist.bytecode.stackmap.BasicBlock;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 *
 */
public class RestAddressService implements IAddressService
{

    // Plugin properties
    private static final String PROPERTY_URL_ADR_COMPLETION = "address.teleservice.url.adr.completion";
    private static final String PROPERTY_URL_ADR_LOCATION = "address.teleservice.url.adr.location";

    // constants (for synchro)
    private static final String CONSTANT_KEY_FEATURES = "Features";
    private static final String CONSTANT_KEY_ADDRESS_ID = "Idadrposte";
    private static final String CONSTANT_KEY_ADDRESS_TYPO = "Adressetypo";
    private static final String CONSTANT_KEY_PROPERTIES = "properties";
    private static final String CONSTANT_KEY_NUM_DISTRICT = "Nqu";
    private static final String CONSTANT_KEY_GEOLOCATION_X = "X";
    private static final String CONSTANT_KEY_GEOLOCATION_Y = "Y";

    // constants
    private static final String SECURITY_LOGGER_NAME = "lutece.security.http";

    /**
     * @param request
     *            Request
     * @param searchTerm
     * @return the XML flux of all adress corresponding
     *
     */
    @Override
    public ReferenceList searchAddress( HttpServletRequest request, String searchTerm )
    {
        ReferenceList list = null;

        try
        {
            String strUri = AppPropertiesService.getProperty( PROPERTY_URL_ADR_COMPLETION );

            HttpAccess ha = new HttpAccess( );
            if ( StringUtil.containsXssCharacters( searchTerm ) )
            {
                Logger logger = Logger.getLogger( SECURITY_LOGGER_NAME );
                logger.warn( "SECURITY WARNING : XSS CHARACTERS DETECTED : " + searchTerm );
                return null;
            }

            String url = strUri + URLEncoder.encode( searchTerm, "UTF-8" ).replaceAll( "\\+", "%20" );
            String strJson = ha.doGet( url );

            if ( strJson == null )
            {
                AppLogService.info( "Error while getting URI : " + strUri + URLEncoder.encode( searchTerm, "UTF-8" ) );
                return null;
            }

            ObjectMapper mapper = new ObjectMapper( );
            JsonNode jsonNode = null;

            try
            {
                jsonNode = mapper.readTree( strJson );
            }
            catch( IOException e )
            {
                AppLogService.info( "Error while reading json : " + strJson );
                return null;
            }

            return jsonToAddressList( jsonNode );

        }
        catch( HttpAccessException | UnsupportedEncodingException e )
        {
            AppLogService.info( e );
            return null;
        }
    }

    /**
     * Build address referenceList from json
     * 
     * @param jsonNode
     * @return address list
     */
    private static ReferenceList jsonToAddressList( JsonNode jsonNode )
    {

        ReferenceList list = new ReferenceList( );
        ArrayNode adrList = (ArrayNode) jsonNode;

        if ( adrList != null )
        {
            for ( JsonNode adr : adrList )
            {
                JsonNode jsonAdrId = adr.get( CONSTANT_KEY_ADDRESS_ID );
                JsonNode jsonAdrTypo = adr.get( CONSTANT_KEY_ADDRESS_TYPO );

                ReferenceItem item = new ReferenceItem( );
                item.setCode( jsonAdrId.asText( ) );
                item.setName( jsonAdrTypo.asText( ) );

                list.add( item );
            }
        }

        return list;
    }

    /**
     * @param request
     *            Request
     * @param term
     * @param strArrondissement
     *            Arrondissement
     * @return the XML flux of all adress corresponding
     *
     */
    @Override
    public ReferenceList searchAddress( HttpServletRequest request, String term, String strArrondissement )
    {

        return searchAddress( request, term );
    }

    /**
     * @param request
     *            Request
     * @param idAdr
     *            the adress id
     * @return the XML flux of an adress
     *
     */
    public Adresse getGeolocalisation( HttpServletRequest request, String idAdr )
    {

        if ( StringUtil.containsXssCharacters( idAdr ) )
        {
            Logger logger = Logger.getLogger( SECURITY_LOGGER_NAME );
            logger.warn( "SECURITY WARNING : XSS CHARACTERS DETECTED : " + idAdr );
            return null;
        }

        try
        {
            long id = Long.parseLong( idAdr );
            return getGeolocalisation( request, id, null, null, false );
        }
        catch( NumberFormatException e )
        {
            return null;
        }
    }

    /**
     * @param request
     *            Request
     * @param idAdr
     *            the adress id
     * @param strAddress
     * @param strDate
     * @param bIsTest
     *            if true test connect at web service, if false search an adress
     * @return the XML flux of an adress
     *
     */
    @Override
    public Adresse getGeolocalisation( HttpServletRequest request, long idAdr, String strAddress, String strDate, boolean bIsTest )
    {
        try
        {
            String strUri = AppPropertiesService.getProperty( PROPERTY_URL_ADR_LOCATION );

            HttpAccess ha = new HttpAccess( );

            String strJson = ha.doGet( strUri + String.valueOf( idAdr ) );

            if ( strJson == null )
            {
                AppLogService.info( "Error while getting URI : " + strUri + idAdr );
                return null;
            }

            ObjectMapper mapper = new ObjectMapper( );
            JsonNode jsonNode;

            try
            {
                jsonNode = mapper.readTree( strJson );
            }
            catch( IOException e )
            {
                AppLogService.info( "Error while reading json : " + strJson );
                return null;
            }

            return jsonToGeoloc( jsonNode );
        }
        catch( HttpAccessException e )
        {
            AppLogService.error( e );
            return null;
        }
    }

    /**
     * get sector numre from json
     * 
     * @param jsonNode
     * @return the sector
     */
    private static Adresse jsonToGeoloc( JsonNode jsonNode )
    {

        // Parse json
        ArrayNode features = (ArrayNode) jsonNode.get( CONSTANT_KEY_FEATURES );
        if ( features != null )
        {
            for ( JsonNode json : features )
            {
                JsonNode properties = json.get( CONSTANT_KEY_PROPERTIES );
                if ( properties != null )
                {
                    JsonNode jsonGeolocX = properties.get( CONSTANT_KEY_GEOLOCATION_X );
                    JsonNode jsonGeolocY = properties.get( CONSTANT_KEY_GEOLOCATION_Y );

                    if ( jsonGeolocX != null )
                    {
                        Adresse adresseReturn = new Adresse( );

                        adresseReturn.setGeoX( jsonGeolocX.asLong( ) );
                        adresseReturn.setGeoY( jsonGeolocY.asLong( ) );
                    }

                }
            }
        }

        // default
        return null;
    }

    /**
     * @param request
     *            Request
     * @param id
     *            the adress id
     * @param bIsTest
     *            if true test connect at web service, if false search an adress
     * @return the XML flux of an adress
     *
     */
    public Adresse getAdresseInfo( HttpServletRequest request, long id, boolean bIsTest )
    {

        Adresse adresseReturn = new Adresse( );

        return adresseReturn;
    }

    @Override
    public ReferenceList searchAddress( HttpServletRequest request, String labeladresse, String strSRID, String strArrondissement ) throws RemoteException
    {
        throw new UnsupportedOperationException( "Not supported yet." ); // To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Adresse getGeolocalisation( HttpServletRequest request, String addresse, String date, boolean bIsTest ) throws RemoteException
    {
        return getGeolocalisation( request, addresse, date, bIsTest );
    }

}
