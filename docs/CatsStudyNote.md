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
    - 协变，不变，逆变的函子(covariant, invariant, contravariant functors)
    - 未整理
    
3. Monads（单子）
    - 定义与使用
    - 5种实用Monads
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

