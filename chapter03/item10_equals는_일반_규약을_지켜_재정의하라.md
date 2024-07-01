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

cis.

equals(str); // true
str.

equals(cis); // false
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

_ColorPoint.class_

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

**_Example_**

- 주어진 점이 (반지름이 1인) 단위 원 안에 있는지를 판별하는 메서드가 필요하다고 가정

_구현 코드_

```java
private static final Set<Point> unitCircle = Set.of(
        new Point(1, 0), new Point(0, 1),
        new Point(-1, 0), new Point(0, -1));

public static boolean onUnitCircle(Point p) {
    return unitCircle.contains(p);
}
```

- 동작은 하는 코드이지만, 아래와 같이 `CounterPoint`라는 클래스를 만들어 인스턴스 개수를 생성자에서 세보도록 하자.

_CounterPoint.java_

```java
public class CounterPoint extends Point {
    private static final AtomicInteger counter = new AtomicInteger();

    public CounterPoint(int x, int y) {
        super(x, y);
        counter.incrementAndGet();
    }

    public static int numberCreated() {
        return counter.get();
    }
}
```

- 과연 "Point의 하위 클래스는 정의상 여전히 Point이므로 어디서든 Point로써 활용이 될 수 있는가?"
- `CounterPoint`의 인스턴스를 onUnitCircle 메서드에 넘기면 false가 반환되는 이상한 코드가 되는 것이다.
- 이러한 문제는 `instaceof` 기반으로 올바르게 구현했다면 제대로 동작할 것이다.
- 구체 클래스의 하위 클래스에서 값을 추가하는 방법은 없지만, 괜찮은 우회 방법으로 `상속 대신 컴포지션`을 사용한느 것이 있다.

_상속 대신 컴포지션_

```java
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

// not extends Point!
public class ColorPoint {

    private final Point point;
    private final Color color;

    public CounterPoint(int x, int y, Color color) {
        this.point = new Point(x, y);
        this.color = Objects.requireNonNull(color);
    }

    /**
     * Returns the point-view of this color point.
     */
    public Point asPoint() {
        return this.point;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ColorPoint)) {
            return false;
        }

        ColorPoint cp = (ColorPoint) o;

        // point의 equals && color의 equals
        return cp.point.equals(point) && cp.color.equals(color);
    }
}
```

- 이러한 문제는 `java.sql.Timestamp`에서 발생한다.
    - `java.util.Date`를 확장한 후 `nanoseconds` 필드를 추가함!
    - 이는 equals의 대칭성을 위배하며, Date 객체와 한 컬렉션에 넣거나 서로 섞어 사용하면 문제가 발생할 수 있다.

#### 일관성

- 두 객체가 같다면 영원히 같아야 한다는 뜻이다.
    - 가변 객체는 비교 시점에 따라 서로 다를 수도 같은 수도 있지만, 불변 객체는 한 번 다르면 끝까지 달라야 한다.
- 클래스가 불변이든 가변이든 **equals의 판단에 신뢰할 수 없는 자원이 끼어들게 해서는 안된다!**
- 대표적인 예시로 `java.net.URL`의 equals는 URL과 매핑된 호스트의 IP 주소를 이용해서 비교한다.
    - 호스트 이름을 IP 주소로 변경하려면 네트워클르 통해야 하는데 그 결과가 항상 같다고 보장할 수 없다.

> equals는 항시 메모리에 존재하는 객체만을 사용한 결정적 계산만 수행해야 한다.

#### null-아님

- 모든 객체가 null과 같지 않아야 한다는 뜻이다.
- 수 많은 코드가 입력이 null인지를 확인해 자신을 보호한다. (나도 주로 이렇게 사용하는 줄 알았는데..)

```java

@Override
public boolean equals(Object o) {
    if (o == null) {
        return false;
    }
    // ...
}

```

- 하지만, 이러한 방식은 `instanceof`를 사용하여 입력 매개변수가 올바른 타입인지 검사하는 방법이 더 낫다.
- `instanceof`는 null이 들어올 경우 false를 반환하므로, null 검사를 따로 할 필요가 없다.

```java

@Override
public boolean equals(Object o) {
    if (!(o instanceof MyType)) {
        return false;
    }

    MyType mt = (MyType) o;
    // ...
}
```

## 양질의 equals 메서드를 구현하는 방법


#### 1. == 연산자를 사용해 입력이 자기 자신의 참조인지 확인

- 자기 자신이면 true
- 단순한 성능 최적화용으로, 비교 작업이 복잡한 상황에서 값어치를 할 것임

#### 2. instanceof 연산자로 입력이 올바른 타입인지 확인

- 이 때 올바른 타입은 equals가 정의된 클래스인 것이 보통이지만, 그 클래스가 구현한 특정 인터페이스가 될 수도 있음.
- 어떤 인터페이스는 자신을 구현한 클래스끼리도 비교할 수 있또록 equals 규약을 수정하기도 한다.
- Set, List, Map, Map.Entry 등이 여기에 해당

#### 3. 입력을 올바른 타입으로 형변환한다.

- 2번의 과정 후 형변환을 하기 때문에 `ClassCastException`이 발생하지 않는다.

#### 4. 입력 객체와 자기 자신의 대응되는 '핵심' 필드들이 모두 일치하는지 하나씩 검사한ㄷ.

- 위의 `ColorPoint`처럼 point 비교 && color 비교 &rarr; 모두 일치하면 true를 반환하도록


## 예외 상황

- `float`, `double`는 Float.compare(float, float), Double.compare(double, double)를 사용하자.
- 배열의 모든 원소가 핵심 필드라면 `Arrays.equals` 메서드들 중 하나를 사용하자.
- 또한, null도 정상 값으로 취급하는 참조 타입 필드라면, `Objects.equals(Object, Object)`로 비교해 `NPE`를 방지하자.

### 참고하면 좋은 것들

- 위의 `CaseInsensitiveSring` 예시처럼 비교하기 복잡한 필드인 경우 표준형을 저장해둔 후 표준형끼리 비교하면 훨씬 경제적이다.
  - 특히, 불변 클래스에 제격이며 가변 객체라면 값이 바뀔 때마다 표준형을 최신 상태로 갱신해줘야 한다.
- 어떤 필드를 먼저 비교하느냐가 `equals` 성능을 좌우하기도 한다.
  - 성능 최적화를 위해 비교 비용이 높은 필드는 마지막에 비교하자.

## 주의 사항
- equals를 재정의할 땐 `hashCode`도 반드시 재정의 하자.
- 너무 복잡하게 해결하려 들지 말자.
- Object 외의 타입을 매개변수로 받는 equals 메서드는 선언하지 말자. (`@Override`로 방지)
