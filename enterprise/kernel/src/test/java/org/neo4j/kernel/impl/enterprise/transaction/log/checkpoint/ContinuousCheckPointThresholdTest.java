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
package org.neo4j.kernel.impl.enterprise.transaction.log.checkpoint;

import org.junit.Test;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ContinuousCheckPointThresholdTest
{
    @Test
    public void continuousCheckPointMustReachThresholdOnEveryCommit()
    {
        ContinuousCheckPointThreshold threshold = new ContinuousCheckPointThreshold();
        threshold.initialize( 10 );
        assertFalse( threshold.thresholdReached( 10 ) );
        assertTrue( threshold.thresholdReached( 11 ) );
        assertTrue( threshold.thresholdReached( 11 ) );
        threshold.checkPointHappened( 12 );
        assertFalse( threshold.thresholdReached( 12 ) );
    }

    @Test
    public void continuousThresholdMustNotBusySpin()
    {
        ContinuousCheckPointThreshold threshold = new ContinuousCheckPointThreshold();
        assertThat( threshold.checkFrequencyMillis(), greaterThan( 0L ) );
    }
}
