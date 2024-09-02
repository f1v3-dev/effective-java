# 3장. 모든 객체의 공통 메서드

Object를 상속하는 클래스(모든 클래스)는메서드들을 일반 규약에 맞게 재정의 해야한다. 
그렇지 않을 경우 규약을 준수한다고 가정하고 만들어둔 클래스에서 어떤 동작이 발생할지 모른다.

### 1. equals는 일반 규약을 지켜 재정의하라

**equals를 재정의 하지 않는 것이 좋은 경우**

- 각 인스턴스가 본질적으로 고유하다 (ex. Thread)
- 인스턴스의 '논리적 동치성(logical equality)'을 검사할 일이 없다.
- 상위 클래스에서 재정의한 euqals가 하위 클래스에도 딱 들어맞는다.
- 클래스가 private 이거나 package-private이고 equals 메서드를 호출알 일이 없다.

**equals를 재정의 해야하는 경우**

두 객체가 물리적으로 같은지를 파악하는 객체 식별성이 아닌 '논리적 동치성'을 확인해야 하는데 
상위 클래스의 equals가 논리적 동치성을 비교하도록 재정의되지 않았을 때

**equals 메서드는 동치관계(equivalence relation)를 구현하며, 다음을 만족한다.**
- 반사성(reflexivity): null 아닌 모든 참조값 x에 대해, x.equals(x) = true
- 대칭성(symmetry): null 아닌 모든 참조값 x, y에 대해, x.equals(y) = true이면, y.equals(x) = true
- 추이성(transitivity): null 아닌 모든 참조값 x, y, z에 대해 x.equals(y) = true고 y.equals(z) = true이면, x.equals(z) = true
- 일관성(consistency): null 아닌 모든 참조값 x, y에 대해 x.equals(y) 를 반복해서 호출하면 항상 같은 값(true/false)이 나온다.
- null-아님: null 아닌 모든 참조값 x에 대해, x.equals(null)은 false다.

### 2. equals와 hashCode는 함께 재정의하라

**equals를 재정의 했을 경우, hashCode를 재정의하지 않는다면 규약에 어긋날 수 있다.**

1. equals 비교에 사용되는 정보가 변경되지 않았다면, 객체의 hashCode 메서드는 항상 일관된 값을 반환해야 한다.
2. equals(Object)가 두 객체를 같다고 판단했으면, 두 객체의 hashCode는 똑같은 값을 반환해야 한다.
3. equals(Object)가 두 객체가 다르다고 판단했더라도, hashCode가 다른 값을 반환할 필요는 없다. 

### 3. 순서를 고려해야 한다면, Comparable 구현을 고려하라

Comparable 인터페이스의 유일한 메서드 `compareTo` 는 Object 메서드가 아니다.

- compareTo는 단순 동치성 비교에 더해 수넛까지 비교할 수 있다.
- compareTo는 제네릭하다.

```markdown
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