/*
 * Copyright (c) 2002-2019 "Neo4j,"
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
package org.neo4j.test;

import org.neo4j.configuration.connectors.ConnectorPortRegister;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.helpers.HostnamePort;
import org.neo4j.kernel.internal.GraphDatabaseAPI;

import static java.util.Objects.requireNonNull;

public final class PortUtils
{
    private PortUtils()
    {
        // nop
    }

    public static int getBoltPort( GraphDatabaseService db )
    {
        return getConnectorAddress( (GraphDatabaseAPI) db, "bolt" ).getPort();
    }

    public static HostnamePort getConnectorAddress( GraphDatabaseAPI db, String connectorKey )
    {
        final ConnectorPortRegister portRegister = db.getDependencyResolver().resolveDependency( ConnectorPortRegister.class );
        return requireNonNull( portRegister.getLocalAddress( connectorKey ), "Connector not found: " + connectorKey);
    }
}
