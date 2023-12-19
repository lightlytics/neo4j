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
package cypher.features

import org.opencypher.tools.tck.api.Scenario

import scala.util.matching.Regex

case class BlacklistEntry(featureName: Option[String], scenarioName: String) {
  def isBlacklisted(scenario: Scenario): Boolean = {
    scenarioName == scenario.name && (featureName.isEmpty || featureName.get == scenario.featureName)
  }

  override def toString: String = {
    if (featureName.isDefined) {
      s"""Feature "${featureName.get}": Scenario "$scenarioName""""
    } else {
      s"""$scenarioName"""  // legacy version
    }
  }
}

object BlacklistEntry {
  val entryPattern: Regex = """Feature "(.*)": Scenario "(.*)"""".r

  def apply(line: String): BlacklistEntry = {
    if (line.startsWith("Feature")) {
      line match {
        case entryPattern(featureName, scenarioName) => new BlacklistEntry(Some(featureName), scenarioName)
        case other => throw new UnsupportedOperationException(s"Could not parse blacklist entry $other")
      }

    } else new BlacklistEntry(None, line)
  }
}
