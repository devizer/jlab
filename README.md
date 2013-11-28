jlab
====
extentions, demos, tests and stress

*jcl* extensions
===
- [x] Parallel.for
- [x] Lazy<T>
- [x] ReliableThreadLocal





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
we need to know **production environment** on (OS, Processor, Memory, Disks) in human readable form.
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



ConnectionMetadataReader
===
we need to know human readable **database structure** on production in human readable form




samples:
- [x] @mentions, #refs, [links](http://google.com), **formatting**, and <del>tags</del> supported
- [x] list syntax required (any unordered or ordered list supported)
- [x] this is a complete item
- [ ] this is an incomplete item