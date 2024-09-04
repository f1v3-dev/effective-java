# effective-java

## 2장. 객체 생성과 파괴

| Item |                                                  내용                                                   | 진행률 |
|:----:|:-----------------------------------------------------------------------------------------------------:|:---:|
|  1   |                 [생성자 대신 정적 팩터리 메서드를 고려하라](chapter02/item1_생성자_대신_정적_팩터리_메서드를_고려하라.md)                 |  ✅  |
|  2   |                 [생성자에 매개변수가 많다면 빌더를 고려하라](chapter02/item2_생성자에_매개변수가_많다면_빌더를_고려하라.md)                 |  ✅  |
|  3   |         [private 생성자나 열거 타입으로 싱글턴임을 보증하라](chapter02/item3_private_생성자나_열거_타입으로_싱글턴임을_보증하라.md)         |  ✅  |
|  4   |          [인스턴스화를 막으려거든 private 생성자를 사용하라](chapter02/item4_인스턴스화를_막으려거든_private_생성자를_사용하라.md)          |  ✅  |
|  5   |           [자원을 직접 명시하지 말고 의존 객체 주입을 사용하라](chapter02/item5_자원을_직접_명시하지_말고_의존_객체_주입을_사용하라.md)           |  ✅  |
|  6   |                         [불필요한 객체 생성을 피하라](chapter02/item6_불필요한_객체_생성을_피하라.md)                         |  ✅  |
|  7   |                         [다 쓴 객체 참조를 해제하라](chapter02/item7_다_쓴_객체_참조를_해제하라.md)                         |  ✅  |
|  8   |              [finalizer와 cleaner 사용을 피하라](chapter02/item8_finalizer와_cleaner_사용을_피하라.md)              |  ✅  |
|  9   | [try-finally보다는 try-with-resources를 사용하라](chapter02/item9_try-finally보다는_try-with-resources를_사용하라.md) |  ✅  |

## 3장. 모든 객체의 공통 메서드

| Item |                                           내용                                           | 진행률 |
|:----:|:--------------------------------------------------------------------------------------:|:---:|
|  10  |         [equals는 일반 규약을 지켜 재정의하라](chapter03/item10_equals는_일반_규약을_지켜_재정의하라.md)         |  ✅  |
|  11  | [equals를 재정의하려거든 hashCode도 재정의하라](chapter03/item11_equals를_재정의하려거든_hashCode도_재정의하라.md) |  ✅  |
|  12  |              [toString을 항상 재정의하라](chapter03/item12_toString을_항상_재정의하라.md)              |  ✅  |
|  13  |            [clone 재정의는 주의해서 진행하라](chapter03/item13_clone_재정의는_주의해서_진행하라.md)            |  ✅  |
|  14  |           [Comparable을 구현할지 고려하라](chapter03/item14_Comparable을_구현할지_고려하라.md)           |  ✅  |

## 4장. 클래스와 인터페이스

| Item |                                                     내용                                                     | 진행률 |
|:----:|:----------------------------------------------------------------------------------------------------------:|:---:|
|  15  |                     [클래스와 멤버의 접근 권한을 최소화하라](chapter04/item15_클래스와_멤버의_접근_권한을_최소화하라.md)                     |  ✅  |
|  16  | [public 클래스에서는 public 필드가 아닌 접근자 메서드를 사용하라](chapter04/item16_public_클래스에서는_public_필드가_아닌_접근자_메서드를_사용하라.md) |  ✅  |
|  17  |                             [변경 가능성을 최소화하라](chapter04/item17_변경_가능성을_최소화하라.md)                             |  ✅  |
|  18  |                          [상속보다는 컴포지션을 사용하라](chapter04/item18_상속보다는_컴포지션을_사용하라.md)                          |  ✅  |
|  19  |     [상속을 고려해 설계하고 문서화하라. 그러지 않았다면 상속을 금지하라](chapter04/item19_상속을_고려해_설계하고_문서화하라._그러지않았다면_상속을_금지하라.md)      |  ✅  |
|  20  |                     [추상 클래스보다는 인터페이스를 우선하라](chapter04/item20_추상_클래스보다는_인터페이스를_우선하라.md)                     |  ✅  |
|  21  |                   [인터페이스는 구현하는 쪽을 생각해 설계하라](chapter04/item21_인터페이스는_구현하는_쪽을_생각해_설계하라.md)                   |  ✅  |
|  22  |                  [인터페이스는 타입을 정의하는 용도로만 사용하라](chapter04/item22_인터페이스는_타입을_정의하는_용도로만_사용하라)                   |  ✅  |
|  23  |               [태그 달린 클래스보다는 클래스 계층구조를 활용하라](chapter04/item23_태그_달린_클래스보다는_클래스_계층구조를_활용하라.md)               |  ✅  |
|  24  |                  [멤버 클래스는 되도록 static으로 만들라](chapter04/item24_멤버_클래스는_되도록_static으로_만들라.md)                  |  ✅  |
|  25  |                    [톱레벨 클래스는 한 파일에 하나만 담으라](chapter04/item25_톱레벨_클래스는_한_파일에_하나만_담으라.md)                    |  ✅  |

## 5장. 제네릭

| Item |                         내용                         | 진행률 |
|:----:|:--------------------------------------------------:|:---:|
|  26  | [로 타입은 사용하지 말라](chapter05/item26_로_타입은_사용하지_말라.md) |  ✅  |
|  27  |  [비검사 경고를 제거하라](chapter05/item27_비검사_경고를_제거하라.md)  |  ✅  |
|  28  |                  배열보다는 리스트를 사용하라                   |     |
|  29  |                 이왕이면 제네릭 타입으로 만들라                  |     |
|  30  |                 이왕이면 제네릭 메서드로 만들라                  |     |
|  31  |            한정적 와일드카드를 사용해 API 유연성을 높이라             |     |
|  32  |               제네릭과 가변인수를 함께 쓸때는 신중하라               |     |
|  33  |                타입 안전 이종 컨테이너를 고려하라                 |     |
