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
package org.neo4j.causalclustering.upstream;

import java.util.LinkedHashSet;
import java.util.NoSuchElementException;

import org.neo4j.causalclustering.identity.MemberId;
import org.neo4j.logging.Log;
import org.neo4j.logging.LogProvider;
import org.neo4j.logging.NullLogProvider;

import static org.neo4j.helpers.collection.Iterables.empty;

public class UpstreamDatabaseStrategySelector
{
    private LinkedHashSet<UpstreamDatabaseSelectionStrategy> strategies = new LinkedHashSet<>();
    private Log log;

    public UpstreamDatabaseStrategySelector( UpstreamDatabaseSelectionStrategy defaultStrategy )
    {
        this( defaultStrategy, empty(), NullLogProvider.getInstance() );
    }

    public UpstreamDatabaseStrategySelector( UpstreamDatabaseSelectionStrategy defaultStrategy, Iterable<UpstreamDatabaseSelectionStrategy> otherStrategies,
            LogProvider logProvider )
    {
        this.log = logProvider.getLog( getClass() );

        if ( otherStrategies != null )
        {
            for ( UpstreamDatabaseSelectionStrategy otherStrategy : otherStrategies )
            {
                strategies.add( otherStrategy );
            }
        }
        strategies.add( defaultStrategy );
    }

    public MemberId bestUpstreamDatabase() throws UpstreamDatabaseSelectionException
    {
        MemberId result = null;
        for ( UpstreamDatabaseSelectionStrategy strategy : strategies )
        {
            log.debug( "Trying selection strategy [%s]", strategy.toString() );
            try
            {
                if ( strategy.upstreamDatabase().isPresent() )
                {
                    result = strategy.upstreamDatabase().get();
                    break;
                }
            }
            catch ( NoSuchElementException ex )
            {
                // Do nothing, this strategy failed
            }
        }

        if ( result == null )
        {
            throw new UpstreamDatabaseSelectionException( "Could not find an upstream database with which to connect." );
        }

        log.debug( "Selected upstream database [%s]", result );
        return result;
    }
}
