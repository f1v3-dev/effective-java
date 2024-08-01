package code;


import java.time.Instant;

class Super {
    public Super() {
        overrideMe();
    }

    public void overrideMe() {
    }
}

public class Sub extends Super {
    private final Instant instant;

    Sub() {
        instant = Instant.now();
    }

    @Override
    public void overrideMe() {
        System.out.println(instant);
    }

    public static void main(String[] args) {
        Sub sub = new Sub();
        sub.overrideMe();
    }
}
