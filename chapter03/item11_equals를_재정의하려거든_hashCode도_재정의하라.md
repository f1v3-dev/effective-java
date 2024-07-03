# 아이템 11. equals를 재정의하려거든 hashCode도 재정의하라

- `equals`를 재정의한 클래스 모두에서 `hashCode`도 재정의해야한다.
- hashCode 일반 규약을 어기게 되어 컬렉션의 원소로 사용될 때 문제가 발생한다.

_Object 명세에서 발췌한 규약_

> - equals 비교에 사용되는 정보가 변경되지 않았다면, 애플리케이션이 실행되는 동안 그 객체의 hashCode 메서드는 몇 번을 호출해도 일관되게 항상 같은 값을 반환해야 한다. 단, 애플리케이션을 다시 실행한다면 이 값이 달라져도 상관없다.
> - equals(Object)가 두 객체를 같다고 판단했다면, 두 객체의 hashCode는 똑같은 값을 반환해야 한다.
> - equals(Object)가 두 객체를 다르다고 판단했더라도, 두 객체의 hashCode가 서로 다른 값을 반환할 필요는 없다. 단, 다른 객체에 대해서는 다른 값을 반환해야 해시테이블의 성능이 좋아힌다.

**hashCode**를 재정의 하지 않았을 때 문제가 발생되는 조항은 두 번째로, 논리적으로 같은 객체는 같은 해시코드를 반환해야 한다.

예시로, item 10의 `PhoneNumber` 클래스를 HashMap 원소로 사용한다고 해보자.

```java
Map<PhoneNumber, String> m = new HashMap<>();
m.put(new PhoneNumber(707, 867, 5309), "seungjo");
```

이 상태에서 아래의 코드를 실행한다면 어떤 값이 나올지 추론해보자.

```java
m.get(new PhoneNumber(707, 867, 5309));
```

- **seungjo**가 반환될 것으로 예상했으나, `null` 이 반환된다.
- 두 `PhoneNumber` 객체는 논리적으로 같은 객체이지만, `hashCode`를 재정의하지 않아 논리적 동치인 두 객체가 서로 다른 해시코드를 반환
- 따라서, null을 반환하게 되는 것이다.

### hashCode 재정의하기

- 최악의 방법으로는 동치인 모든 객체에서 같은 해시코드를 반환하게 하는 방식이다.
- 이 방식의 경우, 해시테이블의 버킷 하나에 모든 객체가 담겨 평균 수행 능력이 O(n)이 되는 최악의 상황이다.

_최악의 (하지만 적법한) hashCode 구현_

```java
@Override
public int hashCode() {
    return 42;
}
```

좋은 해시 함수라면 서로 다른 인스턴스에 다른 해시코드를 반환한다. (hashCode 세 번째 규약이 요구하는 속성)

이상적인 해시 함수는 주어진 인스턴스들을 32비트 정수 범위에서 균일하게 분배해야 한다.

#### 좋은 hashCode 작성하는 요령

1. int 변수 result 선언 후, 값 c로 초기화 한다.
   - 이 때, c는 해당 객체의 첫 번째 핵심 필드를 단계 2.a 방식으로 계산한 해시코드다.
   - (핵심 필드: equals 비교에 사용되는 필드)
2. 해당 객체의 나머지 핵심 필드 f 각각에 대해 다음 작업을 수행
   - a. 해당 필드의 해시코드 c 계산
      - 기본 타입 필드라면, Type.hashCode(f)를 수행 (Type: 해당 기본 타입의 박싱 클래스)
      - 참조 타입 필드면서 이 클래스의 equals 메서드가 이 필드의 equals를 재귀적으로 호출한다면, 이 필드의 hashCode를 재귀적으로 호출한다. 계산이 더 복잡해질 것 같으면, 필드의 표준형을 만들어 표준형의 hashCode를 호출한다. 필드 값이 null이면 0을 사용
      - 필드가 배열이라면, 핵심 원소 각각을 별도 필드처럼 다룬다. 이상의 규칙을 재귀적으로 적용해 각 핵심 원소의 해시코드를 계산한 다음, 단계 2.b 방식으로 갱신한다. 배열에 핵심 원소가 하나도 없다면 단순 상수 (주로, 0)를 사용한다. 모든 원소가 핵심 원소라면 Arrays.hashCode 사용
   - b. 단계 2.a에서 계산한 해시코드 c로 result 갱신 (result = 31 * result + c);
3. return result;

- 다른 필드로부터 계산해낼 수 있는 파생 필드는 해시코드 계산에서 제외해도 된다.
- equals 비교에 사용되지 않은 필드는 **반드시** 제외해야 한다.

> 31을 곱하는 이유: 31을 곱하는 이유는 홀수이면서 소수이기 때문에 해시코드를 만들 때 곱셈과 덧셈을 사용하면서 해시코드를 만들 때 발생하는 충돌을 최소화할 수 있다.


전형적인 hashCode 메서드를 PhoneNumber 클래스에 적용해보자.

```java
@Override
public int hashCode() {
    int result = Short.hashCode(areaCode);
    result = 31 * result + Short.hashCode(prefix);
    result = 31 * result + Short.hashCode(lineNum);
    return result;
}
```

다른 방식으로 Google의 Guava 라이브러리를 사용해보자.

- `Objects` 클래스는 임의의 개수만큼 객체를 받아 해시코드를 계싼해주는 정적 메서드 **hash**를 제공
- 코드는 한 줄로 줄었지만, 속도면에서는 더 느리다는 점에서 아쉽다.

_Guava - Objects.hash 활용 코드_
```java
@Override
public int hashCode() {
    return Objects.hash(lineNum, prefix, areaCode);
}
```

- 클래스가 불변이고 해시코드를 계산하는 비용이 크다면, 캐싱하는 방식을 고려해야 한다.
- - 이 타입의 객체가 주로 해시의 키로 사용될 것 같다면, 인스턴스가 만들어질 때 해시코드를 계산해둬야 한다.
- 해시의 키로 사용되지 않는 경우라면 hashCode가 처음 불릴 때 계산하는 지연 초기화(lazy initialization) 전략도 있긴 하다.

_해시코드를 지연 초기화하는 hahsCode 메서드 - 스레드 안정성도 고려해야하는 단점 존재_

```java
private int hashCode;

@Override
public int hashCode() {
    int result = hashCode;
    if (result == 0) {
        result = Short.hashCode(areaCode);
        result = 31 * result + Short.hashCode(prefix);
        result = 31 * result + Short.hashCode(lineNum);
        hashCode = result;
    }
}
```

> **성능을 높인다고 해시코드를 계산할 때 핵심 필드를 생략하는 일은 일어나면 안된다.**

- hashCode가 반환하는 값의 생성 규칙을 API 사용자에게 자세히 공표하지 말자. 그래야 클라이언트가 이 값에 의지하지 않게 되고, 추후에 계산 방식을 바꿀 수 있다.