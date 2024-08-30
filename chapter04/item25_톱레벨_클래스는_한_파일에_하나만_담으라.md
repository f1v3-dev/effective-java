# 톱레벨 클래스는 한 파일에 하나만 담으라

소스 파일 하나에 톱 레벨 클래스를 여러 개 선언해도 되지만, 아무런 득이 없을 뿐더러 심각한 위험을 감수해야 한다.

- 한 클래스를 여러 가지로 정의할 수 있음.
- 그중 어느 것을 사용할지는 어느 소스파일을 먼저 컴파일하냐에 따라 달라짐

## 두 클래스가 한 파일에 정의된다면?


```java
public class Main {
    public static void main(String[] args) {
        System.out.println(Utensil.NAME + Dessert.NAME);
    }
}
```

위와 같은 Main 클래스가 있다고 가정해보자.

_Utensil.java_
```java
class Utensil {
    static final String NAME = "pan";
}

class Dessert {
    static final String NAME = "cake";
}
```

_Dessert.java_
```java
class Utensil {
    static final String NAME = "pot";
}

class Dessert {
    static final String NAME = "pie";
}
```

위처럼 작성된 `Utensil`, `Dessert` 클래스를 Main에서 어떻게 사용할지는 컴파일 순서에 따라 달라진다.


### 문제점

1. `javac Main.java Dessert.java` 
   - 컴파일 오류와 함께 클래스를 중복 정의했다고 알려준다.
2. `javac Main.java` / `javac Main.java Utensil.java` 
   - `pancake` 가 출력됨.
3. `javac Dessert.java Main.java` 
   - `potpie` 가 출력됨.

이처럼 컴파일러에 어느 소스파일을 먼저 건네느냐에 따라 동작이 달라지는 이상한 현상이 발생한다.

## 해결 방법

**톱레벨 클래스들을 서로 다른 소스 파일로 분리하면 된다.**

만약, 여러 톱레벨 클래스를 한 파일에 담고 싶다면 `정적 멤버 클래스`를 사용하는 방법을 고려해볼 수 있다.

**톱레벨 클래스 &rarr; 정적 멤버 클래스**

```java
public class Test {
    public static void main(String[] args) {
        System.out.println(Utensil.NAME + Dessert.NAME);
    }
    
    private static class Utensil {
        static final String NAME = "pan";
    }
    
    private static class Dessert {
        static final String NAME = "cake";
    }
}
```

## 핵심 정리

- 소스 파일 하나에는 반드시 톱레벨 클래스 (혹은 톱레벨 인터페이스)를 하나만 담자.