package com.github.jacobbishopxy.herdingCats.day6

/**
 * Created by jacob on 2/15/2020
 *
 * Reader datatype
 * Learn You a Haskell for Great Good says:
 *
 * > In the chapter about applicatives, we saw that the function type, (->) r is an instance of Functor.
 * We’ve also seen that functions are applicative functors. They allow us to operate on the eventual results of
 * functions as if we already had their results.
 * Not only is the function type (->) r a functor and an applicative functor, but it’s also a monad. Just like
 * other monadic values that we’ve met so far, a function can also be considered a value with a context.
 * The context for functions is that that value is not present yet and that we have to apply that function to
 * something in order to get its result value.
 */
object S2 {

  import cats._
  import cats.data._
  import cats.implicits._

  val addStuff: Int => Int = for {
    a <- (_: Int) * 2
    b <- (_: Int) + 10
  } yield a + b

  addStuff(3) // 19

  /**
   * Both (*2) and (+10) get applied to the number 3 in this case. return (a+b) does as well, but it ignores it
   * and always presents a+b as the result. For this reason, the function monad is also called the reader monad.
   * All the functions read from a common source.
   *
   * The Reader monad lets us pretend the value is already there. I am guessing that this works only for functions
   * that accepts one parameter.
   */

  /**
   * Dependency injection
   *
   * Imagine we have a case class for a user, and a trait that abstracts the data store to get them.
   */

  case class User(id: Long, parentId: Long, name: String, email: String)
  trait UserRepo {
    def get(id: Long): User
    def find(name: String): User
  }

  // Next we define a primitive reader for each operation defined in the `UserRepo` trait:

  trait Users {
    def getUser(id: Long): UserRepo => User =
      repo => repo.get(id)

    def findUser(name: String): UserRepo => User =
      repo => repo.find(name)
  }

  // Based on the primitive readers, we can compose other readers, including the application.

  object UserInfo extends Users {
    def userInfo(name: String): UserRepo => Map[String, String] =
      for {
        user <- findUser(name)
        boss <- getUser(user.parentId)
      } yield Map(
        "name" -> s"${user.name}",
        "email" -> s"${user.email}",
        "boss_name" -> s"${boss.name}"
      )
  }

  trait Program {
    def app: UserRepo => String =
      for {
        fredo <- UserInfo.userInfo("Fredo")
      } yield fredo.toString
  }

  // To run this app, we need something that provides an implementation for UserRepo:

  val testUsers = List(
    User(0, 0, "Vito", "vito@example.com"),
    User(1, 0, "Michael", "michael@example.com"),
    User(2, 0, "Fredo", "fredo@example.com")
  )

  object Main extends Program {
    def run: String = app(mkUserRepo)
    def mkUserRepo: UserRepo = new UserRepo {
      def get(id: Long): User = (testUsers find { _.id === id }).get
      def find(name: String): User = (testUsers find { _.name === name }).get
    }
  }

  Main.run // Map(name -> Fredo, email -> fredo@example.com, boss_name -> Vito)

}
