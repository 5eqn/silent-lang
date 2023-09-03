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
  %x13 = alloca i32, align 4
  %x14 = alloca i32, align 4
  %x15 = alloca i32, align 4
  store i32 %x1, ptr %x13, align 4
  store i32 %x2, ptr %x14, align 4
  store i32 1, ptr %x15, align 4
  br label %x16

x16:
  %a = load i32, ptr %x13, align 4
  %p = load i32, ptr %x14, align 4
  %r = load i32, ptr %x15, align 4
  %x3 = icmp eq i32 %p, 0
  %x18 = alloca i32, align 4
  %x19 = alloca i32, align 4
  %x20 = alloca i32, align 4
  br i1 %x3, label %x22, label %x23

x22:
  br label %x17

  br label %x24

x23:
  %x4 = and i32 %p, 1
  %x5 = icmp sgt i32 %x4, 0
  %x25 = alloca i32, align 4
  br i1 %x5, label %x27, label %x28

x27:
  %x6 = mul i32 %r, %a
  store i32 %x6, ptr %x25, align 4
  br label %x29

x28:

  store i32 %r, ptr %x25, align 4
  br label %x29

x29:
  %x7 = load i32, ptr %x25, align 4
  %x8 = mul i32 %a, %a
  %x9 = sdiv i32 %p, 2
  store i32 %x8, ptr %x18, align 4
  store i32 %x9, ptr %x19, align 4
  store i32 %x7, ptr %x20, align 4
  br label %x24

x24:
  %x10 = load i32, ptr %x18, align 4
  %x11 = load i32, ptr %x19, align 4
  %x12 = load i32, ptr %x20, align 4
  store i32 %x10, ptr %x13, align 4
  store i32 %x11, ptr %x14, align 4
  store i32 %x12, ptr %x15, align 4
  br label %x16

x17:
  call void @print(i32 noundef %r)
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
