# 아이템 22. 인터페이스는 타입을 정의하는 용도로만 사용하라

클래스가 어떤 인터페이스를 구현한다는 것은 자신의 인스턴스로 무엇을 할 수 있는지를 클라이언트에게 알려주는 것!

## 1. 잘못된 인터페이스

- **상수 인터페이스** 처럼 static final 필드로만 가득찬 인터페이스는 안티 패턴이다.
- 이 상수들을 사용하려는 클래스에서는 정규화된 이름을 쓰는 것을 피하고자 사용한다.

_상수 인터페이스 안티 패턴_

```java
public interface PhysicalConstants {
    // 아보가드로 수 (1/몰)
    static final double AVOGADRO_NUMBER = 6.022_140_857e23;

    // 볼츠만 상수 (J/K)
    static final double BOLTZMANN_CONSTANT = 1.380_648_52e-23;

    // 전자 질량 (kg)
    static final double ELECTRON_MASS = 9.109_383_56e-31;
}
```

- 이처럼 '클래스 내부'에서 사용하는 상수는 외부 인터페이스가 아니라 '내부 구현'에 해당
- 따라서 상수 인터페이스를 구현하는 것은 내부 구현을 클래스의 API로 노출하는 행위!
    - 클라이언트 코드가 내부 구현에 해당하는 이 상수들에 종속되게 하는 문제가 존재
    - 다음 릴리스가 나와 더이상 사용하지 않아도, 바이너리 호환성을 위해 구현하고 있어야 함

### 상수를 공개할 목적이라면?

1. 특정 클래스나 인터페이스에 강하게 연관된 상수라면 **해당 클래스나 인터페이스 자체** 에 추가하자.
   - 대표적인 예시가 Integer 클래스의 MIN_VALUE, MAX_VALUE 상수이다.
2. 열거 타입으로 나타내기 적합한 상수라면 열거 타입으로 만들어 공개하면 된다.

_상수 유틸리티 클래스_
```java
public class PhysicalConstants {
    private PhysicalConstants() {} // 인스턴스화 방지

    // 아보가드로 수 (1/몰)
    public static final double AVOGADRO_NUMBER = 6.022_140_857e23;

    // 볼츠만 상수 (J/K)
    public static final double BOLTZMANN_CONSTANT = 1.380_648_52e-23;

    // 전자 질량 (kg)
    public static final double ELECTRON_MASS = 9.109_383
}
```

위의 유틸리티 클래스를 사용할 땐 `PhysicalConstants.AVOGADRO_NUMBER`와 같이 사용하면 된다.

- 빈번하게 사용하는 경우, 정적 임포트(static import) 하여 클래스의 이름을 생략하는 방법도 있다.

## 핵심 정리

- 인터페이스는 타입을 정의하는 용도이다.
- 상수 공개용 수단으로 사용하지 말자!