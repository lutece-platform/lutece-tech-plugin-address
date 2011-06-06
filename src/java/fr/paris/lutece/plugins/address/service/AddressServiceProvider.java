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
package fr.paris.lutece.plugins.address.service;

import fr.paris.lutece.plugins.address.business.jaxb.Adresse;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.util.ReferenceList;

import java.rmi.RemoteException;

import javax.servlet.http.HttpServletRequest;


/**
 *
 */
public final class AddressServiceProvider
{
    private static IAddressService _service = (IAddressService) SpringContextService.getPluginBean( "address",
            "adresseService" );

    /**
     *
     *
     */
    private AddressServiceProvider(  )
    {
    }

    /**
    * @throws RemoteException the RemoteExecption
    * @param request Request
    * @param labeladresse the  label adress
    * @return the XML flux of all adress corresponding
    *
    */
    public static ReferenceList searchAddress( HttpServletRequest request, String labeladresse )
        throws RemoteException
    {
        return _service.searchAddress( request, labeladresse );
    }

    /**
     * @throws RemoteException the RemoteExecption
     * @param request Request
     * @param labeladresse the  label adress
     * @param strArrondissement Arrondissement
     * @return the XML flux of all adress corresponding
     *
     */
    public static ReferenceList searchAddress( HttpServletRequest request, String labeladresse, String strArrondissement )
        throws RemoteException
    {
        return _service.searchAddress( request, labeladresse, strArrondissement );
    }

    /**
    * @throws RemoteException the RemoteExecption
    * @param request Request
    * @param id the adress id
    * @param bIsTest if true test connect at web service, if false search an adress
    * @return the XML flux of an adress
    *
    */
    public static Adresse getAdresseInfo( HttpServletRequest request, long id, boolean bIsTest )
        throws RemoteException
    {
        return _service.getAdresseInfo( request, id, bIsTest );
    }
    
    /**
     * @throws RemoteException the RemoteExecption
     * @param request Request
     * @param bIsTest if true test connect at web service, if false search an adress
     * @return the XML flux of an adress
     *
     */
    public static Adresse getGeolocalisation( HttpServletRequest request, String strAddress, String strDate, boolean bIsTest )
         throws RemoteException
    {
    	 return _service.getGeolocalisation( request, strAddress, strDate, bIsTest );
    }
    
    /**
     * @throws RemoteException the RemoteExecption
     * @param request Request
     * @param id the RSID
     * @param bIsTest if true test connect at web service, if false search an adress
     * @return the XML flux of an adress
     *
     */
    public static Adresse getGeolocalisation( HttpServletRequest request, long id, String strAddress, String strDate, boolean bIsTest )
         throws RemoteException
     {
    	 return _service.getGeolocalisation( request, id, strAddress, strDate, bIsTest );
     }
}
