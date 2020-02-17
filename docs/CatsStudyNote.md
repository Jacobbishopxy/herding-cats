# Cats Study Notes

## 学习路径

### 前置条件

1. 理解Type class
    - type class模式的定义与构成
        1. type class本身
        2. type class实例（instance）
        3. type class接口（interface）
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
    - 形变（variance）的函子（前置：Scala的形变）
        1. 协变（covariant）
            - `map`
        2. 逆变（contravariant）
            - `contramap`: `def contramap[A, B](fa: F[A])(f: B => A): F[B]`
        3. 不变（invariant）
            - `imap`: `def imap[A, B](fa: F[A])(f: A => B)(g: B => A): F[B]`
    
3. Monads（单子）
    - 定义与使用
    - 5种实用Monad
        - Identity：类型的别名，功能：原子类型 -> 单个参数类型构造函数
        - Eval：求值类型的抽象，包含3个子类
            - `Eval.now`：与Scala的`val`对应（立即求值并储存）
            - `Eval.later`：与Scala的`lazy val`对应（调用计算并储存）
            - `Eval.always`：与Scala的`def`对应（调用计算不储存）
        - Writer：携带log并计算
            - `Writer[W, A]`携带`W`日志类型以及类型`A`的计算
        - Reader：用于序列化依赖某相同输入的一系列函数（可用于依赖注入）
            - `Reader[A, B]`接受`A=>B`函数作为参数
        - State：携带状态（input state转换为output state）与计算，以纯函数方式模拟mutable state
            - `State[S, A]`实例代表`S => (S, A)`函数，`S`为状态类型，`A`为计算类型
            - 5种构造器：
                1. `State.get`：提取并返回state
                2. `State.set`：更新state返回unit
                3. `State.pure`：忽略state返回已提供的结果
                4. `State.inspect`：提取state后通过转换函数返回
                5. `State.modify`：通过更新函数更新state返回unit
    - 自定义Monad
        - 实现：
            1. `flatMap`
            2. `pure`
            3. `tailRecM`
    
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
        1. Identity：单位元法则`fa.map(a => a) == fa`，其中`a => a`即identity函数
        2. Composition：组合法则`fa.map(g(f(_))) == fa.map(f).map(g)`
    - 功能：实现`F[A]`到`F[B]`的转换，保持context（即`F`）不变
    
2. Monad
    - 构成：
        1. `pure`函数：`A => F[A]`，即通过plain value创建一个monadic context（即`F[_]`）
        2. `flatMap`函数：`(F[A], A => F[B]) => F[B]`，即从monadic context中提取值，然后产生一个新的monadic context
    - 法则：
        1. Left Identity：`pure(x).flatMap(f) == f(x)`
        2. Right Identity：`m.flatMap(pure) == m`
        3. Associativity：结合性法则`m.flatMap(f).flatMap(g) == m.flatMap(x => f(x).flatMap(g))`
    - 功能（拆解）：
        1. 绑定已经解除包裹的值`A`
        2. 将已经解除包裹的值输入函数`A => B`
        3. 一个被重新包裹的值被输出`B => F[B]`
    - 扩展：for表达式是对于序列化的flatMap与map整合而进行序列化的操作
    
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
    - Foldable
        - 抽象`foldLef`和`foldRight`操作
        - `foldLeft`实现：`def foldLeft[A, B](fa: F[A], b: B)(f: (B, A) => B): B`
        - `foldRight`实现：`def foldRight[A, B](fa: F[A], lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B]`
        - 构建方法：`accumulator` + `binary function`
            - `accumulator`：monadic context
            - `binary function`：构建`accumulator`的value（类型`B`）与`item`（类型`A`）关系，返回monadic value
    - Traverse
        - 更高级别的抽象相比于`Foldable`，运用`Applicative`的遍历操作（最强大的模式！）
        - 逻辑：
            0. 前提：`F`有`Traverse`实例，以及`G`有`Applicative`实例
            1. traverse: `(G[A], A => F[B]) => F[G[B]]`
            2. sequence: `G[F[A]] => F[G[A]]`

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

### Glossary

