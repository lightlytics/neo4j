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
package org.neo4j.cluster.protocol;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.neo4j.cluster.InstanceId;

public interface ConfigurationContext
{
    InstanceId getMyId();

    List<URI> getMemberURIs();

    URI boundAt();

    Map<InstanceId,URI> getMembers();

    InstanceId getCoordinator();

    URI getUriForId( InstanceId id );

    InstanceId getIdForUri( URI uri );

    boolean isMe( InstanceId server );
}
