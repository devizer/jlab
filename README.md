jlab
====
extentions, demos, tests and stress

JAXBSerializer, SimpleItegrity
===
```
private static final JAXBSerializer<MyVo1> MyVoConverter = new JAXBUtils<MyVo1>(MyVo1.class);
...
byte[] serialize(MyVo1 myVo) {
  byte[] message = MyVoConverter.ToData(myVo);
  return SimpleIntegrity.Store(message, SimpleIntegrity.Alg.md5);
}
...
byte[] serialize(byte[] message) {
  return MyVoConverter.Parse(SimpleIntegrity.Parse(message).getData());
}
```

jcl
Parallel.for
Lazy<T>
ReliableThreadLocal

SimpleSysInfo, ConnectionMetadataReader
===
we need human readable database structure and environment on proudction
