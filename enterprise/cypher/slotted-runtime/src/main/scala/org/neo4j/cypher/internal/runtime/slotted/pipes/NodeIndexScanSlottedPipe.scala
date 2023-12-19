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
package org.neo4j.cypher.internal.runtime.slotted.pipes

import org.neo4j.cypher.internal.compatibility.v3_4.runtime.SlotConfiguration
import org.neo4j.cypher.internal.compatibility.v3_4.runtime.helpers.PrimitiveLongHelper
import org.neo4j.cypher.internal.runtime.QueryContext
import org.neo4j.cypher.internal.runtime.interpreted.ExecutionContext
import org.neo4j.cypher.internal.runtime.interpreted.pipes._
import org.neo4j.cypher.internal.runtime.slotted.SlottedExecutionContext
import org.neo4j.cypher.internal.util.v3_4.attribution.Id
import org.neo4j.cypher.internal.v3_4.expressions.{LabelToken, PropertyKeyToken}
import org.neo4j.internal.kernel.api.{CapableIndexReference, IndexReference}

case class NodeIndexScanSlottedPipe(ident: String,
                                    label: LabelToken,
                                    propertyKey: PropertyKeyToken,
                                    slots: SlotConfiguration,
                                    argumentSize: SlotConfiguration.Size)
                                   (val id: Id = Id.INVALID_ID)
  extends Pipe {

  private val offset = slots.getLongOffsetFor(ident)

  private var reference: IndexReference = CapableIndexReference.NO_INDEX

  private def reference(context: QueryContext): IndexReference = {
    if (reference == CapableIndexReference.NO_INDEX) {
      reference = context.indexReference(label.nameId.id,propertyKey.nameId.id)
    }
    reference
  }

  protected def internalCreateResults(state: QueryState): Iterator[ExecutionContext] = {
    val nodes = state.query.indexScanPrimitive(reference(state.query))
    PrimitiveLongHelper.map(nodes, { node =>
      val context = SlottedExecutionContext(slots)
      state.copyArgumentStateTo(context, argumentSize.nLongs, argumentSize.nReferences)
      context.setLongAt(offset, node)
      context
    })
  }

}
