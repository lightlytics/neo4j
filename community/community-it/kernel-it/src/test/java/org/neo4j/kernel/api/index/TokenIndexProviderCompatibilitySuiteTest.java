/*
 * Copyright (c) "Neo4j"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.kernel.api.index;

import java.nio.file.Path;
import java.util.UUID;

import org.neo4j.common.EntityType;
import org.neo4j.configuration.Config;
import org.neo4j.configuration.database.readonly.ConfigBasedLookupFactory;
import org.neo4j.dbms.database.readonly.ReadOnlyDatabases;
import org.neo4j.index.internal.gbptree.RecoveryCleanupWorkCollector;
import org.neo4j.internal.schema.IndexPrototype;
import org.neo4j.internal.schema.IndexType;
import org.neo4j.internal.schema.SchemaDescriptors;
import org.neo4j.io.fs.FileSystemAbstraction;
import org.neo4j.io.layout.DatabaseLayout;
import org.neo4j.io.pagecache.PageCache;
import org.neo4j.io.pagecache.tracing.PageCacheTracer;
import org.neo4j.kernel.database.DatabaseIdFactory;
import org.neo4j.kernel.impl.index.schema.TokenIndexProviderFactory;
import org.neo4j.monitoring.Monitors;

import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;

class TokenIndexProviderCompatibilitySuiteTest extends SpecialisedIndexProviderCompatibilityTestSuite
{
    @Override
    IndexPrototype indexPrototype()
    {
        return IndexPrototype.forSchema( SchemaDescriptors.forAnyEntityTokens( EntityType.NODE ) );
    }

    @Override
    IndexType indexType()
    {
        return IndexType.LOOKUP;
    }

    @Override
    IndexProvider createIndexProvider( PageCache pageCache, FileSystemAbstraction fs, Path graphDbDir, Config config )
    {
        Monitors monitors = new Monitors();
        String monitorTag = "";
        RecoveryCleanupWorkCollector recoveryCleanupWorkCollector = RecoveryCleanupWorkCollector.immediate();
        DatabaseLayout databaseLayout = DatabaseLayout.ofFlat( graphDbDir );
        var readOnlyDatabases = new ReadOnlyDatabases( new ConfigBasedLookupFactory( config ) );
        var defaultDatabaseId = DatabaseIdFactory.from( DEFAULT_DATABASE_NAME, UUID.randomUUID() ); //UUID required, but ignored by config lookup
        var readOnlyChecker = readOnlyDatabases.forDatabase( defaultDatabaseId );
        return TokenIndexProviderFactory.create( pageCache, graphDbDir, fs, monitors, monitorTag, config, readOnlyChecker, recoveryCleanupWorkCollector,
                databaseLayout, PageCacheTracer.NULL );
    }
}