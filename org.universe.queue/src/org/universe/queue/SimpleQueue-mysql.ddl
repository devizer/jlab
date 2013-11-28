/* Дерби не понимает null, bit и точку с запятой */

Create Table SimpleQueue(
  Id varchar(26) not null,
  OptionalKey varchar(1024),
  QueueName varchar(255) not null,
  Message longblob not null,
  CreatedAt DateTime Not Null,
  ModifiedAt DateTime Not Null,
  AckDate DateTime,
  HandlersCount int not null,
  DeliveryDate DateTime,
  Locked bit not null,
  CONSTRAINT PK_SimpleQueue Primary Key (Id)
);
/*EXEC*/

Create Index IX_SimpleQueue_Key On SimpleQueue(OptionalKey);
/*EXEC*/

Create Index IX_SimpleQueue_Delivery On SimpleQueue(QueueName, ModifiedAt);
