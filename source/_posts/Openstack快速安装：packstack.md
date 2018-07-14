---
title: Openstack快速安装：packstack
date: 2017-03-02 18:13:01
tags: [Openstack]
---

### 0x80 前言

我曾经在差不多两年前安装过Openstack，当时是按照某个论坛的大牛的分享，一步一个脚印，用了很长时间才进入Openstack DashBoard并进行自己劳动成果的体验，感受自己为自己提供的服务。但是那次血泪安装史也并不完美，虽然核心组件成功运行，但是仍旧有部分功能存在问题，迫于精力有限，我便没有再碰过Openstack。
最近又心血来潮想要玩一玩这个曾经的玩物，恰巧了解到Openstack可以近乎一键安装，于是便再次操弄起来。

这里要特别感谢《Linux就该这么学》的主题网站，两年前的论坛我是记不住了，但这个网站我个人觉得是非常适合参照学习来提升个人能力的。[第22章 使用openstack部署云计算服务环境。 | 《Linux就该这么学》](http://www.linuxprobe.com/chapter-22.html)

### 0x81 安装准备

处于学习目的，我使用的资源都是这篇教程的网站提供的。再次感谢让我有一次轻松学习部署Openstack的机会。[软件资源库 | 《Linux就该这么学》](http://www.linuxprobe.com/tools)

### 0x82 RHEL 7 安装

和安装一般的虚拟机一样，但是我们需要添加两块虚拟硬盘，其中一块提供给Openstack的Cinder使用。内存至少4GB（这里坑了我好久，第一次是horizon配置失败，后来不定时的发生错误，经过监视CPU和内存的使用才发现根本原因是内存不够，我分配4G只有3.8可用，最高占用达到过3.77G），虚拟网卡至少要有一个方便主机直接控制虚拟机的，比如Host-Only模式，当然要链接外网下载Package的话可以再分一块NAT，我直接使用的桥接（我是在Virtual Box中完成的）。

### 0x83 软件源的配置

`linuxprobe.com`提供的镜像默认repo是空的（貌似有一个EPEL和一个EPEL-Testing在线源），也就是说你不能安装任何软件包，所以我们要自己配置软件源。当然你可以直接配置在线软件源，由于所需的包那个网站都提供了，所以我配置的是离线仓库。

```Config
[root@openstack ~]# cat /etc/yum.repos.d/rhel.repo
[base]
name=base
baseurl=file:///media/cdrom
enabled=1
gpgcheck=0
[root@openstack ~]# cat /etc/yum.repos.d/epel.repo
[epel]
name=epel
baseurl=file:///media/EPEL
enabled=1
gpgcheck=0
[root@openstack ~]# cat /etc/yum.repos.d/openstack.repo
[openstack]
name=openstack
baseurl=file:///media/openstack-juno
enabled=1
gpgcheck=0
```

这样我们就可以安装常用的vim、net-tools等软件包了。

### 0x84 安装Openstack

我们先安装packstack工具:
> yum install openstack-packstack

之后像执行部署工具：
> packstack --allinone --provision-demo=n --nagios-install=n

这里不设置provision，也不安装nagios。等一切Apply完成，显示如下结果

```Shell
Welcome to Installer setup utility
Packstack changed given value  to required value /root/.ssh/id_rsa.pub

Installing:
Clean Up                                             [ DONE ]
Setting up ssh keys                                  [ DONE ]
Discovering hosts details                           [ DONE ]
Adding pre install manifest entries                  [ DONE ]
Preparing servers                                    [ DONE ]
Adding AMQP manifest entries                         [ DONE ]
Adding MySQL manifest entries                        [ DONE ]
Adding Keystone manifest entries                     [ DONE ]
Adding Glance Keystone manifest entries              [ DONE ]
Adding Glance manifest entries                       [ DONE ]
Adding Cinder Keystone manifest entries              [ DONE ]
Adding Cinder manifest entries                       [ DONE ]
Checking if the Cinder server has a cinder-volumes vg[ DONE ]
Adding Nova API manifest entries                     [ DONE ]
Adding Nova Keystone manifest entries                [ DONE ]
Adding Nova Cert manifest entries                    [ DONE ]
Adding Nova Conductor manifest entries               [ DONE ]
Creating ssh keys for Nova migration                 [ DONE ]
Gathering ssh host keys for Nova migration           [ DONE ]
Adding Nova Compute manifest entries                 [ DONE ]
Adding Nova Scheduler manifest entries               [ DONE ]
Adding Nova VNC Proxy manifest entries               [ DONE ]
Adding Openstack Network-related Nova manifest entries[ DONE ]
Adding Nova Common manifest entries                  [ DONE ]
Adding Neutron API manifest entries                  [ DONE ]
Adding Neutron Keystone manifest entries             [ DONE ]
Adding Neutron L3 manifest entries                   [ DONE ]
Adding Neutron L2 Agent manifest entries             [ DONE ]
Adding Neutron DHCP Agent manifest entries           [ DONE ]
Adding Neutron LBaaS Agent manifest entries          [ DONE ]
Adding Neutron Metering Agent manifest entries       [ DONE ]
Adding Neutron Metadata Agent manifest entries       [ DONE ]
Checking if NetworkManager is enabled and running    [ DONE ]
Adding OpenStack Client manifest entries             [ DONE ]
Adding Horizon manifest entries                      [ DONE ]
Adding Swift Keystone manifest entries               [ DONE ]
Adding Swift builder manifest entries                [ DONE ]
Adding Swift proxy manifest entries                  [ DONE ]
Adding Swift storage manifest entries                [ DONE ]
Adding Swift common manifest entries                 [ DONE ]
Adding MongoDB manifest entries                      [ DONE ]
Adding Ceilometer manifest entries                   [ DONE ]
Adding Ceilometer Keystone manifest entries          [ DONE ]
Adding post install manifest entries                 [ DONE ]
Installing Dependencies                              [ DONE ]
Copying Puppet modules and manifests                 [ DONE ]
Applying 192.168.208.108_prescript.pp
192.168.208.108_prescript.pp:                        [ DONE ]
Applying 192.168.208.108_amqp.pp
Applying 192.168.208.108_mysql.pp
192.168.208.108_amqp.pp:                             [ DONE ]
192.168.208.108_mysql.pp:                            [ DONE ]
Applying 192.168.208.108_keystone.pp
Applying 192.168.208.108_glance.pp
Applying 192.168.208.108_cinder.pp
192.168.208.108_keystone.pp:                         [ DONE ]
192.168.208.108_glance.pp:                           [ DONE ]
192.168.208.108_cinder.pp:                           [ DONE ]
Applying 192.168.208.108_api_nova.pp
192.168.208.108_api_nova.pp:                         [ DONE ]
Applying 192.168.208.108_nova.pp
192.168.208.108_nova.pp:                             [ DONE ]
Applying 192.168.208.108_neutron.pp
192.168.208.108_neutron.pp:                          [ DONE ]
Applying 192.168.208.108_neutron_fwaas.pp
Applying 192.168.208.108_osclient.pp
Applying 192.168.208.108_horizon.pp
192.168.208.108_neutron_fwaas.pp:                    [ DONE ]
192.168.208.108_horizon.pp:                          [ DONE ]
192.168.208.108_osclient.pp:                         [ DONE ]
Applying 192.168.208.108_ring_swift.pp
192.168.208.108_ring_swift.pp:                       [ DONE ]
Applying 192.168.208.108_swift.pp
192.168.208.108_swift.pp:                            [ DONE ]
Applying 192.168.208.108_mongodb.pp
192.168.208.108_mongodb.pp:                          [ DONE ]
Applying 192.168.208.108_ceilometer.pp
192.168.208.108_ceilometer.pp:                       [ DONE ]
Applying 192.168.208.108_postscript.pp
192.168.208.108_postscript.pp:                       [ DONE ]
Applying Puppet manifests                            [ DONE ]
Finalizing                                           [ DONE ]

 **** Installation completed successfully ******


Additional information:
 * A new answerfile was created in: /root/packstack-answers-20170302-045851.txt
 * Time synchronization installation was skipped. Please note that unsynchronized time on server instances might be problem for some OpenStack components.
 * File /root/keystonerc_admin has been created on OpenStack client host 192.168.208.108. To use the command line tools you need to source the file.
 * To access the OpenStack Dashboard browse to http://192.168.208.108/dashboard .
Please, find your login credentials stored in the keystonerc_admin in your home directory.
 * Because of the kernel update the host 192.168.208.108 requires reboot.
 * The installation log file is available at: /var/tmp/packstack/20170302-045851-iuq1om/openstack-setup.log
 * The generated manifests are available at: /var/tmp/packstack/20170302-045851-iuq1om/manifests
```

附加信息里告诉了我们DashBoard地址，以及账号密码的存放位置`/root/keystonerc_admin`，可以登录玩耍了～
