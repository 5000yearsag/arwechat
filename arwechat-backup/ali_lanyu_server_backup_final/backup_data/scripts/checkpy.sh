#!/bin/bash
source /etc/profile
cd /data/
date=`date "+%Y%m%d%H%M%S"`
py=`netstat -an | grep ":58080" | awk '$1 == "tcp" && $NF == "LISTEN" {print $0}' | wc -l`
if [ $py -eq 0 ];then
   echo "$date运行不正常强制启动" >> /data/check.log && /bin/bash /data/startpy.sh
else
  echo "$date运行正常!" >> /data/check.log
fi
