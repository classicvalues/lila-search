package lila

import ornicar.scalalib.Zero
import scala.concurrent.Future

package object search {

  object Date {
    import org.joda.time.format.{ DateTimeFormat, DateTimeFormatter }
    val format = "YYYY-MM-dd HH:mm:ss"
    val formatter: DateTimeFormatter = DateTimeFormat forPattern format
  }

  // fix scala

  type Fu[+A] = Future[A]
  type Funit = Fu[Unit]

  def fuccess[A](a: A) = Future successful a
  def fufail[A <: Throwable, B](a: A): Fu[B] = Future failed a
  def fufail[A](a: String): Fu[A] = fufail(new Exception(a))
  val funit = fuccess(())

  implicit def execontext = play.api.libs.concurrent.Execution.defaultContext

  implicit final class LilaPimpedFuture[A](fua: Fu[A]) {

    def >>-(sideEffect: => Unit): Fu[A] = fua andThen {
      case _ => sideEffect
    }

    def >>[B](fub: => Fu[B]): Fu[B] = fua flatMap (_ => fub)

    def void: Funit = fua map (_ => Unit)

    def inject[B](b: => B): Fu[B] = fua map (_ => b)
  }

  implicit class LilaPimpedBoolean(self: Boolean) {

    def fold[A](t: => A, f: => A): A = if (self) t else f

    def option[A](a: => A): Option[A] = if (self) Some(a) else None
  }

  implicit class LilaPimpedOption[A](self: Option[A]) {

    import scalaz.std.{ option => o }

    def |(a: => A): A = self getOrElse a

    def unary_~(implicit z: Zero[A]): A = self getOrElse z.zero
  }
}
