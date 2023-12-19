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
package org.neo4j.causalclustering.catchup;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.neo4j.causalclustering.catchup.storecopy.PrepareStoreCopyResponse;

public class StoreListingResponseHandler extends SimpleChannelInboundHandler<PrepareStoreCopyResponse>
{
    private final CatchupClientProtocol protocol;
    private final CatchUpResponseHandler handler;

    public StoreListingResponseHandler( CatchupClientProtocol protocol,
            CatchUpResponseHandler handler )
    {
        this.protocol = protocol;
        this.handler = handler;
    }

    @Override
    protected void channelRead0( ChannelHandlerContext ctx, final PrepareStoreCopyResponse msg ) throws Exception
    {
        handler.onStoreListingResponse( msg );
        protocol.expect( CatchupClientProtocol.State.MESSAGE_TYPE );
    }
}

