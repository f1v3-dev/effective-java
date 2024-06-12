# 아이템 1. 생성자 대신 정적 팩터리 메서드를 고려하라

클래스는 생성자와 별도로 `static factory method`를 제공할 수 있다.
이러한 방식의 장점과 단점은 무엇일까?

_User 클래스의 예시_

**Constructor**

```java
public class User {
  private final String name;
  private final String country;

  public User(String name, String country) {
    this.name = name;
    this.country = country;
  }

  // standard getters / toString ...
}
```

**Static Factory Method**

```java
public class User {
  private final String name;
  private final String country;

  private User(String name, String country) {
    this.name = name;
    this.country = country;
  }

  public static User createWithDefaultCountry(String name) {
    return new User(name, "KOREA");
  }
}
```

```java
User user = User.createWithDefaultCountry("f1v3");
```

## Static Factory Method 장점

### 1. 이름을 가질 수 있다.

- 생성자를 통해 새로운 인스턴스를 만들 때, 매개변수와 생성자 자체로만 반환될 객체의 특성을 설명하기 어렵다.

- 반면, 정적 팩터리 메서드같은 경우, 이름을 가질 수 있어 쉽게 묘사할 수 있다.

_example_

**User.class**

```java
public class User {
  private String name;
  private String country;

  // 생성자를 이용한 방식
  public User(String name, String country) {
    this.name = name;
    this.country = country;
  }

  // 정적 팩터리 메서드를 이용한 방식
  public static User createKoreanUserWithName(String name) {
    return new User(name, "KOREA");
  }

  public static User createAmericanUserWithName(String name) {
    return new User(name, "USA");
  }
}
```

**User 생성 예시**

```java
User user = new User("korea", "KOREA");
// 매개변수가 어떤 의미인지 알기 어렵다.

User user = User.createKoreanUserWithName("korea");
// "korea"는 User의 Name인 것을 파악하기 쉽다.


```

> 또한, 동일한 시그니처를 가질 수 있다.

- 생성자를 이용한 방식은 동일한 시그니처를 가진 생성자를 만들 수 없음.
- 정적 팩터리 메서드를 사용하게 된다면, 동일한 시그니처를 가진 메서드를 만들어낼 수 있음.

```java
public class User {

    private String name;
    private String country;

    public User(String name) {
        this.name = name;
    }

    // 동일한 시그니처가 존재하기 때문에 컴파일 에러가 발생함.
    public User(String country) {
        this.country = country;
    }
}
```

_정적 팩터리 메서드를 활용해서 변경해보자._

```java
public class User {

  private String name;
  private String country;

  public static User createUserWithName(String name) {
    User user = new User();
    user.name = name;
    return user;
  }

  public static User createUserWithCountry(String country) {
    User user = new User();
    user.country = country;
    return user;
  }
}

```

### 2. 호출이 될 때마다 새로운 인스턴스를 생성하지 않아도 된다.

- 불변 클래스(immutable class)는 인스턴스를 미리 만들어놓거나 캐싱하여 재활용하는 방식으로 불필요한 객체 생성을 피할 수 있다.

`Boolean.valueOf(boolean)` 메서드를 참고해보자.

```java
public final class Boolean implements java.io.Serializable,
        Comparable<Boolean>, Constable {

    public static final Boolean TRUE = new Boolean(true);

    public static final Boolean FALSE = new Boolean(false);

    // ...

    @IntrinsicCandidate
    public static Boolean valueOf(boolean b) {
        return (b ? TRUE : FALSE);
    }
}
```

### 3. 반환 타입의 하위 타입 객체를 반환할 수 있는 능력이 있다.

- 반환할 객체 클래스를 자유롭게 선택할 수 있다. = **엄청난 유연성!**
- API 설계시 구현 클래스를 공개하지 않고도 그 객체를 반환할 수 있어 API를 작게 유지할 수 있다.

자바 컬렉션 프레임워크 (`java.util.Collection`)

```java
public class Collections {

    // default constructor -> private
    private Collections() {
    }

    // static factory method
    public static <T> Collection<T> unmodifiableCollection(Collection<? extends T> c) {
        if (c.getClass() == UnmodifiableCollection.class) {
            return (Collection<T>) c;
        }
        return new UnmodifiableCollection<>(c);
    }

    // 구현체 : non-public 
    static class UnmodifiableCollection<E> implements Collection<E>, Serializable {
        // ...
    }
}
```

- 구현체를 숨기고 **public static factory method**를 통해 API를 작게 유지할 수 있다.
- 또한, 개념적인 무게(API 사용을 위한 학습)를 낮췄다.

### 4. 입력 매개변수에 따라 매번 다른 클래스의 객체를 반환할 수 있다.

