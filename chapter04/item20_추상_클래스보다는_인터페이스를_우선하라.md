# 아이템 20. 추상 클래스보다는 인터페이스를 우선하라

자바는 `interface`, `abstract class`를 통해 다중 구현 메커니즘을 제공한다.

- Java 8부터 인터페이스도 **default method** 를 제공할 수 있다.
- 둘의 가장 큰 차이는 추상 클래스가 정의한 타입을 **구현하는 클래스**는 반드시 **추상 클래스의 하위 클래스가 되어야 한다는 점**이다.

## 인터페이스와 추상 클래스 차이

- 자바는 `단일 상속`만 지원하므로 **추상 클래스 방식**은 새로운 타입을 정의하는데 큰 규약이 따른다!
- 반면, 인터페이스는 선언한 메서드를 모두 정의하고, 규약을 잘 지킨 클래스라면 손쉽게 인터페이스를 구현해넣을 수 있다.

### 인터페이스의 장점

#### 1. **믹스인(mixin)** 정의에 안성맞춤

- 믹스인: 클래스가 구현할 수 있는 타입으로, 주 클래스의 타입은 아니지만 필요한 기능을 제공한다. (Comparable)
- 기존 클래스에 덧씌울 수 있다는 점에서 큰 장점을 가진다.

#### 2. **계층 구조가 없는** 타입 프레임워크를 만들 수 있다.

```java
public interface Singer {
    AudioClip sing(Song s);
}

public interface Songwriter {
    Song compose(int chartPosition);
}
```

Singer, Songwriter 모두를 구현하는 가수 클래스를 구현할 수도 있고, 둘을 모두 확장하는 인터페이스를 만들 수 있다는 것이다!

```java
public interface SingerSongwriter extends Singer, Songwriter {
    AudioClip strum();

    void actSensitive();
}
```

- 이렇게 만들어둔 인터페이스가 큰 도움이 될 수도 있다.
- 조합 폭발(combinatorial explosion)이라 부르는 조합의 수가 늘어나는 문제를 해결할 수 있다.

#### 3. 래퍼 클래스 관용구와 함께 사용하면 기능을 향상시키는 안전하고 강력한 수단이 된다.

타입을 추상 클래스로 정의해두면, 그 타입에 기능을 추가하는 방법은 상속뿐이다. 이렇게 **상속해서 만든 클래스**는 래퍼 클래스보다 활용도가 떨어지고 깨지기는 더 쉽다.

_default method 장점_

- 구현 방법이 명백한 것이 있다면, 일감을 덜 수 있다.
- 사용했을 경우 `@implSpec` 태그를 통해 문서화 하자.

_default method 제약_

- equals, hashCode 등 Object 메서드는 default method로 제공해서는 안된다.
- 인스턴스 필드를 가질 수 없다.
- public 이 아닌 정적 멤버도 가질 수 없다. (`private static method`는 예외)
- 사용자가 만들지 않은 인터페이스에는 추가할 수 없다.

### 추상 골격 구현 클래스 (skeletal implementation)

인터페이스와 추상 골격 구현 클래스와 함께 사용하면, 인터페이스와 추상 클래스의 장점을 모두 취할 수 있다.

1. 인터페이스: 타입을 정의하고, 필요하면 default method 몇 개도 함께 제공
2. 골격 구현 클래스: 나머지 메서드들까지 구현

이러한 방법을 **템플릿 메서드 패턴**이라고 한다.

#### 관례

인터페이스 이름이 _Interface_라면 그 골격 구현 클래스의 이름은 _AbstractInterface_로 짓는다.

- AbstractCollection, AbstractSet, AbstractList 등

_골격 구현을 사용해 완성한 구체 클래스_

