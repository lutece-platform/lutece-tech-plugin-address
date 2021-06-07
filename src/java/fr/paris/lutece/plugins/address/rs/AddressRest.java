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
package fr.paris.lutece.plugins.address.rs;

import fr.paris.lutece.plugins.address.service.AddressServiceProvider;
import fr.paris.lutece.plugins.rest.service.RestConstants;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.json.ErrorJsonResponse;
import fr.paris.lutece.util.json.JsonResponse;
import fr.paris.lutece.util.json.JsonUtil;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * RentRest
 */
@Path( RestConstants.BASE_PATH + Constants.API_PATH + Constants.VERSION_PATH )
public class AddressRest
{
    private static final int VERSION_1 = 1;
    private final Logger _logger = Logger.getLogger( RestConstants.REST_LOGGER );

    // Msg
    private static final String MSG_ERROR_GET_ADDRESSES = "address.message.getAdress.error";

    /**
     * Get adress list
     * 
     * @param nVersion
     *            the API version
     * @param term
     *            to search
     * @return the address list
     */
    @GET
    @Path( Constants.ID_SEARCH_ADR )
    @Produces( MediaType.APPLICATION_JSON )
    public Response getAdressList( @PathParam( Constants.VERSION ) Integer nVersion, @PathParam( Constants.TERM ) String term )
    {
        switch( nVersion )
        {
            case VERSION_1:
                return getAddressList( term );
            default:
                break;
        }
        _logger.error( Constants.ERROR_NOT_FOUND_VERSION );
        return Response.status( Response.Status.NOT_FOUND )
                .entity( JsonUtil.buildJsonResponse( new ErrorJsonResponse( Response.Status.NOT_FOUND.name( ), Constants.ERROR_NOT_FOUND_VERSION ) ) ).build( );
    }

    /**
     * Get address list V1
     * 
     * @param term
     * @return the address list
     */
    private Response getAddressList( String term )
    {

        ReferenceList list = null;
        try
        {
            if ( "RestAddressService".equals( AddressServiceProvider.getInstanceClass( ) ) )
            {
                list = AddressServiceProvider.searchAddress( null, term );
            }

        }
        catch( RemoteException e )
        {
            AppLogService.error( e );
        }

        if ( list == null )
        {
            _logger.error( Constants.ERROR_NOT_FOUND_RESOURCE );
            return Response.status( Response.Status.NOT_FOUND )
                    .entity( JsonUtil.buildJsonResponse( new ErrorJsonResponse( Response.Status.NOT_FOUND.name( ), MSG_ERROR_GET_ADDRESSES ) ) ).build( );
        }

        return Response.status( Response.Status.OK ).entity( JsonUtil.buildJsonResponse( new JsonResponse( list ) ) ).build( );
    }

}
