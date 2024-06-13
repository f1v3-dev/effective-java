# 아이템 2. 생성자에 매개변수가 많다면 빌더를 고려하라

아이템 1에서 언급한 정적 팩터리와 생성자에는 선택적 매개변수가 많다면 적절하게 대응하기 어렵다. 이에 대한 대처 방법들을 살펴보자.

또한, 주로 사용했던 `Builder`에 대해 학습해보자.

## 점층적 생성자 패턴 (telescoping constructor pattern)

- 필수 매개변수만 받는 생성자 + 선택적 매개변수를 받는 생성자를 늘려가는 방식

_example_
```java
public class NutritionFacts {

    private final int servingSize;      // (ml, 1회 제공량)         필수
    private final int servings;         // (회, 총 n회 제공량)       필수
    private final int calories;         // (1회 제공량당)            선택
    private final int fat;              // (g/1회 제공량)           선택
    private final int sodium;           // (mg/1회 제공량)          선택
    private final int carbohydrate;     // (g/1회 제공량)           선택
    
    public NutritionFacts(int servingSize, int servings) {
        this(servingSize, servings, 0);
    }
    
    public NutritionFacts(int servingSize, int servings, int calories) {
        this(servingSize, servings, calories, 0);
    }
    
    // ...
    
    public NutritionFacts(int servingSize, int servings, int calories,
                          int fat, int sodium, int carbohydrate) {
        this.servingSize = servingSize;
        this.servings = servings;
        this.calories = calories;
        this.fat = fat;
        this.sodium = sodium;
        this.carbohydrate = carbohydrate;
    }
}
```

- 위와 같은 클래스의 인스턴스를 생성하려면, 원하는 매개변수를 모두 포함한 생성자를 호출하면 된다.
- 하지만, **매개변수 개수가 많아진다면** 클라이언트 코드를 작성하거나 읽기 어렵다.
    - 특히, 타입이 같은 매개변수가 연달아 있는 경우 찾기 어려운 버그로 이어질 가능성이 높음

## 자바빈즈 패턴 (JavaBeans Pattern)

- 매개변수가 없는 생성자를 만든 후, Setter 메서드를 호출하여 매개변수의 값을 설정하는 방식

```java
public class NutritionFacts {

    private int servingSize = -1;      // (ml, 1회 제공량)         필수
    private int servings = -1;         // (회, 총 n회 제공량)       필수
    private int calories = 0;         // (1회 제공량당)            선택
    private int fat = 0;              // (g/1회 제공량)            선택
    private int sodium = 0;           // (mg/1회 제공량)           선택
    private int carbohydrate = 0;     // (g/1회 제공량)            선택

    public NutritionFacts() {

    }

    public void setServingSize(int servingSize) {
        this.servingSize = servingSize;
    }

    public void setServings(int servings) {
        this.servings = servings;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public void setFat(int fat) {
        this.fat = fat;
    }

    public void setSodium(int sodium) {
        this.sodium = sodium;
    }

    public void setCarbohydrate(int carbohydrate) {
        this.carbohydrate = carbohydrate;
    }
}
```

- 코드가 길어지긴 했지만, 인스턴스를 만들기 쉽고 더 읽기 쉬운 코드가 되었다.
- 하지만, 자바빈즈 패턴에서는 객체 하나를 만들기 위해서 여러 메서드를 호출해야 하며, 완전히 생성되기 전까지 *일관성(Consistency)이 무너진 상태*라는 치명적인 단점이 존재한다.

```java
NutritionFacts cocaCola = new NutritionFacts();

// 일관성이 무너진 상태로 존재
cocaCola.setServingSize(240);
cocaCola.setServings(8);
cocaCola.setCalrories(100);
cocaCola.setSodium(35);
```

- 이처럼 일관성이 무너지는 문제로 인해 클래스를 불변으로 만들 수 없으며, 스레드 안전성을 얻기 위해 추가적인 작업이 필요하다.
- 대표적인 예시가 자바스크립트에서 `Object.freeze()`를 사용하는 것이다.
- 하지만, 개발자가 freeze 메서드를 확실히 호출해줬는지 보증할 방법이 없어 런타임 오류에 취약하다는 문제가 여전히 존재한다.

