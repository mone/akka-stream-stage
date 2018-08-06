package com.github.mone

import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}

object BatchByStage {

  sealed trait Behavior

  case object BatchForever extends Behavior
  case class FlushAfter(after: Int) extends Behavior
  case class FailAfter(after: Int) extends Behavior

  case object BatchOverflow extends Exception("Batching size exceeded")

  def apply[T](f: (Option[T], T) => Boolean, batchBehavior: BatchByStage.Behavior): BatchByStage[T] =
    new BatchByStage(f, batchBehavior)

}

// TODO doc me!
class BatchByStage[T](f: (Option[T], T) => Boolean, batchBehavior: BatchByStage.Behavior)
  extends GraphStage[FlowShape[T, Seq[T]]] {
  import BatchByStage._

  val in = Inlet[T]("Map.in")
  val out = Outlet[Seq[T]]("Map.out")

  override val shape = FlowShape.of(in, out)

  override def createLogic(attr: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) {

      var batching: Seq[T] = Seq.empty

      def flushAndReset(elem: T) = {
        push(out, batching)
        batching = Seq(elem)
      }

      setHandler(in, new InHandler {
        override def onPush(): Unit = {
          // we've been pushed, grab the element
          val elem = grab(in)
          if (f(batching.headOption, elem)) {
            // need to keep batching, let's see if we can
            batchBehavior match {
              case FailAfter(num) if batching.size == num =>
                fail(out, BatchOverflow)

              case FlushAfter(num) if batching.size == num =>
                // stop batching, flush and start anew
                flushAndReset(elem)
                // won't pull until downstream asks for it

              case _ =>
                // we can keep consuming, let's batch and pull next
                batching = batching :+ elem
                pull(in)

            }

          } else {
            flushAndReset(elem)
            // won't pull until downstream asks for it
          }
        }

        override def onUpstreamFinish(): Unit = {
          // upstream is done, we need to flush what we have
          if (batching.nonEmpty) emit(out, batching)
          // and signal we're done too
          complete(out)
        }
      })

      setHandler(out, new OutHandler {
        override def onPull(): Unit = {
          // when they pull, we pull, and we keep pulling until
          // a batch is complete
          pull(in)
        }
      })
    }

}
