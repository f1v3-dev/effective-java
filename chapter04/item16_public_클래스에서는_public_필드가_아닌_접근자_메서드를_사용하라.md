# 아이템 16. public 클래스에서는 public 필드가 아닌 접근자 메서드를 사용하라.

> **public 클래스에서는 필드를 모두 `private`으로 사용하고, `접근 메서드(getter)`를 제공하자!**

### 잘못 사용된 클래스

```java
class Point {
    public double x;
    public double y;
}
```

- 데이터 필드(x, y)에 직접 접근할 수 있어 `캡슐화`의 이점을 제공하지 못한다.
- API를 수정하지 않고는 내부 표현을 바꿀 수도 없다.
- 불변식을 보장할 수 없다.
- 외부에서 필드에 접근할 때 부수 작업을 수행할 수도 없다.

### 올바르게 사용된 클래스

```java
class Point {
    private double x;
    private double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // getter
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    // setter
    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }
    
}
```

- public 클래스에서라면 이 방법을 적극적으로 사용하자.
- 패키지 바깥에서 접근할 수 있는 클래스라면, **접근자**를 제공하여 내부 표현 방식을 언제든 바꿀 수 있는 유연성을 얻는다.

#### public 클래스가 아니라면? 
- package-private 클래스 / private 중첩 클래스라면 데이터 필드를 노출한다 해도 하등의 문제는 없다.
- 그 클래스가 표현하려는 추상 개념만 올바르게 표현해주면 된다.


### 불변 필드를 노출한 public 클래스

- 아래의 코드를 보며 문제점을 파악해보자.

```java
public final class Time {
    private static final int HOURS_PER_DAY = 24;
    private static final int MINUTES_PER_HOUR = 60;
    
    public final int hour;
    public final int minute;
    
    public Time(int hour, int minute) {

        // check hour
        if (hour < 0 || hour >= HOURS_PER_DAY) {
            throw new IllegalArgumentException("시간: " + hour);
        }
        
        // check minute
        if (minute < 0 || minute >= MINUTES_PER_HOUR) {
            throw new IllegalArgumentException("분: " + minute);
        }
        
        this.hour = hour;
        this.minute = minute;
    }
}
```

- final 필드라 하더라도, 외부로의 노출은 좋지 않다.
- API를 변경하지 않고는 표현 방식을 바꿀 수 없고, 필드를 읽을 때 부수 작업을 수행할 수 없다는 단점이 여전히 존재한다.
- 유효성 검사를 통해 불변식을 보장할 수 있기는 하다.