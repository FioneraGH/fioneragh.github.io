---
title: Java Algorithm
date: 2017-05-13 14:21:04
tags: [速记,Java]
---

```Java
import java.util.Scanner;
import java.util.Stack;

/**
 * 括号匹配方案
 * 合法的括号匹配序列被定义为:
 * 1. 空串""是合法的括号序列
 * 2. 如果"X"和"Y"是合法的序列,那么"XY"也是一个合法的括号序列
 * 3. 如果"X"是一个合法的序列,那么"(X)"也是一个合法的括号序列
 * 4. 每个合法的括号序列都可以由上面的规则生成
 * 例如"", "()", "()()()", "(()())", "(((())))"都是合法的。 东东现在有一个合法的括号序列s,
 * 一次移除操作分为两步:
 * 1. 移除序列s中第一个左括号
 * 2. 移除序列s中任意一个右括号.保证操作之后s还是一个合法的括号序列
 * 东东现在想知道使用上述的移除操作有多少种方案可以把序列s变为空
 * 如果两个方案中有一次移除操作移除的是不同的右括号就认为是不同的方案。
 * 例如: s = "()()()()()",输出1, 因为每次都只能选择被移除的左括号所相邻的右括号.
 * s = "(((())))",输出24, 第一次有4种情况, 第二次有3种情况, ... ,依次类推, 4 * 3 * 2 * 1 = 24
 * 输入描述:
 * 输入包括一行,一个合法的括号序列s,序列长度length(2 ≤ length ≤ 20).
 * 输出描述:
 * 输出一个整数,表示方案数
 * 输入例子1:
 * (((())))
 * 输出例子1:
 * 24
 *
 * 思路：遍历字符串，每次把左括号都压入栈，每次遇到右括号，先统计栈中有几个左括号，统计数与上次统计数相乘
 * 接着弹出栈中的一个左括号
 * 直到遍历结束，结果即为方案数
 */
public class BracketMatch {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String s = sc.next();

        Stack<Character> stack = new Stack<>();
        int result = 1;
        char c;

        for (int i = 0; i < s.length(); i++) {
            c = s.charAt(i);
            if (c == '(') {
                stack.push(c);

            }
            if (c == ')') {
                int size = stack.size();
                result *= size;
                stack.pop();
            }
        }

        System.out.println(result);
    }
}
```

```Java
import java.util.Arrays;
import java.util.Scanner;

/**
 * 神奇数
 * 东东在一本古籍上看到有一种神奇数,如果能够将一个数的数字分成两组,其中一组数字的和等于另一组数字的和,
 * 我们就将这个数称为神奇数。例如242就是一个神奇数,我们能够将这个数的数字分成两组,分别是{2,2}以及{4},
 * 而且这两组数的和都是4.东东现在需要统计给定区间中有多少个神奇数,即给定区间[l, r],统计这个区间中有多
 * 少个神奇数,请你来帮助他。
 * 输入描述:
 * 输入包括一行,一行中两个整数l和r(1 ≤ l, r ≤ 10^9, 0 ≤ r - l ≤ 10^6),以空格分割
 * <p>
 * <p>
 * 输出描述:
 * 输出一个整数,即区间内的神奇数个数
 * <p>
 * 输入例子1:
 * 1 50
 * <p>
 * 输出例子1:
 * 4
 */
public class MagicNumber {
    /**
     * 首先判断数组能否被平分，即数组分割问题，
     * dp[i][j]
     * 表示数组前 i
     * 个数字能否求和得到 j
     * 则
     * dp[i][j]=dp[i−1][j]||dp[i−1][j−array[i]]
     * 其中||是逻辑或运算。
     * 优化：
     * 1、若sum（array）为奇数，直接返回false
     * 2、使用逆序循环将dp数组简化为一维数组
     */

    public static boolean isMagic(int[] nums, int sum) {
        int len = nums.length;

        if (sum % 2 != 0)
            return false;

        int mid = sum / 2;

        int[] dp = new int[mid + 1];
        dp[0] = 1;
        for (int i = 0; i < len; i++) {
            for (int j = mid; j > 0; j--) {
                if (j >= nums[i] && nums[i] != -1)
                    dp[j] = Math.max(dp[j], dp[j - nums[i]]);
            }
        }
        if (dp[mid] > 0)
            return true;
        else
            return false;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int l = sc.nextInt();
        int r = sc.nextInt();

        int result = 0;

        for (int i = l; i <= r; i++) {

            int num = i;
            int[] nums = new int[10];
            int sum = 0;
            Arrays.fill(nums, -1);
            int index = 0;
            while (num > 0) {
                int temp = num % 10;
                nums[index++] = temp;
                sum += temp;
                num = num / 10;
            }

            if (isMagic(nums, sum)) {
                result++;
            }
        }

        System.out.println(result);
    }
}
```
