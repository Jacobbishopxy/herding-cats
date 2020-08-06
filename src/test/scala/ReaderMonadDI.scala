import cats.Id
import cats.data._

/**
 * Created by Jacob Xie on 8/3/2020
 *
 * origin from:
 * https://medium.com/rahasak/dependency-injection-with-reader-monad-in-scala-fe05b29e04dd
 */
object ReaderMonadDI extends App {

  // 1. Dependencies

  trait UserRepo {
    def create(user: User): Long

    def get(id: Long): User
  }

  trait PermissionRepo {
    def create(permission: Permission): Long

    def get(id: Long): Permission

    def search(role: String): List[Permission]
  }

  case class User(id: Long, name: String, role: String)
  case class Permission(id: Long, role: String, name: String)
  case class Repo(userRepo: UserRepo, permissionRepo: PermissionRepo)

  val roles = List("admin", "manager", "legal_officer", "user")
  val permissions = List("use_archive", "use_doc_storage", "use_schema", "use_auth")

  class UserRepoImpl extends UserRepo {
    override def create(user: User): Long = {
      println(s"user created $user")
      user.id
    }

    override def get(id: Long): User = {
      println(s"get user $id")
      User(id, s"lambda$id", util.Random.shuffle(roles).head)
    }
  }

  class PermissionRepoImpl extends PermissionRepo {
    override def create(permission: Permission): Long = {
      println(s"permission created $permission")
      permission.id
    }

    override def get(id: Long): Permission = {
      println(s"get permission $id")
      Permission(id, util.Random.shuffle(roles).head, util.Random.shuffle(permissions).head)
    }

    override def search(role: String): List[Permission] =
      List(
        Permission(1001, role, "user_archive"),
        Permission(1002, role, "use_doc_storage"),
        Permission(1005, role, "use_auth")
      )
  }


  // 2. Reader Monad

  object UserHandler {
    def createUser(user: User): Reader[Repo, Long] =
      Reader((repo: Repo) => repo.userRepo.create(user))

    def getUser(id: Long): Reader[Repo, User] =
      Reader((repo: Repo) => repo.userRepo.get(id))
  }

  object PermissionHandler {
    def createPermission(permission: Permission): Reader[Repo, Long] =
      Reader((repo: Repo) => repo.permissionRepo.create(permission))

    def getPermission(id: Long): Reader[Repo, Permission] =
      Reader((repo: Repo) => repo.permissionRepo.get(id))

    def searchPermission(role: String): Reader[Repo, List[Permission]] =
      Reader((repo: Repo) => repo.permissionRepo.search(role))
  }

  object UserPermissionHandler {
    def getUserPermissions(id: Long): ReaderT[Id, Repo, List[Permission]] =
      for {
        u <- UserHandler.getUser(id)
        p <- PermissionHandler.searchPermission(u.role)
      } yield p
  }

  // 3. App

  val userRepo = new UserRepoImpl
  val permissionRepo = new PermissionRepoImpl
  val repo = Repo(userRepo, permissionRepo)

  val createUserResp =
    UserHandler
      .createUser(User(1001, "lambda", "admin"))
      .run(repo)

  val getUserResp =
    UserHandler
      .getUser(1001)
      .run(repo)

  val createPermissionResp =
    PermissionHandler
      .createPermission(Permission(2001, "admin", "use_auth"))
      .run(repo)

  val getPermissionResp =
    PermissionHandler
      .getPermission(1001)
      .run(repo)

  val getUserPermissions =
    UserPermissionHandler
      .getUserPermissions(1003)
      .run(repo)

}

