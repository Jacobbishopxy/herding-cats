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
    
    

- The infrastructure package

- The config package


### 依赖注入 What about dependency injection?

### 装配组合 Fitting it all together

### 什么是F What is with this F thing?

