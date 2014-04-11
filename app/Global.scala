import com.jungleo.preloaders.PreloadRunner
import controllers.ReaderController
import java.util.logging.Logger
import play.api._
import utils.MailUtil
import play.api.Play.current
/**
 * User: dkovalskyi
 * Date: 18.09.13
 * Time: 18:29
 */
object Global extends GlobalSettings {
    private val logger: Logger = Logger.getLogger("YahooFinanceReader")

    override def onStart(app: Application) {
        logger.info("[Global] Application has started")

        PreloadRunner.load(active = true)
        ReaderController.init(random = false)

    }
}
