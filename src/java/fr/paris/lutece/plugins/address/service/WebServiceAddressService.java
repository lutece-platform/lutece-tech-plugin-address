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

import fr.paris.lutece.plugins.address.business.axis.AdresseService;
import fr.paris.lutece.plugins.address.business.axis.AdresseServiceLocator;
import fr.paris.lutece.plugins.address.business.axis.AdresseServicePortType;
import fr.paris.lutece.plugins.address.business.jaxb.Adresse;
import fr.paris.lutece.plugins.address.business.jaxb.wsSearchAdresse.Adresses;
import fr.paris.lutece.plugins.address.util.LibraryAddressUtils;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.ReferenceList;

import org.apache.axis.client.Stub;

import java.io.StringReader;

import java.net.MalformedURLException;
import java.net.URL;

import java.rmi.RemoteException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.rpc.ServiceException;
import javax.xml.transform.stream.StreamSource;


/**
 *
 */
public class WebServiceAddressService implements IAddressService
{
    //jaxb context
    private static final String JAXB_CONTEXT_WS_FICHE_ADDRESS = "fr.paris.lutece.plugins.address.business.jaxb.wsFicheAdresse";
    private static final String JAXB_CONTEXT_WS_SEARCH_ADDRESS = "fr.paris.lutece.plugins.address.business.jaxb.wsSearchAdresse";
    private static final String SESSION_LIST_ADDRESS_NAME = "LIBRARY" + "_WSLISTADDRESS";

    /**
     * address.geolocation.rsid
     */
    private static final String PROPERTY_GEOLOCATION_RSID = "address.geolocation.rsid";

    /**
     * Default RSID for geolocalization.
     */
    private static final int CONSTANTE_DEFAULT_GEOLOCATION_RSID = 27561;
    private String _strUrlWS;
    private String _strDefaultCity;
    private String _strDateSearch;
    private String _strUserName;
    private String _strPassword;
    private String _strTimeOut;

    /**
     * RSID using {@link #PROPERTY_GEOLOCATION_RSID} if found, {@link #CONSTANTE_DEFAULT_GEOLOCATION_RSID} otherwise.
     * @return RSID value
     */
    private int getGeolocationRSID(  )
    {
        return AppPropertiesService.getPropertyInt( PROPERTY_GEOLOCATION_RSID, CONSTANTE_DEFAULT_GEOLOCATION_RSID );
    }

    /**
     * Finds the geolocalsation.
     * Uses {@link #getGeolocationRSID()} as RSID.
     * @throws RemoteException the RemoteExecption
     * @param request Request
     * @param bIsTest if true test connect at web service, if false search an adress
     * @return the XML flux of an adress
     *
     */
    public Adresse getGeolocalisation( HttpServletRequest request, String addresse, String date, boolean bIsTest )
        throws RemoteException
    {
        return getGeolocalisation( request, getGeolocationRSID(  ), addresse, date, bIsTest );
    }

    /**
     * @throws RemoteException the RemoteExecption
     * @param request Request
     * @param id the rsid
     * @param bIsTest if true test connect at web service, if false search an adress
     * @return the XML flux of an adress
     *
     */
    public Adresse getGeolocalisation( HttpServletRequest request, long id, String addresse, String date,
        boolean bIsTest ) throws RemoteException
    {
        String responseWebService = null;
        AdresseService adresseService = new AdresseServiceLocator(  );

        try
        {
            URL urlWS = null;

            Stub portType = null;

            if ( ( getUrlWS(  ) == null ) || getUrlWS(  ).equals( "" ) )
            {
                portType = (Stub) adresseService.getAdresseServiceHttpPort(  );
            }
            else
            {
                try
                {
                    urlWS = new URL( getUrlWS(  ) );
                }
                catch ( MalformedURLException e )
                {
                    AppLogService.error( e.getMessage(  ), e );
                }

                portType = (Stub) adresseService.getAdresseServiceHttpPort( urlWS );
            }

            portType.setUsername( getUserName(  ) );
            portType.setPassword( getPassword(  ) );

            setTimeout( portType );

            responseWebService = ( (AdresseServicePortType) portType ).geolocalization( getDefaultCity(  ), addresse,
                    String.valueOf( id ), date );
        }
        catch ( ServiceException e )
        {
            AppLogService.error( e.getMessage(  ), e );
        }

        Adresse adresseReturn = new Adresse(  );

        LibraryAddressUtils.fillAddressGeolocation( adresseReturn, responseWebService );

        cleanListWSAdresses( request, SESSION_LIST_ADDRESS_NAME );

        return adresseReturn;
    }

