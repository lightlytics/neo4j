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
package org.neo4j.kernel.stresstests.transaction.checkpoint.mutation;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

class LabelMutation implements Mutation
{
    private final GraphDatabaseService db;

    LabelMutation( GraphDatabaseService db )
    {
        this.db = db;
    }

    @Override
    public void perform( long nodeId, String value )
    {
        Node node = db.getNodeById( nodeId );
        Label label = Label.label( value );
        if ( node.hasLabel( label ) )
        {
            node.removeLabel( label );
        }
        else
        {
            node.addLabel( label );
        }
    }
}
