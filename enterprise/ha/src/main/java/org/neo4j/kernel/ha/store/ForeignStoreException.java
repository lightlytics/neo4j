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
package org.neo4j.kernel.ha.store;

import org.neo4j.kernel.impl.store.StoreId;

import static java.lang.String.format;

public class ForeignStoreException extends HighAvailabilityStoreFailureException
{
    public ForeignStoreException( StoreId store, StoreId clusterStore )
    {
        super( format( "%s has different origin than the rest of the cluster, %s", store, clusterStore ) );
    }
}
