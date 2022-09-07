package it.unibo.pps.controller

import it.unibo.pps.engine.SimulationEngineModule
import it.unibo.pps.model.{Car, Driver, ModelModule, RenderCarParams, Snapshot, Standings, Tyre}
import it.unibo.pps.view.ViewModule
import monix.execution.Scheduler.Implicits.global
import monix.execution.{Ack, Cancelable, contravariantCallback}
import it.unibo.pps.utility.PimpScala.RichOption.*
import monix.eval.Task

import java.awt.Color
import scala.collection.mutable
import scala.collection.mutable.Map
import scala.math.BigDecimal

object ControllerModule:
  trait Controller:
    def notifyStart(): Unit
    def notifyStop(): Unit
    def notifyDecreaseSpeed(): Unit
    def notifyIncreaseSpeed(): Unit
    def startingPositions: List[Car]
    def currentCar: Car
    def currentCarIndex: Int
    def standings: Standings
    def totalLaps: Int
    def fastestLap: Int
    def fastestCar: String
    def currentCarIndex_=(index: Int): Unit
    def totalLaps_=(lap: Int): Unit
    def fastestLap_=(lap: Int): Unit
    def fastestCar_=(carName: String): Unit
    def setPath(path: String): Unit
    def setTyre(tyre: Tyre): Unit
    def setMaxSpeed(speed: Int): Unit
    def setSkills(skills: Int): Unit
    def displaySimulationPanel(): Unit
    def displayStartingPositionsPanel(): Unit
    def displayEndRacePanel(): Unit
    def updateParametersPanel(): Unit
    def updateDisplayedCar(): Unit
    def invertPosition(prevIndex: Int, nextIndex: Int): Unit
    def registerReactiveChartCallback(): Unit
    def convertTimeToMinutes(time: Int): String
    def calcCarPosting(car: Car): String
    def cars: List[Car]

  trait Provider:
    val controller: Controller

  type Requirements = ModelModule.Provider with SimulationEngineModule.Provider with ViewModule.Provider

  trait Component:
    context: Requirements =>
    class ControllerImpl extends Controller:

      private var stopFuture: Option[Cancelable] = None

      override def notifyStart(): Unit = stopFuture = Some(
        context.simulationEngine
          .simulationStep()
          .loopForever
          .runAsync {
            case Left(exp) => global.reportFailure(exp)
            case _ =>
          }
      )

      override def notifyStop(): Unit =
        stopFuture --> (_.cancel())
        stopFuture = None

      override def notifyDecreaseSpeed(): Unit =
        context.simulationEngine.decreaseSpeed()

      override def notifyIncreaseSpeed(): Unit =
        context.simulationEngine.increaseSpeed()

      override def startingPositions: List[Car] = context.model.startingPositions

      override def currentCar: Car = context.model.cars(context.model.currentCarIndex)

      override def currentCarIndex: Int = context.model.currentCarIndex

      override def standings: Standings = context.model.standings

      override def totalLaps: Int = context.model.totalLaps

      override def fastestLap: Int = context.model.fastestLap

      override def fastestCar: String = context.model.fastestCar
      
      override def currentCarIndex_=(index: Int): Unit = context.model.currentCarIndex = index

      override def totalLaps_=(lap: Int): Unit = context.model.totalLaps_(lap)

      override def fastestLap_=(lap: Int): Unit = context.model.fastestLap = lap

      override def fastestCar_=(carName: String): Unit = context.model.fastestCar = carName

      override def setPath(path: String): Unit = context.model.cars(context.model.currentCarIndex).path = path

      override def setTyre(tyre: Tyre): Unit = context.model.cars(context.model.currentCarIndex).tyre = tyre

      override def setMaxSpeed(speed: Int): Unit = context.model.cars(context.model.currentCarIndex).maxSpeed = speed

      override def setSkills(skills: Int): Unit = context.model.cars(context.model.currentCarIndex).driver.skills =
        skills

      override def displaySimulationPanel(): Unit =
        context.model.createStandings()
        context.model.initSnapshot()
        context.view.updateDisplayedStandings()
        context.view.displaySimulationPanel(context.model.track, context.model.standings)
        context.view.updateCars(
          context.model.standings._standings,
          context.model.actualLap,
          context.model.totalLaps
        )

      override def displayStartingPositionsPanel(): Unit =
        context.view.displayStartingPositionsPanel()

      override def displayEndRacePanel(): Unit =
        context.view.displayEndRacePanel()

      override def updateParametersPanel(): Unit =
        context.view.updateParametersPanel()

      override def updateDisplayedCar(): Unit =
        context.view.updateDisplayedCar()

      override def invertPosition(prevIndex: Int, nextIndex: Int): Unit =
        val car: Car = context.model.startingPositions(prevIndex)
        context.model.startingPositions = context.model.startingPositions.updated(prevIndex, context.model.startingPositions(nextIndex))
        context.model.startingPositions = context.model.startingPositions.updated(nextIndex, car)

        val position = context.model.startingPositions(prevIndex).renderCarParams.position
        context.model.startingPositions(prevIndex).renderCarParams.position =
          context.model.startingPositions(nextIndex).renderCarParams.position
        context.model.startingPositions(nextIndex).renderCarParams.position = position

      override def registerReactiveChartCallback(): Unit =
        val onNext = (l: List[Snapshot]) =>
          context.view.updateCharts(l)
          Ack.Continue
        val onError = (t: Throwable) => ()
        val onComplete = () => ()
        context.model.registerCallbackHistory(onNext, onError, onComplete)

      override def convertTimeToMinutes(time: Int): String =
        val minutes: Int = time / 60
        val seconds: Double = time % 60
        BigDecimal(minutes + seconds / 100).setScale(2, BigDecimal.RoundingMode.HALF_EVEN).toString.replace(".", ":")

      override def calcCarPosting(car: Car): String =
        if standings._standings(0).equals(car) then convertTimeToMinutes(car.raceTime)
        else
          val posting = car.raceTime - standings._standings(0).raceTime
          if posting > 0 then s"+${convertTimeToMinutes(posting)}"
          else "+0:00"

      override def cars: List[Car] = context.model.cars

  trait Interface extends Provider with Component:
    self: Requirements =>