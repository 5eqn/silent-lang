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
  %x2 = mul i32 %x1, %x1
  %x3 = srem i32 %x2, 6
  %x4 = mul i32 %x3, 5
  %x5 = add i32 %x4, 1
  call void @print(i32 noundef %x5)
  call void @print(i32 noundef 114)
  %x6 = add i32 1, %x1
  call void @print(i32 noundef %x6)
  %x7 = icmp sgt i32 %x1, 5
  %x49 = alloca i32, align 4
  %x50 = alloca i32, align 4
  br i1 %x7, label %x52, label %x53

x52:

  store i32 4, ptr %x49, align 4
  store i32 4, ptr %x50, align 4
  br label %x54

x53:

  store i32 8, ptr %x49, align 4
  store i32 8, ptr %x50, align 4
  br label %x54

x54:
  %x8 = load i32, ptr %x49, align 4
  %x9 = load i32, ptr %x50, align 4
  %x10 = mul i32 %x8, %x9
  call void @print(i32 noundef %x10)
  %x55 = alloca i32, align 4
  %x56 = alloca i32, align 4
  store i32 0, ptr %x55, align 4
  store i32 %x1, ptr %x56, align 4
  br label %x57

x57:
  %x11 = load i32, ptr %x55, align 4
  %x12 = load i32, ptr %x56, align 4
  %x13 = icmp eq i32 %x12, 0
  %x59 = alloca i32, align 4
  %x60 = alloca i32, align 4
  br i1 %x13, label %x62, label %x63

x62:
  br label %x58

  br label %x64

x63:
  %x14 = add i32 %x11, %x12
  %x15 = sub i32 %x12, 1
  store i32 %x14, ptr %x59, align 4
  store i32 %x15, ptr %x60, align 4
  br label %x64

x64:
  %x16 = load i32, ptr %x59, align 4
  %x17 = load i32, ptr %x60, align 4
  store i32 %x16, ptr %x55, align 4
  store i32 %x17, ptr %x56, align 4
  br label %x57

x58:
  call void @print(i32 noundef %x11)
  %x65 = alloca i32, align 4
  %x66 = alloca i32, align 4
  store i32 1, ptr %x65, align 4
  store i32 %x1, ptr %x66, align 4
  br label %x67

x67:
  %x18 = load i32, ptr %x65, align 4
  %x19 = load i32, ptr %x66, align 4
  %x20 = icmp eq i32 %x19, 1
  %x69 = alloca i32, align 4
  %x70 = alloca i32, align 4
  br i1 %x20, label %x72, label %x73

x72:
  br label %x68

  br label %x74

x73:
  %x21 = mul i32 %x18, %x19
  %x22 = sub i32 %x19, 1
  store i32 %x21, ptr %x69, align 4
  store i32 %x22, ptr %x70, align 4
  br label %x74

x74:
  %x23 = load i32, ptr %x69, align 4
  %x24 = load i32, ptr %x70, align 4
  store i32 %x23, ptr %x65, align 4
  store i32 %x24, ptr %x66, align 4
  br label %x67

x68:
  call void @print(i32 noundef %x18)
  %x75 = alloca i32, align 4
  %x76 = alloca i32, align 4
  %x77 = alloca i32, align 4
  store i32 1, ptr %x75, align 4
  store i32 1, ptr %x76, align 4
  store i32 %x1, ptr %x77, align 4
  br label %x78

x78:
  %x25 = load i32, ptr %x75, align 4
  %x26 = load i32, ptr %x76, align 4
  %x27 = load i32, ptr %x77, align 4
  %x28 = icmp slt i32 %x27, 2
  %x80 = alloca i32, align 4
  %x81 = alloca i32, align 4
  %x82 = alloca i32, align 4
  br i1 %x28, label %x84, label %x85

x84:
  br label %x79

  br label %x86

x85:
  %x29 = add i32 %x25, %x26
  %x30 = sub i32 %x27, 1
  store i32 %x26, ptr %x80, align 4
  store i32 %x29, ptr %x81, align 4
  store i32 %x30, ptr %x82, align 4
  br label %x86

x86:
  %x31 = load i32, ptr %x80, align 4
  %x32 = load i32, ptr %x81, align 4
  %x33 = load i32, ptr %x82, align 4
  store i32 %x31, ptr %x75, align 4
  store i32 %x32, ptr %x76, align 4
  store i32 %x33, ptr %x77, align 4
  br label %x78

x79:
  call void @print(i32 noundef %x26)
  %x34 = call i32 @input()
  %x35 = call i32 @input()
  %x87 = alloca i32, align 4
  %x88 = alloca i32, align 4
  %x89 = alloca i32, align 4
  store i32 %x34, ptr %x87, align 4
  store i32 %x35, ptr %x88, align 4
  store i32 1, ptr %x89, align 4
  br label %x90

x90:
  %x36 = load i32, ptr %x87, align 4
  %x37 = load i32, ptr %x88, align 4
  %x38 = load i32, ptr %x89, align 4
  %x39 = icmp eq i32 %x37, 0
  %x92 = alloca i32, align 4
  %x93 = alloca i32, align 4
  %x94 = alloca i32, align 4
  br i1 %x39, label %x96, label %x97

x96:
  br label %x91

  br label %x98

x97:
  %x40 = and i32 %x37, 1
  %x41 = icmp sgt i32 %x40, 0
  %x99 = alloca i32, align 4
  br i1 %x41, label %x101, label %x102

x101:
  %x42 = mul i32 %x38, %x36
  store i32 %x42, ptr %x99, align 4
  br label %x103

x102:

  store i32 %x38, ptr %x99, align 4
  br label %x103

x103:
  %x43 = load i32, ptr %x99, align 4
  %x44 = mul i32 %x36, %x36
  %x45 = sdiv i32 %x37, 2
  store i32 %x44, ptr %x92, align 4
  store i32 %x45, ptr %x93, align 4
  store i32 %x43, ptr %x94, align 4
  br label %x98

x98:
  %x46 = load i32, ptr %x92, align 4
  %x47 = load i32, ptr %x93, align 4
  %x48 = load i32, ptr %x94, align 4
  store i32 %x46, ptr %x87, align 4
  store i32 %x47, ptr %x88, align 4
  store i32 %x48, ptr %x89, align 4
  br label %x90

x91:
  call void @print(i32 noundef %x38)
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
