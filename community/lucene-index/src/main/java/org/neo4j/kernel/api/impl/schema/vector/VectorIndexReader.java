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
package org.neo4j.kernel.api.impl.schema.vector;

import static org.neo4j.kernel.api.impl.schema.vector.VectorUtils.vectorDimensionsFrom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.search.Query;
import org.neo4j.internal.helpers.collection.BoundedIterable;
import org.neo4j.internal.kernel.api.IndexQueryConstraints;
import org.neo4j.internal.kernel.api.PropertyIndexQuery;
import org.neo4j.internal.kernel.api.PropertyIndexQuery.NearestNeighborsPredicate;
import org.neo4j.internal.kernel.api.exceptions.schema.IndexNotApplicableKernelException;
import org.neo4j.internal.schema.IndexDescriptor;
import org.neo4j.io.IOUtils.AutoCloseables;
import org.neo4j.io.pagecache.context.CursorContext;
import org.neo4j.kernel.api.impl.index.SearcherReference;
import org.neo4j.kernel.api.impl.index.collector.ScoredEntityIterator;
import org.neo4j.kernel.api.impl.index.collector.ValuesIterator;
import org.neo4j.kernel.api.impl.schema.AbstractLuceneIndexReader;
import org.neo4j.kernel.api.impl.schema.LuceneScoredEntityIndexProgressor;
import org.neo4j.kernel.api.impl.schema.TaskCoordinator;
import org.neo4j.kernel.api.impl.schema.reader.IndexReaderCloseException;
import org.neo4j.kernel.api.index.IndexProgressor;
import org.neo4j.kernel.api.index.IndexSampler;
import org.neo4j.kernel.impl.api.index.IndexSamplingConfig;
import org.neo4j.kernel.impl.index.schema.IndexUsageTracker;
import org.neo4j.values.storable.Value;

class VectorIndexReader extends AbstractLuceneIndexReader {
    private final List<SearcherReference> searchers;
    private final int vectorDimensionality;

    VectorIndexReader(
            IndexDescriptor descriptor,
            List<SearcherReference> searchers,
            IndexSamplingConfig samplingConfig,
            TaskCoordinator taskCoordinator,
            IndexUsageTracker usageTracker) {
        // TODO VECTOR: should this actually keep scores? Do we care?
        super(descriptor, samplingConfig, taskCoordinator, usageTracker, true);
        this.searchers = searchers;
        this.vectorDimensionality = vectorDimensionsFrom(descriptor.getIndexConfig());
    }

    @Override
    public long countIndexedEntities(
            long entityId, CursorContext cursorContext, int[] propertyKeyIds, Value... propertyValues) {
        // TODO VECTOR: count indexed entities
        return 0;
    }

    @Override
    public IndexSampler createSampler() {
        return IndexSampler.EMPTY;
    }

    @Override
    protected PropertyIndexQuery validateQuery(PropertyIndexQuery... predicates)
            throws IndexNotApplicableKernelException {
        final var predicate = super.validateQuery(predicates);
        if (predicate instanceof final NearestNeighborsPredicate nearestNeighbour) {
            final var queryVector = nearestNeighbour.query();
            if (queryVector.length != vectorDimensionality) {
                throw new IndexNotApplicableKernelException(
                        "Index query vector has a dimensionality of %d, but indexed vectors have %d."
                                .formatted(queryVector.length, vectorDimensionality));
            }
        }
        return predicate;
    }

    @Override
    protected Query toLuceneQuery(PropertyIndexQuery predicate) {
        return switch (predicate.type()) {
            case ALL_ENTRIES -> VectorQueryFactory.allValues();
            case NEAREST_NEIGHBORS -> {
                final var nearestNeighborsPredicate = (NearestNeighborsPredicate) predicate;
                yield VectorQueryFactory.approximateNearestNeighbors(
                        nearestNeighborsPredicate.query(), nearestNeighborsPredicate.numberOfNeighbors());
            }
            default -> throw invalidQuery(IllegalArgumentException::new, predicate);
        };
    }

    @Override
    protected IndexProgressor indexProgressor(
            Query query, IndexQueryConstraints constraints, IndexProgressor.EntityValueClient client) {
        final var iterator = searchLucene(query, constraints);
        return new LuceneScoredEntityIndexProgressor(iterator, client, constraints);
    }

    @Override
    protected String entityIdFieldKey() {
        return VectorDocumentStructure.ENTITY_ID_KEY;
    }

    @Override
    protected boolean needStoreFilter(PropertyIndexQuery predicate) {
        // We can't do filtering of false positives after the fact because we would
        // need to know which neighbors we missed to do so. We don't know what we don't know.
        return false;
    }

    @Override
    public void close() {
        new AutoCloseables<>(IndexReaderCloseException::new, searchers).close();
    }

    private ValuesIterator searchLucene(Query query, IndexQueryConstraints constraints) {
        // TODO VECTOR: FulltextIndexReader handles transaction state in a similar way
        //              with QueryContext, CursorContext, MemoryTracker
        try {
            // TODO VECTOR: pre-rewrite query? Not sure what rewriting entails
            final var results = new ArrayList<ValuesIterator>(searchers.size());
            for (final var searcher : searchers) {
                final var collector = new VectorResultCollector(constraints);
                searcher.getIndexSearcher().search(query, collector);
                results.add(collector.iterator());
            }
            return ScoredEntityIterator.mergeIterators(results);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    BoundedIterable<Long> newAllEntriesValueReader(long fromIdInclusive, long toIdExclusive) throws IOException {
        final var field = VectorDocumentStructure.ENTITY_ID_KEY;
        final var query = VectorQueryFactory.allValues();
        final var iterables = new ArrayList<BoundedIterable<Long>>(searchers.size());
        for (final var searcher : searchers) {
            iterables.add(newAllEntriesValueReaderForPartition(
                    field, searcher.getIndexSearcher(), query, fromIdInclusive, toIdExclusive));
        }
        return BoundedIterable.concat(iterables);
    }
}
