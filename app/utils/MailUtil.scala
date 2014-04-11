package utils

import com.typesafe.plugin._
import play.api.Play.current


/**
 * User: Dimitr
 * Date: 27.09.13
 * Time: 10:43
 */
class MailUtil {
    private val ADDRESS = "http://ec2-54-221-189-233.compute-1.amazonaws.com:9000/verification/"

    def send(to: String, id: String) {
        val mail = use[MailerPlugin].email
        mail.setSubject("Jungle O verification")
        mail.addFrom("jungleo.jungleo@gmail.com")
        mail.addRecipient(to)
        mail.sendHtml(s"<html><body>Verification <a href='${ADDRESS + id}'>link<a></body></html>")
    }
}
