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
package org.neo4j.cypher.internal.compiler.planner.logical.steps

import org.neo4j.common.EntityType
import org.neo4j.cypher.internal.ast.Hint
import org.neo4j.cypher.internal.ast.UsingAnyIndexType
import org.neo4j.cypher.internal.ast.UsingBtreeIndexType
import org.neo4j.cypher.internal.ast.UsingIndexHint
import org.neo4j.cypher.internal.ast.UsingIndexHintType
import org.neo4j.cypher.internal.ast.UsingJoinHint
import org.neo4j.cypher.internal.ast.UsingRangeIndexType
import org.neo4j.cypher.internal.ast.UsingTextIndexType
import org.neo4j.cypher.internal.ast.prettifier.ExpressionStringifier
import org.neo4j.cypher.internal.ast.prettifier.Prettifier
import org.neo4j.cypher.internal.ast.semantics.SemanticTable
import org.neo4j.cypher.internal.compiler.IndexHintUnfulfillableNotification
import org.neo4j.cypher.internal.compiler.JoinHintUnfulfillableNotification
import org.neo4j.cypher.internal.compiler.planner.logical.LogicalPlanningContext
import org.neo4j.cypher.internal.compiler.planner.logical.steps.index.EntityIndexLeafPlanner.IndexCompatiblePredicate
import org.neo4j.cypher.internal.compiler.planner.logical.steps.index.IndexCompatiblePredicatesProvider
import org.neo4j.cypher.internal.expressions.LabelOrRelTypeName
import org.neo4j.cypher.internal.expressions.LogicalProperty
import org.neo4j.cypher.internal.expressions.LogicalVariable
import org.neo4j.cypher.internal.expressions.PropertyKeyName
import org.neo4j.cypher.internal.expressions.Variable
import org.neo4j.cypher.internal.ir.PlannerQueryPart
import org.neo4j.cypher.internal.ir.QueryGraph
import org.neo4j.cypher.internal.ir.RegularSinglePlannerQuery
import org.neo4j.cypher.internal.logical.plans.LogicalPlan
import org.neo4j.cypher.internal.logical.plans.LogicalPlanToPlanBuilderString
import org.neo4j.cypher.internal.planner.spi.IndexDescriptor.IndexType
import org.neo4j.cypher.internal.util.InputPosition
import org.neo4j.cypher.internal.util.helpers.StringHelper.RichString
import org.neo4j.exceptions.HintException
import org.neo4j.exceptions.IndexHintException
import org.neo4j.exceptions.IndexHintException.IndexHintIndexType
import org.neo4j.exceptions.InternalException
import org.neo4j.exceptions.InvalidHintException
import org.neo4j.exceptions.JoinHintException
import org.neo4j.exceptions.Neo4jException
import org.neo4j.messages.MessageUtil
import org.neo4j.messages.MessageUtil.Numerus

import scala.collection.JavaConverters.seqAsJavaListConverter

object VerifyBestPlan {
  private val prettifier = Prettifier(ExpressionStringifier())

  def apply(plan: LogicalPlan, expected: PlannerQueryPart, context: LogicalPlanningContext): Unit = {
    val constructed: PlannerQueryPart = context.planningAttributes.solveds.get(plan.id)

    if (expected != constructed) {
      val unfulfillableIndexHints = findUnfulfillableIndexHints(expected, context)
      val unfulfillableJoinHints = findUnfulfillableJoinHints(expected)
      val expectedWithoutUnfulfillableHints = expected.withoutHints(unfulfillableIndexHints.hints ++ unfulfillableJoinHints)
      if (expectedWithoutUnfulfillableHints != constructed) {
        val a: PlannerQueryPart = expected.withoutHints(expected.allHints)
        val b: PlannerQueryPart = constructed.withoutHints(constructed.allHints)
        if (a != b) {
          // unknown planner issue failed to find plan (without regard for differences in hints)
          val moreDetails =
            (a, b) match {
              case (aSingle: RegularSinglePlannerQuery, bSingle: RegularSinglePlannerQuery) =>
                aSingle.pointOutDifference(bSingle, "Expected", "Actual")
              case _ => ""
            }

          throw new InternalException(s"Expected: \n$expected \n\nActual: \n$constructed\n\nPlan: \n${LogicalPlanToPlanBuilderString(plan)} \n\n$moreDetails")
        } else {
          // unknown planner issue failed to find plan matching hints (i.e. "implicit hints")
          val expectedHints = expected.allHints
          val actualHints = constructed.allHints
          val missing = expectedHints.diff(actualHints)
          val solvedInAddition = actualHints.diff(expectedHints)
          val inventedHintsAndThenSolvedThem = solvedInAddition.exists(!expectedHints.contains(_))
          if (missing.nonEmpty || inventedHintsAndThenSolvedThem) {
            def out(h: Set[Hint]) = h.map(prettifier.asString).mkString("`", ", ", "`")

            val details = if (missing.isEmpty)
              s"""Expected:
                 |${out(expectedHints)}
                 |
                 |Instead, got:
                 |${out(actualHints)}""".stripMargin
            else
              s"Could not solve these hints: ${out(missing)}"

            val message =
              s"""Failed to fulfil the hints of the query.
                 |$details
                 |
                 |Plan $plan""".stripMargin

            throw new HintException(message)
          }
        }
      } else {
        processUnfulfilledIndexHints(context, unfulfillableIndexHints)
        processUnfulfilledJoinHints(plan, context, unfulfillableJoinHints)
      }
    }
  }

