import mill._
import mill.scalalib._

val akkaVersion = "2.5.14"

// scalastyle:off
object testutil extends ScalaModule {
  def scalaVersion = "2.12.4"

  override def ivyDeps = Agg(
    ivy"org.scalatest::scalatest:3.0.5",
    ivy"org.scalacheck::scalacheck:1.13.4",
    ivy"org.mockito:mockito-core:2.16.0",
    ivy"com.typesafe.akka::akka-stream-testkit:${akkaVersion}"
  )
}

trait CommonModuleDefinition extends ScalaModule {
  def scalaVersion = "2.12.5"

  override def ivyDeps = Agg(ivy"com.typesafe.akka::akka-stream:${akkaVersion}")

  object test extends Tests {
    override def moduleDeps = super.moduleDeps ++ Seq(testutil)

    def testFrameworks = Seq("org.scalatest.tools.Framework")
  }
}

object batchby extends CommonModuleDefinition
