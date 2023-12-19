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
package org.neo4j.causalclustering.handlers;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.neo4j.causalclustering.core.CausalClusteringSettings;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.enterprise.configuration.OnlineBackupSettings;
import org.neo4j.kernel.impl.util.Dependencies;
import org.neo4j.logging.LogProvider;
import org.neo4j.logging.NullLogProvider;

public class VoidPipelineWrapperFactoryTest
{

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void clientSslEncryptionPoliciesThrowException()
    {
        // given
        VoidPipelineWrapperFactory pipelineWrapperFactory = new VoidPipelineWrapperFactory();

        // and
        Config config = Config.defaults();
        config.augment( CausalClusteringSettings.ssl_policy, "cluster" );

        // and
        LogProvider logProvider = NullLogProvider.getInstance();
        Dependencies dependencies = null;

        // then
        expectedException.expectMessage( "Unexpected SSL policy causal_clustering.ssl_policy is a string" );

        // when
        pipelineWrapperFactory.forClient( config, dependencies, logProvider, CausalClusteringSettings.ssl_policy );
    }

    @Test
    public void serverSslEncryptionPoliciesThrowException()
    {
        // given
        VoidPipelineWrapperFactory pipelineWrapperFactory = new VoidPipelineWrapperFactory();

        // and
        Config config = Config.defaults();
        config.augment( OnlineBackupSettings.ssl_policy, "backup" );

        // and
        LogProvider logProvider = NullLogProvider.getInstance();
        Dependencies dependencies = null;

        // then
        expectedException.expectMessage( "Unexpected SSL policy dbms.backup.ssl_policy is a string" );

        // when
        pipelineWrapperFactory.forServer( config, dependencies, logProvider, OnlineBackupSettings.ssl_policy );
    }

    @Test
    public void clientAndServersWithoutPoliciesPass()
    {
        // given
        VoidPipelineWrapperFactory pipelineWrapperFactory = new VoidPipelineWrapperFactory();

        // and
        Config config = Config.defaults();

        // and
        LogProvider logProvider = NullLogProvider.getInstance();
        Dependencies dependencies = null;

        // when
        pipelineWrapperFactory.forServer( config, dependencies, logProvider, CausalClusteringSettings.ssl_policy );
        pipelineWrapperFactory.forClient( config, dependencies, logProvider, CausalClusteringSettings.ssl_policy );
        pipelineWrapperFactory.forServer( config, dependencies, logProvider, OnlineBackupSettings.ssl_policy );
        pipelineWrapperFactory.forClient( config, dependencies, logProvider, OnlineBackupSettings.ssl_policy );
    }
}
