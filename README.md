## silent-lang

English version: TODO

寂语言，专为算法题设计的函数式编程语言，编译到 LLVM-IR。

名字来源：「杀戮尖塔」游戏角色，寂静猎手。

### read guide

[Model.scala](src/main/scala/Model.scala) 存储了 silent-lang 和 LLVM-IR 的语法树，其中 LLVM-IR 的语法树比较有意思：IROps 其实相当于 letlist，用于记住已算出的变量的同时，使得编译结果可以用线性的「操作过程」表述出来。

[Evaluation.scala](src/main/scala/Evaluation.scala) 比较重要，在进行部分求值的时候同时把 silent-lang 里语法树转化成 LLVM-IR 语法树，注释也较多。

[Elaboration.scala](src/main/scala/Elaboration.scala) 暂时用于进行类型检查，同时把 silent-lang 表语法树转化成里语法树。后续该模块还会用于推导置空的类型。

虽然 Scala 有 Parsec 库，但我还是手动实现了一个 [Parser.scala](src/main/scala/Parser.scala)，来加深对 Parsec 的理解，同时让该语言的「极简」名副其实（没有非必要的调库）。

### showcase

参见 [samples](sample/)。

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
  call void @print(i32 noundef %x2)
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
  call void @print(i32 noundef %x3)
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
  call void @print(i32 noundef %x4)
  ret i32 0
}
```

#### 数组和递归（WIP）

斐波那契数列：

```
let n = input in
let x, y, i = 1, 1, 2 rec
  if i == n then nope else
  y, x + y, i + 1 in

print(y)
```

`nope` 关键字名字来源：「炸弹猫咪」桌游卡牌，否决。

快速幂：

```
let a, p, r = input, input, 1 rec
  if p == 0 then nope else
  a * a, p / 2, if p & 1 then r * a else r in

print(r)
```

数组输出：

```
let n = input(100004) in

let a[i of n] = input in
let b[i of n] = print(a[i]) in

0
```

等效于：

```c
int a[100004];
int b[100004];
int main() {
  int i;
  i = 0;
  while (true) {
    if (i == n) {
      break;
    } else {
      a[i] = input();
    }
    i++;
  }
  i = 0;
  while (true) {
    if (i == n) {
      break;
    } else {
      b[i] = print(a[i]);
    }
    i++;
  }
  return 0;
}
```

递归输出：

```
let n = input(100004) in

let a[i of n] = input in
let i = 0 rec
  if i == n then nope else
  let _ = print(a[i]) in
  i + 1 in

i
```

等效于：

```c
int a[100004];
int main() {
  int i;
  i = 0;
  while (true) {
    if (i == n) {
      break;
    } else {
      a[i] = input();
    }
    i++;
  }
  i = 0;
  while (true) {
    if (i == n) {
      break;
    } else {
      print(a[i]);
      i = i + 1;
    }
  }
  return 0;
}
```

树状数组初始化：

```
let n = input in

let a[i of n + 1] = 
  if i == 0 then 0 else input in

let b[i of n + 1] = 
  if i == 0 then 0 else
  let t, m = a[i], i - 1 rec
    if m & 1 
    then t + b[m], m - m & -m
    else nope in
  t

let c[i of n + 1] = print(b[i])
```



### roadmap

- [x] partial evaluation
- [x] input
- [x] print
- [x] control flow
- [x] left-rec syntax
- [x] typecheck for function
- [ ] pretty errors
- [ ] tail-rec
- [x] tuple
- [ ] use llvm binding
- [ ] vector
- [ ] string