  private def processUnfulfilledIndexHints(context: LogicalPlanningContext, unfulfillableIndexHints: UnfulfillableIndexHints): Unit = {
    unfulfillableIndexHints.wrongPropertyTypeHints.headOption.foreach(hint => throw hint.toException)
    val hints = unfulfillableIndexHints.missingIndexHints
    if (hints.nonEmpty) {
      // hints referred to non-existent indexes ("explicit hints")
      if (context.useErrorsOverWarnings) {
        throw hints.head.toException
      } else {
        hints.foreach { hint =>
            context.notificationLogger.log(hint.toNotification)
        }
      }
    }
  }

  private def processUnfulfilledJoinHints(plan: LogicalPlan, context: LogicalPlanningContext, hints: Set[UsingJoinHint]): Unit = {
    if (hints.nonEmpty) {
      // we were unable to plan hash join on some requested nodes
      if (context.useErrorsOverWarnings) {
        throw new JoinHintException(s"Unable to plan hash join. Instead, constructed\n$plan")
      } else {
        hints.foreach { hint =>
          context.notificationLogger.log(JoinHintUnfulfillableNotification(hint.variables.map(_.name).toIndexedSeq))
        }
      }
    }
  }

  /**
   * Index hint to which no index exists.
   * @param hint the original hint
   * @param entityType the type of the entity this hint refers to
   */
  case class MissingIndexHint(hint: UsingIndexHint, entityType: EntityType) {
    def toNotification: IndexHintUnfulfillableNotification =
      IndexHintUnfulfillableNotification(hint.variable.name, hint.labelOrRelType.name, hint.properties.map(_.name), entityType, hint.indexType)

    def toException: IndexHintException = {
      val exceptionIndexType = hint.indexType match {
        case UsingAnyIndexType => IndexHintIndexType.ANY
        case UsingBtreeIndexType => IndexHintIndexType.BTREE
        case UsingTextIndexType => IndexHintIndexType.TEXT
        case UsingRangeIndexType => IndexHintIndexType.RANGE
      }
      new IndexHintException(hint.variable.name, hint.labelOrRelType.name, hint.properties.map(_.name).asJava, entityType, exceptionIndexType)
    }
  }

  /**
   * The given hint cannot be fulfilled because the properties used for this hint a
   * @param hint the offending hint
   */
  case class WrongPropertyTypeHint(hint: UsingIndexHint, foundPredicates: Set[IndexCompatiblePredicate]) {
    def toException: Neo4jException =
      new InvalidHintException(MessageUtil.createTextIndexHintError(
        prettifier.asString(hint),
        Numerus.of(foundPredicates.size)
      ))
  }

  private case class UnfulfillableIndexHints(missingIndexHints: Set[MissingIndexHint], wrongPropertyTypeHints: Seq[WrongPropertyTypeHint]) {
    def hints: Set[Hint] = Set[Hint]() ++ missingIndexHints.map(_.hint) ++ wrongPropertyTypeHints.map(_.hint)
  }

