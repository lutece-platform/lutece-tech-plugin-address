const PORTAL_URL = 'jsp/site/Portal.jsp';

async function callAction( xpage, action, params = {} )
{
    const body = new URLSearchParams( { page: xpage, action } );

    for ( const [ key, value ] of Object.entries( params ) )
    {
        if ( Array.isArray( value ) )
        {
            value.forEach( v => body.append( key, v ) );   // term=a&term=b&term=c
        }
        else if ( value !== null && value !== undefined )
        {
            body.append( key, value );
        }
    }

    const token = document.querySelector( 'input[name="token"]' )?.value;
    if ( token ) { body.append( 'token', token ); }

    const response = await fetch( PORTAL_URL, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
            'Accept': 'application/json'
        },
        body,
        credentials: 'same-origin'
    } );

    if ( !response.ok )
    {
        throw new Error( `${action} : HTTP ${response.status}` );
    }

    return response.json( );
}


async function searchAddress( terms )
{
    const termList = Array.isArray( terms ) ? terms : [ terms ];

    return callAction( 'address', 'searchAdr', { term: termList } );
}
