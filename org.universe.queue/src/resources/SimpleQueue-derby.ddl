/* Дерби не понимает null, bit и точку с запятой */

Create Table SimpleQueue(
  Id varchar(32) not null,
  OptionalKey varchar(1024),
  QueueName varchar(1024) not null,
  Message blob not null,
  CreatedAt TIMESTAMP Not Null,
  ModifiedAt TIMESTAMP Not Null,
  AckDate TIMESTAMP,
  HandlersCount int not null,
  DeliveryDate TIMESTAMP,
  Locked SMALLINT not null,
  CONSTRAINT PK_SimpleQueue Primary Key (Id)
)
/*EXEC*/

Create Index IX_SimpleQueue_Key On SimpleQueue(OptionalKey)
/*EXEC*/

Create Index IX_SimpleQueue_Delivery On SimpleQueue(QueueName, ModifiedAt)
