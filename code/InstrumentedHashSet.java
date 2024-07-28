package code;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;


public class InstrumentedHashSet<E> extends HashSet<E> {

    private int addCount = 0;

    public InstrumentedHashSet() {
    }

    public InstrumentedHashSet(int initCap, float loadFactor) {
        super(initCap, loadFactor);
    }

    @Override
    public boolean add(E e) {
        addCount++;
        return super.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        addCount += c.size();
        return super.addAll(c);
    }

    public int getAddCount() {
        return addCount;
    }


    /**
     * 원했던 결과 3이 아닌 6이 출력된다!
     * <li>HashSet - addAll 메서드가 add 메서드를 사용해 구현되어 있어 문제가 발생한 것이다.</li>
     * <li>중복으로 addCount 증가하는 방식을 구현해버리게 된 것이다.</li>
     * <li>이렇듯 상위 클래스 내부 구현이 변경됨에 따라 하위 클래스가 어떤 동작을 할 지 모른다는 큰 문제가 존재한다.</li>
     */
    public static void main(String[] args) {
        InstrumentedHashSet<String> s = new InstrumentedHashSet<>();
        s.addAll(List.of("틱", "탁탁", "펑"));

        System.out.println(s.getAddCount());
    }
}
