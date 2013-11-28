mysql=/usr/local/mysql
rabbit=/usr/local/rabbitmq_server-3.2.1/sbin
pass=123

memcached -d

cd $mysql
echo $pass | sudo -S mysqld --user=root --port=3306 &

cd $rabbit
echo $pass | sudo -S ./rabbitmq-server &

sleep 30



echo
echo "MEMCACHED VERSION"
echo stats | nc 127.0.0.1 11211 | grep version

echo
echo "RABBITMQ_SERVER VERSION"
echo 123 | sudo -S sudo $rabbit/rabbitmqctl status | grep "{rabbit,"

echo
echo "MYSQL VERSION"
$mysql/mysql -u root -p'root' -e "SHOW VARIABLES LIKE '%version%';"
echo "MYSQL ENVIRONMENT"
$mysql/mysql -u root -p'root' -e "SHOW VARIABLES WHERE Variable_Name IN ('max_allowed_packet', 'storage_engine', 'system_time_zone', 'version', 'unique_checks', 'have_innodb', 'character_set_server');"

