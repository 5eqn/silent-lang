## silent-lang

Silently compiles FP to LLVM-IR.

### showcase

参见 [samples](sample/)。

#### 部分求值

下面是一段充斥着高阶函数的代码：

```
let swap = (f: ptr) => (x: int) => (y: int) => f(y)(x)
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
- [ ] tail-rec
- [x] tuple
- [ ] vector
- [ ] string
