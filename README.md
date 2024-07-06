# effective-java

## 2장. 객체 생성과 파괴

| Item |                   내용                    |                                  정리                                  |
|:----:|:---------------------------------------:|:--------------------------------------------------------------------:|
|  1   |         생성자 대신 정적 팩터리 메서드를 고려하라         |         [README](chapter02/item1_생성자_대신_정적_팩터리_메서드를_고려하라.md)         |
|  2   |         생성자에 매개변수가 많다면 빌더를 고려하라         |         [README](chapter02/item2_생성자에_매개변수가_많다면_빌더를_고려하라.md)         |
|  3   |     private 생성자나 열거 타입으로 싱글턴임을 보증하라     |     [README](chapter02/item3_private_생성자나_열거_타입으로_싱글턴임을_보증하라.md)     |
|  4   |     인스턴스화를 막으려거든 private 생성자를 사용하라      |     [README](chapter02/item4_인스턴스화를_막으려거든_private_생성자를_사용하라.md)      |
|  5   |      자원을 직접 명시하지 말고 의존 객체 주입을 사용하라      |      [README](chapter02/item5_자원을_직접_명시하지_말고_의존_객체_주입을_사용하라.md)      |
|  6   |             불필요한 객체 생성을 피하라             |             [README](chapter02/item6_불필요한_객체_생성을_피하라.md)             |
|  7   |             다 쓴 객체 참조를 해제하라             |             [README](chapter02/item7_다_쓴_객체_참조를_해제하라.md)             |
|  8   |       finalizer와 cleaner 사용을 피하라        |       [README](chapter02/item8_finalizer와_cleaner_사용을_피하라.md)        |
|  9   | try-finally보다는 try-with-resources를 사용하라 | [README](chapter02/item9_try-finally보다는_try-with-resources를_사용하라.md) |

## 3장. 모든 객체의 공통 메서드

| Item |               내용                |                              정리                               |
|:----:|:-------------------------------:|:-------------------------------------------------------------:|
|  1   |     equals는 일반 규약을 지켜 재정의하라     |     [README](chapter02/item1_생성자_대신_정적_팩터리_메서드를_고려하라.md)      |
|  2   | equals를 재정의하려거든 hashCode도 재정의하라 | [README](chapter03/item11_equals를_재정의하려거든_hashCode도_재정의하라.md) |
|  3   |       toString을 항상 재정의하라        |       [README](chapter03/item12_toString을_항상_재정의하라.md)        |
|  4   |      clone 재정의는 주의해서 진행하라       |       [README](chapter03/item13_clone_재정의는_주의해서_진행하라.md)        |
