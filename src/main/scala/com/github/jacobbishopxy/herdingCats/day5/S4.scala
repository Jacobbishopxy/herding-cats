package com.github.jacobbishopxy.herdingCats.day5

/**
 * Created by jacob on 2/14/2020
 *
 * A knight’s quest
 *
 * LYAHFGG:
 *
 * > Here’s a problem that really lends itself to being solved with non-determinism. Say you have a chess board and
 * only one knight piece on it. We want to find out if the knight can reach a certain position in three moves.
 */
object S4 {

  case class KnightPos(c: Int, r: Int) {
    def move: List[KnightPos] =
      for {
        KnightPos(c2, r2) <- List(
          KnightPos(c + 2, r - 1), KnightPos(c + 2, r + 1),
          KnightPos(c - 2, r - 1), KnightPos(c - 2, r + 1),
          KnightPos(c + 1, r - 2), KnightPos(c + 1, r + 2),
          KnightPos(c - 1, r - 2), KnightPos(c - 1, r + 2)
        ) if ((1 to 8).toList contains c2) && ((1 to 8).toList contains r2)
      } yield KnightPos(c2, r2)

    def in3: List[KnightPos] =
      for {
        first <- move
        second <- first.move
        third <- second.move
      } yield third

    def canReachIn3(end: KnightPos): Boolean = in3 contains end
  }
}
