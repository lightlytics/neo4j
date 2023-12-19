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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.neo4j.causalclustering.core.replication.session.GlobalSession;
import org.neo4j.causalclustering.core.replication.session.LocalOperationId;
import org.neo4j.causalclustering.core.state.Result;

public class ProgressTrackerImpl implements ProgressTracker
{
    private final Map<LocalOperationId,Progress> tracker = new ConcurrentHashMap<>();
    private final GlobalSession myGlobalSession;

    public ProgressTrackerImpl( GlobalSession myGlobalSession )
    {
        this.myGlobalSession = myGlobalSession;
    }

    @Override
    public Progress start( DistributedOperation operation )
    {
        assert operation.globalSession().equals( myGlobalSession );

        Progress progress = new Progress();
        tracker.put( operation.operationId(), progress );
        return progress;
    }

    @Override
    public void trackReplication( DistributedOperation operation )
    {
        if ( !operation.globalSession().equals( myGlobalSession ) )
        {
            return;
        }

        Progress progress = tracker.get( operation.operationId() );
        if ( progress != null )
        {
            progress.setReplicated();
        }
    }

    @Override
    public void trackResult( DistributedOperation operation, Result result )
    {
        if ( !operation.globalSession().equals( myGlobalSession ) )
        {
            return;
        }

        Progress progress = tracker.remove( operation.operationId() );

        if ( progress != null )
        {
            result.apply( progress.futureResult() );
        }
    }

    @Override
    public void abort( DistributedOperation operation )
    {
        tracker.remove( operation.operationId() );
    }

    @Override
    public void triggerReplicationEvent()
    {
        tracker.forEach( ( ignored, progress ) -> progress.triggerReplicationEvent() );
    }

    @Override
    public int inProgressCount()
    {
        return tracker.size();
    }
}
