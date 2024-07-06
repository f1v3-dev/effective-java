# 아이템 13. clone 재정의는 주의해서 진행하라

- `Cloneable`은 이름에서도 알 수 있듯이 복제해도 되는 클래스임을 명시하는 용도의 `믹스인 인터페이스(mixin interface)`이다.
- 하지만, **clone 메서드**는 Object의 **protected 메서드**라는 점에서 의도한 목적을 제대로 이루지 못하고 있다.
- `Cloneable`을 구현하는 것만으로는 외부 객체에서 clone 메서드를 호출할 수 없다. (리플렉션은 예외이며, 100% 보장하지 않음)

## Cloneable 인터페이스

- 이 인터페이스는 아무런 메서드도 가지고 있지 않지만, Object 내의 **clone 메서드**의 동작 방식을 결정한다.
    - Cloneable을 구현한 클래스의 인스턴스에서 clone을 호출하면 그 객체의 필드들을 하나씩 복사한 객체를 반환한다.
    - 구현하지 않았을 경우 `ClonenotSupportedException` 예외가 발생한다.

> 원래 같았으면, Cloneable 인터페이스에 clone 메서드를 정의하고, 강제로 구현을 하도록 했을 것 같은데?..
>
> 특이하게 상위 클래스에 정의된 protected 메서드의 동작 방식을 변경한 것이다.  
> **이러한 방식은 상당히 이례적으로 사용한 예이니 따라 하지는 말자.**

- 실무에서 Cloneable을 구현한 클래스는 clone 메서드를 public으로 제공하며, 사용자는 당연히 복제가 제대로 이뤄지리라 기대한다.

### clone 메서드의 일반 규약

> 이이 객체의 복사본을 생성해 반환한다. (복사: 객체를 구현한 클래스에 따라 다를 수 있다.)
>
> 아래의 식은 참이다.
> - x.clone != x
> - x.clone().getClass() == x.getClass()
>
> 하지만, 이상의 요구를 반드시 만족해야 하는 것은 아니다.
> 다음 식은 일반적으로 참이지만, 필수는 아니다.
>
> - x.clone().equals(x)
>
> 관례상, 이 메서드가 반환하는 객체는 super.clone을 호출해서 얻어야 한다.
> 이 클래스와 모든 상위 클래스가 이 관례를 따른다면 다음 식은 참이다. (Object는 예외)
>
> - x.clone().getClass() == x.getClass()
>
> 관례상, 반환된 객체와 원본 객체는 **독립적**이어야 한다.
> 이를 만족하려면 super.clone으로 얻은 객체의 필드 중 하나 이상을 반환 전에수정해야 할 수도 있다.

- 강제성이 없다는 것만 빼면 **생성자 연쇄(constructor chaining)** 와 살짝 비슷한 매커니즘이다.
- 하위 클래스에서 super.clone을 호출한다면 잘못된 클래스의 객체가 만들어지는 것을 조심해야한다.
    - 물론, final 클래스인 경우 하위 클래스가 없으니 이 관례는 무시된다.
    - But, final 클래스의 clone 메서드가 super.clone을 호출하지 않는다면 Cloneable을 구현할 이유도 없음,,
- **'쓸데없는 복사를 지양한다'** 라는 관점에서 보면 불변 클래스는 굳이 clone 메서드를 제공하지 않는 것이 좋다

_가변 상태를 참조하지 않는 클래스용 clone method_

```java

@Override
public PhoneNumber clone() {
    try {
        return (PhoneNumber) super.clone();
    } catch (CloneNotSupportedException e) {
        throw new AssertionError(); // 애초에 발생할 수 없음,,
    }
}
```

- 자바가 공변 반환 타이핑(covariant return typing)을 지원하여 `Object.clone`의 반환 타입을 `PhoneNumber`으로 변경할 수 있다.
- PhoneNumber 클래스가 Cloneable을 구현하므로, super.clone이 성공할 것이다.
    - `CloneNotSupportedException`은 unchecked exception으로 만들어졌어야 거추장스러운 코드에서 벗어날 수 있었을 것이다.

> **공변 반환 타이핑(covariant return typing)**
> - 부모 클래스의 메서드가 반환하는 타입보다 더 구체적인 타입을 자식 클래스의 오버라이딩 메서드에서 반환할 수 있도록 허용하는 것
>
> _example_
>```java
> class Parent { 
>    Parent getObject() {
>        return new Parent();
>    }
>}
>
>class Child extends Parent { 
>    @Override 
>    Child getObject() {
>        return new Child();
>    }
>}
>```

