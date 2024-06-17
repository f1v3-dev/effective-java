# 아이템 6. 불필요한 객체 생성을 피하라

- 같은 기능의 객체를 매번 생성하는 것 보다는 재사용하는 편이 나을 때가 많다.
    - 재사용은 빠르고 세련된다.
    - 특히, 불변 객체인 경우 언제든 재사용할 수 있다.

## 불필요한 객체 생성의 예시

아래의 코드는 하지 말아야 할 극단적인 예시이다.

```java
String s = new String("bikini");
```

- 실행될 때 마다 새로운 String 인스턴스가 생성된다.
- 즉, `bikini` 라는 동일한 내용을 가진 String 인스턴스가 여러 개 생성될 수 있다는 것이다.

그러면 어떻게 개선해야될까?

```java
String s = "bikini";
```

- 이렇게 코드를 작성할 경우, `bikini` 값을 가지는 1개의 인스턴스를 재사용한다.

### 정적 팩터리 메서드를 사용하여 불필요한 객체 생성 피하기

- 생성자 대신 정적 팩터리 메서드를 제공하는 불변 클래스는 `정적 팩터리 메서드`를 사용하여 불필요한 객체 생성을 피할 수 있다.
    - Boolean(String) 생성자 대신 Boolean.valueOf(String) 팩터리 메서드를 사용
    - Java 9에서 생성자가 **deprecated API**로 지정되었다.
- 생성자는 호출할 때 마다 새로운 객체를 생성하지만, 팩터리 메서드는 그렇지 않다.

### 생성 비용이 비싼 객체를 재사용하기

- `비싼 객체`가 반복해서 필요하다면 캐싱하여 재사용하자.
    - 물론, 자신이 만드는 객체가 비싼 객체인지 매번 명확히 알 수는 없다.

_문자열이 유효한 로마 숫자인지 확인하는 메서드_

```java
static boolean isRomanNumeral(String s) {
    return s.matches("^(?=.)M*(C[MD]|D?C{0,3})"
            + "(X[CL]|L?X{0,3})(I[XV]|V?I{0,3})$");
}
```

- String.matches 메서드는 정규표현식으로 문자열 형태를 확인하는 가장 쉬운 방법
- 하지만, 성능이 중요한 상황에서 반복해 사용하기엔 적합하지 않음!
  - 메서드 내부에서 만드는 정규표현식용 Pattern 인스턴스는 한 번 쓰고 가비지 컬렉션 대상이 됨.
  - 또한 Pattern은 유한 상태 머신(finite state machine)을 만들기 때문에 인스턴스 생성 비용이 높다.

그렇다면, 성능을 개선하려면 어떻게 해야될까?
1. 필요한 정규표현식을 표현하는 Pattern 인스턴스를 클래스 초기화 과정에서 직접 생성하여 캐싱한다.
2. 나중에 isRomanNumeral 메서드가 호출될 때 마다 해당 인스턴스를 재사용한다.

직접 코드로 재현해보자.

```java
public class RomanNumeral {
    private static final Pattern ROMAN = pattern.compile(
            "^(?=.)M*(C[MD]|D?C{0,3})"
            + "(X[CL]|L?X{0,3})(I[XV]|V?I{0,3})$");
    
    static boolean isRomanNumeral(String s) {
        return ROMAN.matcher(s).matches();
    }
}
```

- isRomanNumeral 메서드가 빈번히 호출되는 상황에서는 성능 향상이 될 것이다.
- 코드도 명확히 어떤 인스턴스를 사용하는지 알 수 있어 코드의 의미가 훨씬 잘 드러난다.

하지만 단점도 존재한다는 점을 잊지말자.
- 만약, 빈번히 호출되지 않는다면? 극한의 상황으로 아예 사용되지 않는다면 생성 비용이 큰 객체를 굳이 만들어 둬야 할까?
- 이러한 문제를 지연 초기화(lazy initialization)로 해결할 수 있지만, 권장하지는 않는다.

#### 어댑터(뷰)

- 어댑터 : 실제 작업은 뒷단 객체에 위임하고 자신은 제2의 인터페이스 역할을 해주는 객체
- 뒷단 객체 외에는 관리할 상태가 없으므로 뒷단 객체 하나당 어댑터 하나씩만 만들어지면 충분하다.

우리가 주로 사용하는 `Map` 인터페이스의 `keySet` 메서드를 호출했다고 가정해보자.

```java
public static void main(String[] args) {
  Map<String, String> map = new HashMap<>();

  map.put("name", "seungjo");
  map.put("age", "25");
  map.put("language", "java");

  // 1번째 호출
  Set<String> first = map.keySet();
  System.out.println(first.hashCode());

  // 2번째 호출
  Set<String> second = map.keySet();
  System.out.println(second.hashCode());

  // first와 second 비교 
  System.out.println(first == second); // true
}

```

- `keySet` 메서드는 매번 새로운 Set 인스턴스를 생성하는 것이 아니라, Map 인스턴스에 대한 뷰를 반환한다.

#### Auto Boxing

- 불필요한 객체를 만들어내는 또 다른 예시
- 기본 타입과 그에 대응하는 박싱된 기본 타입의 구분을 흐려주지만, **완전히 없애주는 것은 아니다!**
- 성능에서 큰 문제가 될 수 있기 때문에 주의해야 한다.

```java
private static long sum() {
    Long sum = 0L;
    for (long i = 0; i <= Integer.MAX_VALUE; i++) {
        sum += i;
    }
    
    return sum;
} 
```

- long 타입인 i가 Long 타입인 sum에 더해질 때 마다 불필요한 인스턴스가 생성된다.
  - 약 2^31개의 인스턴스가..
- 단순히 sum의 타입을 long으로만 변경시켜 주면 해결된다.

> 박싱된 기본 타입보다는 기본 타입을 사용하고, 의도치 않은 오토박싱이 숨어들지 않도록 주의하자!


## 핵심 정리

- *객체 생성은 비싸니 피해야 된다*가 아니다.
  - **JVM**은 GC를 통해 객체 생성 및 회수하는 일에 크게 부담되지 않는다.
- 그렇다고 단순 객체 생성을 피하고자 '객체 풀'을 만들지는 말자.
  - DB Connection 같은 경우 생성 비용이 워낙 비싸니 재사용하는 편이 낫긴 하다.
  - 하지만, 오히려 코드를 헷갈리게하고, 메모리 사용량을 늘리고 성능을 떨어뜨릴 수 있따.
- "기존 객체를 재사용해야 한다면 새로운 객체를 만들지 마라"