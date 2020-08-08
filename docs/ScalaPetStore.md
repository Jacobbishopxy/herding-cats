# Scala Pet Store

[Scala Pet Store](https://github.com/pauljamescleary/scala-pet-store)

该项目的目的：用函数式的技巧构建一个Scala的App。

## 技术栈

- Http4s
- Circe
- Doobie
- Cats
- ScalaCheck
- Circe Config
- ...

## 概念

### 领域驱动设计 Domain Driven Design (DDD)

DDD是设计一个独特的语言，该语言便于与业务同僚的交流。这里最重要的理念是语言即代码。比如有个"pet"的东西，我们需要在代码中看到`pet`。
这里推荐一本书《Domain Driven Design by Eric Evans》。

本书讨论了设计模式，一些我们可以在该项目中见到。所谓DDD便是让你的代码变得更具有表达性，能确保你与他人沟通时即是在沟通物化后你的代码。
要做的这一点，最好就是保证你的领域纯净pure。简言之在领域中，让业务逻辑和实体成为真实存在的，摒弃其余无关的事务。例如，银行的交易流水
`Transaction`依赖于储蓄账户`Debit`和信用账户`Credit`；这些概念需要在你的领域中表现出来。然而像HTTP, JDBC, SQL这些与领域无关的，
我们需要对其解耦。


### 洋葱（或六边形）架构 Onion (or Hexagonal) Architecture

具体在DDD中，洋葱和六边形架构设计模式可以使我们的领域从丑陋的实现中分离。

我们通过以下的机制构建我们的DDD：

- The domain package

    领域库用于构建我们的领域知识，同时解耦JDBC,JSON,HTTP等技术知识。我们使用服务`Services`作为领域的大颗粒接口，他们特指现实世界的
    用例。我们看到很多的CRUD在项目中，但是领域中的现实用例可以为提取withdraw，或是注册register等。通常情况下，我们会看到服务`Services`
    到HTTP API端口`Endpoints`的一对一映射。
    
    关于领域我们需要了解以下概念：

        1. `Service` - 一个粗颗粒的用例共同与其他的领域知识实现你的用例。
        
        2. `Repository` - 从持久化的数据中读取和写入。重要：Repositories没有任何的业务逻辑，他们不需要知道使用何种内容，也不应该
        泄漏他们实现的细节。
        
        3. `models` - 如`Pet`,`Order`,和`User`他们都是领域对象。我们需要他们简洁（与行为解耦）。所以的行为皆通过`Validations`
        与`Services`实现。
    
    注意`Repository`类似于Java的interface。Scala中我们用`trait`来实现。

- The infrastructure package

    基础库用于存放技术代码例如HTTP,JDBC等等。
    
    关于基础库我们需要了解：
    
        1. `endpoint` - 包含HTTP端使用http4s，以及关于JSON的方法使用circe。
        
        2. `repository` - 包含JDBC代码，我们`Repositories`的实现。这里我们有两种实现，内存型以及doobie型。

- The config package

    配置库也可视为基建，它于领域库无关。当应用程序启动的时候，我们使用的**Circe Config**将加载配置对象。同时，它提供也一个干净整洁的
    配置文件与case class的映射，使得我们不需写额外的代码。


### 依赖注入 What about dependency injection?

本项目现在多处使用`classes`（可能会有争议这并不FP）。关于DI有很多种方式，像是函数参数，隐式表达，monad转换。使用类构件像是在面向对象，
但是这么做可以让有OO背景的人更加便于理解。

本项目没有spring,guice,或是其它的DI/反转控制架构。坐着强烈反对类似这些库。

### 装配组合 Fitting it all together

FP的关键之处是在于保持你的领域纯净，并且解耦其余的知识。应用程序的启动是通过`Sever`这个类，它的职能是确保所有的组件是完善配置并且可用的。

`Server`类将会：

1. 使用纯净配置加载所有的配置。如果没有正确的配置，应用程序将不会启动。

2. 连接数据库。这里，我们序列化的处理连接逻辑确保数据库连接是在正常顺序下。如果有数据库不能被连接，应用程序将不会启动。

3. 创建我们的`Repositories`和`Services`。他们将会由我们的领域连接至一起。我们不使用任何的依赖注入库，而是由constructors传递实例。

4. 绑定端口并暴露我们的服务。如果端口被占用，应用程序将不会启动。

### 什么是F What is with this F thing?

我们可以看到核心领域里面用到了很多`F[_]`。这个叫做高阶类型*higher kinded type*，代表一个类型存储（或应用）其它的类型。例如，`List`和
`Option`就是存储其它类型，如`List[Int]`或`Option[String]`。

我们使用`F[_]`的意味着“某种带有副作用的类型”。我们可以抽象出这个里类型，最终在程序启动时加载于`Server`中。这种懒加载式的绑定，可以最大程度的
抽象你的代码，并在需要时才执行。

当你看到函数签名类似于`def update(pet: Pet)(implicit M: Monad[F])`时，我们可以说`F[_]`必须是一个`Monad`type class。

本项目中，我们使用`cats effect IO`作为我们的副作用类型，使用`cats`库中的Monads，其余的函数式type classes和数据类型。当然我们也可以
使用`scalazIO`和`scalaz`作为另一种实现，并且不需要大改我们的代码。