  private def findUnfulfillableIndexHints(query: PlannerQueryPart, context: LogicalPlanningContext): UnfulfillableIndexHints = {
    val planContext = context.planContext
    val semanticTable = context.semanticTable

    def nodeIndexHintFulfillable(labelOrRelType: LabelOrRelTypeName, properties: Seq[PropertyKeyName], indexHintType: UsingIndexHintType): Boolean = {
      val labelName = labelOrRelType.name
      val propertyNames = properties.map(_.name)

      def btreeExists = planContext.btreeIndexExistsForLabelAndProperties(labelName, propertyNames)
      def textExists = context.planningTextIndexesEnabled && planContext.textIndexExistsForLabelAndProperties(labelName, propertyNames)
      def rangeExists = context.planningRangeIndexesEnabled && planContext.rangeIndexExistsForLabelAndProperties(labelName, propertyNames)

      indexHintType match {
        case UsingAnyIndexType   => btreeExists || textExists || rangeExists
        case UsingBtreeIndexType => btreeExists
        case UsingTextIndexType  => textExists
        case UsingRangeIndexType => rangeExists
      }
    }

    def relIndexHintFulfillable(labelOrRelType: LabelOrRelTypeName, properties: Seq[PropertyKeyName], indexHintType: UsingIndexHintType): Boolean = {
      val relTypeName = labelOrRelType.name
      val propertyNames = properties.map(_.name)

      def btreeExists = planContext.btreeIndexExistsForRelTypeAndProperties(relTypeName, propertyNames)
      def textExists = context.planningTextIndexesEnabled && planContext.textIndexExistsForRelTypeAndProperties(relTypeName, propertyNames)
      def rangeExists = context.planningRangeIndexesEnabled && planContext.rangeIndexExistsForRelTypeAndProperties(relTypeName, propertyNames)

      indexHintType match {
        case UsingAnyIndexType   => btreeExists || textExists || rangeExists
        case UsingBtreeIndexType => btreeExists
        case UsingTextIndexType  => textExists
        case UsingRangeIndexType  => rangeExists
      }
    }

    /**
     * Tests whether there exists a predicate on the given property that can be used by a text index. And if not, return the predicates searched through.
     */
    def hasPropertyOfTypeText(variable: Variable, propertyName: PropertyKeyName, semanticTable: SemanticTable, queryGraph: QueryGraph): Either[Set[IndexCompatiblePredicate], Boolean] = {
      val predicates = queryGraph.selections.flatPredicates.toSet
      val arguments: Set[LogicalVariable] = queryGraph.argumentIds.map(Variable(_)(InputPosition.NONE))
      val matchingPredicates = IndexCompatiblePredicatesProvider.findExplicitCompatiblePredicates(arguments, predicates, semanticTable).collect {
        case pred@IndexCompatiblePredicate(`variable`, LogicalProperty(_, `propertyName`), _, _, _, _, _, _, _, _) => pred
      }
      if (matchingPredicates.exists(_.compatibleIndexTypes.contains(IndexType.Text))) {
        Right(true)
      } else {
        Left(matchingPredicates)
      }
    }

    val hintsForWrongType = query.asSinglePlannerQuery.allPlannerQueries.flatMap(query => query.queryGraph.allHints.flatMap {
      case hint@UsingIndexHint(variable, _, Seq(property), _, UsingTextIndexType) =>
        hasPropertyOfTypeText(variable, property, semanticTable, query.queryGraph).left.toOption.map(WrongPropertyTypeHint(hint, _))

      case _ => None
    })

    val hintsWithoutIndex = query.allHints.flatMap {
      // using index name:label(property1,property2)
      case UsingIndexHint(v, labelOrRelType, properties, _, indexHintType)
        if semanticTable.isNodeNoFail(v.name) && nodeIndexHintFulfillable(labelOrRelType, properties, indexHintType) =>
        None

      // using index name:relType(property1,property2)
      case UsingIndexHint(v, labelOrRelType, properties, _, indexHintType)
        if semanticTable.isRelationshipNoFail(v.name) && relIndexHintFulfillable(labelOrRelType, properties, indexHintType) =>
        None

      // no such index exists
      case hint: UsingIndexHint =>
        // Let's assume node type by default, in case we have no type information.
        val entityType = if (semanticTable.isRelationshipNoFail(hint.variable)) EntityType.RELATIONSHIP else EntityType.NODE
        Some(MissingIndexHint(hint, entityType))
      // don't care about other hints
      case _ => None
    }
    UnfulfillableIndexHints(hintsWithoutIndex, hintsForWrongType)
  }

  private def findUnfulfillableJoinHints(query: PlannerQueryPart): Set[UsingJoinHint] = {
    query.allHints.collect {
      case hint: UsingJoinHint => hint
    }
  }
}