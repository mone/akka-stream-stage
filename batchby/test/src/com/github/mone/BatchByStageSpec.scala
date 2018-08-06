package com.github.mone

import akka.stream.scaladsl.{Flow, Sink, Source}
import com.github.mone.BatchByStage.{BatchForever, BatchOverflow, FailAfter, FlushAfter}
import org.scalatest.Inside
import org.scalatest.concurrent.ScalaFutures

class BatchByStageSpec extends BaseStreamSpec with ScalaFutures with Inside {
  import BatchByStageSpec._

  behavior of "BatchByStage"

  it should "batch elements while predicate returns true and flush last when upstream closes" in {
    val flowUnderTest = Flow[Int].via(BatchByStage(
      (prev: Option[Int], next: Int) => prev.forall(_ == next),
      BatchForever
    ))

    val completed = source
      .via(flowUnderTest)
      .runWith(Sink.fold(Seq.empty[Seq[Int]])(_ :+ _))

    whenReady(completed) { result =>
      inside(result.toList) {
        case ones :: twos :: threes :: Nil =>
          ones should contain theSameElementsAs Seq(1, 1, 1)
          twos should contain theSameElementsAs Seq(2)
          threes should contain theSameElementsAs Seq(3, 3, 3, 3)
      }
    }

  }

  it should "flush if it reaches a max number and behavior is FlushAfter" in {
    val flowUnderTest = Flow[Int].via(BatchByStage(
      (prev: Option[Int], next: Int) => prev.forall(_ == next),
      FlushAfter(2)
    ))

    val completed = source
      .via(flowUnderTest)
      .runWith(Sink.fold(Seq.empty[Seq[Int]])(_ :+ _))

    whenReady(completed) { result =>
      inside(result.toList) {
        case ones :: ones2 :: twos :: threes :: threes2 :: Nil =>
          ones should contain theSameElementsAs Seq(1, 1)
          ones2 should contain theSameElementsAs Seq(1)
          twos should contain theSameElementsAs Seq(2)
          threes should contain theSameElementsAs Seq(3, 3)
          threes2 should contain theSameElementsAs Seq(3, 3)
      }
    }
  }

  it should "fail if it reaches a max number and behavior is FailAfter" in {
    val flowUnderTest = Flow[Int].via(BatchByStage(
      (prev: Option[Int], next: Int) => prev.forall(_ == next),
      FailAfter(3)
    ))

    val completed = source
      .via(flowUnderTest)
      .runWith(Sink.fold(Seq.empty[Seq[Int]])(_ :+ _))

    whenReady(completed.failed) { err =>
      err should matchPattern {
        case BatchOverflow =>
      }

    }
  }

}

object BatchByStageSpec {
  val source = Source.fromIterator(() => Seq(1, 1, 1, 2, 3, 3, 3, 3).toIterator)
}