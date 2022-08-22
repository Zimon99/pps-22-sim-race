package it.unibo.pps.engine

import alice.tuprolog.{Term, Theory}
import it.unibo.pps.prolog.Scala2P
import it.unibo.pps.model.Car

given Conversion[String, Term] = Term.createTerm(_)
given Conversion[Seq[_], Term] = _.mkString("[", ",", "]")
given Conversion[String, Theory] = Theory.parseLazilyWithStandardOperators(_)

trait PrologMovements:
  def newPositionStraight(x: Int, velocity: Double, time: Int, acceleration: Double, i: Int): Int
  def newVelocityStraightAcc(car: Car, time: Int, acceleration: Double): Int
  def newVelocityStraightDec(car: Car, time: Int, acceleration: Double): Int

  def newPositionTurn(): Unit

object PrologMovements:
  def apply(): PrologMovements = new PrologMovementsImpl()

  private class PrologMovementsImpl() extends PrologMovements:

    private val engine = Scala2P.createEngine("/prolog/movements.pl")

    override def newPositionStraight(x: Int, velocity: Double, time: Int, acceleration: Double, i: Int): Int =
      engine(s"computeNewPositionForStraight($x, $velocity, $time, $acceleration, $i, Np)")
        .map(Scala2P.extractTermToString(_, "Np"))
        .toSeq
        .head
        .toDouble
        .toInt

    override def newVelocityStraightAcc(car: Car, time: Int, acceleration: Double): Int =
      engine(s"computeNewVelocity(${car.actualSpeed}, $acceleration, $time,  Ns)")
        .map(Scala2P.extractTermToString(_, "Ns"))
        .toSeq
        .head
        .toDouble
        .toInt

    override def newVelocityStraightDec(car: Car, time: Int, acceleration: Double): Int =
      engine(s"computeNewVelocityDeceleration(${car.actualSpeed}, $acceleration, $time,  Ns)")
        .map(Scala2P.extractTermToString(_, "Ns"))
        .toSeq
        .head
        .toDouble
        .toInt

    override def newPositionTurn(): Unit = ???
