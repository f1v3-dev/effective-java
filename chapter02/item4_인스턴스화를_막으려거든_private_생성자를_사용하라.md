# 아이템 4. 인스턴스화를 막으려거든 private 생성자를 사용하라

- 단순히 정적 메서드(static method)와 정적 필드(static field)만을 담은 클래스가 필요한 경우가 있을 것이다
    - `java.lang.Math`, `java.util.Arrays`, `java.util.Collections` 등
- final 클래스와 관련한 메서드를 모아놓을 때도 사용
    - final 클래스를 상속해서 하위 클래스에 메서드를 넣는 것은 불가능하기 때문

### 주의할 점

- 정적 멤버만 담은 유틸리티 클래스는 새로운 인스턴스가 필요하지 않다.
    - 그저 제공한 정적 메서드와 정적 필드만을 사용하면 됨.
- 그렇다면, 새로운 인스턴스가 만들어지는 것을 막아야된다!
    - 생성자를 명시하지 않을 경우 컴파일러가 자동적으로 `default constructor`를 만든다는 점을 고려하자.
    - 그렇기 때문에 `private constructor`를 사용하여 클래스가 인스턴스화 되는 것을 막자.

> **추상 클래스(abstract class)** 로 만드는 것으로는 인스턴스화를 막을 수 없다. 그저 하위 클래스를 만들어 인스턴스화하면 그만이다. 
> 또한, 상속해서 쓰라는 뜻으로 오해할 수 있기 더 큰 문제가 될 수 있다.

**_example_**

```java
public class UtilityClass {
    private UtilityClass() {
        throw new AssertionError();
    }

    public static void doSomeThing() {
        // do something
    }
}
```