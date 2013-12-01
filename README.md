jlab
----
extentions, demos, tests and stress. details at my blog are labeled with [Java](http://devizer.blogspot.com/search/label/Java) tag.

Sources and releases
----
I use 2 test scopes
- Unit-tests (ant without parameters).
- Integration tests (ant build full-test)

I am using home build server. Since 0.4.35 **each release always fully tested using master snapshot before pushing release tag**. Master branch has **Major.Minor.Build-SNAPSHOT** version
Releases have tags alike **Major.Minor.Build** without 'SNAPSHOT' suffix. Each release is fully successfully tested with integration tests. I put small description on each [public release](https://github.com/devizer/jlab/releases)
Home build server runs on Ubuntu 13.10 x64. Rarely I run all tests, including Integration tests, on
- Fedora 19 (Schrödinger’s Cat) Arm7 hard float
- MAC OS X 10.9
- Windows 7 x64

jcl extensions
----
- [x] Parallel.for
- [x] Lazy<T>
- [x] ReliableThreadLocal<T>





JAXBSerializer, SimpleIntegrity
----
we need to store a **long living** POJO in blobs and queues with *versioning* and integrity check:


```java
static final JAXBSerializer<MyVo1> MyVoConverter = new JAXBSerializer<MyVo1>(MyVo1.class);
...
byte[] serialize(MyVo1 myVo) {
  byte[] message = MyVoConverter.ToData(myVo);
  return SimpleIntegrity.Store(message, SimpleIntegrity.Alg.md5);
}
...
MyVo1 parse(byte[] message) {
  return MyVoConverter.Parse(SimpleIntegrity.Parse(message).getData());
}
```

LightSystemInfo
----
we need to know **production environment** (OS, Processor, Memory, <del>Disks</del>, <del>Video adapter</del>) in human readable form.
Class supports Windows, Linux (arm, x86, x64), MAC OS X without external JNI dependencies.

```java
System.out.print(new LightSystemInfo());
```
```
Family: Mac OS X, x86_64
   CPU: Intel(R) Core(TM)2 Duo CPU P8400 @ 2.26GHz, Cache 3072 Kb, 2 cores
    OS: Mac OS X, 10.9
  Java: Java HotSpot(TM) 64-Bit Server VM, ver 1.7.0_45 by Oracle Corporation
Memory: 2 048 MB
```
```
Family: Windows, amd64
   CPU: Intel(R) Core(TM)2 Duo CPU P8400 @ 2.26GHz, L2 Cache 3072 KB, 2 Cores
    OS: Microsoft(R) Windows(R) XP Professional x64 Edition, Service Pack 2
  Java: Java HotSpot(TM) 64-Bit Server VM, ver 1.7.0_45 by Oracle Corporation
Memory: 511 MB
  Soft: IE 8.00.6001.18702 (longhorn_ie8_rtm(wmbla).090308-0338)
        MSI 4.5.6002.22487
```
```
Family: Linux, arm
   CPU: ARMv7 Processor rev 0 (v7l)
    OS: Fedora 19 (Schrödinger’s Cat)
  Java: Java HotSpot(TM) Client VM, ver 1.7.0_45 by Oracle Corporation
Memory: 1,004 MB
```
```
Family: Linux, x86
   CPU: Intel(R) Xeon(R) CPU E5-2650 0 @ 2.00GHz, Cache 20480 KB
    OS: SUSE Linux Enterprise Server 11 (i586)
  Java: IBM J9 VM, ver 1.7.0 by IBM Corporation
Memory: 641 MB
```




ConnectionMetadataReader
----
we need to know human readable **database structure** on production in human readable form.
Tested against mysql, ms sql server, sqlite, derby.
Supports Table columns, primary keys, foreign keys and indexes.
ConnectionMetadataReader however isnt sql code generator,
just db structure visualizer for production environment:

```java
logger.log(ConnectionMetaDataReader.Build(connection));
```
```sql
/*
  DB-Product 'MySQL' Ver '5.5.34-0ubuntu0.13.10.1'
  JDBC '4.4'
  Driver 'MySQL Connector Java' Ver 'mysql-connector-java-5.1.27 ( Revision: alexander.soklakov@oracle.com-20131021093118-gtm1bh1vb450xipt )'
  Url: jdbc:mysql://127.0.0.1/sandbox
*/

CREATE TABLE SimpleQueue (
  Id VARCHAR(26) Not Null,
  OptionalKey VARCHAR(1024),
  QueueName VARCHAR(255) Not Null,
  Message LONGBLOB Not Null,
  CreatedAt DATETIME Not Null,
  ModifiedAt DATETIME Not Null,
  AckDate DATETIME,
  HandlersCount INT Not Null,
  DeliveryDate DATETIME,
  Locked BIT Not Null,
  Constraint PK_SimpleQueue PRIMARY KEY On (Id)
);
Create UNIQUE Index PRIMARY On SimpleQueue(Id);
Create Index IX_SimpleQueue_Delivery On SimpleQueue(QueueName, ModifiedAt);
Create Index IX_SimpleQueue_Key On SimpleQueue(OptionalKey);
```




