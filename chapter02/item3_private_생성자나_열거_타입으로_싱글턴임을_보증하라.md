# 아이템 3. private 생성자나 열거 타입으로 싱글턴임을 보증하라

> `Singleton`: 인스턴스를 오직 하나만 생성할 수 있는 클래스

싱글턴 예시

- 무상태(stateless) 객체 - 함수
- 설계상 유일해야 하는 시스템 컴포넌트
- 주로 사용하는 스프링 빈 또한 싱글턴으로 관리됨

클래스를 싱글턴으로 만들게 될 경우, 이를 사용하는 클라이언트를 테스트하기 어렵다는 단점이 존재한다.
이러한 문제점을 해결하기 위해 Mockito 라이브러리 사용한 경험이 있음.

## 1. 싱글턴을 만드는 방법

- 생성자를 private으로 선언
- 유일한 인스턴스에 접근할 수 있는 수단으로 public static 멤버를 마련

### 1. public static final field 방식의 Singleton

```java
public class Elvis {

    public static final Elvis INSTANCE = new Elvis();

    private Elvis() {
        throw new AssertionError("CAN'T MAKE INSTANCE");
    }

    public void leaveTheBuilding() {
        // ...
    }
}
```

- private constructor는 `Elvis.INSTANCE` 초기화 할 때 한 번만 호출됨.
- 전체 시스템에서 하나의 인스턴스만 존재함을 보장
- `public static` 필드가 `final`이기 때문에 다른 객체를 참조할 수 없음!

#### 예외

- 권한이 있는 클라이언트가 `Reflection API`인 AccessibleObject.setAccessible()을 사용하여 private 생성자를 호출할 수 있음.
- 물론, 생성자 내부에 두 번째 인스턴스를 생성하려고 할 때 예외를 던지게 하면 해결이 가능하다.

### 2. 정적 팩터리 방식의 Singleton

```java
public class Elvis {
    private static final Elvis INSTANCE = new Elvis();

    private Elvis() {
        // ...
    }

    public static Elvis getInstance() {
        return INSTANCE;
    }

    public void leaveTheBuilding() {
        // ...
    }
}
```

- Elvis.getInstance()는 항상 같은 객체의 참조를 반환
- 위의 방식과 예외는 동일하다.

#### 장점

1. API를 바꾸지 않고도 싱글턴이 아니게 변경할 수 있다. 
2. 정적 팩터리를 제네릭 싱글턴 팩터리로 만들 수 있다.
3. 정적 팩터리 메서드를 참조하여 공급자(supplier)로 사용할 수 있다.

### 3. 열거 타입 방식의 Singleton

```java
public enum Elvis {
    INSTANCE;
    
    public void leaveTheBuilding() {
        // ...
    }
}
```

- public 필드 방식과 비슷
- 직렬화, Reflection 공격에도 완벽히 막아줌.
- 대부분 상황에서 원소가 하나뿐인 열거 타입이 싱글턴을 만드는 가장 좋은 방법!

#### 단점
- 만들려는 싱글턴이 Enum 외의 클래스를 상속해야 한다면 사용할 수 없음.
  - Enum Type이 다른 인터페이스를 구현하도록 선언할 수는 있다.

## 2. 싱글턴 클래스 직렬화

위이 1, 2번 방식으로 만든 싱글턴 클래스를 직렬화 하기 위해서는 단순히 Serializable을 구현한다고 선언하는 것만으로는 부족하다.

- 모든 인스턴스 필드를 `transient`로 선언
- `readResolve` 메서드를 제공해야 한다.

직렬화된 인스턴스를 역직렬화할 때마다 새로운 인스턴스가 만들어지는 것을 막기 위함.

```java
private Object readResolve() {
    return INSTACNE;
}
```

---

```java
import java.lang.reflect.Constructor;

public class Elvis {

    public static final Elvis INSTANCE = new Elvis();

    private Elvis() {
        throw new AssertionError("You can't instance me!");
    }

    public void leftTheBuilding() {
        System.out.println("Ladies and gentlemen, Elvis has left the building!");
    }

    public static void main(String[] args) {

        try {
            // Elvis class
            Constructor<Elvis> constructor = Elvis.class.getDeclaredConstructor();

            // 생성자에 접근 가능하게 설정
            constructor.setAccessible(true);

            // Elvis instance 생성! -> 과연 오류가 날까?
            Elvis elvis = constructor.newInstance();

            elvis.leftTheBuilding();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

```

```shell
Exception in thread "main" java.lang.AssertionError: You can't instance me!
	at Elvis.<init>(Elvis.java:8)
	at Elvis.<clinit>(Elvis.java:5)
```