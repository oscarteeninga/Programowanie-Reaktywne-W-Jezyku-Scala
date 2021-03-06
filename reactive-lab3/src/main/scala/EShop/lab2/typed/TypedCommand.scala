package EShop.lab2.typed

import akka.actor.typed.ActorRef

trait TypedCommand

case class AddItem(item: Any)                                extends TypedCommand
case class RemoveItem(item: Any)                             extends TypedCommand
case object ExpireCart                                       extends TypedCommand
case object StartCheckout                                    extends TypedCommand
case class CheckoutStarted(actorRef: ActorRef[TypedCommand]) extends TypedCommand
case object ConfirmCheckoutCancelled                         extends TypedCommand
case object ConfirmCheckoutClosed                            extends TypedCommand
case class SelectDeliveryMethod(method: String)              extends TypedCommand
case object CancelCheckout                                   extends TypedCommand
case object ExpireCheckout                                   extends TypedCommand
case class SelectPayment(payment: String)                    extends TypedCommand
case object ExpirePayment                                    extends TypedCommand
case object ConfirmPaymentReceived                           extends TypedCommand
