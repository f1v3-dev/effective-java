# 아이템 23. 태그 달린 클래스보다는 클래스 계층구조를 활용하라

```java
class Figure {
    enum Shape { RECTANGLE, CIRCLE }
    
    // tag field - 현재 모양을 나타냄
    final Shape shape;
    
    // 사각형(RECTANGLE)일 때만 쓰이는 필드
    double length;
    double width;
    
    // 원(CIRCLE)일 때만 쓰이는 필드
    double radius;
    
    // 사각형 생성자
    Figure(double length, double width) {
        shape = Shape.RECTANGLE;
        this.length = length;
        this.width = width;
    }
    
    // 원 생성자
    Figure(double radius) {
        shape = Shape.CIRCLE;
        this.radius = radius;
    }
    
    double area() {
        switch (shape) {
            case RECTANGLE:
                return length * width;
            case CIRCLE:
                return Math.PI * (radius * radius);
            default:
                throw new AssertionError(shape);
        }
    }
}
```

위 클래스처럼 여러 의미를 표현할 수 있으며, 현재 표현하는 의미를 태그 값으로 알려주는 클래스가 존재한다.

하지만, 태그 달린 클래스에는 단점이 정말 많으니 되도록 사용하지 않고 클래스 계층구조로 변경하는 것이 좋다.

### 태그 달린 클래스의 단점

1. 열거 타입 선언, 태그 필드, switch 문 등 쓸데없는 코드가 많아진다.
2. 여러 구현이 한 클래스에 혼합되어 가독성이 떨어진다.
3. 다른 의미를 위한 코드도 언제나 함께하니 메모리도 많이 사용한다.
4. 쓰지 않는 필드를 초기화하는 불필요한 코드가 늘어남.
5. 인스턴스 타입만으로는 현재 나타내는 의미를 알 길이 전혀 없다.

종합해보자면,  
- **태그 달린 클래스는 장황하고, 오류를 내기 쉽고, 비효율적** 이라는 것이다!
- **클래스 계층구조를 어설프게 흉내**낸 아류일 뿐이다.

### 클래스 계층구조로 변경

1. 가장 먼저 계층구조의 루트(root)가 될 추상 클래스를 정의
2. 태그 값에 따라 동작이 달라지는 메서드들을 루트 클래스의 **추상 메서드** 로 선언 (Figure - area)
3. 모든 하위 클래스에서 공통으로 사용하는 데이터 필드도 루트 클래스에 선언하자
4. 루트 클래스를 확장한 구체 클래스를 **의미별로 하나씩 정의** 한다.

**클래스 계층구조로 변경한 코드**

```java
abstract class Figure {
    abstract double area();
}

// 원 클래스 
class Circle extends Figure {
    final double radius;
    
    Circle(double radius) {
        this.radius = radius;
    }
    
    @Override
    double area() {
        return Math.PI * (radius * radius);
    }
}

// 사각형 클래스
class Rectangle extends Figure {
    final double length;
    final double width;
    
    Rectangle(double length, double width) {
        this.length = length;
        this.width = width;
    }
    
    @Override
    double area() {
        return length * width;
    }
}
```

> 간결하고 명확하며, 쓸데없는 코드도 모두 사라졌다.

루트 클래스의 코드를 건드리지 않고도 다른 프로그래머들이 독립적으로 계층구조를 확장하고 함께 사용할 수 있다!  

- 정사각형도 지원한다고 가정한다면 아래처럼 아주 간단하게 반영할 수 있다!

```java
class Square extends Rectangle {
    Square(double side) {
        super(side, side);
    }
}
```

## 핵심 정리

- 태그 달린 클래스를 써야하는 상황은 거의 없다.
- 클래스 계층구조로 대체하는 방법으로 리팩터링 할 수 있는지 고민해보자.

