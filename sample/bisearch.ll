target datalayout = "e-m:e-p270:32:32-p271:32:32-p272:64:64-i64:64-f80:128-n8:16:32:64-S128"
target triple = "x86_64-pc-linux-gnu"

@.str = private unnamed_addr constant [3 x i8] c"%d\00", align 1
@.str.1 = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1

; Function Attrs: noinline nounwind optnone sspstrong uwtable
define dso_local i32 @input() #0 {
  %1 = alloca i32, align 4
  %2 = call i32 (ptr, ...) @__isoc99_scanf(ptr noundef @.str, ptr noundef %1)
  %3 = load i32, ptr %1, align 4
  ret i32 %3
}

declare i32 @__isoc99_scanf(ptr noundef, ...) #1

; Function Attrs: noinline nounwind optnone sspstrong uwtable
define dso_local void @print(i32 noundef %0) #0 {
  %2 = alloca i32, align 4
  store i32 %0, ptr %2, align 4
  %3 = load i32, ptr %2, align 4
  %4 = call i32 (ptr, ...) @printf(ptr noundef @.str.1, i32 noundef %3)
  ret void
}

declare i32 @printf(ptr noundef, ...) #1

; Function Attrs: noinline nounwind optnone sspstrong uwtable
define dso_local i32 @main() #0 {
  %x1 = call i32 @input()
  %x2 = call i32 @input()
  %x22 = alloca i32, align 4
  %x23 = alloca i32, align 4
  store i32 0, ptr %x22, align 4
  store i32 100, ptr %x23, align 4
  br label %x24

x24:
  %l = load i32, ptr %x22, align 4
  %u = load i32, ptr %x23, align 4
  %x3 = add i32 %l, 1
  %x4 = icmp eq i32 %x3, %u
  %x26 = alloca i32, align 4
  %x27 = alloca i32, align 4
  br i1 %x4, label %x29, label %x30

x29:
  br label %x25

  br label %x31

x30:
  %x5 = add i32 %l, %u
  %x6 = sdiv i32 %x5, 2
  %x32 = alloca i32, align 4
  %x33 = alloca i32, align 4
  %x34 = alloca i32, align 4
  store i32 %x6, ptr %x32, align 4
  store i32 %x1, ptr %x33, align 4
  store i32 1, ptr %x34, align 4
  br label %x35

x35:
  %a = load i32, ptr %x32, align 4
  %p = load i32, ptr %x33, align 4
  %r = load i32, ptr %x34, align 4
  %x7 = icmp eq i32 %p, 0
  %x37 = alloca i32, align 4
  %x38 = alloca i32, align 4
  %x39 = alloca i32, align 4
  br i1 %x7, label %x41, label %x42

x41:
  br label %x36

  br label %x43

x42:
  %x8 = and i32 %p, 1
  %x9 = icmp sgt i32 %x8, 0
  %x44 = alloca i32, align 4
  br i1 %x9, label %x46, label %x47

x46:
  %x10 = mul i32 %r, %a
  store i32 %x10, ptr %x44, align 4
  br label %x48

x47:

  store i32 %r, ptr %x44, align 4
  br label %x48

x48:
  %x11 = load i32, ptr %x44, align 4
  %x12 = mul i32 %a, %a
  %x13 = sdiv i32 %p, 2
  store i32 %x12, ptr %x37, align 4
  store i32 %x13, ptr %x38, align 4
  store i32 %x11, ptr %x39, align 4
  br label %x43

x43:
  %x14 = load i32, ptr %x37, align 4
  %x15 = load i32, ptr %x38, align 4
  %x16 = load i32, ptr %x39, align 4
  store i32 %x14, ptr %x32, align 4
  store i32 %x15, ptr %x33, align 4
  store i32 %x16, ptr %x34, align 4
  br label %x35

x36:
  %x17 = icmp sgt i32 %r, %x2
  %x49 = alloca i32, align 4
  %x50 = alloca i32, align 4
  br i1 %x17, label %x52, label %x53

x52:

  store i32 %l, ptr %x49, align 4
  store i32 %x6, ptr %x50, align 4
  br label %x54

x53:

  store i32 %x6, ptr %x49, align 4
  store i32 %u, ptr %x50, align 4
  br label %x54

x54:
  %x18 = load i32, ptr %x49, align 4
  %x19 = load i32, ptr %x50, align 4
  store i32 %x18, ptr %x26, align 4
  store i32 %x19, ptr %x27, align 4
  br label %x31

x31:
  %x20 = load i32, ptr %x26, align 4
  %x21 = load i32, ptr %x27, align 4
  store i32 %x20, ptr %x22, align 4
  store i32 %x21, ptr %x23, align 4
  br label %x24

x25:
  call void @print(i32 noundef %l)
  ret i32 0
}

attributes #0 = { noinline nounwind optnone sspstrong uwtable "frame-pointer"="all" "min-legal-vector-width"="0" "no-trapping-math"="true" "stack-protector-buffer-size"="8" "target-cpu"="x86-64" "target-features"="+cx8,+fxsr,+mmx,+sse,+sse2,+x87" "tune-cpu"="generic" }
attributes #1 = { "frame-pointer"="all" "no-trapping-math"="true" "stack-protector-buffer-size"="8" "target-cpu"="x86-64" "target-features"="+cx8,+fxsr,+mmx,+sse,+sse2,+x87" "tune-cpu"="generic" }

!llvm.module.flags = !{!0, !1, !2, !3, !4}
!llvm.ident = !{!5}

!0 = !{i32 1, !"wchar_size", i32 4}
!1 = !{i32 8, !"PIC Level", i32 2}
!2 = !{i32 7, !"PIE Level", i32 2}
!3 = !{i32 7, !"uwtable", i32 2}
!4 = !{i32 7, !"frame-pointer", i32 2}
!5 = !{!"clang version 16.0.6"}
