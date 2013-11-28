jlab
====
extentions, demos, tests and stress

jcl
===
- [x] Parallel.for
- [x] Lazy<T>
- [x] ReliableThreadLocal


JAXBSerializer, SimpleItegrity
===
we need to store long living POJO in blobs and queues with versioning and integrity check:
```
private static final JAXBSerializer<MyVo1> MyVoConverter = new JAXBUtils<MyVo1>(MyVo1.class);
...
byte[] serialize(MyVo1 myVo) {
  byte[] message = MyVoConverter.ToData(myVo);
  return SimpleIntegrity.Store(message, SimpleIntegrity.Alg.md5);
}
...
byte[] parse(byte[] message) {
  return MyVoConverter.Parse(SimpleIntegrity.Parse(message).getData());
}
```


SimpleSysInfo, ConnectionMetadataReader
===
we need to know human readable database structure and environment on production
