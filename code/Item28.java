package code;


import java.util.ArrayList;
import java.util.List;

public class Item28 {

    public static void covariant() {
        Object[] objectArray = new Long[1];
        objectArray[0] = "문자열이 들어갈 수 있나요?";
    }

    public static void invariant() {
        List<Object> ol = new ArrayList<Long>(); // 호환되지 않아 컴파일 에러 발생
        ol.add("타입이 달라 넣을 수가 없어요.");
    }

    public static void main(String[] args) {
        covariant();
        invariant();
    }
}
