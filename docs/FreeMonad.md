# Free Monad

## 什么是Free Monad？

基本上可以认为Free Monad是一种表达程序的方式（这里的程序特指抽象过的业务过程）。更准确的说法是一种用于表达计算的AST（Abstract Syntax Tree）。
Free Monad可以结合这些计算使之成为函数，而之后再结合变为高阶函数。这么做便可以大大的提高我们代码的组合性！

除此之外，Free Monad消除了业务实现部分细节修改的必要（分离解释与实现）。
另外我们代码的API不再局限于定义为`Option`，`Futuer`或是其他的Monads。

由于描述性的解释必须被实现，这就意味着需要运行带有我们AST的解释器，因此只需要在解释器中去关心实现的问题。
由于这些细节是与我们业务逻辑分离的，因此非常容易进行测试。此外，同样的函数可以被执行在不同的地方，即使使用了不同的解释器。

## Free Monad如何工作的

1. 创建AST

2. 创建DSL

3. program

4. interpreter

5. 运行program

