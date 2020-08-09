package com.reactivebbq.loyalty

trait AkkaSpec extends BeforeAndAfterAll {
  this: Suite =>
  implicit val system: ActorSystem = ActorSystem()
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = Timeout(5.seconds)

  override protected def afterAll(): Unit = {
    super.afterAll()
    Await.result(system.terminate(), 5.seconds)
  }
}
