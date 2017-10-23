---
title: 有关Java数字格式化的示例
date: 2017-08-22 17:32:03
tags: [Java]
---

### 0x81 浮点数带来的问题

平时我们在使用数字的时候，直接使用往往不太会出现什么问题，但是如果我们需要对浮点数进行格式化输出或者是进行数学运算，就有可能遇到各种各样的问题。导致这些问题的原因有很多，根本原因就是浮点数的二进制保存机制问题，所以出现`0.1 != 0.1`的状况很正常，而有一些特殊的情况下数字会被使用科学计数法表示，这也不是面向普通用户应该出现的状况。这篇文章不是剖析Java的浮点数机制，而是一个浮点数使用示例总结，所以可能文章内容会不断的发生变化。

### 0x82 几种格式化方法

对于浮点数的显示，我们有很多格式化方法，最基本的StringFormat和数学运算、BigDecimal、DecimalFormat等等，但在使用它们时我还是会遇到各种各样的问题，下面就是一些示例和结果。

### 0x83 格式化示例

下面是一个单元测试类，结果为JDK8下结果：

```Java
public class TestDecimalComputeUtil {

    @BeforeClass
    public static void setUp() {

    }

    @Test
    public void test() {
        System.out.println("StringFormat Use");
        System.out.println(String.format(Locale.CHINA, "%f", 0.0001));
        System.out.println(String.format(Locale.CHINA, "%.4f", 0.0001));
        System.out.println(String.format(Locale.CHINA, "%s", 0.0001));
        System.out.println(String.format(Locale.CHINA, "%f", 1.0001));
        System.out.println(String.format(Locale.CHINA, "%.4f", 1.0001));
        System.out.println(String.format(Locale.CHINA, "%.4f", 1.00014));
        System.out.println(String.format(Locale.CHINA, "%.4f", 1.00015));
        System.out.println(String.format(Locale.CHINA, "%s", 1.0001));
        System.out.println(String.format(Locale.CHINA, "%f", 10.0000 / 10));
        System.out.println(String.format(Locale.CHINA, "%.4f", 10.0000 / 10));
        System.out.println(String.format(Locale.CHINA, "%s", 10.0000 / 10));
        System.out.println(String.format(Locale.CHINA, "%f", 10.0000 - 10));
        System.out.println(String.format(Locale.CHINA, "%.4f", 10.0000 - 10));
        System.out.println(String.format(Locale.CHINA, "%s", 10.0000 - 10));
        System.out.println(String.format(Locale.CHINA, "%f", 10.0001 - 10));
        System.out.println(String.format(Locale.CHINA, "%.4f", 10.0001 - 10));
        System.out.println(String.format(Locale.CHINA, "%s", 10.0001 - 10));
        System.out.println("StringParse Use");
        double test1 = Double.parseDouble("10.0001");
        System.out.println(String.format(Locale.CHINA, "%f", test1 - 10));
        System.out.println(String.format(Locale.CHINA, "%.4f", test1 - 10));
        System.out.println(String.format(Locale.CHINA, "%s", test1 - 10));
        System.out.println("ValueOf Use");
        double test2 = Double.parseDouble("11.0000");
        System.out.println(String.valueOf(1));
        System.out.println(String.valueOf(1.0));
        System.out.println(String.valueOf(1.0001));
        System.out.println(String.valueOf(1.1));
        System.out.println(String.valueOf(test2 - 10));
        System.out.println(String.valueOf(test2 - 10.0));
        System.out.println(String.valueOf(test2 - 10.1));
        System.out.println(String.valueOf(11.0001 - 10));
        System.out.println(String.valueOf(11.0001 - 10.0));
        System.out.println("DecimalFormat Use");
        System.out.println(DecimalFormatUtil.formatNormal(1));
        System.out.println(DecimalFormatUtil.formatNormal(1.0));
        System.out.println(DecimalFormatUtil.formatNormal(1.0001));
        System.out.println(DecimalFormatUtil.formatNormal(11.0001 - 10));
        System.out.println(DecimalFormatUtil.formatNormal(10.0 / 10));
        System.out.println(DecimalFormatUtil.formatNormal("1"));
        System.out.println(DecimalFormatUtil.formatNormal("1.0"));
        System.out.println(DecimalFormatUtil.formatNormal("1.0001"));
        System.out.println(DecimalFormatUtil.formatNormal("10000.0001"));
        System.out.println("ValueOf DecimalFormat Use");
        System.out.println(DecimalFormatUtil.formatNormal(Double.parseDouble(String.valueOf(10.0001)) - 10));
        System.out.println(DecimalFormatUtil.formatNormal(Double.parseDouble(String.valueOf(10.1)) - 10));
        System.out.println(DecimalFormatUtil.formatNormal(Double.valueOf(String.valueOf(10.0001)) - 10));
        System.out.println(DecimalFormatUtil.formatNormal(Double.valueOf(String.valueOf(10.1)) - 10));
        System.out.println("BigDecimal Use");
        System.out.println(DecimalComputeUtil.add("10.0001", "10"));
        System.out.println(DecimalComputeUtil.add("10.0001", 10));
        System.out.println(DecimalComputeUtil.add("10.0001", new BigDecimal(10)));
        System.out.println(DecimalComputeUtil.sub("10.0001", "10"));
        System.out.println(DecimalComputeUtil.sub("10.0001", 10));
        System.out.println(DecimalComputeUtil.sub("10.0001", new BigDecimal(10)));
        System.out.println("BigDecimal PlainString Use");
        System.out.println(DecimalComputeUtil.subPlain("10.0001", "10"));
        System.out.println(DecimalComputeUtil.mul("0.1", 10));
        System.out.println(DecimalComputeUtil.mul("0.1", 100));
        System.out.println(DecimalComputeUtil.mul("0.1", 1000));
        System.out.println(DecimalComputeUtil.mul("0.1", 10000));
        System.out.println(DecimalComputeUtil.mul("0.1", 100000));
        System.out.println(DecimalComputeUtil.mul("0.1", 1000000));
        System.out.println(DecimalComputeUtil.mul("0.1", 10000000));
        System.out.println(DecimalComputeUtil.mul("0.1", 100000000));
        System.out.println(DecimalComputeUtil.mul("0.1", 1000000000));
        System.out.println(DecimalComputeUtil.mulPlain("0.1", 1000000000));
        System.out.println(DecimalComputeUtil.mul("10.0001", "10"));
        System.out.println(DecimalComputeUtil.mul("10.0001", "100"));
        System.out.println(DecimalComputeUtil.mul("10.0001", "1000"));
        System.out.println(DecimalComputeUtil.mul("10.0001", "10000"));
        System.out.println(DecimalComputeUtil.mul("10.0001", "100000"));
        System.out.println(DecimalComputeUtil.mul("10.0001", "1000000"));
        System.out.println(DecimalComputeUtil.mulPlain("10.0001", "1000000"));
        System.out.println("BigDecimal DecimalFormat Use");
        System.out.println(DecimalFormatUtil.formatNormal(DecimalComputeUtil.mul("10.0001", "1000000")));
        System.out.println(DecimalFormatUtil.formatNormal(DecimalComputeUtil.mulPlain("10.0001", "1000000")));
        System.out.println("Mix Use");
        System.out.println(String.valueOf(Double.parseDouble(DecimalComputeUtil.subPlain("10.0001", "10"))));
    }
}
```

结果与单元测试一一对应（之后会有更具体补充）：

```Shell
StringFormat Use
0.000100
0.0001
1.0E-4
1.000100
1.0001
1.0001
1.0002
1.0001
1.000000
1.0000
1.0
0.000000
0.0000
0.0
0.000100
0.0001
9.999999999976694E-5
StringParse Use
0.000100
0.0001
9.999999999976694E-5
ValueOf Use
1
1.0
1.0001
1.1
1.0
1.0
0.9000000000000004
1.0000999999999998
1.0000999999999998
DecimalFormat Use
1
1
1.0001
1
1
1
1
1.0001
10,000.0001
ValueOf DecimalFormat Use
0
0.0999
0
0.0999
BigDecimal Use
20.0001
20.0001
20.0001
1.0E-4
1.0E-4
1.0E-4
BigDecimal PlainString Use
0.0001
1.0
10.0
100.0
1000.0
10000.0
100000.0
1000000.0
1.0E7
1.0E8
100000000.0
100.001
1000.01
10000.1
100001.0
1000010.0
1.00001E7
10000100.0000
BigDecimal DecimalFormat Use
10,000,100
10,000,100
Mix Use
1.0E-4

Process finished with exit code 0
```
