# 아이템 18. 상속보다는 컴포지션을 사용하라

> 상속은 코드를 재사용하는 강력한 수단이지만, 항상 최선은 아니다.  
> 여기서 말하는 상속은 구현 상속(클래스가 다른 클래스를 확장하는 것)을 말한다.

## 상속의 문제점

### 1. 메서드 호출과 달리, 상속은 캡슐화를 깨뜨린다.

- 상위 클래스가 어떻게 구현되느냐에 따라 하위 클래스의 동작에 이상이 생길 수 있다.
- 이러한 영향으로 인해, 상위 클래스가 업데이트 되었고 문서화를 하지 않았더라면 수정하는데 어려움을 겪을 것이다.

_example - 잘못된 상속 사용!_

```java
public class InstrumentedHashSet<E> extends HashSet<E> {

    private int addCount = 0;

    public InstrumentedHashSet() {
    }

    public InstrumentedHashSet(int initCap, float loadFactor) {
        super(initCap, loadFactor);
    }

    @Override
    public boolean add(E e) {
        addCount++;
        return super.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        addCount += c.size();
        return super.addAll(c);
    }

    public int getAddCount() {
        return addCount;
    }

    public static void main(String[] args) {
        InstrumentedHashSet<String> s = new InstrumentedHashSet<>();
        s.addAll(List.of("틱", "탁탁", "펑"));

        System.out.println(s.getAddCount());
    }
}
```

원했던 결과 **3**이 아닌 **6**이 출력된다!

- HashSet - addAll 메서드가 add 메서드를 사용해 구현되어 있어 문제가 발생한 것이다.
- 중복으로 addCount가 증가되는 메서드를 구현해버리게 된 것이다.
- 이렇듯 상위 클래스 내부 구현이 변경됨에 따라 하위 클래스가 어떤 동작을 할 지 모른다는 큰 문제가 존재한다.

addAll 메서드를 재정의하지 않으면 문제를 고칠 수 있긴 하다.

- 하지만, 당장 제대로 동작할지 모르나, HashSet의 addAll이 add 메서드를 이용해 구현했음을 가정한 해법이라는 한계가 존재한다.
- 자신의 다른 부분을 사용하는 `자기사용(self-use)` 여부는 해당 클래스 내부 구현 방식에 해당되며, 여전히 문제가 발생한다.

addAll 메서드를 다음과 같이 재정의하여 해결을 할 수도 있긴 하다.

```java

@Override
public boolean addAll(Collection<? extends E> c) {

    boolean modified = false;
    for (E e : c) {
        if (this.add(e)) {
            modified = true;
        }
    }

    return modified;
}
```

- 하지만, 이 방식은 구현하기 어렵고 시간도 더 들고, 오류를 내거나 성능을 떨어뜨릴 수 있다.
- 그리고, 외부에서 접근하지 못하도록 `private` 제어자를 사용한 메서드라면 구현 자체가 불가능하다.

### 2. 상위 클래스에 새로운 메서드를 추가한다면?

보안 때문에 컬렉션에 추가된 모든 원소가 특정 조건을 만족해야만 하는 프로그램을 생각해보자.

> **Pre-Condition / Post-Condition** 을 검증하는 방식이 추가된다면?

- 하위 클래스는 검증 조건에 대해 검사하지 않아, '허용되지 않은' 원소를 추가할 수 있게되는 문제가 발생할 수 있다.
- 실제로도 컬렉션 프레임워크 이전에 존재하던 HashTable, Vector를 컬렉션 프레임워크에 포함시키자 이러한 문제가 발생했었다.

정말 운이 없게도 상위 클래스에 새로운 메서드가 추가되었는데, 하위 클래스에 추가한 메서드와 시그니처가 같고 반환 타입은 다르다면 해당 클래스는 컴파일 조차 되지 않는다.

- 게다가 상위 클래스의 메서드가 요구하는 규약을 만족하지 못할 가능성이 크다.

## 어떻게 상속을 피해야 할까?

기존 클래스를 확장하는 대신, 새로운 클래스를 만들고 `private` 필드로 기존 클래스의 인스턴스를 참조하게 하자.

> **컴포지션(Composition)**: 기존 클래스가 새로운 클래스의 구성요소로 사용되는 패턴

