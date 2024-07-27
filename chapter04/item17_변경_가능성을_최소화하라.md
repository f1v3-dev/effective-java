# 아이템 17. 변경 가능성을 최소화하라

> 불변 클래스(Immutable Class): 인스턴스 내부 값을 **수정할 수 없는** 클래스  
> _(String, BigInteger, BigDecimal)_

### 불변 클래스의 다섯가지 규칙

1. 객체의 상태를 변경하는 메서드(변경자)를 제공하지 않는다.
2. 클래스를 확장할 수 없도록 한다.
    - 하위 클래스에서 객체의 상태를 변하게 만드는 사태를 막자 (**final class**)
3. 모든 필드를 `final`로 선언한다.
    - 셀계자의 의도를 명확히 드러내는 방법!
4. 모든 필드를 `private`으로 선언한다.
    - 필드가 참조하는 가변 객체를 클라이언트에서 직접 접근해 수정하는 것을 막자.
5. 자신 외에는 내부의 가변 컴포넌트에 접근할 수 없도록 한다.
    - 클래스에 가변 객체를 참조하는 필드가 있을 때, 그 객체의 참조를 클라이언트가 얻을 수 없도록 해야 함!

_불변 복소수 클래스_

```java
public final class Complex {

    private final double re;
    private final double im;

    public Complex(double re, double im) {
        this.re = re;
        this.im = im;
    }

    public double realPart() {
        return re;
    }

    public double imaginaryPart() {
        return im;
    }

    public Complex plus(Complex c) {
        return new Complex(re + c.re, im + c.im);
    }

    public Complex minus(Complex c) {
        return new Complex(re - c.re, im - c.im);
    }

    public Complex times(Complex c) {
        return new Complex(
                re * c.re - im * c.im,
                re * c.im + im * c.re);
    }

    public Complex dividedBy(Complex c) {
        double tmp = c.re * c.re + c.im * c.im;
        return new Complex(
                (re * c.re + im * c.im) / tmp,
                (im * c.re - re * c.im) / tmp);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }


        if (!(obj instanceof Complex)) {
            return false;
        }

        Complex c = (Complex) obj;

        return Double.compare(c.re, re) == 0
                && Double.compare(c.im, im) == 0;
    }

    @Override
    public int hashCode() {
        return 31 * Double.hashCode(re) + Double.hashCode(im);
    }

    @Override
    public String toString() {
        return "(" + re + " + " + im + ")";
    }
}
```

**사칙연산 메서드들에 주목을 해보자.**

- 자신을 수정하지 않고, 새로운 인스턴스를 만들어 반환한다.
- 피연산자에 함수를 적용해 그 결과를 반환하지만, 피연산자 자체는 그대로이다.
- 이러한 프로그래밍 패턴을 함수형 프로그래밍이라고 한다.
- 동사(add) 대신 전치사(plus)를 사용하여 '객체의 값을 변경하지 않는다'를 강조함!

그렇다면 불변 객체는 어떠한 장점이 있는걸까?

### 불변 객체의 장점

- 불변 객체는 단순하다.
    - 생성된 시점의 상태를 파괴될 때까지 유지한다.
    - 모든 생성자가 클래스 불변식(class invariant)를 보장한다면, 그 클래스를 사용하는 프로그래머가 다른 노력을 들이지 않아도 된다.
- 불변 객체는 스레드 안전(thread-safety)하여 따로 동기화할 필요가 없다.
    - 여러 스레드가 동시에 사용해도 절대 훼손되지 않음!
    - 이로 인해, 불변 객체는 안심하고 공유할 수 있다.

#### 불변 클래스라면 한 번 만든 인스턴스를 최대한 재사용하기를 권한다.

_example_

```java
public static final Complex ZERO = new Complex(0, 0);
public static final Complex ONE = new Complex(1, 0);
public static final Complex I = new Complex(0, 1);
```

- 자주 사용되는 인스턴스를 캐싱하여 중복 생성하지 않게 해주는 정적 팩터리를 제공할 수도 있다.
- `static`으로 선언을 하여, GC의 대상이 되지 않는다.
- 이런식의 불변 객체를 자유롭게 공유할 수 잇다는 점은 방어적 복사도 필요 없다는 결론으로 이어진다. &rarr; 불변 클래스는 `clone` 메서드를 만들지 말자.

#### 불변 객체는 자유롭게 공유할 수 있음은 물론, 불변 객체끼리 내부 데이터를 공유할 수 있다.

BigInteger 클래스의 부호(signum), 크기(magnitude)는 서로 다른 객체지만, 내부 배열을 공유한다.

#### 객체를 만들 때 다른 불변 객체들을 구성요소로 사용하면 이점이 많다.

- 값이 바뀌지 않는 구성요소들로 이루어진 객체라면 그 구조가 아무리 복잡하더라도 **불변식**을 유지하기 훨씬 수월하다.
- 좋은 예시로, 불변 객체는 맵의 Key, Set의 원소로 쓰기에 안성맞춤이다.

#### 불변 객체는 그 자체로 실패 원자성을 제공한다.

> **실패 원자성**: 메서드에서 예외가 발생한 후에도 그 객체는 여전히 유효한 상태여야 한다는 성질

### 불변 객체의 단점

#### 값이 다르면 반드시 독립된 객체로 만들어야 한다.

- 백만 비트짜리 `BigInteger`에서 비트 하나를 바꿔야 한다고 했을 때에도 새로운 객체를 만들어야 한다.
  - 이러한 연산은 크기에 비례해 시간과 공간을 잡아먹는다.

#### 원하는 객체를 완성하기까지 단계까 많고, 중간 단계에서 만들어진 객체들이 버려진다면 성능 문제가 더욱 심각해진다.

_이 문제를 대처하는 방법_
1. 흔히 쓰일 다단계 연산들을 예측하여 기본 기능으로 제공
2. 클라이언트들이 원하는 복잡한 연산들을 정확히 예측할 수 있다면, `package-private`의 가변 동반 클래스 제공

### 불변 클래스를 만드는 설계 방법

#### 1. 모든 생성자를 private 혹은 package-private으로 만들고 public 정적 팩터리 제공

```java
public class Complex {
    private final double re;
    private final double im;
    
    // private constructor!!
    private Complex(double re, double im) {
        this.re = re;
        this.im = im;
    }
    
    // static factory method (public)
    public static Complex valueOf(double re, double im) {
        return new Complex(re, im);
    }
}
```

- 패키지 바깥의 클라이언트에서 바라본 이 불변 객체는 사실상 `final` 이다.

BigInteger, BigDecimal의 경우 '재정의'를 막지 않아 하위 호환성이 발목을 잡아 지금까지도 이 문제가 존재한다고 한다.

---

## 정리

- `getter`가 있다고 무조건 `setter`를 만들지는 말자.
- 클래스는 꼭 필요한 경우가 아니라면 **불변**이어야 한다.
- 단순한 값 객체는 항상 불변으로 만들자!
- 또한, `String`, `BigInteger`처럼 무거운 객체도 불변으로 만들 수 있는지 고심하자.
- 불변으로 만들 수 없는 클래스라도 **변경할 수 있는 부분**을 **최소한**으로 줄이자.
- 다른 합당한 이유가 없다면 모든 필드는 **private final**이어야 한다.
- 생성자는 불변식 설정이 모두 완료된, 초기화가 완벽히 끝난 상태의 객체를 생성해야 한다. (precondition)