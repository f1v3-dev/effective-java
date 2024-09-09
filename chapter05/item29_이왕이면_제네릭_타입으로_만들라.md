# 아이템 29. 이왕이면 제네릭 타입으로 만들라

제네릭 타입을 새로 만드는 일은 조금 어렵지만, 배워두면 그만한 값어치는 충분히 한다.

## 1. 제네릭을 만들어보자

**Object 기반 스택 - 제네릭이 절실한 강력 후보!**
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
        
        Object result = elements[--size];
        elemtns[size] = null; // 다 쓴 참조 해제
        return result;
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
    
    private void ensureCapacity() {
        if (elements.length == size) {
            elements = Arrays.copyOf(elements, 2 * size + 1);
        }
    }
}
```

위의 클래스를 제네릭으로 바꾼다 하더라도 현재 버전을 사용하는 클라이언트들에게 아무런 해가 없다.
오히려 지금 상태에서 스택에서 꺼낸 객체의 형변환시 런타임 오류가 발생할 수 있는 위험을 줄일 수 있다.

### 1. 클래스 선언에 타입 매개변수를 추가하자

일반 클래스를 제네릭 클래스로 만드는 첫 단계는 클래스 선언에 타입 매개변수를 추가하는 일이다.

이 때, 타입 이름으로 보통 E를 사용한다.

```java
public class Stack<E> {
    private E[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 0;

    public Stack() {
        elements = new E[DEFAULT_INITIAL_CAPACITY];
    }

    public void push(E e) {
        ensureCapacity();
        elements[size++] = e;
    }

    public E pop() {
        if (size == 0) {
            throw new EmptyStackException();
        }
        
        E result = elements[--size];
        elements[size] = null // 다 쓴 참조 해제
        return result;
    }
    
    // ... isEmpty와 ensureCapacity 메서드는 그대로
}
```

`elements = new E[DEFAULT_INITIAL_CAPACITY]` 에서 오류가 발생할 것이다.

`E` 와 같은 제네릭 타입은 실체화 불가 타입이기 때문에 배열을 마들 수 없다. 따라서 다음과 같이 해결 방법이 존재한다.

**1. 제네릭 배열 생성을 금지하는 제약을 대놓고 우회하는 방법**

- Object 배열을 생성하고 제네릭 배열로 형변환
- 컴파일러가 오류 대신 경고를 보내지만 타입 안전하지 않다.
- 비검사 형변환이 안전함을 직접 증명했다면 `@SuppressWarnings` 어노테이션으로 경고를 숨기자.

```java
// 배열 elements는 push(E)로 넘어온 E 인스턴스만 담는다.
// 따라서 타입 안전성을 보장하지만,
// 이 배열의 런타임 타입은 E[] 가 아닌 Object[] 다!
@SuppressWarnings("unchecked")
public Stack() {
    elements = (E[]) new Object[DEFAULT_INITIAL_CAPACITY];
}
```

이 방식은, 가독성이 좋으며 형변환을 배열 생성시 단 한번만 해주면 된다. 
하지만, 배열의 런타임 타입이 컴파일타임 타입과 달라 힙 오염을 일으킨다. 

**2. elements 필드의 타입을 `E[]` 에서 `Object[]` 로 바꾸기**

다음 코드에서 에러가 발생할 것이다.

```shell
E result = elements[--size];
```

배열이 반환한 원소를 E로 형변환 할 경우 오류 대신 경고가 뜬다.

```shell
E result = (E) elements[--size];
```

- E는 실체화 불가 타입이므로 컴파일러는 런타임에 이뤄지는 형변환이 안전한건지 증명할 방법이 없다.
- 이번에도 직접 증명하고 경고를 숨겨보자. 단, 전체 메서드가 아닌 비검사 형변환을 수행하는 부분만!

```java
// 비검사 경고를 적절히 숨긴다.
public E pop() {
    if (size == 0) {
        throw new EmptyStackException();
    }
    
    // push에서 E 타입만 허용하므로 이 형변환은 안전하다.
    @SuppressWarnings("unchecked") E result = (E) elements[--size];
    
    elements[size] = null;
    return result;
}
```

이 방식은, 배열에서 원소를 읽을 때마다 해줘야 한다. 


## 핵심 정리

- 클라이언트에서 직접 형변환해야 하는 타입보다 제네릭 타입이 더 안전하고 쓰기 편하다.
- 새로운 타입 설계시 형변환 없이 사용할 수 있도록 하자.