- 반환 타입의 하위 타입이기만 하면 반환이 가능한 것이다.

`EnumSet 클래스`를 참고해보자.

```java
    public static <E extends Enum<E>> EnumSet<E> noneOf(Class<E> elementType) {
    Enum<?>[] universe = getUniverse(elementType);
    if (universe == null)
        throw new ClassCastException(elementType + " not an enum");

    if (universe.length <= 64)
        return new RegularEnumSet<>(elementType, universe);
    else
        return new JumboEnumSet<>(elementType, universe);
}
```

- 클라이언트는 `RegularEnumSet`, `JumboEnumSet` 두 클래스의 존재를 알 수 없고 알 필요도 없다.

### 5. Static Factory Method를 작성하는 시점에는 반환할 객체의 클래스가 존재하지 않아도 된다.

- 서비스 제공자 프레임워크를 만드는 근간
    - ex. `JDBC(Java Database Connectivity)`

> **서비스 제공자 프레임워크의 3가지 핵심 컴포넌트**
> 1. 서비스 인터페이스(service interface) : 구현체으 동작을 정의
> 2. 제공자 등록 API(provider registration API) : 제공자가 구현체를 등록할 때 사용
> 3. 서비스 접근 API(service access API) : 클라이언트가 서비스의 인스턴스를 얻을 때 사용
>
> 클라이언트는 서비스 접근 API를 사용할 떄 원하는 구현체의 조건을 명시

- 종종 위의 3개 컴포넌트와 서비스 제공자 인터페이스(service provider interface) 컴포넌트가 쓰인다.
    - 사용되지 않을 경우 `Reflection` 사용

_JDBC의 예시_

```java
// Provider Registration API
DriverManager.registerDriver();

// Service Access API
DriverManager.

getConnection();

// Service Provider Interface
public interface Driver {
    Connection connect(String url, java.util.Properties info)
            throws SQLException;
}
```

> [Java DriverManager 클래스](https://www.ibm.com/docs/ko/i/7.3?topic=connections-java-drivermanager-class)

## Static Factory Method 단점

### 1. 상속은 public, protected 생성자가 필요하다.

- 정적 팩터리 메서드만 제공하게 된다면, 하위 클래스를 만들 수 없다.
- 위에서 언급한 `Collection Framework`의 유틸리티 구현 클래스들은 상속할 수 없다는 얘기
- 어떻게 본다면 상속보다 컴포지션을 사용하도록 유도하며 불변 타입을 만들려면 제약을 지켜야 한다는 점에서 장점일 수도 있다.

> [Java - Composition](https://gyoogle.dev/blog/computer-language/Java/Composition.html)

### 2. 프로그래머가 찾기 어렵다.

API 설명에 명확히 드러나지 않음. 즉, 사용자가 정적 팩터리 메서드를 찾기 어렵다.

아래와 같이 정적 팩터리 메서드에서 사용하는 방식을 고려해보자.

- from: 매개변수를 하나 받아서 해당 타입의 인스턴스로 반환하는 **형변환 메서드**
```java
Date d = Date.from(instant);
```

- of: 여러 매개변수를 받아 적합한 타입의 인스턴스를 반환하는 **집계 메서드**
```java
Set<Rank> faceCards = EnumSet.of(JACK, QUEEN, KING);
```

- valueOf: from과 of의 더 자세한 버전
```java
BigInteger prime = BigInteger.valueOf(Integer.MAX_VALUE);
```

- instance, getInstance: 매개변수로 명시한 인스턴스 반환, but 같은 인스턴스임을 보장하지는 않음. 
```java
StackWalker luke = StackWalker.getInstance(options);
```

- create, newInstance: instance, getInstance와 같지만, 매번 새로운 인스턴스를 생성하여 반환함을 보장
```java
Object newArray = Array.newInstance(classObject, arrayLen);
```

> _"Type"_ 은 팩터리 메서드가 반환할 객체의 타입!

- getType: getInstance와 같으나, 생성할 클래스가 아닌 다른 클래스에 팩터리 메서드를 정의할 때 사용
```java
FileStore fs = Files.getFileStore(path);
```

- newType: newInstance와 같으나, 생성할 클래스가 아닌 다른 클래스에 팩터리 메서드를 정의할 때 사용   
```java
BufferedReader br = Files.newBufferedReader(path);
```

- type: getType과 newType의 심플 버전
```java
List<Complaint> litany = Collections.list(legacyLitany);
```

---

### 🎈 결론 
무작정 public 생성자를 제공하는 것 보단, 정적 팩터리 메서드와 public 생성자의 장단점을 생각해보고 적절하게 사용하는 습관을 길러보자.


> [Java Constructors vs Static Factory Methods](https://www.baeldung.com/java-constructors-vs-static-factory-methods)
