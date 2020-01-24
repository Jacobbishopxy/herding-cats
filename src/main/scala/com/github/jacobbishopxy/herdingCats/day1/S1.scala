package com.github.jacobbishopxy.herdingCats.day1

import cats._
import cats.data._
import cats.implicits._


/**
 * Created by Jacob Xie on 1/15/2020
 *
 * Haskell:
 * A typeclass is a sort of interface that defines some behavior. If a type is a part of a typeclass,
 * that means that it supports and implements the behavior the typeclass describes.
 *
 * CATS:
 * We are trying to make the library modular. It will have a tight core which will contain only the
 * typeclasses and the bare minimum of data structures that are needed to support them. Support for
 * using these typeclasses with the Scala standard library will be in the std project.
 *
 *
 * 1. Eq
 * Eq is used for types that support equality testing. The functions its members implement are == and /=.
 *
 * 2. Order
 * Ord is for types that have an ordering. Ord covers all the standard comparing functions such as >, <, >= and <=.
 *
 * 3. PartialOrder
 *
 * 4. Show
 * Members of Show can be presented as strings.
 *
 * 5. Read
 * Read is sort of the opposite typeclass of Show. The read function takes a string and returns a type
 * which is a member of Read.
 *
 * 6. Enum members are sequentially ordered types - they can be enumerated. The main advantage of the Enum
 * typeclass is that we can use its types in list ranges. They also have defined successors and predecessors,
 * which you can get with the succ and pred functions.
 *
 * 7. Numeric
 * Num is a numeric typeclass. Its members have the property of being able to act like numbers.
 */
object S1 {

  //println(1 === 2)

  /**
   * Instead of the standard ==, Eq enables === and =!= syntax by declaring eqv method. The main
   * differences is that === would fail compilation if you tried to compare Int and String.
   */

  //println(1 max 2.0)

  /**
   * Order enables compare syntax which returns Int: negative, zero, or positive. It also enables min and
   * max operators. Similar to Eq, comparing Int and Double fails compilation.
   */

  //println(1 tryCompare 2)

  /**
   * PartialOrder enables tryCompare syntax which returns Option[Int]. According to algebra, it'll return
   * None if operands are not comparable. It's returning Some(-1) when comparing 1.0 and Double.NaN.
   *
   * PartialOrder also enables >, >=, < and <= operators, but these are tricky to use because if you're mot
   * careful you could end up using the built-in comparison operators.
   */

  def lt[A: PartialOrder](a1: A, a2: A): Boolean = a1 <= a2

  lt[Int](1, 2)

  /**
   * It might silly to define `Show` because Scala already has toString on Any. Any also means anything would
   * match the criteria, so you lose type safety. The toString could be junk supplied by some parent class.
   */

  case class Person(name: String)
  case class Car(model: String)

  implicit val personShow: Show[Person] = Show.show[Person](_.name)

  Person("Jacob").show

  implicit val carShow: Show[Car] = Show.fromToString[Car]

  Car("Benz").show

}

object ATrafficLightDatatype {

  sealed trait TrafficLight
  object TrafficLight {
    case object Red extends TrafficLight
    case object Yellow extends TrafficLight
    case object Green extends TrafficLight
  }

  // let's define an instance for Eq.
  implicit val trafficLightEq: Eq[TrafficLight] =
    (x: TrafficLight, y: TrafficLight) => x == y

  //TrafficLight.Red === TrafficLight.Yellow  // error: value === is not a member of object TrafficLight.Red

  // So apparently Eq[TrafficLight] doesn't get picked up because Eq has non-variant subtyping: Eq[A]
  // One way to workaround this issue is to define helper functions to cast them up to TrafficLight:

  sealed trait TrafficLightPro
  object TrafficLightPro {
    case object Red extends TrafficLightPro
    case object Yellow extends TrafficLightPro
    case object Green extends TrafficLightPro

    def red: TrafficLightPro = Red
    def yellow: TrafficLightPro = Yellow
    def green: TrafficLightPro = Green
  }

  implicit val trafficLightProEq: Eq[TrafficLightPro] =
    (x: TrafficLightPro, y: TrafficLightPro) => x == y

  TrafficLightPro.red === TrafficLightPro.yellow
}
