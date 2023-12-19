/*
 * Copyright (c) 2002-2020 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j Enterprise Edition. The included source
 * code can be redistributed and/or modified under the terms of the
 * Neo4j Sweden Software License, as found in the associated LICENSE.txt
 * file.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Neo4j Sweden Software License for more details.
 */
package org.neo4j.causalclustering.protocol.handshake;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.neo4j.causalclustering.protocol.Protocol;
import org.neo4j.helpers.collection.Pair;

public class ClientHandshakeException extends Exception
{
    public ClientHandshakeException( String message )
    {
        super( message );
    }

    public ClientHandshakeException( String message, @Nullable Protocol.ApplicationProtocol negotiatedApplicationProtocol,
            List<Pair<String,Optional<Protocol.ModifierProtocol>>> negotiatedModifierProtocols )
    {
        super( message + " Negotiated application protocol: " + negotiatedApplicationProtocol +
                " Negotiated modifier protocols: " + negotiatedModifierProtocols );
    }
}
