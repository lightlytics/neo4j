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
package org.neo4j.cluster.protocol.heartbeat;

import java.net.URI;

import org.neo4j.cluster.InstanceId;
import org.neo4j.cluster.com.message.Message;
import org.neo4j.cluster.com.message.MessageHolder;
import org.neo4j.cluster.protocol.cluster.ClusterListener;

/**
 * When an instance joins a cluster, setup a heartbeat for it
 */
public class HeartbeatJoinListener extends ClusterListener.Adapter
{
    private final MessageHolder outgoing;

    public HeartbeatJoinListener( MessageHolder outgoing )
    {
        this.outgoing = outgoing;
    }

    @Override
    public void joinedCluster( InstanceId member, URI atUri )
    {
        outgoing.offer( Message.internal( HeartbeatMessage.reset_send_heartbeat, member ) );
    }
}
