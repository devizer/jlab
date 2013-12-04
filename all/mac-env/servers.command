mysql=/usr/local/mysql
rabbit=/usr/local/rabbitmq_server-3.2.1/sbin

memcached -d

cd $mysql
# echo 123 | sudo -S mysqld --user=root --port=3306 &

cd $rabbit
echo 123 | sudo -S ./rabbitmq-server &

sleep 30



echo
echo "MEMCACHED VERSION"
echo stats | nc 127.0.0.1 11211 | grep version

echo
echo "RABBITMQ_SERVER VERSION"
echo $pass | sudo -S sudo $rabbit/rabbitmqctl status | grep "{rabbit,"

echo
echo "MYSQL VERSION"
mysql -u root -p'root' —h 127.0.0.1 -e "SHOW VARIABLES LIKE '%version%';"

echo "MYSQL ENVIRONMENT"
mysql -u root -p'root' —h 127.0.0.1 -e "SHOW VARIABLES WHERE Variable_Name IN ('max_allowed_packet', 'storage_engine', 'system_time_zone', 'version', 'unique_checks', 'have_innodb', 'character_set_server');"

