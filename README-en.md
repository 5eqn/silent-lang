## silent-lang

Silent-Lang is a language targeting Competitive Programming. It compiles to LLVM-IR (for now).

The name is from a character of *Slay the Spire*, The Silent.

### samples

Please refer to [samples](sample/).

#### partial eval

```
let swap = (f: int -> int -> int) => (x: int) => (y: int) => f(y)(x)
let test = (x: int) => (y: int) => x
let x = input
let y = input
print(swap(test)(x)(y))
```

compiles to:

```
define dso_local i32 @main() #0 {
  %x1 = call i32 @input()
  %x2 = call i32 @input()
  call void @print(i32 noundef %x2)
  ret i32 0
}
```

#### control flow

```
let a = input
let b = if a == 1 then 114 else 514
let c = if a == 2 then 51121 else b
print(c)
```

compiles to:

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

#### tuple

```
let a, b = 3, input in
let c, d = input, 4 in
let pair = 1, 1 in
let add = (p: int, int) =>
  let x, y = p in
  x + y in
print(add(pair) + a + d + c + b)
```

compiles to:

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
