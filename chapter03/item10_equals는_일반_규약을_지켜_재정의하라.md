# 아이템 10. equals는 일반 규약을 지켜 재정의하라

- equals는 재정의하기 쉬워 보이지만, 곳곳에 함정이 있어 자칫하면 큰 오류를 발생시킬 수 있다.
- 위와 같은 문제를 회피하는 가장 쉬운 방법은 **재정의하지 않는 것**이다.

### 아래의 상황과 같다면 재정의 하지 말자

#### 1. 각 인스턴스가 본질적으로 고유하다

- `Thread`와 같이 값을 표현하는게 아닌 동작하는 개체를 표현하는 클래스가 여기에 해당

#### 2. 인스턴스의 '논리적 동치성(logical equality)'을 검사할 일이 없다.

- `java.util.regex.Pattern`은 equals를 재정의해서 두 Pattern 인스턴스가 같은 정규표현식을 나타내는지 '논리적 동치성'을 검사하는 방법도 있다.
- 하지만, 설계자는 이를 원하지 않거나 필요하지 않는다고 판단할 수 있다.
    - 후자의 경우 Object의 기본 equals만으로 해결이 가능

#### 3. 상위 클래스에서 재정의한 equals가 하위 클래스에도 딱 들어맞는다.

- AbstractSet의 equals를 대부분의 Set 구현체가 사용
- AbstractList의 equals를 대부분의 List 구현체가 사용
- AbstractMap의 equals를 대부분의 Map 구현체가 사용

#### 4. 클래스가 private이거나 package-private이고 equals 메서드를 호출할 일이 없다.

- 만약 위험을 철저히 회피하는 스타일이라 equals가 실수로라도 호출되는 것을 막고 싶다면 아래처럼 구현하자.

```java

@Override
public boolean equals(Object o) {
    throw new AssertionError(); // DO NOT CALL THIS METHOD!
}
```

### equals를 재정의해야 하는 경우

객체의 식별성(두 객체가 물리적으로 같은지)이 아닌 **논리적 동치성**을 확인해야 하는 경우

- 주로 **값 클래스**들이 여기 해당한다.
    - Integer, String 처럼 값을 표현하는 클래스
- 논리적 동치성을 비교하도록 재정의 한다면 개발자의 기대처럼 동작할 것이다.
- 또한, Map의 키와 Set의 원소로 사용할 수 있게 된다.

#### 예외적 상황

값 클래스라도 값이 같은 인스턴스가 둘 이상 만들어지지 않음을 보장하는 인스턴스 통제 클래스라면 equals를 재정의하지 않아도 된다.

- Enum, Singleton 등

## equals 메서드 재정의 규약

> equals 메서드는 동치관계(equivalence relation)를 구현하며, 다음을 만족한다.
> - **반사성(reflexivity)** : null이 아닌 모든 참조 값 x에 대해, x.equals(x) = true
> - **대칭성(symmetry)** : null이 아닌 모든 참조 값 x, y에 대해 x.equals(y) = true면 y.equals(x) = true이다.
> - **추이성(transitivity)** : null이 아닌 모든 참조 값 x, y, z에 대해 x.equals(y) = true이고, y.equals(z)도 true이면 x.equals(z)도 true이다.
> - **일관성(consistency)** : null이 아닌 모든 참조 값 x, y에 대해, x.equals(y)를 반복해서 호출하면 항상 true 또는 false를 반환하다.
> - **null-아님** : null이 아닌 모든 참조 값 x에 대해, x.equals(null) = false

### 동치 관계 (equivalence relation)

집합을 서로 같은 원소들로 이뤄진 부분집합으로 나누는 연산을 의미한다.

- 이 부분 집합을 동치 클래스(동치류)라고 한다.
- equals 메서드가 쓸모 있으려면 모든 원소가 같은 동치류에 속한 어떤 원소와도 서로 교환할 수 있어야 한다.

#### 반사성

- 단순히 말하면 객체는 자기 자신과 같아야 한다는 뜻
- 이 요건을 어긴 클래스는 해당 클래스의 인스턴스를 컬렉션에 넣은 다음 `contains` 메서드를 호출하면 false가 반환될 것이다.
- 사실, 해당 조건을 일부러 어기는 경우가 아니면 만족시키기 못하기가 더 어렵다.

#### 대칭성

- 두 객체는 서로에 대한 동치 여부에 똑같이 답해야 한다는 뜻이다.
- 대소문자를 구별하지 않는 문자열을 구현한 다음 클래스를 에로 살펴보자

```java
public final class CaseInsensitiveString {

  private final String str;

  public CaseInsensitiveString(String str) {
    this.str = Objects.requireNonNull(str);
  }

  // 대칭성 위배
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof CaseInsensitiveString) {
      return str.equalsIgnoreCase(((CaseInsensitiveString) obj).str);
    }

    if (obj instanceof String) {
      return str.equalsIgnoreCase((String) obj);
    }

    return false;
  }
}

```

- `equalsIgnoreCase`를 사용했기 때문에 대소문자를 무시하는 상황이 만들어짐.
- 이러한 경우 아래의 코드에서 대칭성에 맞는지 판단해보자.

```java
CaseInsensitiveString cis = new CaseInsensitiveString("JeongSeungjo");
String str = "jeongseungjo";

cis.equals(str); // true
str.equals(cis); // false
```

- 한 쪽에서는 true, 한 쪽에서는 false가 나오는 기이한 현상이 발생한다.
- List<CaseInsensitiveString>에 cis를 추가한 다음 contains를 호출하여도 어떻게 반응할지 모른다.

