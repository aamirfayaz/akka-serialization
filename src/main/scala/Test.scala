import scala.util.Random

object Test extends App {

  val t1 = System.currentTimeMillis()
  Thread.sleep(2812)
  val t2 = System.currentTimeMillis()

  val res = (t2 - t1) * 1.0 / 1000

  println(f"Received  votes in $res%5.2f seconds") //5 digits in total with 2 decimal

  println(res)
}

object FTest extends App {
  var floatVar = 10.123
  var intVar = 5000
  var stringVar = "Hello There"

  var fs = printf("The value of the float variable is " + "%88.1f, while the value of the integer " + "variable is %d, and the string" + "is %s", floatVar, intVar, stringVar);

  println(fs)
}
