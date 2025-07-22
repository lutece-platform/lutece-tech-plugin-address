/*
 * Copyright (c) 2002-2023, City of Paris
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
package fr.paris.lutece.plugins.address.business;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Address
{
    private String _Addresstypo;
    private String _streetNumber;
    private String _bisLabel;
    private String _streetType;
    private String _streetLabel;
    private String _addressLine1;
    private String _addressLine2;
    private String _postalCode;
    private String _city;

    private Long _geoX;
    private Long _geoY;

    @JsonProperty( "Adressetypo" )
    private void setAdressTypo( String libelletypo )
    {
        this._Addresstypo = libelletypo;
        
        String [ ] parts = libelletypo.split( ", " );
        String [ ] street = parts [0].split( " " );

        if(street[0].matches( "\\d+" ))
        {
            this._streetNumber = street[0];
        }
        else
        {
            String[ ] streetParts = street[0].split("(?<=\\d)(?=\\D)", 2);
            this._streetNumber = streetParts[0];
            this._bisLabel = streetParts.length > 1 ? streetParts[1] : "";
        }

        this._streetType = street[1];

        for( int i = 2; i < street.length - 1; i++ )
        {
            this._streetLabel = ( this._streetLabel == null ? "" : this._streetLabel + " " ) + street[i];
        }

        String [ ] cityAndPostalCode = parts [1].split( " " );
        this._postalCode = cityAndPostalCode [0];
        this._city = cityAndPostalCode [1];
    }

    public String getAddressTypo()
    {
        return this._Addresstypo;
    }

    public String getStreetNumber()
    {
        return this._streetNumber;
    }
    
    public String getBisLabel()
    {
        return this._bisLabel;
    }

    public String getStreetType()
    {
        return this._streetType;
    }

    public String getStreetLabel()
    {
        return this._streetLabel;
    }

    public String getAddressLine1()
    {
        return this._addressLine1;
    }

    public String getAddressLine2()
    {
        return this._addressLine2;
    }

    public String getCity( )
    {
        return this._city;
    }
    public String getPostalCode( )
    {
        return this._postalCode;
    }

        public void setStreetNumber( String streetNumber )
    {
        this._streetNumber = streetNumber;
    }

    public void setBisLabel( String bisLabel )
    {
        this._bisLabel = bisLabel;
    }

    public void setStreetType( String streetType )
    {
        this._streetType = streetType;
    }

    public void setStreetLabel( String streetLabel )
    {
        this._streetLabel = streetLabel;
    }

    public void setAddressLine1( String addressLine1 )
    {
        this._addressLine1 = addressLine1;
    }

    public void setAddressLine2( String addressLine2 )
    {
        this._addressLine2 = addressLine2;
    }

    @JsonProperty( "City" )
    public void setCity( String city )
    {
        this._city = city;
    }

    @JsonProperty( "PostalCode" )
    public void setPostalCode( String postalCode )
    {
        this._postalCode = postalCode;
    }

    @JsonProperty( "GeoX" )
    public void setGeoX( Long geoX )
    {
        this._geoX = geoX;
    }

    @JsonProperty( "GeoY" )
    public void setGeoY( Long geoY )
    {
        this._geoY = geoY;
    }
}

