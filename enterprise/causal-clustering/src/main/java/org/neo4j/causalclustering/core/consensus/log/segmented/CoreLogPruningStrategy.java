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
package org.neo4j.causalclustering.core.consensus.log.segmented;

public interface CoreLogPruningStrategy
{
    /**
     * Returns the index to keep depending on the configuration strategy.
     * This does not factor in the value of the safe index to prune to.
     *
     * It is worth noting that the returned value may be the first available value,
     * rather than the first possible value. This signifies that no pruning is needed.
     *
     * @param segments The segments to inspect.
     * @return The lowest index the pruning configuration allows to keep. It is a value in the same range as
     * append indexes, starting from -1 all the way to {@link Long#MAX_VALUE}.
     */
    long getIndexToKeep( Segments segments );
}