```java
static List<Integer> intArrayList(int[] a) {
    Objects.requireNonNull(a);

    return new AbstractList<>() {

        @Override
        public Integer get(int i) {
            return a[i];
        }

        @Override
        public Integer set(int i, Integer val) {
            int oldVal = a[i];
            a[i] = val;
            return oldVal;
        }

        @Override
        public int size() {
            return a.length;
        }
    };
}
```

> - int 배열을 받아 Integer 인스턴스의 리스트 형태로 보여주는 어댑터이기도 하다.
> - int, Integer 인스턴스 사이의 변환 (박싱, 언박싱) 때문에 성능상 좋지는 않다.
> - 또한, 익명 클래스(anonymous class) 형태를 사용했음을 주목하자.


골격 구현 클래스는 추상 클래스처럼 구현을 도와주는 동시에, 추상 클래스로 타입을 정의할 때 따라오는 심각한 제약에서 자유롭다는 점을 기억하자.

- 구조상 골격 구현을 확장하지 못하는 처지라면 인터페이스를 직접 구현해야 한다.
- 인터페이스를 구현한 클래스에서 해당 골격 구현을 확장한 private 내부 클래스를 정의하고, 각 메서드 호출을 내부 클래스의 인스턴스에 전달하는 방식으로 골격 구현 클래스를 우회적으로 이용도 가능
    - `시뮬레이트한 다중 상속(simulated multiple inheritance)` 패턴이라고 한다.
    - 다중 상속의 많은 장점을 제공하는 동시에 단점은 피하게 해준다.

### 골격 구현 작성

1. 인터페이스에 다른 메서드들의 구현에 사용되는 기반 메서드 선정
2. 기반 메서드들을 사용해 직접 구현할 수 있는 메서드를 모두 디폴트 메서드로 제공 (Object method 제외!!)
3. 만약, 인터페이스의 메서드 모두가 기반 메서드와 디폴트 메서드가 된다면 골격 구현 클래스는 별도로 만들 이유는 없다.
4. 기반 메서드나 디폴트 메서드로 만들지 못한 메서드가 남아 있다면, 골격 구현 클래스를 만들어 남은 메서드 작성
    - 필요한 경우, public 아닌 필드와 메서드를 추가해도 된다.

_골격 구현 클래스 (Map.Entry)_

```java
public abstract class AbstractMapEntry<K, V>
        implements Map.Entry<K, V> {

    // TODO: 변경 가능한 엔트리는 이 메서드를 반드시 재정의!
    @Override
    public V setValue(V value) {
        throw new UnsupportedOperationException();
    }

    // Map.Entry.equals의 일반 규약을 구현한다.
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Map.Entry)) {
            return false;
        }

        Map.Entry<?, ?> e = (Map.Entry) o;
        return Objects.equals(e.getKey(), getKey())
                && Objects.equals(e.getValue(), getValue());
    }

    // Map.Entry.hashCode의 일반 규약을 구현한다.
    @Override
    public int hashCode() {
        return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
    }

    @Override
    public String toString() {
        return getKey() + "=" + getValue();
    }
}
```

골격 구현은 기본적으로 '상속해서 사용하는 것을 가정'하기 때문에 설계 및 문서화 지침을 모두 따라야 한다.

- **단순 구현(simple implementation)** 은 골격 구현의 작은 변종으로, `AbstractMap.SimpleEntry` 가 좋은 예시이다.

### 핵심 정리

- 일반적으로, 다중 구현용 타입으로는 추상 클래스보다 인터페이스가 적합하다.
- 복잡한 인터페이스면 구현하는 수고를 덜어주는 `골격 구현`을 함께 제공하는 방법을 꼭 고려해보자.
- 이러한 골격 구현은 '가능한 한' 인터페이스의 디폴트 메서드로 제공하여 그 인터페이스를 구현한 모든 곳에서 활용할 수 있게 해야 한다.
- '가능한 한': 인터페이스에 걸려 있는 구현상의 제약으로 인해 골격 구현을 추상 클래스로 제공하는 경우가 더 흔하기 때문