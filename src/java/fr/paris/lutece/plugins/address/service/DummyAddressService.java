/*
 * Copyright (c) 2002-2012, Mairie de Paris
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

import fr.paris.lutece.plugins.address.business.jaxb.Adresse;
import fr.paris.lutece.plugins.address.business.jaxb.wsSearchAdresse.Adresses;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.ReferenceList;

import java.io.FileInputStream;
import java.io.StringReader;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import java.rmi.RemoteException;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;


/**
 *
 */
public class DummyAddressService implements IAddressService
{
    //jaxb context
    private static final String JAXB_CONTEXT_WS_FICHE_ADDRESS = "fr.paris.lutece.plugins.address.business.jaxb.wsFicheAdresse";
    private static final String JAXB_CONTEXT_WS_SEARCH_ADDRESS = "fr.paris.lutece.plugins.address.business.jaxb.wsSearchAdresse";

    //properties
    private static final String PROPERTY_XML_PATH_DIRECTORY = "address.xml.file.test.directory";
    private static final String PROPERTY_XML_FILE_SEARCH_ADDRESS = "address.xml.file.searchAddress";
    private static final String PROPERTY_XML_FILE_ADDRESS_INFO = "address.xml.file.addressInfo";
    private String _strUrlWS;
    private String _strDefaultCity;
    private String _strDateSearch;
    private String _strUserName;
    private String _strPassword;
    private String _strTimeOut;
    private Adresses _listAdresses;

    /**
    * @param request Request
    * @param labeladresse the  label adress
    * @return the XML flux of all adress corresponding
    *
    */
    public ReferenceList searchAddress( HttpServletRequest request, String labeladresse )
    {
        String strFluxAddress = null;

        String strFilePath = AppPathService.getPath( PROPERTY_XML_PATH_DIRECTORY );
        String strFileName = AppPropertiesService.getProperty( PROPERTY_XML_FILE_SEARCH_ADDRESS );

        byte[] out = new byte[128];

        try
        {
            out = read( strFilePath + strFileName );
        }
        catch ( Exception e )
        {
            AppLogService.error( e.getMessage(  ), e );
        }

        strFluxAddress = new String( out );

        //traitement du flux xml		
        Adresses adresses = null;

        JAXBContext jc;

        try
        {
            jc = JAXBContext.newInstance( JAXB_CONTEXT_WS_SEARCH_ADDRESS );

            Unmarshaller u = jc.createUnmarshaller(  );
            StringBuffer xmlStr = new StringBuffer( strFluxAddress );
            adresses = (Adresses) u.unmarshal( new StreamSource( new StringReader( xmlStr.toString(  ) ) ) );
        }
        catch ( JAXBException e )
        {
            AppLogService.error( e.getMessage(  ), e );
        }

        List<fr.paris.lutece.plugins.address.business.jaxb.wsSearchAdresse.Adresse> listAdresses = adresses.getAdresse(  );

        ReferenceList refList = null;

        //build the list choice
        if ( ( listAdresses != null ) && !listAdresses.isEmpty(  ) )
        {
            refList = new ReferenceList(  );

            for ( fr.paris.lutece.plugins.address.business.jaxb.wsSearchAdresse.Adresse currentAdresse : listAdresses )
            {
                String suffixe = "";

                if ( currentAdresse.getSuffixe(  ) != null )
                {
                    suffixe = currentAdresse.getSuffixe(  );
                }

                String strCurrentAdresse = currentAdresse.getNumero(  ) + " " + suffixe + " " +
                    currentAdresse.getTypeVoie(  ) + " " + currentAdresse.getNomVoie(  ) + " " +
                    currentAdresse.getCommune(  );

                String strIdAdresse = currentAdresse.getIdentifiant(  ).toString(  );

                refList.addItem( strIdAdresse, strCurrentAdresse );
            }

            _listAdresses = adresses;
        }

        return refList;
    }

