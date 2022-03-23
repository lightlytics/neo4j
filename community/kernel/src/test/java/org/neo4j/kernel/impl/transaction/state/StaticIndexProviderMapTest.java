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
package org.neo4j.kernel.impl.transaction.state;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.neo4j.collection.Dependencies;
import org.neo4j.internal.schema.IndexProviderDescriptor;
import org.neo4j.kernel.api.impl.fulltext.FulltextIndexProvider;
import org.neo4j.kernel.api.impl.schema.TextIndexProvider;
import org.neo4j.kernel.api.index.IndexProvider;
import org.neo4j.kernel.impl.index.schema.GenericNativeIndexProvider;
import org.neo4j.kernel.impl.index.schema.PointIndexProvider;
import org.neo4j.kernel.impl.index.schema.RangeIndexProvider;
import org.neo4j.kernel.impl.index.schema.TokenIndexProvider;

class StaticIndexProviderMapTest {

    @Test
    void testGetters() throws Exception {
        var tokenIndexProvider = mockProvider(TokenIndexProvider.class);
        var btreeIndexProvider = mockProvider(GenericNativeIndexProvider.class);
        var textIndexProvider = mockProvider(TextIndexProvider.class);
        var fulltextIndexProvider = mockProvider(FulltextIndexProvider.class);
        var rangeIndexProvider = mockProvider(RangeIndexProvider.class);
        var pointIndexProvider = mockProvider(PointIndexProvider.class);
        var map = new StaticIndexProviderMap(
                tokenIndexProvider,
                btreeIndexProvider,
                textIndexProvider,
                fulltextIndexProvider,
                rangeIndexProvider,
                pointIndexProvider,
                new Dependencies());
        map.init();

        assertThat(map.getTextIndexProvider()).isEqualTo(textIndexProvider);
        assertThat(map.getFulltextProvider()).isEqualTo(fulltextIndexProvider);
        assertThat(map.getTokenIndexProvider()).isEqualTo(tokenIndexProvider);
        assertThat(map.getBtreeIndexProvider()).isEqualTo(btreeIndexProvider);
        assertThat(map.getDefaultProvider()).isEqualTo(rangeIndexProvider);
        assertThat(map.getPointIndexProvider()).isEqualTo(pointIndexProvider);
    }

    @Test
    void testLookup() throws Exception {
        var tokenIndexProvider = mockProvider(TokenIndexProvider.class);
        var btreeIndexProvider = mockProvider(GenericNativeIndexProvider.class);
        var textIndexProvider = mockProvider(TextIndexProvider.class);
        var fulltextIndexProvider = mockProvider(FulltextIndexProvider.class);
        var rangeIndexProvider = mockProvider(RangeIndexProvider.class);
        var pointIndexProvider = mockProvider(PointIndexProvider.class);
        var map = new StaticIndexProviderMap(
                tokenIndexProvider,
                btreeIndexProvider,
                textIndexProvider,
                fulltextIndexProvider,
                rangeIndexProvider,
                pointIndexProvider,
                new Dependencies());
        map.init();

        asList(
                        tokenIndexProvider,
                        btreeIndexProvider,
                        textIndexProvider,
                        fulltextIndexProvider,
                        rangeIndexProvider,
                        pointIndexProvider)
                .forEach(p -> {
                    assertThat(map.lookup(p.getProviderDescriptor())).isEqualTo(p);
                    assertThat(map.lookup(p.getProviderDescriptor().name())).isEqualTo(p);
                });
    }

    @Test
    void testAccept() throws Exception {
        var tokenIndexProvider = mockProvider(TokenIndexProvider.class);
        var btreeIndexProvider = mockProvider(GenericNativeIndexProvider.class);
        var textIndexProvider = mockProvider(TextIndexProvider.class);
        var fulltextIndexProvider = mockProvider(FulltextIndexProvider.class);
        var rangeIndexProvider = mockProvider(RangeIndexProvider.class);
        var pointIndexProvider = mockProvider(PointIndexProvider.class);
        var map = new StaticIndexProviderMap(
                tokenIndexProvider,
                btreeIndexProvider,
                textIndexProvider,
                fulltextIndexProvider,
                rangeIndexProvider,
                pointIndexProvider,
                new Dependencies());
        map.init();

        var accepted = new ArrayList<>();
        map.accept(accepted::add);

        assertThat(accepted)
                .containsExactlyInAnyOrder(
                        tokenIndexProvider,
                        btreeIndexProvider,
                        textIndexProvider,
                        fulltextIndexProvider,
                        rangeIndexProvider,
                        pointIndexProvider);
    }

    @Test
    void testWithExtension() throws Exception {
        var extension = mockProvider(IndexProvider.class);
        var dependencies = new Dependencies();
        dependencies.satisfyDependency(extension);
        RangeIndexProvider rangeIndexProvider = mockProvider(RangeIndexProvider.class);
        var map = new StaticIndexProviderMap(
                mockProvider(TokenIndexProvider.class),
                mockProvider(GenericNativeIndexProvider.class),
                mockProvider(TextIndexProvider.class),
                mockProvider(FulltextIndexProvider.class),
                rangeIndexProvider,
                mockProvider(PointIndexProvider.class),
                dependencies);
        map.init();

        assertThat(map.lookup(extension.getProviderDescriptor())).isEqualTo(extension);
        assertThat(map.lookup(extension.getProviderDescriptor().name())).isEqualTo(extension);
        var accepted = new ArrayList<>();
        map.accept(accepted::add);
        assertThat(accepted).contains(extension);
    }

    private static <T extends IndexProvider> T mockProvider(Class<? extends T> clazz) {
        var mock = mock(clazz);
        when(mock.getProviderDescriptor()).thenReturn(new IndexProviderDescriptor(clazz.getName(), "o_O"));
        return mock;
    }
}