> [[TOAST UI] Object.freeze vs Object.seal - 불변성과 관련된 두 가지 네이티브 메서드](https://ui.toast.com/posts/ko_20220420)


## 빌더 패턴 (Builder Pattern)

#### 점층적 생성자 패턴의 안전성과 자바빈즈 패턴의 가독성을 겸비 

1. 클라이언트는 필수 매개변수만으로 생성자(혹슨 정적 팩터리)를 호출해 빌더 객체를 얻는다.
2. 빌더 객체가 제공한느 일종의 setter 메서드로 매개변수를 설정
3. build 메서드를 호출하여 필요한 객체를 얻는다.

_example_
```java
public class NutritionFacts {

    private final int servingSize;
    private final int servings;
    private final int calories;
    private final int fat;
    private final int sodium;
    private final int carbohydrate;

    public static class Builder {

        // 필수 매개변수
        private final int servingSize;
        private final int servings;

        // 선택 매개변수 - 기본 값으로 초기화
        private int calories;
        private int fat;
        private int sodium;
        private int carbohydrate;

        public Builder(int servingSize, int servings) {
            this.servingSize = servingSize;
            this.servings = servings;
        }

        public Builder calories(int calories) {
            this.calories = calories;
            return this;
        }

        public Builder fat(int fat) {
            this.fat = fat;
            return this;
        }

        public Builder sodium(int sodium) {
            this.sodium = sodium;
            return this;
        }

        public Builder carbohydrate(int carbohydrate) {
            this.carbohydrate = carbohydrate;
            return this;
        }

        public NutritionFacts build() {
            return new NutritionFacts(this);
        }
    }

    private NutritionFacts(Builder builder) {
        servingSize = builder.servingSize;
        servings = builder.servings;
        calories = builder.calories;
        fat = builder.fat;
        sodium = builder.sodium;
        carbohydrate = builder.carbohydrate;
    }
}
```


_클라이언트 코드_
```java
NutritionFacts cocaCola = new Builder(240, 8)
        .calories(100)
        .sodium(35)
        .carbohydrate(27)
        .build();
```

- NutritionFacts 클래스는 불변
- Builder - setter method는 빌더 자신을 반환! -> 연쇄적으로 호출이 가능함
  - method chaining, fluent API 
- 선택적 매개변수를 흉내 낸 것으로, 쓰기도 쉽고 읽기도 쉽다는 장점이 존재한다.
- 추가적으로 build 시점에 invariant 검사를 하여 안정성을 높이면 좋을 것 같다.

#### 계층적으로 설계뙨 클래스와 함께 쓰기에 좋다.

- 각 계층 클래스에 관련 빌더를 멤버로 정의
- 추상 클래스 - 추상 빌더
- 구체 클래스 - 구체 빌더

_example_

**Pizza.class**
```java
public abstract class Pizza {

    public enum Topping {
        HAM,
        MUSHROOM,
        ONION,
        PEPPER,
        SAUSAGE
    }

    final Set<Topping> toppings;

    abstract static class Builder<T extends Builder<T>> {
        EnumSet<Topping> toppings = EnumSet.noneOf(Topping.class);

        public T addTopping(Topping topping) {
            toppings.add(Objects.requireNonNull(topping));
            return self();
        }

        abstract Pizza build();

        // 하위 클래스는 이 메서드를 오버라이딩하여
        // "this" 반환!
        protected abstract T self();
    }

    Pizza(Builder<?> builder) {
        toppings = builder.toppings.clone();
    }
}

```

**NyPizza.class**
```java
public class NyPizza extends Pizza {

    public enum Size {
        SMALL,
        MEDIUM,
        LARGE
    }

    private final Size size;

    public static class Builder extends Pizza.Builder<Builder> {

        private final Size size;

        public Builder(Size size) {
            this.size = Objects.requireNonNull(size);
        }

        @Override
        public NyPizza build() {
            return new NyPizza(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    private NyPizza(Builder builder) {
        super(builder);
        size = builder.size;
    }
}
```

**Calzone.class**
```java
public class Calzone extends Pizza {
    
    private final boolean sauceInside;

    public static class Builder extends Pizza.Builder<Builder> {
        private boolean sauceInside = false;

        public Builder sauceInside() {
            sauceInside = true;
            return this;
        }

        @Override
        public Calzone build() {
            return new Calzone(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    private Calzone(Builder builder) {
        super(builder);
        sauceInside = builder.sauceInside;
    }
}
```

_클라이언트 코드_
```java
NyPizza pizza = new NyPizza.Builder(SMALL)
        .addToping(SAUSAGE)
        .addToping(ONION)
        .build();

Calzone calzone = new Calzone.Builder()
        .addToping(HAM)
        .sauceInside()
        .build();
```

- Pizza.Builder 클래스는 재귀적 타입 한정을 이용하는 **제네릭 타입**
- 추상 메서드인 `self()`를 통해 하위 클래스에서 형변환을 하지 않고도 method chaining 가능
  - 시뮬레이트한 셀프 타입 (simulated self-type) 관용구라 한다.


### 단점
1. 객체를 만들기 위해서 빌더부터 만들어야 함.
   - 빌더 생성 비용이 크지는 않지만, 성능에 민감할 경우 문제가 될 수 있음.
2. 점층적 생성자 패턴보다는 코드의 수가 많음
   - 매개변수가 4개 이상은 되어야 값어치를 한다고 함.

---

**Book.class Builder Pattern 만들어보기**

```java
public class Book {

    private final String author;
    private final String name;
    private final String category;
    private final int stock;

    private static class Builder {

        private final String author;
        private final String name;
        private String category = "NULL";
        private int stock = 0;

        public Builder(String author, String name) {
            this.author = author;
            this.name = name;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder stock(int stock) {
            this.stock = stock;
            return this;
        }

        public Book build() {
            return new Book(this);
        }
    }

    private Book(Builder builder) {
        this.author = builder.author;
        this.name = builder.name;
        this.category = builder.category;
        this.stock = builder.stock;
    }

    @Override
    public String toString() {
        return "Book{" +
                "author='" + author + '\'' +
                ", name='" + name + '\'' +
                ", cateogry='" + category + '\'' +
                ", stock=" + stock +
                '}';
    }

    public static void main(String[] args) {
        Book book = new Builder("seungjo", "JAVA의 정석")
                .category("Programming Language")
                .stock(100)
                .build();

        System.out.println(book);
    }
}

```


### 📌 읽을거리
> [[LinkedIn] How to implement the builder pattern in Java using three different techniques](https://www.linkedin.com/pulse/java-builder-pattern-sebastian-kaiser/)  
> [[Refactoring.Guru] 빌더 패턴](https://refactoring.guru/ko/design-patterns/builder)