[Link](https://typelevel.org/cats/nomenclature.html)

#### Type-Classes over an `F[_]`

1. Functor

    |Type|Method Name|
    | --- | --- |
    |`F[A] => F[Unit]`|`void`|
    |`F[A] => B => F[B]`|`as`|
    |`F[A] => (A => B) => F[B]`|`map`|
    |`F[A] => (A => B) => F[(A, B)]`|`fproduct`|
    |`F[A] => B => F[(B, A)]`|`typleLeft`|
    |`F[A] => B => F[(A, B)]`|`typleRight`|
    |`(A => B) => (F[A] => F[B])`|`lift`|

2. Apply

    |Type|Method Name|Symbol|
    | --- | --- | --- |
    |`F[A] => F[B] => F[A]`|`productL`|`<*`|
    |`F[A] => F[B] => F[B]`|`productR`|`*>`|
    |`F[A] => F[B] => F[(A, B)]`|`product`||
    |`F[A => B] => F[A] => F[B]`|`ap`|`<*>`|
    |`F[A => B => c] => F[A] => F[B] => F[C]`|`ap2`||
    |`F[A] => F[B] => (A => B => c) => F[C]`|`map2`||

3. Applicative

    |Type|Method Name|Notes|
    | --- | --- | --- |
    |`A => F[A]`|`pure`||
    |`=> F[Unit]`|`unit`||
    |`Boolean => F[Unit] => F[Unit]`|`when`|Performs effect iff condition is true|
    | |`unless`|Adds effect iff condition is false|

4. FlatMap

    |Type|Method Name|
    | --- | --- |
    |`F[F[A]] => F[A]`|`flatten`|
    |`F[A] => (A => F[B]) => F[B]`|`flatMap`|
    |`F[A] => (A => F[B]) => F[(A,B)]`|`productM`|
    |`F[Boolean] => F[A] => F[A] => F[A]`|`ifM`|
    |`F[A] => (A => F[B]) => F[A]`|`flatTap`|

5. FunctorFilter

    |Type|Method Name|Notes|
    | --- | --- | --- |
    |`F[A] => (A => Boolean) => F[A]`|`filter`||
    |`F[A] => (A => Option[B]) => F[B]`|`mapFilter`||
    |`F[A] => (A => B) => F[B]`|`collect`|The `A => B` is a PartialFunction|
    |`F[Option[A]] => F[A]`|`flattenOption`||

6. ApplicativeError

7. MonadError

8. UnorderedFoldable

9. Foldable

    |Type|Method Name|Constraints|
    | --- | --- | --- |
    |`F[A] => A`|`fold`|`A: Monoid`|
    |`F[A] => B => ((B,A) => B) => F[B]`|`foldLeft`||
    |`F[A] => (A => B) => B`|`foldMap`|`B: Monoid`|
    |`F[A] => (A => G[B]) => G[B]`|`foldMapM`|`G: Monad` and `B: Monoid`|
    |`F[A] => (A => B) => Option[B]`|`collectFirst`|The `A => B` is a `PartialFunction`|
    |`F[A] => (A => Option[B]) => Option[B]`|`collectFirstSome`||
    |`F[A] => (A => G[B]) => G[Unit]`|`traverse_`|`G: Applicative`|
    |`F[G[A]] => G[Unit]`|`sequence_`|`G: Applicative`|
    |`F[A] => (A => Either[B, C] => (F[B], F[C])`|`partitionEither`|`G: Applicative`|

10. Reducible

    |Type|Method Name|Constraints|
    | --- | --- | --- |
    |`F[A] => ((A,A) => A) => A`|`reduceLeft`||
    |`F[A] => A`|`reduce`|`A: Semigroup`|

11. Traverse

    |Type|Method Name|Constraints|
    | --- | --- | --- |
    |`F[G[A]] => G[F[A]]`|`sequence`|`G: Applicative`|
    |`F[A] => (A => G[B]) => G[F[B]]`|`traverse`|`G: Applicative`|
    |`F[A] => (A => G[F[B]]) => G[F[B]]`|`flatTraverse`|`F: FlatMap` and `G: Applicative`|
    |`F[G[F[A]]] => G[F[A]]`|`flatSequence`|`G: Applicative` and `F: FlatMap`|
    |`F[A] => F[(A,Int)]`|`zipWithIndex`||
    |`F[A] => ((A,Int) => B) => F[B]`|`mapWithIndex`||
    |`F[A] => ((A,Int) => G[B]) => G[F[B]]`|`traverseWithIndex`|`F: Monad`|

#### Transformers

1. Constructors and wrappers

    |`Data Type`|is an alias or wrapper of|
    | --- | --- |
    |`OptionT[F[_], A]`|`F[Option[A]]`|
    |`EitherT[F[_], A, B]`|`F[Either[A,B]`|
    |`Kleisli[F[_], A, B]`|`A => F[B]`|
    |`Reader[A, B]`|`A => B`|
    |`ReaderT[F[_], A, B]`|`Kleisli[F, A, B]`|
    |`Writer[A, B]`|`(A,B)`|
    |`WriterT[F[_], A, B]`|`F[(A,B)]`|
    |`Tuple2K[F[_], G[_], A]`|`(F[A], G[A])`|
    |`EitherK[F[_], G[_], A]`|`Either[F[A], G[A]]`|
    |`FunctionK[F[_], G[_]`|`F[X] => G[X]` for every X|
    |`F ~> G`|Alias of `FunctionK[F, G]`|

2. OptionT

    使用`OT`缩写`OptionT`.
    
    |Type|Method Name|Constraints|
    | --- | --- | --- |
    |`=> OT[F, A]`|`none`|`F: Applicative`|
    |`A => OT[F, A]`|`some`or `pure`|`F: Applicative`|
    |`F[A] => OT[F, A]`|`liftF`|`F: Functor`|
    |`OT[F, A] => F[Option[A]]`|`value`||
    |`OT[F, A] => (A => B) => OT[F, B]`|`map`|`F: Functor`|
    |`OT[F, A] => (F ~> G) => OT[G, B]`|`mapK`||
    |`OT[F, A] => (A => Option[B]) => OT[F, B]`|`mapFilter`|`F: Functor`|
    |`OT[F, A] => B => (A => B) => F[B]`|`fold`or `cata`||
    |`OT[F, A] => (A => OT[F, B]) => OT[F,B]`|`flatMap`||
    |`OT[F, A] => (A => F[Option[B]]) => F[B]`|`flatMapF`|`F: Monad`|
    |`OT[F, A] => A => F[A]`|`getOrElse`|`F: Functor`|
    |`OT[F, A] => F[A] => F[A]`|`getOrElseF`|`F: Monad`|
    |`OT[F, A] => OT[F, A] => OT[F, A]`||

3. EitherT

    使用`ET`缩写`EitherT`，使用`A`和`B`代替`Either`的`Left`和`Right`.
    
    |Type|Method Name|Constraints|
    | --- | --- | --- |
    |`A => ET[F, A, B]`|`leftT`|`F: Applicative`|
    |`B => ET[F, A, B]`|`rightT`|`F: Applicative`|
    | |`pure`|`F: Applicative`|
    |`F[A] => ET[F, A, B]`|`left`|`F: Applicative`|
    |`F[B] => ET[F, A, B]`|`right`|`F: Applicative`|
    | |`liftF`|`F: Applicative`|
    |`Either[A, B] => ET[F, A, B]`|`fromEither`|`F: Applicative`|
    |`Option[B] => A => ET[F, A, B]`|`fromOption`|`F: Applicative`|
    |`F[Option[B]] => A => ET[F, A, B]`|`fromOptionF`|`F: Functor`|
    |`Boolean => B => A => ET[F, A, B]`|`cond`|`F: Applicative`|
    |`ET[F, A, B] => (A => C) => (B => C) => F[C]`|`fold`|`F: Functor`|
    |`ET[F, A, B] => ET[F, B, A]`|`swap`|`F: Functor`|
    |`ET[F, A, A] => F[A]`|`merge`| |	 

4. Kleisli(or ReaderT)

    使用`Ki`缩写`Kleisli`.
    
    |Type|Method Name|Constraints
    | --- | --- | --- |
    |`Ki[F, A, B] => (A => F[B])`|`run`||	 
    |`Ki[F, A, B] => A => F[B]`|`apply`||	 
    |`A => Ki[F, A, A]`|`ask`|`F: Applicative`|
    |`B => Ki[F, A, B]`|`pure`|`F: Applicative`|
    |`F[B] => Ki[F, A, B]`|`liftF`||	 
    |`Ki[F, A, B] => (C => A) => Ki[F, C, B]`|`local`||	 
    |`Ki[F, A, B] => Ki[F, A, A]`|`tap`||	 
    |`Ki[F, A, B] => (B => C) => Ki[F, A, C]`|`map`||	 
    |`Ki[F, A, B] => (F ~> G) => Ki[G, A, B]`|`mapK`||	 
    |`Ki[F, A, B] => (F[B] => G[C]) => Ki[F, A, C]`|`mapF`||	 
    |`Ki[F, A, B] => Ki[F, A, F[B]]`|`lower`||	 

#### Type Classes for types `F[_, _]`

1. Bifunctor

2. Profunctor

3. Strong Profunctor

4. Compose, Category, Choice

5. Arrow

6. ArrowChoice

#### Simplifications

- 用`A, B, C`来表示类型变量`*`，用`F, G, H`用来表示高阶类型变量。

- 用科里化结构表示类型签名：每次传入一个参数，他们之间用箭头`=>`分割。Scala中，一个方法的参数有可能被分割成多个以逗号区分的数组。

- 在type-class trait（如`trait Functor`），或是伴生对象，或是语法伴生类（如`implicit class`）上，各种方法保持一致。

- 对于在typeclass trait上被定义为方法的函数，忽略receiver对象。

- 忽略代表着type-class约束的隐式参数，表中用`Constraints`列表示。

- 使用`A => B`表达`Function1[A, B]`和`PartialFunction[A, B]`参数，表中用`PartialFunction`列表示。

- 忽略个别使用[Partially Applied Type Params](https://typelevel.org/cats/guidelines.html#partially-applied-type-params)定义的函数。

- 忽略`by-name`和`by-value`的入参差异。用`=> A`表示无入参的产量函数。

- 忽略Scala的variance的写法。在某些方法中需要子类型约束的（如`B >: A`），同样忽略这些额外的类型参数。
