package EShop.lab2

import EShop.lab2.TypedCartActor.ConfirmCheckoutCancelled
import akka.actor.Cancellable
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

import scala.language.postfixOps
import scala.concurrent.duration._

object TypedCheckout {

  sealed trait Data
  case object Uninitialized                               extends Data
  case class SelectingDeliveryStarted(timer: Cancellable) extends Data
  case class ProcessingPaymentStarted(timer: Cancellable) extends Data

  sealed trait Command
  case object StartCheckout                       extends Command
  case class SelectDeliveryMethod(method: String) extends Command
  case object CancelCheckout                      extends Command
  case object ExpireCheckout                      extends Command
  case class SelectPayment(payment: String)       extends Command
  case object ExpirePayment                       extends Command
  case object ConfirmPaymentReceived              extends Command

  sealed trait Event
  case object CheckOutClosed                        extends Event
  case class PaymentStarted(payment: ActorRef[Any]) extends Event
}

class TypedCheckout {
  import TypedCheckout._

  val checkoutTimerDuration: FiniteDuration = 1 seconds
  val paymentTimerDuration: FiniteDuration  = 1 seconds

  def start: Behavior[TypedCheckout.Command] = Behaviors.receive(
    (context, _) => selectingDelivery(context.scheduleOnce(checkoutTimerDuration, context.self, ExpireCheckout))
  )

  def selectingDelivery(timer: Cancellable): Behavior[TypedCheckout.Command] = Behaviors.receiveMessage {
      case SelectDeliveryMethod(_) => selectingPaymentMethod(timer)
      case CancelCheckout =>
        // context.self ! ConfirmCheckoutCancelled
        cancelled
      case ExpireCheckout =>
        // context.self ! ConfirmCheckoutCancelled
        cancelled
      case _ => Behaviors.same
  }

  def selectingPaymentMethod(timer: Cancellable): Behavior[TypedCheckout.Command] = Behaviors.receive(
    (context, msg) => msg match {
      case SelectPayment(_) => processingPayment(context.scheduleOnce(paymentTimerDuration, context.self, ExpirePayment))
      case CancelCheckout =>
        // context.self ! ConfirmCheckoutCancelled
        cancelled
      case ExpireCheckout =>
        // context.self ! ConfirmCheckoutCancelled
        cancelled
      case _ => Behaviors.same
    }
  )

  def processingPayment(timer: Cancellable): Behavior[TypedCheckout.Command] = Behaviors.receive {
    (context, msg) => msg match {
      case ConfirmPaymentReceived =>
        // context.self ! ConfirmCheckoutClosed
        closed
      case CancelCheckout =>
        // context.self ! ConfirmCheckoutCancelled
        cancelled
      case ExpirePayment =>
        // context.self ! ConfirmCheckoutCancelled
        cancelled
      case _ => Behaviors.same
    }
  }

  def cancelled: Behavior[TypedCheckout.Command] = Behaviors.stopped

  def closed: Behavior[TypedCheckout.Command] =
    Behaviors.stopped

}