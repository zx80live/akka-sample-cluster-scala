package sample.cluster.util

import scala.language.implicitConversions

/**
  * Source at https://github.com/zx80live/com.zx80.mod.util.console
  */
object ConsoleCSS {

  object Foreground {
    val DefaultForeground = "\u001b[39m"
    val Black = "\u001b[30m"
    val Red = "\u001b[31m"
    val Green = "\u001b[32m"
    val Yellow = "\u001b[33m"
    val Blue = "\u001b[34m"
    val Magenta = "\u001b[35m"
    val Cyan = "\u001b[36m"
    val LightGray = "\u001b[37m"
    val DarkGray = "\u001b[90m"
    val LightRed = "\u001b[91m"
    val LightGreen = "\u001b[92m"
    val LightYellow = "\u001b[93m"
    val LightBlue = "\u001b[94m"
    val LightMagenta = "\u001b[95m"
    val LightCyan = "\u001b[96m"
    val White = "\u001b[97m"

    def color(c: Int) = s"\u001b[38;5;${c}m"
  }

  object Background {
    val DefaultBackgroundColor = "\u001b[49m"
    val Black = "\u001b[40m"
    val Red = "\u001b[41m"
    val Green = "\u001b[42m"
    val Yellow = "\u001b[43m"
    val Blue = "\u001b[44m"
    val Magenta = "\u001b[45m"
    val Cyan = "\u001b[46m"
    val LightGray = "\u001b[47m"
    val DarkGray = "\u001b[100m"
    val LightRed = "\u001b[101m"
    val LightGreen = "\u001b[102m"
    val LightYellow = "\u001b[103m"
    val LightBlue = "\u001b[104m"
    val LightMagenta = "\u001b[105m"
    val LightCyan = "\u001b[106m"
    val White = "\u001b[107m"

    def color(c: Int) = s"\u001b[48;5;${c}m"
  }

  object Format {
    val Bold = "\u001b[1m"
    val Dim = "\u001b[2m"
    val Underlined = "\u001b[4m"
    val Blink = "\u001b[5m"
    val Reverse = "\u001b[7m"
    val Hidden = "\u001b[8m"
  }

  implicit class StringExt(s: String) {

    def |(s2: String): List[String] = s :: s2 :: Nil

    def foreground(c: Int): String = attr(s"\u001b[38;5;${c}m")

    def background(c: Int): String = attr(s"\u001b[48;5;${c}m")

    def attr(a: List[String]): String = s"""${a.mkString("")}$s${Console.RESET}"""

    def attr(a: String*): String = s"""${a.mkString("")}$s${Console.RESET}"""
  }

  implicit class StringListExt(xs: List[String]) {
    def |(s: String): List[String] = s :: xs
  }

  implicit def string2List(s: String): List[String] = s :: Nil

  def printlnStyled(s: Any, attr: List[String] = Nil): Unit = println(s.toString.attr(attr))

  def printStyled(s: Any, attr: List[String] = Nil): Unit = print(s.toString.attr(attr))
}