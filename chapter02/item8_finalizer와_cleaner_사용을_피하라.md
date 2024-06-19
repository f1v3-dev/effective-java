# 아이템 8. finalizer와 cleaner 사용을 피하라

자바에서는 `finalizer`, `cleaner` 라는 두 가지 객체 소멸자를 제공한다.

- **finalizer**: 예측할 수 없고, 상황에 따라 위험할 수 있어 기본적으로 '쓰지 말아야' 한다.
- **cleaner**: finalizer보다는 덜 위험하지만, 여전히 예측할 수 없고, 느리고, 일반적으로 불필요하다.

**C++에서의 _파괴자(destructor)_ 와는 다른 개념인 것을 명심하자.**

- 비메모리 자원을 회수하는 용도로 쓰이는 것으로, 자바에서는 **try-with-resources**, **try-finally**를 사용하여 해결한다.

## finalizer와 cleaner 사용을 피해야 하는 이유

#### 1. 즉시 수행된다는 보장이 없다.

- 객체에 접근할 수 없게 된 후 언제 실행되는지 알 수 없다.
- 즉, `finalizer`와 `cleaner`로는 제때 실행되어야 하는 작업을 절대 할 수 없다.
    - 가비지 컬렉터의 알고리즘에 따라 언제 실행되는지 결정되기 때문이다.

#### 2. 상태를 영구적으로 수정하는 작업에는 적합하지 않다.

- 데이터베이스 같은 공유 자원의 영구 락(lock) 해제를 finalizer나 cleaner에 맡겨 놓으면 분산 시스템 전체가 서서히 멈출 것이다.
- 또한, 수행 시점뿐 아니라 수행 여부조차 보장하지 않는다는 것이다.

#### 3. `System.gc`나 `System.runFinalization` 메서드에 현혹되지 말자.

- finalizer와 cleaner가 실행될 가능성을 높여줄 수는 있으나, 보장해주지는 않는다.
    - `System.runFinalizersOnExit`과 `Runtime.runFinalizersOnExit`가 보장해주겠다는 메서드가 있기는 하다.
    - 하지만, 이 메서드는 심각한 결함(ThreadStop) 때문에 수십년간 지탄받아 왔다는 점을 기억하자.

#### 4. finalizer 동작 중 발생한 예외는 무시되며, 처리할 작업이 남았더라도 종료된다.

- 보통 잡지 못한 예외가 스레드를 중단시키고 스택 추적 내역을 출력하겠지만, 경고조차 출력하지 않는다.
- 그나마 `cleaner`를 사용하는 라이브러리는 자신의 스레드를 통제하기 때문에 이러한 일은 발생하지 않음.

#### 5. 성능 문제도 동반한다.

- AutoCloseable 객체를 생성하고 가비지 컬렉터가 수거하기까지 12ns가 걸림 (try-with-resources)
- finalizer를 사용하면 550ns가 걸림 즉, 50배나 느려진 것
- 안전망 방식에서는 66ns로 5배 정도 려진다는 뜻

#### 6. 심각한 보안 문제를 일으킬 수 있다.

finalizer를 사용한 클래스는 finalizer 공격에 노출될 수 있음

- 생성자나 직렬화 과정에서 예외가 발생하면, 생성되다 만 객체에서 악의적인 하위 클래스의 finalizer가 수행될 수 있게 된다.
- 이 finalizer는 정적 필드에 자신의 참조를 할당해서 GC가 수집하지 못하게 할 수 있음!

**객체 생성을 막으려면 생성자에서 예외를 던지는 것만으로 충분하지만, finalizer가 있다면 그렇지도 않다.**

### 대안 방법

#### AutoCloseable

- AutoCloseable을 구현하고 클라이언트에서 인스턴스를 다 사용하면 close 메서드를 호출하면 된다.
- 일반적으로 예외가 발생해도 정상 종료가 되도록 `try-with-resources`를 사용해야 한다.
- 또한, close 메서드에서 더 이상 유효하지 않음을 필드에 기록하고, 다시 호출될 경우 예외(IllegalStateException)을 던지는 방법을 고려하자.

그렇다면 cleaner와 finalizer는 언제 쓰라는 걸까?

1. 자원의 소유자가 close 메서드를 호출하지 않는 것에 대한 **안전망** 역할
    - `FileInputStream`, `FileOutputStream`, `ThreadPoolExecutor` 등
2. 네이티브 피어(native peer)와 연결된 객체에서 사용
    - 네이티브 피어는 자바 객체가 아니기 때문에 가비지 컬렉터는 그 존재를 알지 못한다.
    - 성능 저하를 감당할 수 있고, 네이티브 피어가 심각한 자원을 가지고 있지 않을 때만 해당

#### Cleaner 사용 예시

```java
import java.lang.ref.Cleaner;

public class Room implements AutoCloseable {

    private static final Cleaner cleaner = Cleaner.create();

    // 청소가 필요한 자원. 절대 Room을 참조해서는 안된다! -> 순환 참조 발생
    private static class State implements Runnable {
        int numJunkPiles; // 방(Room) 안의 쓰레기 수

        State(int numJunkPiles) {
            this.numJunkPiles = numJunkPiles;
        }

        // close 메서드나 cleaner 메서드가 호출한다.
        @Override
        public void run() {
            System.out.println("방 청소");
            numJunkPiles = 0;
        }
    }

    // 방의 상태. cleanable과 공유한다.
    private final State state;

    // cleanable 객체. 수거 대상이 되면 방을 청소한다.
    private final Cleaner.Cleanable cleanable;

    public Room(int numJunkPiles) {
        state = new State(numJunkPiles);
        cleanable = cleaner.register(this, state);
    }

    @Override
    public void close() throws Exception {
        cleanable.clean();
    }
}
```

```java
public class Adult {

    public static void main(String[] args) throws Exception {
        try (Room myRoom = new Room(7)) {
            System.out.println("안녕~");
        }
    }
}

```

```java
public class Teenager {
    public static void main(String[] args) {
        new Room(99);
        System.out.println("아무렴");
    }
}

```

- 위 코드를 보고 어떤 결과가 나올지 예상해보자.
- System.exit을 호출할 때 cleaner 동작은 구현하기 나름인 것을 명심하자.
- Teenager에 `System.gc();`를 호출하면 `방 청소`가 출력될 수 있지만, 보장되는 것은 아니다.
