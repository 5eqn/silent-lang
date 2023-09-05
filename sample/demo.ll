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
  %x6 = call i32 @input()
  %x7 = add i32 1, %x6
  %x8 = add i32 5, %x7
  call void @print(i32 noundef %x8)
  %x9 = call i32 @input()
  %x63 = alloca i32, align 4
  %x64 = alloca i32, align 4
  store i32 0, ptr %x63, align 4
  store i32 %x9, ptr %x64, align 4
  br label %x65

x65:
  %x10 = load i32, ptr %x63, align 4
  %x11 = load i32, ptr %x64, align 4
  %x12 = icmp eq i32 %x11, 0
  %x67 = alloca i32, align 4
  %x68 = alloca i32, align 4
  br i1 %x12, label %x70, label %x71

x70:
  br label %x66

  br label %x72

x71:
  %x13 = add i32 %x10, %x11
  %x14 = sub i32 %x11, 1
  store i32 %x13, ptr %x67, align 4
  store i32 %x14, ptr %x68, align 4
  br label %x72

x72:
  %x15 = load i32, ptr %x67, align 4
  %x16 = load i32, ptr %x68, align 4
  store i32 %x15, ptr %x63, align 4
  store i32 %x16, ptr %x64, align 4
  br label %x65

x66:
  call void @print(i32 noundef %x10)
  %x17 = call i32 @input()
  %x73 = alloca i32, align 4
  %x74 = alloca i32, align 4
  store i32 1, ptr %x73, align 4
  store i32 %x17, ptr %x74, align 4
  br label %x75

x75:
  %x18 = load i32, ptr %x73, align 4
  %x19 = load i32, ptr %x74, align 4
  %x20 = icmp eq i32 %x19, 1
  %x77 = alloca i32, align 4
  %x78 = alloca i32, align 4
  br i1 %x20, label %x80, label %x81

x80:
  br label %x76

  br label %x82

x81:
  %x21 = mul i32 %x18, %x19
  %x22 = sub i32 %x19, 1
  store i32 %x21, ptr %x77, align 4
  store i32 %x22, ptr %x78, align 4
  br label %x82

x82:
  %x23 = load i32, ptr %x77, align 4
  %x24 = load i32, ptr %x78, align 4
  store i32 %x23, ptr %x73, align 4
  store i32 %x24, ptr %x74, align 4
  br label %x75

x76:
  call void @print(i32 noundef %x18)
  %x25 = call i32 @input()
  %x83 = alloca i32, align 4
  %x84 = alloca i32, align 4
  %x85 = alloca i32, align 4
  store i32 1, ptr %x83, align 4
  store i32 1, ptr %x84, align 4
  store i32 %x25, ptr %x85, align 4
  br label %x86

x86:
  %x26 = load i32, ptr %x83, align 4
  %x27 = load i32, ptr %x84, align 4
  %x28 = load i32, ptr %x85, align 4
  %x29 = icmp sle i32 %x28, 2
  %x88 = alloca i32, align 4
  %x89 = alloca i32, align 4
  %x90 = alloca i32, align 4
  br i1 %x29, label %x92, label %x93

x92:
  br label %x87

  br label %x94

x93:
  %x30 = add i32 %x26, %x27
  %x31 = sub i32 %x28, 1
  store i32 %x27, ptr %x88, align 4
  store i32 %x30, ptr %x89, align 4
  store i32 %x31, ptr %x90, align 4
  br label %x94

x94:
  %x32 = load i32, ptr %x88, align 4
  %x33 = load i32, ptr %x89, align 4
  %x34 = load i32, ptr %x90, align 4
  store i32 %x32, ptr %x83, align 4
  store i32 %x33, ptr %x84, align 4
  store i32 %x34, ptr %x85, align 4
  br label %x86

x87:
  call void @print(i32 noundef %x27)
  %x35 = call i32 @input()
  %x36 = call i32 @input()
  %x95 = alloca i32, align 4
  %x96 = alloca i32, align 4
  %x97 = alloca i32, align 4
  store i32 %x35, ptr %x95, align 4
  store i32 %x36, ptr %x96, align 4
  store i32 1, ptr %x97, align 4
  br label %x98

x98:
  %x37 = load i32, ptr %x95, align 4
  %x38 = load i32, ptr %x96, align 4
  %x39 = load i32, ptr %x97, align 4
  %x40 = icmp eq i32 %x38, 0
  %x100 = alloca i32, align 4
  %x101 = alloca i32, align 4
  %x102 = alloca i32, align 4
  br i1 %x40, label %x104, label %x105

x104:
  br label %x99

  br label %x106

x105:
  %x41 = and i32 %x38, 1
  %x42 = icmp sgt i32 %x41, 0
  %x107 = alloca i32, align 4
  br i1 %x42, label %x109, label %x110

x109:
  %x43 = mul i32 %x39, %x37
  store i32 %x43, ptr %x107, align 4
  br label %x111

x110:

  store i32 %x39, ptr %x107, align 4
  br label %x111

x111:
  %x44 = load i32, ptr %x107, align 4
  %x45 = mul i32 %x37, %x37
  %x46 = sdiv i32 %x38, 2
  store i32 %x45, ptr %x100, align 4
  store i32 %x46, ptr %x101, align 4
  store i32 %x44, ptr %x102, align 4
  br label %x106

x106:
  %x47 = load i32, ptr %x100, align 4
  %x48 = load i32, ptr %x101, align 4
  %x49 = load i32, ptr %x102, align 4
  store i32 %x47, ptr %x95, align 4
  store i32 %x48, ptr %x96, align 4
  store i32 %x49, ptr %x97, align 4
  br label %x98

x99:
  call void @print(i32 noundef %x39)
  %x50 = call i32 @input()
  %x112 = alloca i32, align 4
  %x113 = alloca i32, align 4
  store i32 0, ptr %x112, align 4
  store i32 46340, ptr %x113, align 4
  br label %x114

x114:
  %x51 = load i32, ptr %x112, align 4
  %x52 = load i32, ptr %x113, align 4
  %x53 = add i32 %x51, 1
  %x54 = icmp eq i32 %x53, %x52
  %x116 = alloca i32, align 4
  %x117 = alloca i32, align 4
  br i1 %x54, label %x119, label %x120

x119:
  br label %x115

  br label %x121

x120:
  %x55 = add i32 %x51, %x52
  %x56 = sdiv i32 %x55, 2
  %x57 = mul i32 %x56, %x56
  %x58 = icmp sle i32 %x57, %x50
  %x122 = alloca i32, align 4
  %x123 = alloca i32, align 4
  br i1 %x58, label %x125, label %x126

x125:

  store i32 %x56, ptr %x122, align 4
  store i32 %x52, ptr %x123, align 4
  br label %x127

x126:

  store i32 %x51, ptr %x122, align 4
  store i32 %x56, ptr %x123, align 4
  br label %x127

x127:
  %x59 = load i32, ptr %x122, align 4
  %x60 = load i32, ptr %x123, align 4
  store i32 %x59, ptr %x116, align 4
  store i32 %x60, ptr %x117, align 4
  br label %x121

x121:
  %x61 = load i32, ptr %x116, align 4
  %x62 = load i32, ptr %x117, align 4
  store i32 %x61, ptr %x112, align 4
  store i32 %x62, ptr %x113, align 4
  br label %x114

x115:
  call void @print(i32 noundef %x51)
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
