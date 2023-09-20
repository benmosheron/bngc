package bngc

import scala.xml.XML

object XmlHelper {

  def removeContainerAndFormat(xmlStringWithContainer: String): String = {

    val container = XML.loadString(xmlStringWithContainer)

    val settings = (container \ "Settings").head
    val singleRaceGroup =
      (container \ "Group").find(x => x \@ "BngcId" == "SingleRaceGroup").get
    val tournamentGroup =
      (container \ "Group").find(x => x \@ "BngcId" == "TournamentGroup").get

    val pp = new scala.xml.PrettyPrinter(1000, 2)

    val finalString = List(
      settings,
      singleRaceGroup,
      tournamentGroup
    ).map(xml => pp.format(xml))
      .mkString("\r\n")

    finalString
  }
}
