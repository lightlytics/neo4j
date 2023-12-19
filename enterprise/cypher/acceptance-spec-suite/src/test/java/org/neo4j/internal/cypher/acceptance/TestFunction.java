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
package org.neo4j.internal.cypher.acceptance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

public class TestFunction
{
    @Context
    public GraphDatabaseService db;

    @UserFunction( "test.toSet" )
    public List<Object> toSet( @Name( "values" ) List<Object> list )
    {
        return new ArrayList<>( new LinkedHashSet<>( list ) );
    }

    @UserFunction( "test.nodeList" )
    public List<Object> nodeList()
    {
        Result result = db.execute( "MATCH (n) RETURN n LIMIT 1" );
        Object node = result.next().get( "n" );
        return Collections.singletonList( node );
    }

    @UserFunction( "test.sum" )
    public double sum( @Name( "numbers" ) List<Number> list )
    {
        double sum = 0;
        for ( Number number : list )
        {
            sum += number.doubleValue();
        }
        return sum;
    }
}
