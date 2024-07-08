# 아이템 14. Comparable을 구현할지 고려하라

`Comparable` 인터페이스에 존재하는 `compareTo` 메서드를 알아보자.

- **compareTo**는 Object 메서드가 아니다!
- `equals`와는 다르게 단순 동치성 비교에 더해 **순서까지 비교**할 수 있으며, **제네릭**하다!

Comparable을 구현한 객체들의 배열은 다음과 같이 손쉽게 정렬할 수 있다.

```java
Arrays.sort(arr);

// 알고리즘 문제를 풀면서 많이 봤을 것이다!
```

또한, 검색, 극단값 계산, 자동 정렬되는 **컬렉션** 관리도 역시 쉽게 할 수 있다.

_WordList.java_

```java
public class WordList {
    public static void main(String[] args) {
        Set<String> set = new TreeSet<>();
        Collections.addAll(set, args);
        System.out.println(set);
    }
}
```

위 코드를 실행할 때 명령줄 인수들을 정렬해서 정렬된 값을 보여줄 것이다.

- 왜? `String`은 `Comparable`을 구현하였기 때문!

> Comparable 구현은 이 인터페이스를 활용하는 수 많은 제네릭 알고리즘과 컬렉션의 힘을 누릴 수 있다는 것이다.  
> 알파벳, 숫자, 연대 같이 **순서가 명확한 값 클래스를 작성한다면** 반드시 `Comparable` 인터페이스를 구현하자.

### compareTo 메서드의 일반 규약

_Comparable.java_

```java
public interface Comparable<T> {
    int compareTo(T t);
}
```

```markdown
이 객체와 주어진 객체의 순서를 비교한다.
이 객체가 주어진 객체보다 작으면 음의 정수를, 같으면 0을, 크면 양의 정수를 반환한다.
비교할 수 없다면 'ClassCaseException' 예외를 던진다.

다음 설명에서 sgn(표현식) 표기는 수학에서 말하는 부호 함수(signum function)를 뜻하며, 표현식의 값이 음수, 0, 양수일 때 -1, 0, 1을 반환하도록 정의했다.

- Comparable을 구현한 클래스는 모든 x, y에 대해 sgn(x.compareTo(y)) == -sgn(y.compareTo(x))여야 한다.
    - 따라서 x.compareTo(y)는 y.compareTo(x)가 예외를 던질 때에 예외를 던져야 한다.
- Comparable을 구현한 클래스는 추이성을 보장해야 한다.
    - 즉, (x.compareTo(y) > 0 && y.compareTo(z) > 0) 이면 x.compareTo(z) > 0 이다.
- Comparable을 구현한 클래스는 모든 z에 대해 x.compareTo(y) == 0이면 sgn(x, compareTo(z)) == sgn(y.compareTo(z)) 다.
- 필수는 아니지만 꼭 지키는게 좋은 것: (x.compareTo(y) == 0) == (x.equals(y))
    - Comparable을 구현하고 이 권고를 지키지 않는 모든 클래스는 그 사실을 명시해야 한다.
    - "주의: 이 클래스의 순서는 equals 메서드와 일관되지 않다."  
```

- equals 처럼 모든 객체에 대해 전역 동치관계를 부여하는 것이 아닌, 타입이 다른 객체를 신경쓰지 않아도 된다. (ClassCastException 예외를 던지면 됨)
- compareTo 규약을 지키지 못하면 비교를 활용하는 클래스와 어울리지 못한다.
    - 컬렉션: `TreeSet`, `TreeMap`
    - 유틸리티 클래스: `Collections`, `Arrays`

---

- 규약 1, 2, 3번: compareTo 메서드로 수행하는 동치성 검사는 반사성, 대칭성, 추이성을 충족해야 한다.
    - equals 규약과 동일하며, 주의사항도 똑같음!
    - 기존 클래스를 확장한 구체 클래스에서 새로운 값 컴포넌트를 추가했다면, 지킬 방법이 없다.
    - 우회법: 확장 대신 독립된 클래스를 만들어, 원래 클래스의 인스턴스를 가리키는 필드를 두자. 그 후 내부 인스턴스를 반환하는 '뷰' 메서드를 제공하면 된다.
- 마지막 규약: compareTo 메서드로 수행한 동치성 테스트 결과가 equals와 같아야 한다는 것이다.
  - 필수는 아니지만 꼭 지키길 권장한다.
  - 두 메서드의 결과가 일관되지 않은 클래스라도 동작은 하겠지만, 정렬된 컬렉션에 넣으면 해당 컬렉션이 구현한 인터페이스(Collection, Set, Map)에 정의된 동작과 엇박자를 낼 것이다.

마지막 규약을 지키지 않은 BigDecimal 클래스를 예로 생각해보자.

- HashSet 인스턴스에 `new BigDecimal("1.0")`과 `new BigDecimal("1.00")`을 차례로 추가한다.
  - 이 때 두 객체는 equals 메서드로 비교하면 서로 다르기 때문에 HashSet이 원소를 2개를 가지게 된다.
