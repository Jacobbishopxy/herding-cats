# Cats Study Notes

## 学习路径

### 前置条件

1. 理解Type class
    - type class模式的定义与构成
    - type class 与 CATS
    - type class的Scala语法
    - 扩展：使用simulacrum构建type class
    
2. 理解Scala的implicits
    - 理解implicit
    - implicits 与 type class
    - Scala语法
    
3. CATS库的使用
    - CATS实现type class
    - 如何使用CATS库
    
4. Scala的Variance
    - 丰富type class的类型参数
    - variance种类与作用

### 理论与实践

1. Monoids与Semigroups（幺半群与半群）
    - 定义与使用
    - 用途说明
    
2. Functors（函子）
    - 定义与使用
    - 使用方法
    - 协变，不变，逆变的函子(covariant, invariant, contravariant functors)
    - 未整理
    
3. Monads（单子）
    - 定义与使用
    - 5种实用Monads
        - Identity
        - Eval
        - Writer
        - Reader
        - State
    - 自定义Monad
    
4. Monad Transformers （单子转换）
    - 未整理
    
5. Semigroupal and Applicative 
    - 未整理
    
6. Foldable and Traverse
    - 未整理

## 概念

1. Functor
    - 构成：
        1. `F[A]`
        2. `map`函数：`(A => B) => F[B]`
    - 法则：
        1. Identity
        2. Composition
    - 功能：实现`F[A]`到`F[B]`的转换，保持context（即`F`）不变
    
2. Monad
    - 构成：
        1. `pure`函数：`A => F[A]`，即通过plain value创建一个monadic context（即`F[_]`）
        2. `flatMap`函数：`(F[A], A => F[B]) => F[B]`，即从monadic context中提取值，然后产生一个新的monadic context
    - 法则：
        1. Left Identity
        2. Right Identity
        3. Associativity
    - for表达式是对于序列化的flatMap整合而进行的操作
    
3. Monad Transformer
    - 每一种Transformer都是一种数据类型，定义在`cats.data`
    - 多个Monad实例嵌套转化为一个新的Monad实例
    
4. Semigroupal and Applicative
    - Semigroupal
        - 构成：
            1. `product`函数：`(F[A], F[B]) => F[(A, B)]`，`fa`与`fb`相互独立，即可以以任意顺序计算
        - 使用`apply`语法构建：
            1. `tupled`：Scala `tuple2` -> `tuple22`
            2. `mapN`：Scala case class
    - Validated（数据类型）
        - 功能：`Semigroupal[Validated]`可以自由实现accumulating的`product`函数
        - 子类：`Valid`和`Invalid`（类似于`Either`的`Left`和`Right`）
        - 比较：
            - `Either`: 快速返回的错误处理
            - `Validated`: 积累式的错误处理
    - Applicative
        - 构成：
            1. `Apply`:
                - 继承`Semigroupal`和`Functor`
                - `ap`函数：`(F[A], F[A => B]) => F[B]`，即function in context应用到value in context
            2. `Applicative`
                - 继承`Apply`
                - `pure`函数（同Monad的`pure`函数）
        - 内在函数关系：
            - `product` = `ap` + `map`
            - 实现：`def product[A, B](fa: F[A], fb: F[B]) = ap(map(fa)(a => (b: B) => (a, b)))(fb)`
        - 比较：
            - `Applicative`：在`Apply`基础上添加`pure`函数
            - `Monoid`：在`Semigroup`基础上添加`empty`函数
            - 其中：`pure`与`empty`概念类似
            
5. Foldable and Traverse

## Cats库模块

### Type Class

- Semigroup
- Monoid
- Applicative and Traversable Functors
    - Functor
    - Applicative
    - Traverse
- Monads
- Comonads
- Variance and Functors
    - Functor
    - Contravariant
    - ContravariantMonoidal
    - Invariant
    - InvariantMonoidal
- Alternative
- Bifunctor
- Eq
- Foldable
- Parallel
- SemigroupK
- MonoidK
- Show
- Reducible
- NonEmptyTraverse
- Arrow

### Data Type

- Chain
- Const
- ContT
- Either
- Eval
- FreeApplicatives
- FreeMonads
- FunctionK
- Id
- Ior
- Kleisli
- Nested
- NonEmptyList
- OneAnd
- OptionT
- EitherT
- IorT
- State
- Validated