- 전달(forwarding): 새 클래스의 인스턴스 메서드들은 기존 클래스의 대응하는 메서드를 호출하여 그 결과를 반환
- 전달 메서드(forwarding method): 새 클래스의 메서드들이 기존 클래스의 메서드들을 호출하는 메서드

_재사용할 수 있는 전달 클래스_

```java
public class ForwardingSet<E> implements Set<E> {

    private final Set<E> s;

    public ForwardingSet(Set<E> s) {
        this.s = s;
    }

    public int size() {
        return s.size();
    }

    public boolean isEmpty() {
        return s.isEmpty();
    }

    public boolean contains(Object o) {
        return s.contains(o);
    }

    public Iterator<E> iterator() {
        return s.iterator();
    }

    public Object[] toArray() {
        return s.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return s.toArray(a);
    }

    public boolean add(E e) {
        return s.add(e);
    }

    public boolean remove(Object o) {
        return s.remove(o);
    }

    // containsAll, addAll, removeAll, retainAll, clear ...

    @Override
    public boolean equals(Object o) {
        return s.equals(o);
    }

    @Override
    public int hashCode() {
        return s.hashCode();
    }

    @Override
    public String toString() {
        return s.toString();
    }
}
```

_상속 대신 컴포지션을 사용 - Wrapper Class_

```java
public class InstrumentedHashSet<E> extends ForwardingSet<E> {

    private int addCount = 0;

    public InstrumentedHashSet(Set<E> s) {
        super(s);
    }

    @Override
    public boolean add(E e) {
        addCount++;
        return super.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        addCount += c.size();
        return super.addAll(c);
    }

    public int getAddCount() {
        return addCount;
    }

    /**
     * 이제 우리가 원했던 값 3이 나온다.
     */
    public static void main(String[] args) {
        InstrumentedHashSet<String> s = new InstrumentedHashSet<>(new HashSet<>());
        s.addAll(List.of("Snap", "Crackle", "Pop"));
        System.out.println(s.getAddCount());
    }
}
```

- 임의의 Set에 계측 기능을 덧씌워 새로운 Set을 만드는 것이 이 클래스의 핵심!
- 어떠한 Set 구현체라도 계측할 수 잇으며, 기존 생성자들과도 함께 사용할 수 있다.
- InstrumentedSet 클래스와 같이 다른 Set 인스턴스를 감싸고(wrap) 있는 클래스를 래퍼 클래스라고 한다.
- 다른 Set에 계측 기능을 덧씌운다는 뜻에서 데코레이터 패턴(Decorator Pattern)이라고 한다.

> 컴포지션과 전달의 조합은 넓은 의미로 `위임(delegation)`이라고 부른다.  
> 단, 엄밀히 따지면 래퍼 객체가 내부 객체에 자기 자신의 참조를 넘기는 경우만 위임에 해당!

### Wrapper Class 주의점

콜백(callback) 프레임워크에서 래퍼 클래스를 사용할 땐 주의해야한다.

- 자기 자신의 참조를 다른 객체에 넘겨서 다음 호출 때 사용하도록 하기 때문에 SELF 문제가 발생할 수 있다.

> SELF 문제: [스택오버플로우 - Wrapper Classes are not suited for callback frameworks](https://stackoverflow.com/questions/28254116/wrapper-classes-are-not-suited-for-callback-frameworks)

## 언제 상속을 사용해야 할까?

하위 클래스가 상위 클래스의 '진짜' 하위 타입인 상황에서만 써야 한다!

- 즉, 클래스 B가 클래스 A와 `is-a` 관계일 때만 상속을 사용해야 한다.
- 만약, `is-a` 관계가 아니라면 위에서 언급한 컴포지션 방법을 사용하도록 하자.

---

## 핵심 정리

- 상속은 캡슐화를 깨뜨리는 문제가 존재한다.
- 상위 클래스와 하위 클래스의 관계가 `is-a` 일 때만 상속을 사용하자.
- is-a 관계일지라도 하위 클래스의 패키지가 상위 클래스와 다르고, 확장을 고려하지 않았을 수도 있다.
- 이러한 문제로 인해, 컴포지션과 전달을 사용하는 방법을 고려해보는게 어떨까?