# 아이템 26. 로 타입은 사용하지 말라

## 들어가기 전,

제네릭 클래스, 제네릭 인터페이스란 클래스와 인터페이스 선언에 **타입 매개변수(type parameter)** 가 쓰이는 것을 말한다.

가장 대표적인 예시로 List<E> 가 있다. List 인터페이스는 원소 타입을 나타내는 타입 매개변수 E를 받는다.

이러한 제네릭 클래스와 제네릭 인터페이스를 통틀어 **제네릭 타입(generic type)** 이라고 한다.

### 제네릭 타입 (Generic Type)

각각의 제네릭 타입은 일련의 **매개변수화 타입(parameterized type)** 을 정의한다.

1. 먼저 클래스(혹은 인터페이스) 이름이 나오고
2. 이어서 꺽쇠 괄호 `< >` 안에 실제 타입 매개변수들을 나열한다.
3. `List<String>` 에서 `String` 이 정규(formal) 타입 매개변수 E에 해당하는 실제(actual) 타입 매개변수이다.

### 로 타입 (Raw Type)

> 제네릭 타입 하나를 정의하면 그에 딸린 `로 타입` 도 함께 정의된다.

- 제네릭 타입에서 타입 매개변수를 전혀 사용하지 않을 때를 말한다.
    - `List<E>` 의 로 타입 : `List`

제네릭이 도래하기 전 코드와 호환되도록 하기 위해 로 타입을 제공한다.

## 제네릭을 지원하기 전

```java
// Stamp 인스턴스만 취급하는 컬렉션
private final Collection stamps = ...;
```

제네릭을 지원하기 전에는 컬렉션을 다음과 선언했다. (여전히 동작하는 코드이지만 좋은 예라고는 할 수 없음!)

- 이 코드를 사용하면 실수로 도장(Stamp) 대신 동전(Coin) 을 넣어도 아무 오류 없이 컴파일되고 실행된다.
- 물론, 컴파일러가 모호한 경고 메시지를 보여주기는 할 것이다.

```java
// Stamp 가 아닌 Coin 인스턴스를 넣는다. 
stamps.add(new Coin(...)); // "unchecked call" 경고 발생
```

해당 컬렉션에서 동전을 다시 꺼내기 전까지는 오류를 알아채지 못함.

```java
for(Iterator i = stamps.iterator(); i.

hasNext(); ){
Stamp stamp = (Stamp) i.next(); // ClassCastException 발생
    stamp.

cancel();
}
```

- 컬렉션에서 꺼낸 객체를 캐스팅 할 때 예외가 발생하게 된다.
    - 컴파일 시점에 파악하는 것이 아닌, 실제로 코드를 돌려 런타임 시점에 발견되는 문제이다.
- 이러한 문제를 제네릭을 활용하여 변경해보자.

## 제네릭을 사용해보자

```java
private final Collection<Stamp> stamps = ...;
```

매개변수화된 컬렉션 타입을 통해 타입 안정성을 확보한 상태이다.

- 이제 컴파일러는 `stamps` 에 Stamp 인스턴스가 아닌 다른 타입 인스턴스를 넣으려고 하면 경고를 발생시킨다.
- 또한, 컬렉션에서 객체를 꺼낼 때 보이지 않는 곳에서 형변환하여 `절대 실패하지 않음` 을 보장한다.

## 로 타입을 사용하지 말자

로 타입을 쓰면 제네릭이 안겨주는 **안정성과 표현력** 을 모두 잃게 된다.

- 로 타입은 호환성을 위해 남겨져있을 뿐이다.
- 자바라는 언어가 제네릭을 받아들이기까지 10년이 걸린 탓에 이미 존재하는 코드가 호환성을 유지하기 위해 로 타입을 사용할 수 밖에 없다.
- `List` 같은 로 타입은 사용해서는 안되나, `List<Object>` 처럼 임의 객체를 허용하는 매개변수화 타입은 괜찮다.

**`List` vs `List<Object>`**

- `List`: 제네릭 타입에서 완전히 발을 뺀 것
- `List<Object>`: 모든 타입을 허용한다는 의사를 컴파일러에게 전달
  즉, 로 타입을 사용하면 타입 안전성을 잃게 된다!

### 구체적인 예

**unsafeAdd 메서드 - 로 타입(List) 사용**

```java
public static void main(String[] args) {
    List<String> strings = new ArrayList<>();
    unsafeAdd(strings, Integer.valueOf(42));
    String s = strings.get(0); // 컴파일러가 자동으로 형변환 코드를 넣어줌.
}

private static void unsafeAdd(List list, Object o) {
    list.add(o);
}
```

- `list.add(o)` 에서 경고를 통해 로 타입을 사용했음을 알려준다.
- 또한, strings.get(0) 결과를 형변환 하려할 때 `ClassCastException` 발생!

**모르는 타입의 원소도 받는 로 타입 사용**

```java
static int numElementsInCommon(Set s1, Set s2) {
    int result = 0;
    for (Object o1 : s1) {
        if (s2.contains(o1)) {
            result++;
        }
    }
    return result;
}
```

- 이 메서드는 동작은 하지만, 로 타입을 사용해 안전하지 않다.
- **비한정적 와일드카드 타입(unbounded wildcard type)** 을 사용해보자.

**비한정적 와일드카드 타입 사용 - 타입 안전 및 유연**

```java
static int numElementsInCommon(Set<?> s1, Set<?> s2) { ...}
```

_Set<?> vs Set_

- 와일드카드 타입(?)은 안전하고, 로 타입은 안전하지 않다.
- 로 타입 컬렉션: 아무 원소나 넣을 수 있으니 타입 불변식을 훼손하기 쉽다.
- Collection<?> 에는 null 이외의 어떤 원소도 넣을 수 없다.
    - 컬렉션의 타입 불변식을 훼손하지 못하게 컴파일러에서 에러가 발생한다.

### 예외 상황

1. 클래스 리터럴에는 로 타입을 사용해야 한다.
2. instanceof 연산자에는 로타입을 사용해야 한다.

```java
if(o instanceof Set){
Set<?> s = (Set<?>) o;
// ...
}
```

## 핵심 정리

- 로 타입을 사용하면 런타임 에러가 발생할 수 있으니 사용하지 말자.
- 그저, 호환성을 위해 남겨둔 것일 뿐이다.
- Set<Object>는 어떤 타입의 객체도 저장할 수 있는 매개변수화 타입이다.
- Set<?>는 모종의 타입 객체만 저장할 수 있는 와일드카드 타입!
