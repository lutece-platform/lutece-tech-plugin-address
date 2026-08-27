/*
 * Copyright (c) 2002-2026, City of Paris
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
package fr.paris.lutece.plugins.address.rs;

import java.rmi.RemoteException;
import java.util.List;

import fr.paris.lutece.test.LuteceTestCase;
import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.address.business.Address;
import fr.paris.lutece.plugins.address.service.IAddressService;
import fr.paris.lutece.test.ReflectionTestUtils;
import fr.paris.lutece.util.ReferenceItem;
import fr.paris.lutece.util.ReferenceList;
import jakarta.servlet.http.HttpServletRequest;


public class AdressXPageTest extends LuteceTestCase
{
    @Test
    void testSearchAdrReturnsResultsWhenServiceHasResults( ) throws Exception
    {
        AdressXPage xpage = new AdressXPage( );
        ReflectionTestUtils.setField( xpage, "_addressService", new FakeAddressServiceWithResult( ) );

        ReferenceList list = xpage.searchAdr( List.of( "toto" ) );

        assertNotNull( list );
        assertEquals( 1, list.size( ) );
        assertEquals( "toto address found", list.get( 0 ).getName( ) );
    }

    @Test
    void testSearchAdrJoinsMultipleTermsWithSlash( ) throws Exception
    {
        AdressXPage xpage = new AdressXPage( );
        ReflectionTestUtils.setField( xpage, "_addressService", new FakeAddressServiceWithResult( ) );

        ReferenceList list = xpage.searchAdr( List.of( "toto", "titi" ) );

        assertNotNull( list );
        assertEquals( "toto/titi address found", list.get( 0 ).getName( ) );
    }

    @Test
    void testSearchAdrReturnsNullWhenServiceReturnsNull( ) throws Exception
    {
        AdressXPage xpage = new AdressXPage( );
        ReflectionTestUtils.setField( xpage, "_addressService", new FakeAddressServiceEmpty( ) );

        ReferenceList list = xpage.searchAdr( List.of( "toto" ) );

        assertNull( list );
    }

    private static class FakeAddressServiceWithResult implements IAddressService
    {
        @Override
        public ReferenceList searchAddress( HttpServletRequest request, String labeladresse ) throws RemoteException
        {
            ReferenceItem item = new ReferenceItem( );
            item.setCode( "CODE1" );
            item.setName( labeladresse + " address found" );

            ReferenceList list = new ReferenceList( );
            list.add( item );
            return list;
        }

        @Override
        public ReferenceList searchAddress( HttpServletRequest request, String labeladresse, String strArrondissement ) throws RemoteException
        {
            return searchAddress( request, labeladresse );
        }

        @Override
        public ReferenceList searchAddress( HttpServletRequest request, String labeladresse, String strSRID, String strArrondissement ) throws RemoteException
        {
            return searchAddress( request, labeladresse );
        }

        @Override
        public Address getAdresseInfo( HttpServletRequest request, long id, boolean bIsTest ) throws RemoteException
        {
            return null;
        }

        @Override
        public Address getGeolocalisation( HttpServletRequest request, String addresse, String date, boolean bIsTest ) throws RemoteException
        {
            return null;
        }

        @Override
        public Address getGeolocalisation( HttpServletRequest request, long id, String strAddress, String strDate, boolean bIsTest ) throws RemoteException
        {
            return null;
        }

        @Override
        public String getSimpleName( )
        {
            return "FakeAddressServiceWithResult";
        }
    }

    private static class FakeAddressServiceEmpty implements IAddressService
    {
        @Override
        public ReferenceList searchAddress( HttpServletRequest request, String labeladresse ) throws RemoteException
        {
            return null;
        }

        @Override
        public ReferenceList searchAddress( HttpServletRequest request, String labeladresse, String strArrondissement ) throws RemoteException
        {
            return null;
        }

        @Override
        public ReferenceList searchAddress( HttpServletRequest request, String labeladresse, String strSRID, String strArrondissement ) throws RemoteException
        {
            return null;
        }

        @Override
        public Address getAdresseInfo( HttpServletRequest request, long id, boolean bIsTest ) throws RemoteException
        {
            return null;
        }

        @Override
        public Address getGeolocalisation( HttpServletRequest request, String addresse, String date, boolean bIsTest ) throws RemoteException
        {
            return null;
        }

        @Override
        public Address getGeolocalisation( HttpServletRequest request, long id, String strAddress, String strDate, boolean bIsTest ) throws RemoteException
        {
            return null;
        }

        @Override
        public String getSimpleName( )
        {
            return "FakeAddressServiceEmpty";
        }
    }
}
