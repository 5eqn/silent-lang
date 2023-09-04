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
  %x26 = alloca i32, align 4
  %x27 = alloca i32, align 4
  store i32 0, ptr %x26, align 4
  store i32 46340, ptr %x27, align 4
  br label %x28

x28:
  %x2 = load i32, ptr %x26, align 4
  %x3 = load i32, ptr %x27, align 4
  %x4 = add i32 %x2, 1
  %x5 = icmp eq i32 %x4, %x3
  %x30 = alloca i32, align 4
  %x31 = alloca i32, align 4
  br i1 %x5, label %x33, label %x34

x33:
  br label %x29

  br label %x35

x34:
  %x6 = add i32 %x2, %x3
  %x7 = sdiv i32 %x6, 2
  %x36 = alloca i32, align 4
  %x37 = alloca i32, align 4
  %x38 = alloca i32, align 4
  store i32 %x7, ptr %x36, align 4
  store i32 2, ptr %x37, align 4
  store i32 1, ptr %x38, align 4
  br label %x39

x39:
  %x8 = load i32, ptr %x36, align 4
  %x9 = load i32, ptr %x37, align 4
  %x10 = load i32, ptr %x38, align 4
  %x11 = icmp eq i32 %x9, 0
  %x41 = alloca i32, align 4
  %x42 = alloca i32, align 4
  %x43 = alloca i32, align 4
  br i1 %x11, label %x45, label %x46

x45:
  br label %x40

  br label %x47

x46:
  %x12 = and i32 %x9, 1
  %x13 = icmp sgt i32 %x12, 0
  %x48 = alloca i32, align 4
  br i1 %x13, label %x50, label %x51

x50:
  %x14 = mul i32 %x10, %x8
  store i32 %x14, ptr %x48, align 4
  br label %x52

x51:

  store i32 %x10, ptr %x48, align 4
  br label %x52

x52:
  %x15 = load i32, ptr %x48, align 4
  %x16 = mul i32 %x8, %x8
  %x17 = sdiv i32 %x9, 2
  store i32 %x16, ptr %x41, align 4
  store i32 %x17, ptr %x42, align 4
  store i32 %x15, ptr %x43, align 4
  br label %x47

x47:
  %x18 = load i32, ptr %x41, align 4
  %x19 = load i32, ptr %x42, align 4
  %x20 = load i32, ptr %x43, align 4
  store i32 %x18, ptr %x36, align 4
  store i32 %x19, ptr %x37, align 4
  store i32 %x20, ptr %x38, align 4
  br label %x39

x40:
  %x21 = icmp sle i32 %x10, %x1
  %x53 = alloca i32, align 4
  %x54 = alloca i32, align 4
  br i1 %x21, label %x56, label %x57

x56:

  store i32 %x7, ptr %x53, align 4
  store i32 %x3, ptr %x54, align 4
  br label %x58

x57:

  store i32 %x2, ptr %x53, align 4
  store i32 %x7, ptr %x54, align 4
  br label %x58

x58:
  %x22 = load i32, ptr %x53, align 4
  %x23 = load i32, ptr %x54, align 4
  store i32 %x22, ptr %x30, align 4
  store i32 %x23, ptr %x31, align 4
  br label %x35

x35:
  %x24 = load i32, ptr %x30, align 4
  %x25 = load i32, ptr %x31, align 4
  store i32 %x24, ptr %x26, align 4
  store i32 %x25, ptr %x27, align 4
  br label %x28

x29:
  call void @print(i32 noundef %x2)
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