- 하지만, TreeSet을 사용하게 된다면, 원소 1개만 갖게 된다.
  - compareTo 메서드로 비교할 때는 인스턴스가 동일하다고 나오기 때문이다.

```java
public class CompareTest {

  public static void main(String[] args) {
    BigDecimal val1 = new BigDecimal("1.0");
    BigDecimal val2 = new BigDecimal("1.00");

    HashSet<BigDecimal> hashSet = new HashSet<>();
    hashSet.add(val1);
    hashSet.add(val2);

    TreeSet<BigDecimal> treeSet = new TreeSet<>();
    treeSet.add(val1);
    treeSet.add(val2);

    System.out.println("Hash Set Size : " + hashSet.size());
    System.out.println("Tree Set Size : " + treeSet.size());
  }
}

/*
[result]
Hash Set Size : 2
Tree Set Size : 1
 */
```

> compareTo 메서드는 각 필드가 **동치**인지 비교하는 것이 아닌, 그 **순서**를 비교한다.

- Comparable을 구현하지 않는 경우, 비교자(Comparator)를 대신 사용한다.
  - 비교자는 자바에서 제공하는 것을 사용하거나, 직접 만들면 된다.

---

### 정수 기본 타입 필드 비교

- compareTo 메서드에서 관계 연산자 <와 >를 사용하는 이전 방식은 거추장스럽고 오류를 유발하니 추천하지 않는다.
- 클래스에 핵심 필드가 여러개인 경우, '비교 순서'가 중요하다.
  - 가장 핵심적인 필드부터 비교해 나가며, 순서가 결정되면 거기서 끝내라.

_PhoneNumber 클래스용 compareTo 메서드 구현_
```java
// areaCode, prefix, lineNum 필드 존재

public int compareTo(PhoneNumber pn) {
    int result = Short.compare(this.areaCode, pn.areaCode);     // 가장 중요한 필드

    if (result == 0) {
        result = Short.compare(this.prefix, pn.prefix);         // 두 번째로 중요한 필드
        if (result == 0) {
            result = Short.compare(this.lineNum, pn.lineNum);   // 세 번째로 중요한 필드
        }
    }
    return result;
}
```

- 자바 8에서는 Comparator 인터페이스가 일련의 비교자 생성 메서드(comparator construction method)와 팀을 꾸려 메서드 연쇄 방식으로 비교자를 생성할 수 있게 되었다.
  - 간결하지만, 약간의 성능 저하가 있다는 점을 유의하자.

_비교자 생성 메서드를 활용한 비교자_
```java
import java.util.Comparator;

import static java.util.Comparator.comparingInt;

public class PhoneNumber implements Comparable<PhoneNumber> {

  private static final Comparator<PhoneNumber> COMPARATOR
          = comparingInt((PhoneNumber pn) -> pn.areaCode)
          .thenComparing(pn -> pn.prefix)
          .thenComparing(pn -> pn.lineNum);

  private int areaCode;
  private int prefix;
  private int lineNum;

  @Override
  public int compareTo(PhoneNumber pn) {
    return COMPARATOR.compare(this, pn);
  }
}

// 깔끔하네,,
```

'값의 차'를 기준으로 첫 번재 값이 두 번째 값보다 작으면 음수를, 두 값이 같으면 0을, 첫 번째 값이 크면 양수를 반환하는 compareTo나 compare 메서드와 마주할 것이다. 

```java
import java.util.Comparator;

static Comparator<Object> hashCodeOrder = new Comparator<>() {
    @Override
    public int compare(Object o1, Object o2) {
        return o1.hashCode() - o2.hashCode();
    }
};
```

- **이 방식은 사용하지 말자**
  - '부동소수점 계산 방식'에 또한 월등히 빠르지도 않을 것이다.

대신 아래의 방법 두 가지중 하나를 사용하자.

_정적 compare 메서드를 활용한 비교자_
```java
import java.util.Comparator;
static Comparator<Object> hashCodeOrder = new Comparator<>() {
    @Override
    public int compare(Object o1, Object o2) {
        return Integer.compare(o1.hashCode(), o2.hashCode());
    }
};
```

_비교자 생성 메서드를 활용한 비교자_
```java
import java.util.Comparator;static Comparator<Object> hashCodeOrder =
        Comparator.comparingInt(o -> o.hashCode());
```

## 핵심 정리

- 순서를 고려해야 하는 값 클래스를 작성한다면 `Comparable` 인터페이스를 구현하여 정렬, 검색, 비교 기능을 제공하는 컬렉션과 어우러지도록 하자.
- compareTo 메서드의 필드 값 비교할 때 <, >와 같은 비교자 대신 **static method** `compare`를 사용하거나, **Comparator 인터페이스가 제공**하는 `비교자 생성 메서드`를 사용하자!!