    /**
     * @param request Request
     * @param labeladresse the  label adress
     * @param strArrondissement Arrondissement
     * @return the XML flux of all adress corresponding
     *
     */
    public ReferenceList searchAddress( HttpServletRequest request, String labeladresse, String strArrondissement )
    {
        String strFluxAddress = null;

        String strFilePath = AppPathService.getPath( PROPERTY_XML_PATH_DIRECTORY );
        String strFileName = AppPropertiesService.getProperty( PROPERTY_XML_FILE_SEARCH_ADDRESS );

        byte[] out = new byte[128];

        try
        {
            out = read( strFilePath + strFileName );
        }
        catch ( Exception e )
        {
            AppLogService.error( e.getMessage(  ), e );
        }

        strFluxAddress = new String( out );

        //traitement du flux xml		
        Adresses adresses = null;

        JAXBContext jc;

        try
        {
            jc = JAXBContext.newInstance( JAXB_CONTEXT_WS_SEARCH_ADDRESS );

            Unmarshaller u = jc.createUnmarshaller(  );
            StringBuffer xmlStr = new StringBuffer( strFluxAddress );
            adresses = (Adresses) u.unmarshal( new StreamSource( new StringReader( xmlStr.toString(  ) ) ) );
        }
        catch ( JAXBException e )
        {
            AppLogService.error( e.getMessage(  ), e );
        }

        List<fr.paris.lutece.plugins.address.business.jaxb.wsSearchAdresse.Adresse> listAdresses = adresses.getAdresse(  );

        ReferenceList refList = null;

        //build the list choice
        if ( ( listAdresses != null ) && !listAdresses.isEmpty(  ) )
        {
            refList = new ReferenceList(  );

            for ( fr.paris.lutece.plugins.address.business.jaxb.wsSearchAdresse.Adresse currentAdresse : listAdresses )
            {
                String suffixe = "";

                if ( currentAdresse.getSuffixe(  ) != null )
                {
                    suffixe = currentAdresse.getSuffixe(  );
                }

                String strCurrentAdresse = currentAdresse.getNumero(  ) + " " + suffixe + " " +
                    currentAdresse.getTypeVoie(  ) + " " + currentAdresse.getNomVoie(  ) + " " +
                    currentAdresse.getCommune(  );

                String strIdAdresse = currentAdresse.getIdentifiant(  ).toString(  );

                refList.addItem( strIdAdresse, strCurrentAdresse );
            }

            _listAdresses = adresses;
        }

        return refList;
    }

    /**
     * @throws RemoteException the RemoteExecption
     * @param request Request
     * @param id the adress id
     * @param bIsTest if true test connect at web service, if false search an adress
     * @return the XML flux of an adress
     *
     */
    public Adresse getGeolocalisation( HttpServletRequest request, long id, String strAddress, String strDate,
        boolean bIsTest )
    {
        String strFluxAddress = null;

        String strFilePath = AppPathService.getPath( PROPERTY_XML_PATH_DIRECTORY );
        String strFileName = AppPropertiesService.getProperty( PROPERTY_XML_FILE_ADDRESS_INFO );

        byte[] out = new byte[128];

        try
        {
            out = read( strFilePath + strFileName );
        }
        catch ( Exception e )
        {
            AppLogService.error( e.getMessage(  ), e );
        }

        strFluxAddress = new String( out );

        //traitement du flux xml
        fr.paris.lutece.plugins.address.business.jaxb.wsFicheAdresse.Adresse adresse = null;

        JAXBContext jc;

        try
        {
            jc = JAXBContext.newInstance( JAXB_CONTEXT_WS_FICHE_ADDRESS );

            Unmarshaller u = jc.createUnmarshaller(  );
            StringBuffer xmlStr = new StringBuffer( strFluxAddress );
            adresse = (fr.paris.lutece.plugins.address.business.jaxb.wsFicheAdresse.Adresse) u.unmarshal( new StreamSource( 
                        new StringReader( xmlStr.toString(  ) ) ) );
        }
        catch ( JAXBException e )
        {
            AppLogService.error( e.getMessage(  ), e );
        }

        Adresse adresseReturn = new Adresse(  );

        adresseReturn.setIadresse( adresse.getIdentifiant(  ) );
        adresseReturn.setDunumero( adresse.getNumero(  ) );
        adresseReturn.setDubis( adresse.getSuffixe1(  ) );
        adresseReturn.setCodeCommune( adresse.getCodeInsee(  ).toString(  ) );

        String responseWebService = adresse.getGeometry(  );
        responseWebService = responseWebService.substring( responseWebService.lastIndexOf( "(" ) + 1,
                responseWebService.length(  ) - 1 );

        adresseReturn.setGeoX( Float.parseFloat( responseWebService.substring( 0, responseWebService.lastIndexOf( " " ) ) ) );
        adresseReturn.setGeoY( Float.parseFloat( responseWebService.substring( responseWebService.lastIndexOf( " " ),
                    responseWebService.length(  ) ) ) );

        if ( !bIsTest )
        {
            List<fr.paris.lutece.plugins.address.business.jaxb.wsSearchAdresse.Adresse> listAddress = _listAdresses.getAdresse(  );

            for ( fr.paris.lutece.plugins.address.business.jaxb.wsSearchAdresse.Adresse currentAdresse : listAddress )
            {
                if ( String.valueOf( currentAdresse.getIdentifiant(  ) ).equals( String.valueOf( id ) ) )
                {
                    adresseReturn.setTypeVoie( currentAdresse.getTypeVoie(  ) );
                    adresseReturn.setLibelleVoie( currentAdresse.getNomVoie(  ) );

                    break;
                }
            }
        }

        return adresseReturn;
    }

