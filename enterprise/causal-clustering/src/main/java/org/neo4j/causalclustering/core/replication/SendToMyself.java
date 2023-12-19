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
package org.neo4j.causalclustering.core.replication;

import org.neo4j.causalclustering.core.consensus.RaftMessages;
import org.neo4j.causalclustering.messaging.Outbound;
import org.neo4j.causalclustering.identity.MemberId;

public class SendToMyself
{
    private final MemberId myself;
    private final Outbound<MemberId,RaftMessages.RaftMessage> outbound;

    public SendToMyself( MemberId myself, Outbound<MemberId,RaftMessages.RaftMessage> outbound )
    {
        this.myself = myself;
        this.outbound = outbound;
    }

    public void replicate( ReplicatedContent content )
    {
        outbound.send( myself, new RaftMessages.NewEntry.Request( myself, content ) );
    }
}
