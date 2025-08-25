ip=`hostname -I | awk '{print $1}'`
echo $ip > /data/ehcache/ip
