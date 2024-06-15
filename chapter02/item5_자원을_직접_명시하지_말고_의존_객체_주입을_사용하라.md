# 아이템 5. 자원을 직접 명시하지 말고 의존 객체 주입을 사용하라

- 많은 클래스가 하나 이상의 자원에 의존한다. 이러한 관계는 어떻게 관리해야 할까?

아래의 맞춤법 검사기 클래스를 보자.

- 맞춤법 검사기는 사전(dictionary)에 의존
- 정적 유틸리티 클래스로 구현한 모습을 드물지않게 볼 수 있음.
- 이러한 경우 *유연하지 않고 테스트하기 어려움!*

**_정적 유틸리티를 잘못 사용한 예_**

```java
public class SpellChecker {

    private static final Lexicon dictionary = ...;

    private SpellChecker() {
    } // Class should not be instantiated.

    public static boolean isValid(Strin word) {
        // ...
    }

    public static List<String> suggestions(String typo) {
        // ...
    }
}
```

**_싱글턴을 잘못 사용한 예_**
```java
public class SpellChecker {
    
    private final Lexicon dictionary = ...;
    
    private SpellChecker(...) {
    }
    
    public static SpellChecker INSTANCE = new SpellChecker(...);
    
    public boolean isValid(String word) {
        // ...
    }
    
    public List<String> suggestions(String type) {
        // ...
    }
}
```

위의 두 방식에서 새로운 사전으로 바꾸려면 어떻게 해야될까?

- dictionary 필드에서 final을 제거하고, 다른 사전으로 교체하는 메서드를 추가할 수 있음
- 하지만, 어색하고 오류를 내기 쉬우며 멀티 스레드 환경에서는 쓸 수 없다.

**사용하는 자원에 따라 동작이 달라지는 클래스에는 정적 유틸리티 클래스나 싱글턴 방식이 적합하지 않다.**

### 의존 객체 주입

- 클래스가 여러 자원 인스턴스를 지원하며, 클라이언트가 원하는 자원을 사용하도록 하는 방식
- 흔히 사용하는 Spring Framework에서의 DI(Dependency Injection)가 이러한 방식을 사용한다.

한 번 코드를 변경해보자

```java
public class SpellChecker {
    private final Lexicon dictionary;

    public SpellChecker(Lexicon dictionary) {
        this.dictionary = Objects.requireNonNull(dictionary);
    }

    public boolean isValid(String word) {
        // ...
    }

    public List<String> suggestions(String typo) {
        // ...
    }
}
```

- 이제 우리는 자원이 몇 개든 의존 관계가 어떻든 상관없이 잘 작동하는 코드를 만들었다.
- 의존 객체 주입은 생성자, 정적 팩터리, 빌더 모두에 똑같이 응용할 수 있다.

### 생성자에 자원 팩터리를 넘겨주는 방식

> 팩터리(factory) : 호출할 때마다 특정 타입의 인스턴스를 반복해서 만들어주는 객체

- Java 8 - **Supplier\<T\> 인터페이스**가 팩터리를 표현한 완벽한 예
- Supplier를 입력으로 받는 메서드는 일반적으로 한정적 와일드 카드 타입을 사용하여 팩터리 타입 매개변수를 제한해야 한다.

```java
Mosaic create(Supplier<? extends Tile> tileFactory) { ... }
```

- 의존 객체 주입은 유연성과 테스트 용이성을 개선
- But, 의존성이 수천 개나 되는 큰 프로젝트에서는 코드를 어지럽게 만들 수 있다는 점을 주의
    - 의존 객체 주입 프레임워크를 사용하여 해소할 수 있기는 함.


---

## 정리

- 싱글턴과 정적 유틸리티 클래스를 잘못 사용하고 있지는 않은지 고민해보자.
  - 특히, 클래스가 내부적으로 하나 이상의 자원에만 의존한다.
  - 그 자원이 클래스 동작에 영향을 준다.
  - 이러한 경우, 싱글턴이나 정적 유틸리티 클래스는 사용하지 않는 것이 좋다.
  - 이 자원들을 클래스가 직접 만들게 해서는 안된다.
- 필요한 자원을 생성자에 넘겨주는 의존 객체 주입이라는 기법을 활용해보자!



