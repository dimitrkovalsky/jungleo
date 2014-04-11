package actors

import utils.MailUtil

import akka.actor.Actor
/**
 * User: dkovalskyi
 * Date: 26.09.13
 * Time: 14:40
 */
class VerificationActor extends Actor {
    private val mailer: MailUtil = new MailUtil

    def receive = {
        case msg: VerificationMessage => mailer.send(msg.address, msg.link)
    }
}

case class VerificationMessage(address: String, link:String)
