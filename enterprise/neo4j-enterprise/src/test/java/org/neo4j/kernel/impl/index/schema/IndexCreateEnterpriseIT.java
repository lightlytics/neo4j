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
package org.neo4j.kernel.impl.index.schema;

import org.junit.Test;

import org.neo4j.internal.kernel.api.SchemaWrite;
import org.neo4j.internal.kernel.api.exceptions.KernelException;
import org.neo4j.test.TestEnterpriseGraphDatabaseFactory;
import org.neo4j.test.TestGraphDatabaseFactory;

public class IndexCreateEnterpriseIT extends IndexCreateIT
{
    @Override
    protected TestGraphDatabaseFactory createGraphDatabaseFactory()
    {
        return new TestEnterpriseGraphDatabaseFactory();
    }

    @Test
    public void shouldCreateNodeKeyConstraintWithSpecificExistingProviderName() throws KernelException
    {
        shouldCreateWithSpecificExistingProviderName( SchemaWrite::nodeKeyConstraintCreate );
    }

    @Test
    public void shouldFailCreateNodeKeyWithNonExistentProviderName() throws KernelException
    {
        shouldFailWithNonExistentProviderName( SchemaWrite::nodeKeyConstraintCreate );
    }
}
