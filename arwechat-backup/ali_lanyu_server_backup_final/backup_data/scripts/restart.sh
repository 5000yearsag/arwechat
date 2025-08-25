n=`echo "${1}"`
cd /data
/usr/local/bin/docker-compose stop
if [ $? -eq 0 ]; then
     echo "停止容器succeed"
else
     echo "停止容器failed"
fi
/usr/local/bin/docker-compose up -d --force-recreate
if [ $? -eq 0 ]; then
     echo "启动容器succeed"
else
     echo "启动容器failed"
fi
cd /data
sa=`head -1 /data/nginx/sign`
if [ $sa -eq 0 ]; then
     /usr/local/bin/docker-compose stop java-slave
elif [ $sa -eq 1 ]; then 
     /usr/local/bin/docker-compose stop java-master
fi

