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
package org.neo4j.kernel.impl.newapi;

import org.eclipse.collections.impl.iterator.ImmutableEmptyLongIterator;
import org.neo4j.internal.kernel.api.NodeCursor;
import org.neo4j.internal.kernel.api.PartitionedScan;
import org.neo4j.internal.kernel.api.security.AccessMode;
import org.neo4j.io.pagecache.context.CursorContext;
import org.neo4j.storageengine.api.AllNodeScan;
import org.neo4j.util.Preconditions;

final class PartitionedNodeCursorScan implements PartitionedScan<NodeCursor> {
    private final AllNodeScan storageScan;
    private final Read read;
    private final int numberOfPartitions;
    private final long batchSize;

    PartitionedNodeCursorScan(AllNodeScan storageScan, Read read, int desiredNumberOfPartitions, long totalCount) {
        Preconditions.requirePositive(desiredNumberOfPartitions);
        this.storageScan = storageScan;
        this.read = read;
        if (desiredNumberOfPartitions < totalCount) {
            this.numberOfPartitions = desiredNumberOfPartitions;
        } else {
            this.numberOfPartitions = (int) totalCount;
        }
        this.batchSize = (int) Math.ceil((double) totalCount / numberOfPartitions);
    }

    @Override
    public int getNumberOfPartitions() {
        return numberOfPartitions;
    }

    @Override
    public boolean reservePartition(NodeCursor cursor, CursorContext cursorContext, AccessMode accessMode) {
        return ((DefaultNodeCursor) cursor)
                .scanBatch(read, storageScan, batchSize, ImmutableEmptyLongIterator.INSTANCE, false, accessMode);
    }
}