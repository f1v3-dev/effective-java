# 아이템 9. try-finally보다는 try-with-resources를 사용하라

Java 라이브러리에서 `InputStream`, `OutputStream`, `java.sql.Connection` 등 **close** 메서드를 호출해 직접 닫아줘야하는 자원이 많다.

- 클라이언트가 놓치기 쉬워서 예측할 수 없는 성능 문제로 이어지기도 함.
- 전통적으로 try-finally 방식으로 제대로 닫힘을 보장하는 수단으로 써왔다.

_try-finally 방식 예_

```java
static String firstLineOfFile(String path) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(path));

    try {
        return br.readLine();
    } finally {
        br.close();
    }
}
```

위의 방식이 나쁘지는 않지만 자원을 하나 더 쓴다면?

```java
import java.io.FileInputStream;
import java.io.FileOutputStream;

static void copy(String src, String dst) throws IOException {
    InputStream in = new FileInputStream(src);

    try {
        OutputStream out = new FileOutputStream(dst);
        try {
            byte[] buf = new byte[BUFFER_SIZE];
            int n;
            while ((n = in.read(buf)) >= 0) {
                out.write(buf, 0, n);
            }
        } finally {
            out.close();
        }
    } finally {
        in.close();
    }
}
```

### try-finally 방식의 문제점

- 예외는 try 블록과 finally 블록 모두에서 발생할 수 있음.
- 두 번째 예외가 첫 번째 예외를 집어삼킬 수 있다는 문제가 존재
    - 스택 추적 내역에 첫 번째 예외에 관한 정보가 남지 않음!! &rarr; 디버깅을 어렵게 만듦
    - 물론, 코드로 기록하도록 만들 수 있지만 코드가 너무 지저분해짐

이러한 문제를 해결하기 위해 Java 7이 `try-with-resources` 덕에 모두 해결되었다.

## try-with-resources

- 이 구조를 사용하기 위해서는 AutoCloseable 인터페이스를 구현해야 한다.

> AutoCloseable: 단순히 void를 반환하는 close 메서드 하나만 덩그러니 정의한 인터페이스

_try-with-resources 적용 예_
```java
static String firstLineOfFile(String path) throws IOException {
    try (BufferedReader br = new BufferedReader(
            new FileReader(path))) {
        return br.readLine();
  }
}
```

```java
static void copy(String src, String dst) throws IOException {
  try (InputStream in = new FileInputStream(src);
       OutputStream out = new FileOutputStream(dst)) {
      byte[] buf = new byte[BUFFER_SIZE];
      int n;
      
      while ((n = in.read(buf)) >= 0) {
          out.write(buf, 0, n);
      }
  }
}
```

- 보통의 try-finally 처럼 try-with-resources에서도 catch 절을 쓸 수 있다.
- catch 절 덕분에 try문을 더 중첩하지 않고도 다수의 예외를 처리할 수 있다.

_try-with-resources에 catch 절 추가_
```java
static String firstLineOfFile(String path, String defaultVal) {
    try (BufferedReader br = new BufferedReader(
            new FileReader(path))) {
        return br.readLine();
    } catch (IOException e) {
        return defaultVal;
    }
}
```

---

### 핵심 정리

- 꼭 회수해야 하는 자원을 다룰 땐 try-finally가 아니라 try-with-resources를 사용하자!