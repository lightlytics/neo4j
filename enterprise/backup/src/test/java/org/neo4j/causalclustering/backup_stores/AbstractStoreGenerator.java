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
package org.neo4j.causalclustering.backup_stores;

import java.io.File;
import java.util.Optional;
import java.util.UUID;

import org.neo4j.causalclustering.discovery.Cluster;
import org.neo4j.causalclustering.discovery.CoreClusterMember;

import static org.neo4j.causalclustering.BackupUtil.createBackupFromCore;

public abstract class AbstractStoreGenerator implements BackupStore
{
    abstract CoreClusterMember createData( Cluster cluster ) throws Exception;

    abstract void modify( File backup );

    @Override
    public Optional<File> generate( File backupDir, Cluster backupCluster ) throws Exception
    {
        CoreClusterMember core = createData( backupCluster );
        File backupFromCore = createBackupFromCore( core, backupName(), backupDir );
        modify( backupFromCore );
        return Optional.of( backupFromCore );
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName();
    }

    private String backupName()
    {
        return "backup-" + UUID.randomUUID().toString().substring( 5 );
    }
}
