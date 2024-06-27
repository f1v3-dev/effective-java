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

#### 추이성
- a == b, b == c이면 a == c여야 한다는 뜻
- 아래의 `Point` 클래스를 예시로 한 번 살펴보자

_Point.class
```java
public class Point {
    private final int x;
    private final int y;
    
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    @Override
  public boolean equals(Object o) {
        if (!(o instanceof Point)) {
            return false;
        }
        
        Point p = (Point) o;
        return p.x == x && p.y == y;
    }
}
```

- 위의 코드를 확장해서 점에 색상을 더해보자.

_ColorPoint.class
```java
public class ColorPoint extends Point {
    private final Color color;
    
    public ColorPoint(int x, int y, Color color) {
        super(x, y);
        this.color = color;
    }
    
    // ...
}
```

- equals 메서드를 그대로 둘 경우, Point의 equals를 사용하게 되어 색상 정보는 무시한 채 결과 값을 반환하게 될 것이다.
- 이러한 경우, 추이성을 만족시키지 못하게 된다.

_잘못된 코드 - 대칭성 위배_
```java
@Override
public boolean equals(Object o) {
    if (!(o instanceof ColorPoint)) {
        return false;
    }
    return super.equals(o) && ((ColorPoint) o).color == color;
}
```

- Point를 ColorPoint에 비교한 결과와 그 둘을 바꿔 비교한 결과가 다를 수 있음.

```java
Point p = new Point(1, 2);
ColorPoint cp = new ColorPoint(1, 2, Color.RED);
```

- p.equals(cp) == true, cp.equals(p) == false
- `ColorPoint.equals`가 Point와 비교할 때 색상을 무시하면 해결이 될까?

_잘못된 코드2 - 추이성 위배_
```java
@Override
public boolean equals(Object o) {
    if (!(o instanceof Point)) {
        return false;
    }
    
    // o가 일반 Point면 색상 무시 후 비교
    if (!(o instanceof ColorPoint)) {
        return o.euqlas(this);
    }
    
    // o가 ColorPoint라면 색상까지 비교
    return super.equals(o) && ((ColorPoint) o).color == color;
}
```

- 위 방식에서 대칭성은 유지했지만, 추이성은 위배되고 있다.

```java
ColorPoint p1 = new ColorPoint(1, 2, Color.RED);
Point p2 = new Point(1, 2);
ColorPoint p3 = new ColorPoint(1, 2, Color.BLUE);

// p1 == p2, p2 == p3 이지만 p1 != p3
```

- 객체 지향 언어의 동치관계에서 나타나는 근본적인 문제로, 무한 재귀에 빠질 위험도 존재함.
- **구체 클래스를 확장해 새로운 값을 추가하면서 equals 규약을 만족시킬 방법은 존재하지 않는다는 점을 기억하자.**

_리스코프 치환 원칙 위배 코드_
```java
@Override
public boolean equals(Object o) {
    if (o == null || o.getClass() != getClass()) {
        return false;
    }
    
    Point p = (Point) o;
    return p.x == x && p.y == y;
}
```

- 위의 코드는 같은 구현 클래스에서만 `true`를 반환한다.
- 하위 클래스에서 `equals`를 사용할 수 없게되니 리스코프 치환 원칙을 위배하게 된다.

