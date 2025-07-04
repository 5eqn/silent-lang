## silent-lang

[Tree-sitter](https://tree-sitter.github.io/tree-sitter/) grammar: [Zjl37/tree-sitter-silent-lang](https://github.com/Zjl37/tree-sitter-silent-lang)

English version: [README-en.md](README-en.md)

### 概念验证中……

~期望的特性~ 作者已经退坑 PL 了，所以不会做以下的特性：

- 媲美 C 的性能
  - 不动于衷：[FIP / FBIP](https://koka-lang.github.io/koka/doc/book.html#sec-fbip)
  - 线性数组：[Ordered Type](https://www.cs.cmu.edu/~rwh/papers/ordered/popl.pdf)
  - 缓存友好：[Parameterised over Pools](https://dl.acm.org/doi/10.1145/3133850.3133861)
  - 激进内联：[Partial Evaluation](https://pages.cs.wisc.edu/~cs701-1/LectureNotes/trunk/cs701-lec-09-03-2015/cs701-lec-09-03-2015.pdf)
- 自由的结构体
  - 任意纵切：[Row Poly](https://www.cl.cam.ac.uk/teaching/1415/L28/rows.pdf)
  - 自然元组：[Unboxed Tuple](https://ghc.gitlab.haskell.org/ghc/doc/users_guide/exts/primitives.html#unboxed-tuples)
  - 依值类型：[Dependent Type](https://wiki.haskell.org/Dependent_type)
- 美丽的可视化

### 介绍

绷语言，专为算法题设计的函数式编程语言，编译到 LLVM-IR。

推荐使用 😅 作为文件后缀和行注释标识。

当然，你也可以使用 `.silent` 作为文件后缀，用 C 语言风格的注释格式。

名字来源：「杀戮尖塔」游戏角色，寂静猎手（The Silent）。

### 样例展示

`demo.😅`：

```
👉 
   欢迎体验绷语言，Silent-Lang！
   我们使用 😅 表示开始「行注释」，相当于 C 语言的 //
   同时，左右手指之间的是「块注释」，相当于 C 语言的 /* */
   你有没有发现，这一段话就是块注释的内容？
👈

😅 使用 input 读入一个数字
let a = input

😅 C 里面的大部分操作符，例如 + * % & && > != ~，全部支持！
let b = -a + ~8

😅 使用匿名函数的形式创建函数
let 🙃 = (x: int) => 0 - x

😅 下面的数会在编译期被计算为 5，不会调用 🙃 函数造成额外性能开销！
let 五 = 🙃(-5)

😅 使用 print 函数输出值
let _ = print(🙃(b))

👉
   Silent-Lang 有用起来「非常舒服」的无括号元组！
   一句话概括，how you expect it to work, how it WILL work.
   无括号元组编译后会被变成一个个单独的变量，
   不会造成任何额外性能开销！
👈

😅 令 c, d, 妈妈生的 分别为 113, a, 9
let c, d, 妈妈生的 = 113, a, 9

😅 元组可以打包
let pair = 1, input

😅 元组还能作为函数参数
let add = (p: int, int) =>

  😅 元组可以解包
  let x, y = p
  x + y

😅 可以直接令 pair 为函数参数
let e = add(pair)

😅 也可以指定元组的两个值，可以任意重组！
let res = add(五, e)
let _ = print(res)

👉 
   Silent-Lang 有更适合 FP 宝宝体质的 while 循环！
   假设要计算 1 + 2 + ... + n: 
👈

😅 res 和 n 的初始值分别是 0 和 input，但其会按照 rec 后的内容进行「状态更新」
let res, n = 0, input rec
  
  😅 如果 n = 0，res 已经是最终结果，拒绝更新状态
  if n == 0 then nope else

  😅 否则计算下一状态的 res 和 n 并更新
  res + n, n - 1

😅 输出结果看看对不对！
let _ = print(res)

😅 感觉不理解？用 rec 还可以写阶乘：
let res, n = 1, input rec

  😅 如果 n = 1，res 已经是最终结果，拒绝更新状态
  if n == 1 then nope else

  😅 否则计算下一状态的 res, n 并更新
  res * n, n - 1

😅 输出结果看看对不对！
let _ = print(res)

😅 用 rec 还可以计算斐波那契数列：
let 上一项, 这一项, n = 1, 1, input rec
  
  😅 如果 n <= 2，这一项 已经是最终结果，拒绝更新状态
  if n <= 2 then nope else

  😅 否则计算下一状态的 上一项, 这一项, n 并更新
  这一项, 上一项 + 这一项, n - 1

😅 输出结果看看对不对！
let _ = print(这一项)

😅 还能算快速幂！res 将被计算为 a 的 p 次方
let a, p, res = input, input, 1 rec

  😅 如果 p = 0，res 直接就是结果
  if p == 0 then nope else

  😅 否则假如 p 是奇数，就需要给结果乘上 a
  let next = if p & 1 > 0 then res * a else res

  😅 转移结果
  a * a, p / 2, next

😅 输出结果看看对不对！
let _ = print(res)

😅 小挑战：怎么用 silent-lang 写个二分，以求出根号 n 的值？
let n = input
let 左边界, 右边界 = 0, 46340 rec
  if 左边界 + 1 == 右边界 then nope else
  let 🖕 = (左边界 + 右边界) / 2
  if 🖕 * 🖕 <= n
  then 🖕, 右边界
  else 左边界, 🖕

😅 输出结果看看对不对！
let _ = print(左边界)

😅 后续如果我有空，还会做数组喵
0
```

### 美丽的错误消息！

#### 变量未定义

```
😅 读入两个数字
let x, 测试 = input, 傻呗😋

😅 加起来
let z = x + 测试

😅 输出
print(z)
```

编译结果：

```
第 2 行 20 列有错误：
 | let x, 测试 = input, 傻呗😋
 |                      ^^^^^^
该变量未定义
```

注意中文和 emoji 的宽度会被正确处理！（在命令行里）

#### 类型不匹配

```
😅 读入两个数字
let x, y = input, input

😅 加起来
let z = x + ((a: int) => a)

😅 输出
print(z)
```

编译结果：

```
第 5 行 13 列有错误：
 | let z = x + ((a: int) => a)
 |             ^^^^^^^^^^^^^^^
这玩意的类型应该是 i32，但实际上是 ptr
```

#### 语法错误

```
😅 读入两个数字
let x, y = input, input

😅 加起来
let z != x + ((a: int) => a)

😅 输出
print(z)
```

编译结果：

```
第 5 行 7 列有错误：
 | let z != x + ((a: int) => a)
 |       ^
文件的语法有错误！
```

### 使用方式

1. 确保自己安装了 Java，且在命令行中使用 `java` 不会被告知该命令不存在
2. 在 [Releases](https://github.com/5eqn/silent-lang/releases) 中下载从 jar 包封装出来的可执行文件
3. 在命令行中直接执行下载到的文件，例如 `./Downloads/silent-launcher-jar`，如果看到 `Illegal command line: more arguments expected`，说明运行成功
4. 要编译一个文件，请指定源文件和输出路径，例如 `./silent-launcher-jar fn.silent fn.ll`
5. 可以考虑把 `silent-launcher-jar` 改名为 `silent` 并加入环境变量，这样可以直接通过 `silent` 来调用该程序

### 开发计划

- [x] partial evaluation
- [x] input
- [x] print
- [x] control flow
- [x] left-rec syntax
- [x] typecheck for function
- [x] more operators
- [x] comment
- [x] pretty errors
- [x] tail-rec
- [x] tuple
- [x] track grammar mistake
- [x] allow utf-8 variable names
- [x] performance improve
- [x] return value
- [x] prefix operators
- [ ] nope anywhere
- [ ] vector
- [ ] string
- [ ] print as operation
- [ ] pattern match anywhere
- [ ] dependent subtypes
- [ ] custom prefix and infix operators
- [ ] visuallized debugging
- [ ] mutual-rec
- [ ] closure
- [ ] use llvm binding
- [ ] c backend

### 技术细节

[Syntax.scala](src/main/scala/Syntax.scala) 存储了 silent-lang 的表语法树和里语法树。表语法树有些信息（例如类型标记）被省略，由类型检查器推导出类型之后转化成有更多信息的里语法树。

[LLVM.scala](src/main/scala/LLVM.scala) 存储了 LLVM-IR 的语法树，分为可引用的值 IRVal 和值的计算过程 IROps。后者用于记住已算出的变量的同时，使得编译结果可以用线性的「操作过程」表述出来。

[Elaboration.scala](src/main/scala/Elaboration.scala) 暂时用于进行类型检查，同时把 silent-lang 表语法树转化成里语法树。后续该模块还会用于推导置空的类型。

[Evaluation.scala](src/main/scala/Evaluation.scala) 比较重要，在进行部分求值的时候同时把 silent-lang 里语法树转化成 LLVM-IR 语法树，注释也较多。

虽然 Scala 有 Parsec 库，但我还是手动实现了一个 [Parser.scala](src/main/scala/Parser.scala)，来加深对 Parsec 的理解，同时让该语言的「极简」名副其实（没有非必要的调库）。

#### 部分求值

下面是一段充斥着高阶函数的代码：

```
let swap = (f: int -> int -> int) => (x: int) => (y: int) => f(y)(x)
let test = (x: int) => (y: int) => x
let x = input
let y = input
print(swap(test)(x)(y))
```

silent-lang 会对代码进行「部分求值」，发现实际上被 `print` 出来的只是 `y`！

所以这段代码会被编译成：

```
define dso_local i32 @main() #0 {
  %x1 = call i32 @input()
  %x2 = call i32 @input()
  call nope @print(i32 noundef %x2)
  ret i32 0
}
```

#### 控制流

下面的代码中 `if` 不能被部分求值消去：

```
let a = input
let b = if a == 1 then 114 else 514
let c = if a == 2 then 51121 else b
print(c)
```

在函数式编程中，`if ... then ... else ...` 是一个值！要怎么把它变成控制流？

silent-lang 会先定义一个「指针」，然后 `if` 的分支负责把结果「装填」到指针中。

所以这段代码会被编译成：

```
define dso_local i32 @main() #0 {
  %x1 = call i32 @input()
  %x4 = icmp eq i32 %x1, 1
  %x5 = alloca i32, align 4
  br i1 %x4, label %x6, label %x7

x6:
  store i32 114, ptr %x5, align 4
  br label %x8

x7:
  store i32 514, ptr %x5, align 4
  br label %x8

x8:
  %x2 = load i32, ptr %x5, align 4
  %x9 = icmp eq i32 %x1, 2
  %x10 = alloca i32, align 4
  br i1 %x9, label %x11, label %x12

x11:
  store i32 51121, ptr %x10, align 4
  br label %x13

x12:
  store i32 %x2, ptr %x10, align 4
  br label %x13

x13:
  %x3 = load i32, ptr %x10, align 4
  call nope @print(i32 noundef %x3)
  ret i32 0
}
```

#### 元组

对元组可以整很多花样，比如多重赋值：

```
let a, b = 3, input in
let c, d = input, 4 in
```

装包：

```
let pair = 1, 1 in
```

接受元组作为匿名函数的参数：

```
let add = (p: int, int) =>
```

解包：

```
  let x, y = p in
  x + y in
```

还需要能对元组进行部分求值：

```
print(add(pair) + a + d + c + b)
```

silent-lang 可以处理这些！上面的代码会被编译成：

```
define dso_local i32 @main() #0 {
  %x1 = call i32 @input()
  %x2 = call i32 @input()
  %x3 = add nsw i32 9, %x2
  %x4 = add nsw i32 %x3, %x1
  call nope @print(i32 noundef %x4)
  ret i32 0
}
```

#### 递归

斐波那契数列：

```
let n = input in
let x, y, i = 1, 1, 2 rec
  if i == n then nope else
  y, x + y, i + 1 in
print(y)
```

其实上面的代码等效于：

```
let n = input in
let fib = (args: int, int, int) =>
  let x, y, i = args in
  if i == n then x, y, i else
  fib(y, x + y, i + 1) in
let x, y, i = fib(1, 1, 2) in
print(y)
```

快速幂：

```
let a, p, r = input, input, 1 rec
  if p == 0 then nope else
  a * a, p / 2, (if p & 1 > 0 then r * a else r) in
print(r)
```

`nope` 关键字名字来源：「炸弹猫咪」桌游卡牌，否决。

#### 数组（WIP）

数组输出：

```
let n = input

let a[i] = rec if i == n then nope else input
let _[i] = rec if i == n then nope else print(a[i])

0
```

树状数组初始化：

```
let n = input in

let a[i] = 0 rec
  if i > n then nope else input

let b[i] = 0 rec
  if i > n then nope else
  let t, m = a[i], i - 1 rec
    if m & 1 
    then t + b[m], m - m & -m
    else nope
  t

let _[i] = 0 rec print(b[i])
```

二维数组：

```
let a[i] = rec if i == n then nope else
  let b[j] = rec if j == m then nope else input
  b
```

归并排序：

```
let sort = (arr: int[n]) =>
  match(n)
    [0, 1]   => arr
    [2, inf) => let a[i], x, y = nope, sort(arr[0..n/2]), sort(arr[n/2..1]) rec
      match(x, y)
        nil     , nil      => nope
        xh :: xt, nil      => xh, xt, nil
        nil     , yh :: yt => yh, nil, yt
        xh :: xt, yh :: yt => if xh < yh then xh, xt, y else yh, x, yt
```

快速排序：

```
let filt = (arr: int[n]) => (pred: int -> bool) =>
  let a[i], rest = nope, arr rec
    match(arr)
      nil => nope
      hd :: tl => if pred(hd) then hd, tl else nope, tl
  
let sort = (arr: int[n]) =>
  match(n)
    [0, 1]   => arr
    [2, inf) => 
      sort(filt(arr)((x: int) => x < arr[0])) + [arr[0]] + 
      sort(filt(arr)((x: int) => x >= arr[0]))
```

递归：

```
let fib = (x: int) =>
  if x == 0 then 0 else
  if x == 1 then 1 else
  fib(x - 1) + fib(x - 2)

fib(8)
```

```
init = 8
x <- init

```