    /**
    * @throws RemoteException the RemoteExecption
    * @param request Request
    * @param id the adress id
    * @param bIsTest if true test connect at web service, if false search an adress
    * @return the XML flux of an adress
    *
    */
    public Adresse getAdresseInfo( HttpServletRequest request, long id, boolean bIsTest )
        throws RemoteException
    {
        String responseWebService = null;
        AdresseService adresseService = new AdresseServiceLocator(  );

        try
        {
            URL urlWS = null;

            Stub portType = null;

            if ( ( getUrlWS(  ) == null ) || getUrlWS(  ).equals( "" ) )
            {
                portType = (Stub) adresseService.getAdresseServiceHttpPort(  );
            }
            else
            {
                try
                {
                    urlWS = new URL( getUrlWS(  ) );
                }
                catch ( MalformedURLException e )
                {
                    AppLogService.error( e.getMessage(  ), e );
                }

                portType = (Stub) adresseService.getAdresseServiceHttpPort( urlWS );
            }

            portType.setUsername( getUserName(  ) );
            portType.setPassword( getPassword(  ) );

            setTimeout( portType );

            responseWebService = ( (AdresseServicePortType) portType ).getAdresseInfo( getDefaultCity(  ), id );
        }
        catch ( ServiceException e )
        {
            AppLogService.error( e.getMessage(  ), e );
        }

        //      traitement du flux xml
        fr.paris.lutece.plugins.address.business.jaxb.wsFicheAdresse.Adresse adresse = null;

        JAXBContext jc;

        try
        {
            jc = JAXBContext.newInstance( JAXB_CONTEXT_WS_FICHE_ADDRESS );

            Unmarshaller u = jc.createUnmarshaller(  );
            StringBuffer xmlStr = new StringBuffer( responseWebService );
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
        adresseReturn.setVille( adresse.getCommune(  ) );

        // FIXME : use this when SRID can be passed to getAdressInfo
        // String strGeometry = adresse.getGeometry(  );
        // 
        // LibraryAddressUtils.fillAddressGeolocation( adresseReturn, strGeometry );
        if ( !bIsTest )
        {
            List<fr.paris.lutece.plugins.address.business.jaxb.wsSearchAdresse.Adresse> listAddress = getListWSAdresses( request )
                                                                                                          .getAdresse(  );

            for ( fr.paris.lutece.plugins.address.business.jaxb.wsSearchAdresse.Adresse currentAdresse : listAddress )
            {
                if ( String.valueOf( currentAdresse.getIdentifiant(  ) ).equals( String.valueOf( id ) ) )
                {
                    adresseReturn.setTypeVoie( currentAdresse.getTypeVoie(  ) );
                    adresseReturn.setLibelleVoie( currentAdresse.getNomVoie(  ) );

                    // FIXME : see on top
                    LibraryAddressUtils.fillAddressGeolocation( adresseReturn, currentAdresse.getGeometry(  ) );

                    break;
                }
            }
        }

        cleanListWSAdresses( request, SESSION_LIST_ADDRESS_NAME );

        return adresseReturn;
    }

    /**
    * @throws RemoteException the RemoteExecption
    * @param request Request
    * @param labeladresse the  label adress
    * @return the XML flux of all adress corresponding
    *
    */
    public ReferenceList searchAddress( HttpServletRequest request, String labeladresse )
        throws RemoteException
    {
        String responseWebService = null;
        AdresseService adresseService = new AdresseServiceLocator(  );

        try
        {
            URL urlWS = null;

            Stub portType = null;

            if ( ( getUrlWS(  ) == null ) || getUrlWS(  ).equals( "" ) )
            {
                portType = (Stub) adresseService.getAdresseServiceHttpPort(  );
            }
            else
            {
                try
                {
                    urlWS = new URL( getUrlWS(  ) );
                }
                catch ( MalformedURLException e )
                {
                    AppLogService.error( e.getMessage(  ), e );
                }

                portType = (Stub) adresseService.getAdresseServiceHttpPort( urlWS );
            }

            portType.setUsername( getUserName(  ) );
            portType.setPassword( getPassword(  ) );

            setTimeout( portType );

            responseWebService = ( (AdresseServicePortType) portType ).searchAddress( getDefaultCity(  ), labeladresse,
                    null, getDateSearch(  ) );

            // check null result and then return null list
            if ( responseWebService == null )
            {
                return null;
            }
        }
        catch ( ServiceException e )
        {
            AppLogService.error( e.getMessage(  ), e );
        }

        //traitement du flux xml		
        Adresses adresses = null;

        JAXBContext jc;

        try
        {
            jc = JAXBContext.newInstance( JAXB_CONTEXT_WS_SEARCH_ADDRESS );

            Unmarshaller u = jc.createUnmarshaller(  );
            StringBuffer xmlStr = new StringBuffer( responseWebService );
            adresses = (Adresses) u.unmarshal( new StreamSource( new StringReader( xmlStr.toString(  ) ) ) );
        }
        catch ( JAXBException e )
        {
            AppLogService.error( e.getMessage(  ), e );
        }

        List<fr.paris.lutece.plugins.address.business.jaxb.wsSearchAdresse.Adresse> listAdresses = adresses.getAdresse(  );

        ReferenceList refList = null;

        //      Added for filter the double adresse
        Set<AdresseElement> eltSet = null;

        //build the list choice
        if ( ( listAdresses != null ) && !listAdresses.isEmpty(  ) )
        {
            refList = new ReferenceList(  );

            //Added for filter the double adresse
            eltSet = new HashSet<AdresseElement>(  );

            for ( fr.paris.lutece.plugins.address.business.jaxb.wsSearchAdresse.Adresse currentAdresse : listAdresses )
            {
                String suffixe = "";

                if ( currentAdresse.getSuffixe(  ) != null )
                {
                    suffixe = currentAdresse.getSuffixe(  );
                }

                String strCurrentAdresse = "";

                if ( currentAdresse.getTypeVoie(  ) == null )
                {
                    strCurrentAdresse = currentAdresse.getNumero(  ) + " " + suffixe + " " +
                        currentAdresse.getNomVoie(  ) + " " + currentAdresse.getCommune(  );
                }
                else if ( LibraryAddressUtils.isTerminateByApostrophe( currentAdresse.getTypeVoie(  ) ) )
                {
                    strCurrentAdresse = currentAdresse.getNumero(  ) + " " + suffixe + " " +
                        currentAdresse.getTypeVoie(  ) + currentAdresse.getNomVoie(  ) + " " +
                        currentAdresse.getCommune(  );
                }
                else
                {
                    strCurrentAdresse = currentAdresse.getNumero(  ) + " " + suffixe + " " +
                        currentAdresse.getTypeVoie(  ) + " " + currentAdresse.getNomVoie(  ) + " " +
                        currentAdresse.getCommune(  );
                }

                String strIdAdresse = currentAdresse.getIdentifiant(  ).toString(  );

                //Added for filter the double adresse
                boolean isAdded = eltSet.add( new AdresseElement( strIdAdresse, strCurrentAdresse ) );

                if ( isAdded )
                {
                    refList.addItem( strIdAdresse, strCurrentAdresse );
                }
            }

            setListWSAdresses( request, adresses );
        }

        return refList;
    }

    /**
     * @throws RemoteException the RemoteExecption
     * @param request Request
     * @param labeladresse the  label adress
     * @param strArrondissement Arrondissement
     * @return the XML flux of all adress corresponding
     * @see <a href="http://dev.lutece.paris.fr/jira/browse/ADDRESS-8">ADDRESS-8</a>
     *
     */
    public ReferenceList searchAddress( HttpServletRequest request, String labeladresse, String strArrondissement )
        throws RemoteException
    {
        return searchAddress( request, labeladresse, Integer.toString( getGeolocationRSID(  ) ), strArrondissement );
    }

    /**
     * @throws RemoteException the RemoteExecption
     * @param strSRID the srsid
     * @param request Request
     * @param labeladresse the  label adress
     * @param strArrondissement Arrondissement
     * @return the XML flux of all adress corresponding
     * @see <a href="http://dev.lutece.paris.fr/jira/browse/ADDRESS-8">ADDRESS-8</a>
     */
    public ReferenceList searchAddress( HttpServletRequest request, String labeladresse, String strSRID,
        String strArrondissement ) throws RemoteException
    {
        String responseWebService = null;
        AdresseService adresseService = new AdresseServiceLocator(  );

        try
        {
            URL urlWS = null;

            Stub portType = null;

            if ( ( getUrlWS(  ) == null ) || getUrlWS(  ).equals( "" ) )
            {
                portType = (Stub) adresseService.getAdresseServiceHttpPort(  );
            }
            else
            {
                try
                {
                    urlWS = new URL( getUrlWS(  ) );
                }
                catch ( MalformedURLException e )
                {
                    AppLogService.error( e.getMessage(  ), e );
                }

                portType = (Stub) adresseService.getAdresseServiceHttpPort( urlWS );
            }

            portType.setUsername( getUserName(  ) );
            portType.setPassword( getPassword(  ) );

            setTimeout( portType );

            responseWebService = ( (AdresseServicePortType) portType ).searchAddress( getDefaultCity(  ), labeladresse,
                    strSRID, getDateSearch(  ) );

            // check null result and then return null list
            if ( responseWebService == null )
            {
                return null;
            }
        }
        catch ( ServiceException e )
        {
            AppLogService.error( e.getMessage(  ), e );
        }

        //traitement du flux xml		
        Adresses adresses = null;

        JAXBContext jc;

        try
        {
            jc = JAXBContext.newInstance( JAXB_CONTEXT_WS_SEARCH_ADDRESS );

            Unmarshaller u = jc.createUnmarshaller(  );
            StringBuffer xmlStr = new StringBuffer( responseWebService );
            adresses = (Adresses) u.unmarshal( new StreamSource( new StringReader( xmlStr.toString(  ) ) ) );
        }
        catch ( JAXBException e )
        {
            AppLogService.error( e.getMessage(  ), e );
        }

        List<fr.paris.lutece.plugins.address.business.jaxb.wsSearchAdresse.Adresse> listAdresses = adresses.getAdresse(  );

        ReferenceList refList = null;

        //      Added for filter the double adresse
        Set<AdresseElement> eltSet = null;

        //build the list choice
        if ( ( listAdresses != null ) && !listAdresses.isEmpty(  ) )
        {
            refList = new ReferenceList(  );

            //Added for filter the double adresse
            eltSet = new HashSet<AdresseElement>(  );

            for ( fr.paris.lutece.plugins.address.business.jaxb.wsSearchAdresse.Adresse currentAdresse : listAdresses )
            {
                String suffixe = "";

                if ( currentAdresse.getSuffixe(  ) != null )
                {
                    suffixe = currentAdresse.getSuffixe(  );
                }

                String strCurrentAdresse = "";

                if ( currentAdresse.getTypeVoie(  ) == null )
                {
                    strCurrentAdresse = currentAdresse.getNumero(  ) + " " + suffixe + " " +
                        currentAdresse.getNomVoie(  ) + " " + currentAdresse.getCommune(  );
                }
                else if ( LibraryAddressUtils.isTerminateByApostrophe( currentAdresse.getTypeVoie(  ) ) )
                {
                    strCurrentAdresse = currentAdresse.getNumero(  ) + " " + suffixe + " " +
                        currentAdresse.getTypeVoie(  ) + currentAdresse.getNomVoie(  ) + " " +
                        currentAdresse.getCommune(  );
                }
                else
                {
                    strCurrentAdresse = currentAdresse.getNumero(  ) + " " + suffixe + " " +
                        currentAdresse.getTypeVoie(  ) + " " + currentAdresse.getNomVoie(  ) + " " +
                        currentAdresse.getCommune(  );
                }

                String strIdAdresse = currentAdresse.getIdentifiant(  ).toString(  );

                String arr = currentAdresse.getCommune(  );
                int index = arr.indexOf( "E" );
                arr = arr.substring( index - 2, index );
                index = arr.indexOf( "-" );

                if ( index != -1 )
                {
                    arr = arr.substring( 1, 2 );
                }

                //Added for filter the double adresse
                boolean isAdded = eltSet.add( new AdresseElement( strIdAdresse, strCurrentAdresse ) );

                if ( isAdded )
                {
                    if ( arr.equals( strArrondissement ) )
                    {
                        refList.addItem( strIdAdresse, strCurrentAdresse );
                    }
                }
            }

            setListWSAdresses( request, adresses );
        }

        return refList;
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

    /**
     * Sets the timeout to the stub
     * @param portType
     */
    private void setTimeout( Stub portType )
    {
        try
        {
            portType.setTimeout( Integer.parseInt( getTimeOut(  ) ) );
        }
        catch ( NumberFormatException e )
        {
            AppLogService.error( 
                "WebServiceAddressService : timeOut is not set correctly for WebServiceAddressService. Please check address_context.xml. Will use no timeout" );
        }
    }

    /**
     *
     * @param request Resquest
     * @return adresses
     */
    private Adresses getListWSAdresses( HttpServletRequest request )
    {
        HttpSession session = request.getSession( true );
        String strSessionAttribute = SESSION_LIST_ADDRESS_NAME;
        strSessionAttribute = strSessionAttribute.toUpperCase(  );

        return (Adresses) session.getAttribute( strSessionAttribute );
    }

    /**
     *
     * @param request Request
     * @param adresses Adresses
     */
    private void setListWSAdresses( HttpServletRequest request, Adresses adresses )
    {
        HttpSession session = request.getSession( true );
        String strSessionAttribute = SESSION_LIST_ADDRESS_NAME;
        strSessionAttribute = strSessionAttribute.toUpperCase(  );
        session.setAttribute( strSessionAttribute, adresses );
    }

    /**
    *
    * @param request Request
    * @param strAttributeName Name attribute
    */
    private void cleanListWSAdresses( HttpServletRequest request, String strAttributeName )
    {
        HttpSession session = request.getSession( true );
        String strSessionAttribute = strAttributeName;
        strSessionAttribute = strSessionAttribute.toUpperCase(  );
        session.removeAttribute( strSessionAttribute );
    }

    /**
     * Added for filter the double adresse
     * An adresse element for eliminate element already present
     */
    private class AdresseElement
    {
        private String _idAdresse;
        private String _labelAdresse;

        /**
         * Instance a new AdresseElement
         *
         * @param strIdAdresse the adresse id
         * @param strLabelAdresse the adresse label
         */
        public AdresseElement( String strIdAdresse, String strLabelAdresse )
        {
            _idAdresse = strIdAdresse;
            _labelAdresse = strLabelAdresse;
        }

        /**
         * Get The label adresse
         * @return the _labelAdresse
         */
        public String getLabelAdresse(  )
        {
            return _labelAdresse;
        }

        /**
         * Get The label adresse
         * @return the _idAdresse
         */
        public String getIdAdresse(  )
        {
            return _idAdresse;
        }

        /**
         * Get The hashcode for labelAdresse
         * @return the _idAdresse
         */
        public int hashCode(  )
        {
            return _labelAdresse.hashCode(  );
        }

        /**
         * @param o Object
         * @return boolean
         */
        public boolean equals( Object o )
        {
            AdresseElement adresseToCompare = (AdresseElement) o;

            return this._labelAdresse.equals( adresseToCompare.getLabelAdresse(  ) );
        }
    }
}