### 클래스가 가변 객체를 참조하는 경우 (Stack)

**_Stack.java_**

```java
public class Stack {

    private Object[] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    public Stack() {
        this.elements = new Object[DEFAULT_INITIAL_CAPACITY];
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
        elements[size] = null; // 다 쓴 객체 참조 해제
        return result;
    }

    private void ensureCapacity() {
        if (elements.length == size) {
            elements = Arrays.copyOf(elements, 2 * size + 1);
        }
    }
}
```

**clone 메서드가 단순히 super.clone의 결과를 그대로 반환한다면 어떻게 될까?**

1. clone 메서드가 반환한 객체는 원본 객체와 같은 배열을 참조한다.
2. 이러한 경우, 복제본을 수정할 경우 원본도 같이 수정되어 불변식을 해친다는 이야기다.

그렇다면, 어떻게 해결해야 할까?

- Stack 클래스의 하나뿐인 생성자를 호출한다면 이러한 상황은 절대 일어나지 않는다.
- `clone` 메서드는 사실상 생성자와 같은 효과를 낸다는 것을 기억하자.
    - clone은 원본 객체에 아무런 해를 끼치지 않는 동시에 복제된 객체의 불변식을 보장해야한다.

_가변 상태를 참조하는 클래스용 clone 메서드_

```java

@Override
public Stack clone() {
    try {
        Stack result = (Stack) super.clone();
        result.elements = elements.clone();
        return result;
    } catch (CloneNotSupportedException e) {
        throw new AssertionError();
    }
}
```

- 위와 같이 구현한다면, 우리가 원하는 방식대로 clone 메서드가 동작한다!
- 한편, elements 필드가 final 이었다면 새로운 값을 할당할 수 없기 때문에 작동하지 않을 것이다.
    - **Cloneable 아키텍처는 '가변 객체를 참조하는 필드는 final로 선언하라'는 용법과 충돌한다.**
    - 따라서, 복제할 수 있는 클래스를 만들기 위해서 일부 필드에서 final 한정자를 없애야하는 경우도 발생한다.

### 해시 테이블용 clone

- 해시 테이블 내부는 버킷들의 배열
- 각 버킷은 '키-값' 쌍을 담는 연결리스트의 첫 번째 엔트리를 참조
- 성능을 위해 `java.util.LinkedList` 대신 직접 구현한 경량 연결 리스트를 사용해서 해보자

```java
public class HashTable implements Cloneable {

    private Entry[] buckets = ...;

    private static class Entry {
        final Object key;
        Object value;
        Entry next;

        Entry(Object key, Object value, Entry next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    // ... 
}
```

Stack처럼 단순히 버킷 배열의 clone을 재귀적으로 호출해보자.

```java

@Override
public HashTable clone() {
    try {
        HashTable result = (HashTable) super.clone();
        result.buckets = buckets.clone();
        return result;
    } catch (CloneNotSupportedException e) {
        throw new AssertionError();
    }
}
```

- 위와 같이 구현할 경우, 복제본은 자신만의 버킷을 가지기는 하지만, 이 배열은 원본과 같은 연결 리스트를 참조함!!
- 이를 해결하기 위해 각 버킷을 구성하는 연결 리스트를 복사해야 한다.

_복잡한 가변 상태를 갖는 클래스용 재귀적 clone 메서드_

```java
public class HashTable implements Cloneable {

    private Entry[] buckets = ...;

    private static class Entry {
        final Object key;
        Object value;
        Entry next;

        Entry(Object key, Object value, Entry next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }

        Entry deepCopy() {
            return new Entry(key, value,
                    next == null ? null : next.deepCopy());
        }
    }

    @Override
    public HashTable clone() {
        try {
            HashTable result = (HashTable) super.clone();
            result.buckets = buckets.clone();

            // deepCopy 과정을 거침
            for (int i = 0; i < buckets.length; i++) {
                if (buckets[i] != null) {
                    result.buckets[i] = buckets[i].deepCopy();
                }
            }

            return result;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
```

