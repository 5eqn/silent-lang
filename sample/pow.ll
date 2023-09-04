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
  %x16 = alloca i32, align 4
  %x17 = alloca i32, align 4
  %x18 = alloca i32, align 4
  store i32 %x1, ptr %x16, align 4
  store i32 %x2, ptr %x17, align 4
  store i32 1, ptr %x18, align 4
  br label %x19

x19:
  %x3 = load i32, ptr %x16, align 4
  %x4 = load i32, ptr %x17, align 4
  %x5 = load i32, ptr %x18, align 4
  %x6 = icmp eq i32 %x4, 0
  %x21 = alloca i32, align 4
  %x22 = alloca i32, align 4
  %x23 = alloca i32, align 4
  br i1 %x6, label %x25, label %x26

x25:
  br label %x20

  br label %x27

x26:
  %x7 = and i32 %x4, 1
  %x8 = icmp sgt i32 %x7, 0
  %x28 = alloca i32, align 4
  br i1 %x8, label %x30, label %x31

x30:
  %x9 = mul i32 %x5, %x3
  store i32 %x9, ptr %x28, align 4
  br label %x32

x31:

  store i32 %x5, ptr %x28, align 4
  br label %x32

x32:
  %x10 = load i32, ptr %x28, align 4
  %x11 = mul i32 %x3, %x3
  %x12 = sdiv i32 %x4, 2
  store i32 %x11, ptr %x21, align 4
  store i32 %x12, ptr %x22, align 4
  store i32 %x10, ptr %x23, align 4
  br label %x27

x27:
  %x13 = load i32, ptr %x21, align 4
  %x14 = load i32, ptr %x22, align 4
  %x15 = load i32, ptr %x23, align 4
  store i32 %x13, ptr %x16, align 4
  store i32 %x14, ptr %x17, align 4
  store i32 %x15, ptr %x18, align 4
  br label %x19

x20:
  call void @print(i32 noundef %x5)
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
