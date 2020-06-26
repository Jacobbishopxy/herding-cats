# Free Monad

## 什么是Free Monad？

基本上可以认为Free Monad是一种表达程序的方式（这里的程序特指抽象过的业务过程）。更准确的说法是一种用于表达计算的AST（Abstract Syntax Tree）。
Free Monad可以结合这些计算使之成为函数，而之后再结合变为高阶函数。这么做便可以大大的提高我们代码的组合性！

除此之外，Free Monad消除了业务实现部分细节修改的必要（分离解释与实现）。
另外我们代码的API不再局限于定义为`Option`，`Futuer`或是其他的Monads。

由于描述性的解释必须被实现，这就意味着需要运行带有我们AST的解释器，因此只需要在解释器中去关心实现的问题。
由于这些细节是与我们业务逻辑分离的，因此非常容易进行测试。此外，同样的函数可以被执行在不同的地方，即使使用了不同的解释器。

Free monad提供了以下实践：

1. 将有状态的计算表示为数据，并运行它们；

2. 以堆栈安全的方式运行递归计算；

3. 构建嵌入式DSL；

4. 用一种自然的方式将计算重新定位到一个解释器上。

数学原理：一个free monad（至少在编程语言环境中）是一个左伴随的定义域是Monads，值域是自函子（Endofunctors）的结构（Monads -> Endofunctors）
所投射出的“健忘”（Forgetful）的函子。具体而言，Free是一个足够聪明的结构，它允许我们从任何函子（functor）构建出一个简单的Monad。
这个所谓“健忘”的函子（Forgetful functor）它接受一个Monad输入：

1. 忽略它的monadic（flatMap）部分；

2. 忽略它的pointed（pure）部分；

3. 只保留了functor（map）部分。

Free程序由三部分组成：

1. 描述（description）：用ADT（algebraic data type）来抽象行为，也就是定义A；

2. 关注分离（separation of concern）：实现suspend，即用DSL来实现业务流程；

3. 实现（implementation）：实现Pure，得到左伴随（Monad），即根据ADT得到Monad。

我们的程序相应地可以编写为“程序”（即“描述”部分），编译“程序”（“实现”部分），和执行“程序”（执行分离出来的运算流程）三个部分。


## Free Monad如何工作的

1. 创建AST

2. 创建DSL

3. program

4. interpreter

5. 运行program

