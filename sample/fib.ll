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
  %x11 = alloca i32, align 4
  %x12 = alloca i32, align 4
  %x13 = alloca i32, align 4
  store i32 1, ptr %x11, align 4
  store i32 1, ptr %x12, align 4
  store i32 2, ptr %x13, align 4
  br label %x14

x14:
  %x2 = load i32, ptr %x11, align 4
  %x3 = load i32, ptr %x12, align 4
  %x4 = load i32, ptr %x13, align 4
  %x5 = icmp eq i32 %x4, %x1
  %x16 = alloca i32, align 4
  %x17 = alloca i32, align 4
  %x18 = alloca i32, align 4
  br i1 %x5, label %x20, label %x21

x20:
  br label %x15

  br label %x22

x21:
  %x6 = add i32 %x2, %x3
  %x7 = add i32 %x4, 1
  store i32 %x3, ptr %x16, align 4
  store i32 %x6, ptr %x17, align 4
  store i32 %x7, ptr %x18, align 4
  br label %x22

x22:
  %x8 = load i32, ptr %x16, align 4
  %x9 = load i32, ptr %x17, align 4
  %x10 = load i32, ptr %x18, align 4
  store i32 %x8, ptr %x11, align 4
  store i32 %x9, ptr %x12, align 4
  store i32 %x10, ptr %x13, align 4
  br label %x14

x15:
  call void @print(i32 noundef %x3)
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
