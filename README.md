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
private static final JAXBSerializer<MyVo1> MyVoConverter = new JAXBSerializer<MyVo1>(MyVo1.class);
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

SimpleSysInfo
===
we need to know **production environment** on (OS, Processor, Memory, Disks) in human readable form

ConnectionMetadataReader
===
we need to know human readable **database structure** on production in human readable form




samples:
- [x] @mentions, #refs, [links](http://google.com), **formatting**, and <del>tags</del> supported
- [x] list syntax required (any unordered or ordered list supported)
- [x] this is a complete item
- [ ] this is an incomplete item