    /**
    * @param request Request
    * @param id the adress id
    * @param bIsTest if true test connect at web service, if false search an adress
    * @return the XML flux of an adress
    *
    */
    public Adresse getAdresseInfo( HttpServletRequest request, long id, boolean bIsTest )
    {
        String strFluxAddress = null;

        String strFilePath = AppPathService.getPath( PROPERTY_XML_PATH_DIRECTORY );
        String strFileName = AppPropertiesService.getProperty( PROPERTY_XML_FILE_ADDRESS_INFO );

        byte[] out = new byte[128];

        try
        {
            out = read( strFilePath + strFileName );
        }
        catch ( Exception e )
        {
            AppLogService.error( e.getMessage(  ), e );
        }

        strFluxAddress = new String( out );

        //traitement du flux xml
        fr.paris.lutece.plugins.address.business.jaxb.wsFicheAdresse.Adresse adresse = null;

        JAXBContext jc;

        try
        {
            jc = JAXBContext.newInstance( JAXB_CONTEXT_WS_FICHE_ADDRESS );

            Unmarshaller u = jc.createUnmarshaller(  );
            StringBuffer xmlStr = new StringBuffer( strFluxAddress );
            adresse = (fr.paris.lutece.plugins.address.business.jaxb.wsFicheAdresse.Adresse) u.unmarshal( new StreamSource( 
                        new StringReader( xmlStr.toString(  ) ) ) );
        }
        catch ( JAXBException e )
        {
            AppLogService.error( e.getMessage(  ), e );
        }

        Adresse adresseReturn = new Adresse(  );

        adresseReturn.setIadresse( adresse.getIdentifiant(  ) );
        adresseReturn.setDunumero( adresse.getNumero(  ) );
        adresseReturn.setDubis( adresse.getSuffixe1(  ) );
        adresseReturn.setCodeCommune( adresse.getCodeInsee(  ).toString(  ) );

        if ( !bIsTest )
        {
            List<fr.paris.lutece.plugins.address.business.jaxb.wsSearchAdresse.Adresse> listAddress = _listAdresses.getAdresse(  );

            for ( fr.paris.lutece.plugins.address.business.jaxb.wsSearchAdresse.Adresse currentAdresse : listAddress )
            {
                if ( String.valueOf( currentAdresse.getIdentifiant(  ) ).equals( String.valueOf( id ) ) )
                {
                    adresseReturn.setTypeVoie( currentAdresse.getTypeVoie(  ) );
                    adresseReturn.setLibelleVoie( currentAdresse.getNomVoie(  ) );

                    break;
                }
            }
        }

        return adresseReturn;
    }

    /**
     * @throws Exception the execption
     * @param file the file
     * @return the byte array of file
     *
     */
    private byte[] read( String file ) throws Exception // lots of exceptions
    {
        FileInputStream fis = new FileInputStream( file );

        FileChannel fc = fis.getChannel(  );
        byte[] data = new byte[(int) fc.size(  )]; // fc.size returns the size
        ByteBuffer bb = ByteBuffer.wrap( data );
        fc.read( bb );

        return data;
    }

    /**
    *
    * @return the date for parameter methodes of web service
    */
    public String getDateSearch(  )
    {
        return _strDateSearch;
    }

    /**
     *
     * @param strDateSearch the new date search
     */
    public void setDateSearch( String strDateSearch )
    {
        _strDateSearch = strDateSearch;
    }

    /**
     *
     * @return the default city for parameter methodes of web service
     */
    public String getDefaultCity(  )
    {
        return _strDefaultCity;
    }

    /**
     *
     * @param strDefaultCity the new default city
     */
    public void setDefaultCity( String strDefaultCity )
    {
        _strDefaultCity = strDefaultCity;
    }

    /**
     *
     * @return the url of the web service
     */
    public String getUrlWS(  )
    {
        return _strUrlWS;
    }

    /**
     *
     * @param strUrlWS the new web service url
     */
    public void setUrlWS( String strUrlWS )
    {
        _strUrlWS = strUrlWS;
    }

    /**
     *
     * @return the password
     */
    public String getPassword(  )
    {
        return _strPassword;
    }

    /**
     *
     * @param password the password
     */
    public void setPassword( String password )
    {
        _strPassword = password;
    }

    /**
     *
     * @return the user name
     */
    public String getUserName(  )
    {
        return _strUserName;
    }

    /**
     *
     * @param userName the user name
     */
    public void setUserName( String userName )
    {
        _strUserName = userName;
    }

    /**
    *
    * @return the timeout
    */
    public String getTimeOut(  )
    {
        return _strTimeOut;
    }

    /**
     *
     * @param timeOut the timeout
     */
    public void setTimeOut( String timeOut )
    {
        _strTimeOut = timeOut;
    }

    public Adresse getGeolocalisation( HttpServletRequest request, String addresse, String date, boolean bIsTest )
        throws RemoteException
    {
        return getGeolocalisation( request, 0, addresse, date, bIsTest );
    }

    /**
     *
     *{@inheritDoc}
     */
    public ReferenceList searchAddress( HttpServletRequest request, String labeladresse, String strSRID,
        String strArrondissement ) throws RemoteException
    {
        return searchAddress( request, labeladresse, strArrondissement );
    }
}
