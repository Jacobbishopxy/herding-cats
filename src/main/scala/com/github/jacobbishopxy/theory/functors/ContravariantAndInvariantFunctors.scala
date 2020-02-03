package com.github.jacobbishopxy.theory.functors

/**
 * Created by jacob on 2/3/2020
 *
 * As we have seen, we can think of Functor's map method as "appending" a transformation to a chain.
 * We're now going to look at two other type classes, one representing prepending operations to a chain,
 * and one representing building a bidirectional chain of operations.
 * These are called contravariant and invariant functors respectively.
 */
object ContravariantAndInvariantFunctors {

  /**
   * Contravariant Functors and the `contramap` Method
   *
   * The first of our type classes, the contravariant functor, provides an operation called `contramap`
   * that represents "prepending" an operation to a chain.
   * (F[B] contramap A => b -> F[A])
   *
   * The contramap method only makes sense for data types that represent transformations. For example,
   * we can't define contramap for an Option because there is no way of feeding a value in an Option[B]
   * backwards through a function A => B. However, we can define contramap for the Printable type class.
   */

  // A `Printable[A]` represents a transformation from A to String. Its contramap method accepts a function
  // func of type b => A and creates a new Printable[B]

  trait Printable[A] {
    def format(value: A): String

    def contramap[B](func: B => A): Printable[B] = ???
  }

  def format[A](value: A)(implicit p: Printable[A]): String =
    p.format(value)


  /**
   * Invariant functors implement a method called imap that is informally equivalent to a combination of
   * map and contramap. If map generates new-type class instances by appending a function to a chain, and
   * contramap generates them by prepending an operation to a chain, imap generates them via a pair of
   * bidirectional transformations.
   *
   * (F[A] imap A => B B => A -> F[B])
   */

  /**
   * The most intuitive examples of this are a type class that represents encoding and decoding as some
   * data type, such as Play JSON's Format and scodec's Codec. We can build our own Codec by enhancing
   * Printable to support encoding and decoding to/from a String:
   */

  trait Codec[A] {
    def encode(value: A): String
    def decode(value: String): A
    def imap[B](dec: A => B, enc: B => A): Codec[B] = ???
  }

  def encode[A](value: A)(implicit c: Codec[A]): String =
    c.encode(value)

  def decode[A](value: String)(implicit c: Codec[A]): A =
    c.decode(value)

  // If we have a Codec[A] and a pair of functions A => B and B => A, the imap method creates a Codec[B]
  // As an example use case, imagine we have a basic Codec[String], whose encode and decode methods are
  // both a no-op:

  implicit val stringCodec: Codec[String] =
    new Codec[String] {
      override def encode(value: String): String = value
      override def decode(value: String): String = value
    }

  // We can construct many useful Codecs for other types by building off of stringCodec using imap:
  implicit val intCodec: Codec[Int] =
    stringCodec.imap(_.toInt, _.toString)

  implicit val booleanCodec: Codec[Boolean] =
    stringCodec.imap(_.toBoolean, _.toString)

}

object Exercise1 {

  // Showing off with Contramap

}

object Exercise2 {

  // Transformative Thinking with imap

}
