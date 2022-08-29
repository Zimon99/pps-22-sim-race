package it.unibo.pps.model

import it.unibo.pps.utility.GivenConversion.TrackBuilderGivenConversion.given
import alice.tuprolog.{Term, Theory}
import it.unibo.pps.view.simulation_panel.{
  DrawingCarParams,
  DrawingStartingPointParams,
  DrawingStraightParams,
  DrawingTurnParams
}
import it.unibo.pps.prolog.Scala2P
import monix.eval.Task
import it.unibo.pps.model.StartingPoint

trait TrackBuilder:

  /** Method that loads a base track from a prolog file
    * @return
    *   a [[Track]]
    */
  def createBaseTrack(): Track

object TrackBuilder:

  def apply(): TrackBuilder =
    new TrackBuilderImpl()

  private class TrackBuilderImpl() extends TrackBuilder:

    private val engine = Scala2P.createEngine("/prolog/basetrack.pl")

    override def createBaseTrack(): Track =
      val track = Track()
      loadStraights().foreach(track.addSector(_))
      loadTurns().foreach(track.addSector(_))
      loadStartingGrid().foreach(
        track.addStartingPoint(_)
      )
      track

    private def loadStraights(): List[Straight] =
      val l = List("ID", "X0_E", "Y0_E", "X1_E", "Y1_E", "X0_I", "Y0_I", "X1_I", "Y1_I")
      for
        s <- engine(
          "straight(id(ID), startPointE(X0_E, Y0_E), endPointE(X1_E, Y1_E), startPointI(X0_I, Y0_I), endPointI(X1_I, Y1_I))"
        )
        x = Scala2P.extractTermsToListOfStrings(s, l)
        straight = mkStraight(x)
      yield straight

    private def loadTurns(): List[Turn] =
      val l = List("ID", "X", "Y", "X0_E", "Y0_E", "X0_I", "Y0_I", "X1_E", "Y1_E", "X1_I", "Y1_I", "D")
      for
        s <- engine(
          "turn(id(ID), center(X, Y), startPointE(X0_E, Y0_E), startPointI(X0_I, Y0_I), endPointE(X1_E, Y1_E), endPointI(X1_I, Y1_I), direction(D))"
        )
        x = Scala2P.extractTermsToListOfStrings(s, l)
        turn = mkTurn(x)
      yield turn

    private def loadStartingGrid(): List[StartingPoint] =
      val l = List("ID", "X_POSITION", "Y_POSITION")
      for
        s <- engine(
          "startingPoint(id(ID), position(X_POSITION, Y_POSITION))"
        )
        x = Scala2P.extractTermsToListOfStrings(s, l)
        startingPoint = mkStartingPoint(x)
      yield startingPoint

    private def mkStraight(l: List[String]): Straight = l match {
      case List(id, xP0Ex, yP0Ex, xP1Ex, yP1Ex, xP0In, yP0In, xP1In, yP1In) =>
        val end = if id.equals("1") then 725 else 181
        val d = DrawingStraightParams(
          (xP0Ex, yP0Ex),
          (xP1Ex, yP1Ex),
          (xP0In, yP0In),
          (xP1In, yP1In),
          end
        )
        Straight(id, d)
    }

    private def mkTurn(l: List[String]): Turn = l match {
      case List(id, x_center, y_center, x_SP_E, y_SP_E, x_SP_I, y_SP_I, x_EP_E, y_EP_E, x_EP_I, y_EP_I, direction) =>
        val end = if id.equals("2") then 725 else 181
        val d = DrawingTurnParams(
          (x_center, y_center),
          (x_SP_E, y_SP_E),
          (x_SP_I, y_SP_I),
          (x_EP_E, y_EP_E),
          (x_EP_I, y_EP_I),
          direction,
          end
        )
        Turn(id, d)
    }

    private def mkStartingPoint(l: List[String]): StartingPoint = l match {
      case List(id, x, y) => StartingPoint(id, DrawingStartingPointParams((x, y)))
    }