package com.reactivebbq.loyalty

import com.reactivebbq.loyalty.LoyaltyActor.{LoyaltyAdjustmentApplied, LoyaltyAdjustmentRejected}

class LoyaltyRoutes(loyaltyActors: ActorRef)(implicit ec: ExecutionContext) {
  private implicit val timeout: Timeout = Timeout(5.seconds)

  lazy val routes: Route =
    pathPrefix("loyalty") {
      pathPrefix(Segment) { id =>
        path("award" / IntNumber) { value =>
          post {
            val loyaltyId = LoyaltyId(id)
            val command = LoyaltyActor.ApplyLoyaltyAdjustment(Award(value))
            val result = (loyaltyActors ? LoyaltyActorSupervisor.Deliver(command, loyaltyId))
              .mapTo[LoyaltyActor.Event]

            onComplete(result) {
              case Success(LoyaltyAdjustmentApplied(adjustment)) =>
                complete(StatusCodes.OK, s"Applied: $adjustment")
              case Success(LoyaltyAdjustmentRejected(adjustment, reason)) =>
                complete(StatusCodes.BadRequest, s"Rejected: $reason")
              case Failure(ex) =>
                complete(StatusCodes.InternalServerError, ex.getMessage)
            }
          }
        } ~
          path("deduct" / IntNumber) { value =>
            post {
              val loyaltyId = LoyaltyId(id)
              val command = LoyaltyActor.ApplyLoyaltyAdjustment(Deduct(value))
              val result = (loyaltyActors ? LoyaltyActorSupervisor.Deliver(command, loyaltyId))
                .mapTo[LoyaltyActor.Event]

              onComplete(result) {
                case Success(LoyaltyAdjustmentApplied(adjustment)) =>
                  complete(StatusCodes.OK, s"Applied: $adjustment")
                case Success(LoyaltyAdjustmentRejected(adjustment, reason)) =>
                  complete(StatusCodes.BadRequest, s"Rejected: $reason")
                case Failure(ex) =>
                  complete(StatusCodes.InternalServerError, ex.getMessage)
              }
            }
          } ~
          pathEnd {
            get {
              val loyaltyId = LoyaltyId(id)
              val command = LoyaltyActor.GetLoyaltyInformation()
              val result = (loyaltyActors ? LoyaltyActorSupervisor.Deliver(command, loyaltyId))
                .mapTo[LoyaltyInformation]
                .map { info =>
                  s"Current Balance: ${info.currentTotal}\nHistory:\n" + info.adjustments.mkString("- ", "\n- ", "")
                }

              complete(result)
            }
          }
      }
    }
}
