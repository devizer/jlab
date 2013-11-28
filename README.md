jlab
====
extentions, demos, tests and stress. details at my blog are labeled with [Java](http://devizer.blogspot.com/search/label/Java) tag.

*jcl* extensions
===
- [x] Parallel.for
- [x] Lazy<T>
- [x] ReliableThreadLocal<T>





JAXBSerializer, SimpleItegrity
===
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
===
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
===
we need to know human readable **database structure** on production in human readable form.
Tested against mysql, ms sql server, sqlite, derby




samples:
- [x] @mentions, #refs, [blog](http://google.com), **formatting**, and <del>tags</del> supported
- [x] list syntax required (any unordered or ordered list supported)
- [x] this is a complete item
- [ ] this is an incomplete item