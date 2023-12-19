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
package org.neo4j.server.security.enterprise.auth;

import org.junit.Rule;

import java.util.Map;

import org.neo4j.graphdb.mockfs.UncloseableDelegatingFileSystemAbstraction;
import org.neo4j.kernel.impl.util.ValueUtils;
import org.neo4j.test.rule.fs.EphemeralFileSystemRule;

public class BoltAuthScenariosInteractionIT extends AuthScenariosInteractionTestBase<BoltInteraction.BoltSubject>
{
    @Rule
    public EphemeralFileSystemRule fileSystemRule = new EphemeralFileSystemRule();

    public BoltAuthScenariosInteractionIT()
    {
        super();
        IS_EMBEDDED = false;
        IS_BOLT = true;
    }

    @Override
    public NeoInteractionLevel<BoltInteraction.BoltSubject> setUpNeoServer( Map<String,String> config )
    {
        return new BoltInteraction( config,
                () -> new UncloseableDelegatingFileSystemAbstraction( fileSystemRule.get() ) );
    }

    @Override
    protected Object valueOf( Object obj )
    {
        return ValueUtils.of( obj );
    }
}
