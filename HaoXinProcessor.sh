#!/bin/sh
#守护进程脚本，当网关异常关闭时主动重启网关
#建议直接通过该脚本启动网关！
#author:杨承
#网关编号
NUM=1
#基础目录，指定配置文件以及网关日志都在当前路径下
BASE_DIR=/opt
#是否开启集群 YES;NO
ISCLUSTER=NO
#前置端口--开源版本无法指定前置端口，此处无意义
PORT=8888
#前置IP
MASTERIP=127.0.0.1
#zookeeper地址
ZK_ADDR=192.168.18.27:2181,192.168.18.27:2182,192.168.18.27:2183
#网关日志名称
LOGNAME=iotgate
#JVM优化参数
JVM="-Xms2g -Xmx2g -XX:NewRatio=2 -XX:+UseG1GC -Dio.netty.leakDetectionLevel=advanced -Xloggc:$BASE_DIR/gc.log -XX:MaxDirectMemorySize=1G -Dio.netty.allocator.pageSize=8192 -Dio.netty.allocator.maxOrder=10 -Dio.netty.recycler.maxCapacity=0 -Dio.netty.recycler.maxCapacity.default=0"
PAT=`dirname $0`
function start {
        if [ "$ISCLUSTER" == “YES” ]
                then
                sleep 5
                command=`nohup java $JVM -jar $BASE_DIR/IOTGate.jar -c -n $NUM -f $BASE_DIR/iotGate.conf -z $ZK_ADDR > $BASE_DIR/$LOGNAME.log &`
                `info $command`
        else
               command=`nohup java $JVM -jar $BASE_DIR/IOTGate.jar -n $NUM -f $BASE_DIR/iotGate.conf -m $MASTERIP  > $BASE_DIR/$LOGNAME.log &`
                `info $command`
        fi
        tail -n 30  $BASE_DIR/$LOGNAME.log
}


for((i=0 ; ; i++))
do
  sleep 1
  server=`ps aux | grep IOTGate.jar | grep -v grep`
        if [ ! "$server" ]; then
            #如果不存在就启动
            start
        fi
        sleep 1
done
