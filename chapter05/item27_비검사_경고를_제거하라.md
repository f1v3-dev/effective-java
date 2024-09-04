# 아이템 27. 비검사 경고를 제거하라

제네릭을 사용하기 시작하면 수많은 컴파일러 경고가 발생할 것이다.

제네릭에 익숙해질수록 마주치는 경고 수는 줄겠지만, 새로 작성한 코드가 한 번에 깨끗하게 컴파일되리라 기대하지는 말자.

## 비검사 경고

**잘못된 코드**

```java
Set<Lark> exaltation = new HashSet();
```

(컴파일 시 javac 에 `-Xlint:uncheck` 옵션을 추가)

위의 코드를 컴파일 할 경우, 경고 메시지가 다음과 같이 출력이 될 것이다.

```shell
Venery.java:4: warning: [unchecked] unchecked conversion
    Set<Lark> exaltation = new HashSet();
                           ^
    required: Set<Lark>
    found:    HashSet
```

- 컴파일러가 알려준 대로 수정하면 경고가 사라진다.
- 자바 7부터 지원하는 다이아몬드 연산자(`<>`) 를 사용해도 된다.

이러한 방식으로, **할 수 있는 한 모든 비검사 경고를 제거하라.** 타입 안전성을 보장이 되는 것이 좋다. (런타임에 ClassCastException 발생 X)

### @SuppressWarnings("unchecked)

경고를 제거할 수 없지만, 타입이 안전하다고 확신할 수 있다면 `@SuppressWarnings("unchecked")` 어노테이션을 달아 경고를 숨기자.

- 항상 가능한 한 좁은 범위에 적용하는 것이 좋다.
- 그 경고를 무시해도 안전한 이유를 항상 주석으로 남기자.

**지역변수를 추가해 경고 범위를 좁히는 방법**

```java

public <T> T[] toArray(T[] a) {
    if (a.length < size) {
        // 생성한 배열과 매개변수로 받은 배열의 타입이 모두 T[]로 같으므로
        // 올바른 형변환이다.
        @SuppressWarnings("unchecked") T[] result
                = (T[]) Arrays.copyOf(elements, size, a.getClass());
        return result;
    }

    System.arraycopy(elements, 0, a, 0, size);
    if (a.length > size) {
        a[size] = null;
    }
    return a;
}
```

## 핵심 정리

- 비검사 경고는 중요하니 무시하지 말자.
    - 런타임에 `ClassCastException` 이 발생할 수 있다.
- 만약 경고를 없앨 방법이 없다면, 그 코드가 타입 안전함을 증명하고 범위를 좁혀 `@SuppressWarnings("unchecked")` 어노테이션을 사용하자.
- 그런 다음, 경고를 숨기기로 한 근거를 주석으로 남기자.



