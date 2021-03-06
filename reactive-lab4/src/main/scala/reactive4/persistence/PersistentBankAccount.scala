package reactive4.persistence

import akka.actor._
import akka.persistence._
import akka.event.LoggingReceive
import scala.math.BigInt.int2bigInt
import scala.concurrent.Await
import scala.concurrent.duration.Duration

////////////////////////////////////////
// Persistence example: Bank account //
////////////////////////////////////////

object PersistentBankAccount {
  case class Deposit(amount: BigInt) {
    require(amount > 0)
  }
  case class Withdraw(amount: BigInt) {
    require(amount > 0)
  }
  case object Snap
  case object Print
  case object Done

}

case class BalanceChangeEvent(delta: BigInt)

case class AccountState(balance: BigInt = 0) {

  def updated(evt: BalanceChangeEvent): AccountState = {
    println(s"Applying $evt")
    AccountState(balance + evt.delta)
  }

  override def toString: String = balance.toString
}

class PersistentBankAccount extends PersistentActor {

  import PersistentBankAccount._

  override def persistenceId = "persistent account-id-2"

  var state = AccountState()

  def updateState(event: BalanceChangeEvent): Unit =
    state = state.updated(event)

  val receiveCommand = LoggingReceive {
    case Deposit(amount) =>
      persist(BalanceChangeEvent(amount)) {
        // event handler
        event =>
          updateState(event)
      }
    case Withdraw(amount) if amount <= state.balance =>
      persist(BalanceChangeEvent(-amount)) {
        // event handler
        event =>
          updateState(event)
      }
    case Snap  => saveSnapshot(state)
    case Print => println(s"Current balance: $state")
    case Done  => context.system.terminate()
  }

  val receiveRecover: Receive = {
    case evt: BalanceChangeEvent                  => updateState(evt)
    case SnapshotOffer(_, snapshot: AccountState) => state = snapshot
  }

}

object PersistentBankAccountMain extends App {

  import PersistentBankAccount._

  val system = ActorSystem("example")

  val example = system.actorOf(Props[PersistentBankAccount], "account")

  example ! Deposit(1)
  example ! Deposit(2)
  example ! Snap // please uncomment and try!
  example ! Deposit(3)
  example ! Withdraw(3)

  example ! Print

  example ! Done

  example ! PoisonPill

  val exapmle2 = system.actorOf(Props[PersistentBankAccount], "account")

  Await.result(system.whenTerminated, Duration.Inf)

}
