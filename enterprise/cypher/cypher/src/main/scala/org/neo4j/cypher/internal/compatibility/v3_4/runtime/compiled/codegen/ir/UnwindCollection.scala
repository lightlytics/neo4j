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
package org.neo4j.cypher.internal.compatibility.v3_4.runtime.compiled.codegen.ir

import org.neo4j.cypher.internal.compatibility.v3_4.runtime.compiled.codegen.ir.expressions.{CodeGenExpression, CodeGenType}
import org.neo4j.cypher.internal.compatibility.v3_4.runtime.compiled.codegen.spi.MethodStructure
import org.neo4j.cypher.internal.compatibility.v3_4.runtime.compiled.codegen.{CodeGenContext, Variable}

case class UnwindCollection(opName: String, collection: CodeGenExpression, elementCodeGenType: CodeGenType) extends LoopDataGenerator {
  override def init[E](generator: MethodStructure[E])(implicit context: CodeGenContext): Unit =
    collection.init(generator)

  override def produceLoopData[E](iterVar: String, generator: MethodStructure[E])
                                 (implicit context: CodeGenContext): Unit = {
    generator.declareIterator(iterVar, elementCodeGenType)
    val iterator = generator.iteratorFrom(collection.generateExpression(generator))
    generator.assign(iterVar, elementCodeGenType, iterator)
  }

  override def getNext[E](nextVar: Variable, iterVar: String, generator: MethodStructure[E])
                         (implicit context: CodeGenContext): Unit = {
    assert(elementCodeGenType == nextVar.codeGenType)
    val next = generator.iteratorNext(generator.loadVariable(iterVar), nextVar.codeGenType)
    generator.assign(nextVar, next)
  }

  override def checkNext[E](generator: MethodStructure[E], iterVar: String): E =
    generator.iteratorHasNext(generator.loadVariable(iterVar))

  override def close[E](iterVarName: String,
                        generator: MethodStructure[E]): Unit = {/*nothing to close*/}
}
