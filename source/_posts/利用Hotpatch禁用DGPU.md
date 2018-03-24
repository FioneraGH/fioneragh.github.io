---
title: 利用Hotpatch禁用DGPU
date: 2018-03-24 15:12:53
tags: [macOS, Clover, Hotpatch, ACPI, Hackintosh]
---

### 0x81 Clover-Hotpatch

Hackintosh，这应该是这个系列的第一篇，黑苹果的折腾笔记绝对可以写本书，只是拿不出这么多的时间来做这件事情。

Clover，一个强大的引导工具，基于rEFInd魔改而来，提供强大的驱动注入和二进制patch的功能，[@RehabMan](https://github.com/RehabMan)等大牛的参与更是为Clover带来更加强大的诸如AutoMerge等功能，让Clover可以为黑苹果工作的更好。

Hotpatch，字面意思热补丁，主要是对于Clover强大的ACPI修改功能的支持，将hackintosh复杂而繁琐的ACPI修改中解放出来，从而实现patch像DSDT设备改名等功能。Hotpatch能做到DSDT/SSDT对象命名，插入补丁，方法重定向，方法重写等功能，通过灵活的扩展SSDT文件，我们只要自己学会ACPI汇编的语法从而会阅读aml文件经过iasl反汇编的dsl源文件，便可以完成自己想做的任何事。当然这个过程看起来容易，往往需要大量的基础知识和充分的测试才能写出能够使用的Hotpatch文件。

### 0x82 High Sierra 对WindowServer的修改

macOS由于自身独特的封闭性，所以对传统laptop的DGPU并没有提供足够的支持，尤其是nVidia显卡。纵使nVidia官方提供了macOS可用的WebDriver来驱动我们的GT显卡，但是这些驱动往往是提供给桌面级独立GPU使用的。

在10.13之前，包括10.12，我们可以通过nv_diable参数禁用macOS对DGPU显卡进行驱动，当然这样也只是禁止了驱动的加载，在设备上电时仍会消耗电池资源。真正正确的做法是对SSDT/DSDT进行修改，通过_OFF方法禁用显卡，这种修改方式有一个很棘手的问题，就是EC，大量的笔记本（Lenovo几乎全系）有EmbeddedController，通过它来进行设备的电源管理。在我们在调用_OFF方法时EC可能还没准备好，因此我们需要将代码段里的方法转移到REG（具体的我没仔细研究）中来保证正常的工作。

到了10.13，系统底层做了大量的修改，尤其是显卡驱动部分，就像10.11的USB栈重写，nv_disable参数已经失效，系统在监测到设备会尝试进行驱动，由于根本无法驱动DGPU，所以WindowServer会出现“Window Server Service only ran for 0 seconds”的提示，其实如果能看到完整的log你就会发现WindowServer的确起不来，服务运行0s也就说得通了。

之前禁用独立显卡的方法仍然有效，但是在引导安装器阶段可能会遇到问题，于是RehabMan出了篇教程[[FIX] "Window Server Service only ran for 0 seconds" with dual-GPU](https://www.tonymacx86.com/threads/fix-window-server-service-only-ran-for-0-seconds-with-dual-gpu.233092/)就是解决这个问题。这篇教程通过DeviceSpecificMethod注入了一些属性，其实作用和Clover的Devices/AddProperties作用类似，有机会做个对比。

### 0x83 SSDT-DDGPU.dsl

这时候Hotpatch大放异彩，我们只需要写一个简单的dsl文件并将它编译后的aml文件置入@EFI/EFI/Clover/ACPI/patched当中就可以起到禁用DGPU的作用。

```ACPI
// For disabling the discrete GPU

DefinitionBlock("", "SSDT", 2, "hack", "_DDGPU", 0)
{
    // Note: The _OFF path should be customized to correspond to your native ACPI
    // the two paths provided here should be considered examples only
    // it is best to edit the code such that only the single _OFF path that your ACPI
    // uses is included.
    External(_SB.PCI0.PEG0.PEGP._OFF, MethodObj)
    External(_SB.PCI0.PEGP.DGFX._OFF, MethodObj)

    Device(RMD1)
    {
        Name(_HID, "RMD10000")
        Method(_INI)
        {
            // disable discrete graphics (Nvidia/Radeon) if it is present
            If (CondRefOf(\_SB.PCI0.PEG0.PEGP._OFF)) { \_SB.PCI0.PEG0.PEGP._OFF() }
            If (CondRefOf(\_SB.PCI0.PEGP.DGFX._OFF)) { \_SB.PCI0.PEGP.DGFX._OFF() }
        }
    }
}
//EOF
```

代码没多少，引用了外部的方法，定义了新的设备RMD1，这个抽象的设备就做了一件事，在_INI方法设备初始化的时候检查饮用的方法有没有定义，定义了就执行。其中的PEGP和DGFX通常指代nVidia/Radeon设备，而PCI0后的路径使设备作用域路径。

设备路径很好找，在我们之前iasl反汇编的dsl文件中过滤_OFF方法就能找到，比如`grep -l Method.*_OFF *.dsl`结果可能是SSDT-5.dsl，进去找到正确的路径位置就行。

### 0x84 Collection Of Some

```ACPI
/*
 * Intel ACPI Component Architecture
 * AML/ASL+ Disassembler version 20161210-64(RM)
 * Copyright (c) 2000 - 2016 Intel Corporation
 * 
 * Disassembling to non-symbolic legacy ASL operators
 *
 * Disassembly of iASLZEyCG3.aml, Sat Mar 24 15:48:25 2018
 *
 * Original Table Header:
 *     Signature        "SSDT"
 *     Length           0x000004E9 (1257)
 *     Revision         0x02
 *     Checksum         0xDD
 *     OEM ID           "hack"
 *     OEM Table ID     "spoof"
 *     OEM Revision     0x00000000 (0)
 *     Compiler ID      "INTL"
 *     Compiler Version 0x20161210 (538317328)
 */
DefinitionBlock ("", "SSDT", 2, "hack", "_DDGPU", 0x00000000)
{
    External (_SB_.PCI0.PEG0.PEGP._OFF, MethodObj)    // 0 Arguments (from opcode)
    External (_SB_.PCI0.PEG0.PEGP._ON_, MethodObj)    // 0 Arguments (from opcode)
    External (_SB_.PCI0.PEG2.PEGP._OFF, MethodObj)    // 0 Arguments (from opcode)
    External (_SB_.PCI0.PEG2.PEGP._ON_, MethodObj)    // 0 Arguments (from opcode)
    External (_SB_.PCI0.PEGP.DGFX._OFF, MethodObj)    // 0 Arguments (from opcode)
    External (_SB_.PCI0.PEGP.DGFX._ON_, MethodObj)    // 0 Arguments (from opcode)
    External (_SB_.PCI0.PEG_.VID_._PS0, MethodObj)    // 0 Arguments (from opcode)
    External (_SB_.PCI0.PEG_.VID_._PS3, MethodObj)    // 0 Arguments (from opcode)
    External (_SB_.PCI0.PEG_.VID_.XDSM, MethodObj)    // 4 Arguments (from opcode)
    External (_SB_.PCI0.RP01.PEGP._OFF, MethodObj)    // 0 Arguments (from opcode)
    External (_SB_.PCI0.RP01.PEGP._ON_, MethodObj)    // 0 Arguments (from opcode)
    External (_SB_.PCI0.RP01.PXSX._OFF, MethodObj)    // 0 Arguments (from opcode)
    External (_SB_.PCI0.RP01.PXSX._ON_, MethodObj)    // 0 Arguments (from opcode)
    External (_SB_.PCI0.RP05.PXSX._OFF, MethodObj)    // 0 Arguments (from opcode)
    External (_SB_.PCI0.RP05.PXSX._ON_, MethodObj)    // 0 Arguments (from opcode)

    Device (DGPU)
    {
        Name (_HID, "DGPU1000")  // _HID: Hardware ID
        Name (RMEN, One)
        Method (_INI, 0, NotSerialized)  // _INI: Initialize
        {
            _OFF ()
        }

        Method (_ON, 0, NotSerialized)  // _ON_: Power On
        {
            If (CondRefOf (\_SB.PCI0.PEG0.PEGP._ON))
            {
                \_SB.PCI0.PEG2.PEGP._ON ()
            }

            If (CondRefOf (\_SB.PCI0.PEG2.PEGP._ON))
            {
                \_SB.PCI0.PEG0.PEGP._ON ()
            }

            If (CondRefOf (\_SB.PCI0.PEGP.DGFX._ON))
            {
                \_SB.PCI0.PEGP.DGFX._ON ()
            }

            If (CondRefOf (\_SB.PCI0.PEG.VID._PS0))
            {
                \_SB.PCI0.PEG.VID._PS0 ()
            }

            If (CondRefOf (\_SB.PCI0.RP01.PEGP._ON))
            {
                \_SB.PCI0.RP01.PEGP._ON ()
            }

            If (CondRefOf (\_SB.PCI0.RP01.PXSX._ON))
            {
                \_SB.PCI0.RP01.PXSX._ON ()
            }

            If (CondRefOf (\_SB.PCI0.RP05.PXSX._ON))
            {
                \_SB.PCI0.RP05.PXSX._ON ()
            }
        }

        Method (_OFF, 0, NotSerialized)  // _OFF: Power Off
        {
            If (CondRefOf (\_SB.PCI0.PEG0.PEGP._OFF))
            {
                \_SB.PCI0.PEG2.PEGP._OFF ()
            }

            If (CondRefOf (\_SB.PCI0.PEG2.PEGP._OFF))
            {
                \_SB.PCI0.PEG0.PEGP._OFF ()
            }

            If (CondRefOf (\_SB.PCI0.PEGP.DGFX._OFF))
            {
                \_SB.PCI0.PEGP.DGFX._OFF ()
            }

            If (CondRefOf (\_SB.PCI0.PEG.VID._PS3))
            {
                \_SB.PCI0.PEG.VID.XDSM (ToUUID ("a486d8f8-0bda-471b-a72b-6042a6b5bee0"), 0x0100, 0x1A, Buffer (0x04)
                    {
                         0x01, 0x00, 0x00, 0x03
                    })
                \_SB.PCI0.PEG.VID._PS3 ()
            }

            If (CondRefOf (\_SB.PCI0.RP01.PEGP._OFF))
            {
                \_SB.PCI0.RP01.PEGP._OFF ()
            }

            If (CondRefOf (\_SB.PCI0.RP01.PXSX._OFF))
            {
                \_SB.PCI0.RP01.PXSX._OFF ()
            }

            If (CondRefOf (\_SB.PCI0.RP05.PXSX._OFF))
            {
                \_SB.PCI0.RP05.PXSX._OFF ()
            }
        }
    }
}
```

很多人可能有疑虑，为什么这里_OFF方法可以放心调用，我猜测是因为Hotpatch的SSDT是在最后才将设备挂上，而不是像之前的_INI方法调用比较早，有可能EC还没准备好，所以需要将EC的调用放到确保EC已经准备好的位置。

目前为止，配合正确的config文件和驱动你应该已经能看到Installer了。