- 위와 같이 `깊은 복사(deep copy)`를 지원하도록 보강한다.
- 재귀 호출의 문제로 인해 연결 리스트를 복제하는 방법으로는 그다지 좋은 방식은 아니다.
    - 잘못하면 Stackoverflow 발생의 위험도 존재하기 때문이다.
- 재귀 호출 대신 **반복자**를 써서 순회하는 방향으로 변경해보자.

```java
Entry deepCopy() {
    Entry result = new Entry(key, value, next);
    for (Entry p = result; p.next != null; p = p.next) {
        p.next = new Entry(p.next.key, p.next.value, p.next.next);
    }

    return result;
}
```

#### 복잡한 가변 객체를 복제하는 마지막 방법

1. super.clone을 호출하여 얻은 객체의 모든 필드를 초기 상태로 설정
2. 원본 객체의 상태를 다시 생성하는 고수준의 메서드 호출

이처럼 고수준 API를 사용하면 간단하고 제법 우아한 코드를 얻게 되지만, 저수준으로 처리할 때 보다 느리다는 단점이 존재한다.  
또한 Cloneable 아키텍처의 기초가 되는 필드 단위 객체 복사를 우회하기 때문에 전체 Cloneable 아키텍처와는 어울리지 않는 방식이긴 하다.

#### Cloneable 구현한 스레드 안전 클래스는 적절한 동기화가 필요!

- Object의 clone 메서드는 동기화를 신경 쓰지 않았다는 점을 기억하자.

### 주의할 점

1. 재정의될 수 있는 메서드를 clone 메서드에서 호출하지 않아야 한다.
    - 만약, clone이 하위 클래스에서 재정의한 메서드를 호출한다면, 하위 클래스는 복제 과정에서 원본과 복제본의 상태가 달라질 가능성이 높다.
    - 따라서, private, final 처럼 재정의할 수 없는 메서드만 호출해야 한다.
2. Object의 clone 메서드는 `CloneNotSupportedException` 예외가 발생한다고 선언했지만, 재정의한 메서드는 그렇지 않다!
    - public인 clone 메서드에서는 throws 절을 없애야 한다.
    - 검사 예외를 던지지 않아야 그 메서드를 사용하기 편할 것이기 때문이다.
3. 상속해서 쓰기 위한 클래스는 `Cloneable`을 구현해서는 안 된다.
    - 제대로 작동하는 clone 메서드를 구현하여 protected로 두고 CloneNotSupportedException을 던지게 하자.
    - 아니면, 아래의 코드처럼 해서 clone을 퇴화시켜놓자.

_clone 메서드를 퇴화시킨 클래스_

```java

@Override
protected final Object clone() throws CloneNotSupportedException {
    return new CloneNotSupportedException();
}
```

## 요약

- Cloneable을 구현하는 모든 클래스는 `clone` 메서드를 재정의하자!
    - 접근 제어자: public
    - 반환 타입: 해당 클래스 자기 자신
    - 가장 먼저 super.clone을 호출한 후 필요한 필드를 전부 적절히 수정 (깊은 복사가 필요한 경우도 존재)
- 기본 타입 필드와 불변 객체만 참조만 가지고 있는 클래스라면 해당 과정을 스킵하자.

### 다른 방식

- 이 모든 작업이 필요한 것일까?
- 복사 생성자와 복사 팩터리는 Cloneable/clone 방식보다 나은 면이 많다.
    1. 언어 모순적이고 위험천만한 객체 생성 매커니즘(생성자를 사용하지 않는)을 사용하지 않는다.
    2. 엉성하게 문서화된 규약에 기대지 않음, 정상적인 final 필드 용법과 충돌하지 않으며, 불필요한 검사 예외를 던지지도 않고, 형변환도 필요 없다!
    3. 게다가 해당 클래스가 구현한 '인터페이스' 타입의 인스턴스를 인수로 받을 수 있음!!


> 복제 기능은 생성자와 팩터리를 이용하는게 베스트 방식  
> 예외적으로 '배열'만은 clone 메서드 방식이 가장 깔끔하다.

#### 복사 생성자
- 단순히 자신과 같은 클래스의 인스턴스를 인수로 받는 생성자

```java
public Yum(Yum yum) {
    // ...
}
```


#### 복사 팩터리
- 복사 생성자를 모방한정적 팩터리

```java
public static Yun newInstance(Yum yum) {
    // ...
}
```

