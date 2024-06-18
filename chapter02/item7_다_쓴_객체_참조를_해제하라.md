# 아이템 7. 다 쓴 객체 참조를 해제하라

- Java의 GC(Garbage Collection)는 다 쓴 객체를 알아서 회수해줌!
- 그렇다고 메모리 관리에 더 이상 신경을 쓰지 않아도 되는 것은 아님

_아래의 간단한 Stack 구현 코드를 보자_

```java
public class Stack {

    private Object[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    public Stack() {
        elements = new Object[DEFAULT_INITIAL_CAPACITY];
    }

    public void push(Object e) {
        ensureCapacity();
        elements[size++] = e;
    }

    public Object pop() {
        if (size == 0) {
            throw new EmptyStackException();
        }

        return elements[--size];
    }

    /**
     * 원소를 위한 공간을 적어도 하나 이상 확보한다.
     * 배열 크기를 늘려야 할 때마다 대략 두 배씩 늘린다.
     */
    private void ensureCapacity() {
        if (elements.length == size) {
            elements = Arrays.copyOf(elements, 2 * size + 1);
        }
    }
}
```

- 별 문제가 없어 보이지만, `메모리 누수`라는 문제가 숨어있다.
- 위의 스택을 사용하는 프로그램을 오래 실행하면 가비지 컬렉션 활동과 메모리 사용량이 늘어 성능이 저하될 것이다.
- 심한 경우, **디스크 페이징**이나 **OutOfMemoryError**까지 일으켜 예기치않게 프로그램이 종료될 수 있다.

### 문제 원인

어디서 **메모리 누수**가 발생하는 것일까?

- Stack이 커졌다가 줄어들었을 때 스택에서 꺼내진 객체들은 가비지 컬렉터가 회수하지 않는다.
- 프로그램에서 그 객체들을 더 이상 사용하지 않더라도 스택이 그 객체들의 `다 쓴 참조(obsolete reference)`를 여전히 가지고 있기 때문!

> `다 쓴 참조`: 다시 쓰지 않을 참조
> - elements 배열의 '**활성 영역(size)**' 밖의 참조)

### 문제 해법

- 해당 참조를 다 썼을 때 참조 해제(null)를 해주면 된다.
- 실수로 사용하게 될 경우 `NullPointerException`이 발생하므로 잘못된 일을 방지할 수 있음.

_제대로 구현한 pop 메서드_

```java
public Object pop() {
    if (size == 0) {
        throw new EmptyStackException();
    }

    Object result = elements[--size];
    elements[size] = null; // 다 쓴 참조 해제 (null 처리)
    return result;
}
```

### 위의 방식처럼 객체 참조를 null 처리하는 일은 예외적인 경우여야 한다.

- 다 쓴 참조를 해제하는 가장 좋은 방법은 그 참조를 담은 변수를 유효 범위(scope) 밖으로 밀어내는 것이다.

#### 1. 자기 메모리를 직접 관리하는 클래스

- 왜 Stack 클래스는 메모리 누수에 취약할까?
- 자기 메모리를 직접 관리함!!
- elements 배열로 저장소 풀을 만들어 원소들을 관리 &rarr; 가비지 컬렉터는 활성 영역과 비활성 영역을 알 수 없음
- 일반적으로 자기 메모리를 직접 관리하는 클래스라면 프로그래머는 항상 메모리 누수의 주의해야 한다.

#### 2. 캐시

- 객체 참조를 캐시에 넣고 객체를 다 쓴 뒤로도 그 참조를 놔두게 된다면?
- 캐시 외부에서 키(key)를 참조하는 동안만 엔트리가 살아있는 캐시가 필요한 상황이라면 `WeakHashMap`을 사용
    - 이러한 사황에서만 유용하다는 점은 기억하고 사용하자
- 백그라운드 스레드를 활용하거나 캐시에 새 엔트리를 추가할 때 부수 작업으로 수행하는 방법도 존재
    - LinkedHashMap은 removeEldestEntry 메서드를 사용하여 후자의 방식으로 처리함
- 더 복잡한 캐시를 만들고 싶다면 `java.lang.ref` 패키지를 직접 활용하자

#### 3. 리스너(listener) / 콜백(callback)

- 클라이언트가 콜백을 등록만 하고 명확히 해지하지 않는다면 콜백은 계속 쌓임
- 콜백을 약한 참조(weak reference)로 저장하면 가비지 컬렉터가 즉시 수거할 수 있음
    - ex. WeakHashMap에 키로 저장

