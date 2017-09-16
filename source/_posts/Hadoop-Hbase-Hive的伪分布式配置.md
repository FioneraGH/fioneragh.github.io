---
title: 'Hadoop,Hbase,Hive的伪分布式配置'
date: 2017-09-11 21:12:01
tags: [Hadoop,HBase,Hive]
---

### 0x81 伪分布式Hadoop
Hadoop提供三种配置方式:单机模式,伪分布式模式和完全分布式.其中单机模式是指Hadoop运行在一个Jvm进程当中,通常是我们开发时用于调试MapReduce作业.而伪分布式是指我们在同一台节点机器上运行Hadoop集群的多个Jvm进程,意在本机上启用完整的Hadoop运行环境,完全分布式与之相对,是指在正确的网络拓扑环境中,各个节点由自己的任务进程组成,从而实现HA的服务器支持.

### 0x82 伪分布式配置Hadoop
1. 创建hadoop帐户
    为了保证我们的环境足够纯净,我们将hadoop运行在hadoop用户下,可以保证权限等不会出现奇怪的问题.创建用户的方式很简单,和创建一般的用户一样:`useradd hadoop`,之后用户相关的操作就见仁见智了.

2. 下载hadoop的tar包
    hadoop是apache基金会的明星项目,我们可以在[官网](http://hadoop.apache.org/releases.html)下载到tar包,截至到目前为止最新版本是3.0.0-alpha4,这里我下载的是2.6.5.

3. 配置hadoop
    我们将tar包移动到hadoop用户的Home目录下,执行`tar zxvf hadoop-2.6.5-bin.tar.gz`将hadoop软件包解压,然后进入`hadoop-2.6.5/etc/hadoop`下对相应的配置文件进行更改(必要时拷贝template文件),主要修改的几个文件如下:
    ```XML
    core-site.xml
    <configuration>
        <property>
            <name>fs.defaultFS</name>
            <value>hdfs://single:9000</value>
        </property>
    </configuration>

    hdfs-site.xml
    <configuration>
        <property>
            <name>dfs.replication</name>
            <value>1</value>
        </property>
        <property>
            <name>dfs.namenode.secondary.http-address</name>
	    <value>single:50090</value>
        </property>
        <property>
            <name>dfs.namenode.name.dir</name>
            <value>/home/hadoop/hadoop-2.x/dfs/name</value>
        </property>
        <property>
            <name>dfs.datanode.data.dir</name>
            <value>/home/hadoop/hadoop-2.x/dfs/data</value>
        </property>
    </configuration>

    mapred-site.xml
    <configuration>
        <property>
            <name>mapreduce.framework.name</name>
            <value>yarn</value>
        </property>
    </configuration>

    yarn-site.xml
    <configuration>
        <property>
            <name>yarn.resourcemanager.hostname</name>
            <value>single</value>
        </property>
        <property>
            <name>yarn.nodemanager.aux-services</name>
            <value>mapreduce_shuffle</value>
        </property>
    </configuration>
    ```
    ```Bash
    hadoop.env
    mapred.env
    yarn.env
    export JAVA_HOME=/home/hadoop/jdk
    ```

4. 启动hadoop
    `hadoop-2.6.5/sbin`中提供了启动脚本,其中startall.sh可以直接启动hadoop,但是该脚本已被视为废弃,官方推荐使用start-dfs.sh和start-yarn.sh来启动NameNode,DataNode和Node/ResourceManager.

### 0x83 在HDFS上配置HBase
1. 下载hbase的tar包
    与hadoop类似,我们可以在[Apache HBase官网](http://www.apache.org/dyn/closer.cgi/hbase/)下载到tar包,各个镜像站都提供二进制包下载,截至到目前为止最新版本是2.0.0-alpha2,我们需要注意版本兼容性,这里我下载的是1.3.1

2. 配置hbase
    我们将tar包移动到hadoop用户的Home目录下,执行`tar zxvf hbase-1.3.1-bin.tar.gz`将hadoop软件包解压,然后进入`hbase-1.3.1/config`下对相应的配置文件进行更改,我们主要配置JAVA环境变量和hbase所使用的rootdir:
    ```XML
    hbase-site.xml
    <configuration>
        <property>
            <name>hbase.rootdir</name>
            <value>hdfs://single:9000/hbase</value>
        </property>
        <property>
            <name>hbase.cluster.distributed</name>
            <value>true</value>
        </property>
    </configuration>
    ```
    如果要禁用hbase自带的zookeeper,还需要进行配置并启用自己的zookeeper服务.

3. 启动hbase
    hbase的启动方式也很简单,但有一个重要前提是hadoop的hdfs必须处于启动状态,调用`hbase-1.3.1/bin/start-hbase.sh`脚本即可启动HMaster,HRegionServer和HQuorumPeer.

### 0x84 在HDFS上配置Hive
