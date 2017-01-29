package ru.ifmo.ctddev.zyulyaev.range;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author zyulyaev
 * @since 29.01.17
 */
@RunWith(Parameterized.class)
public class RangeTreeTest {
    private static final Random random = new Random(239);

    //
    private static final int[] MAX_COORDS = {10, 100, 1000, 10000};
    private static final int[] COUNTS = {10, 100, 1000, 10000};
    private static final int[] DIMS = {2, 3, 4, 5, 20};

    @Parameterized.Parameters
    public static Object[][] parameters() {
        List<Object[]> result = new ArrayList<>();
        for (int points : COUNTS) {
            for (int dims : DIMS) {
                if (points * Math.pow(Math.log(points), dims) >= 200_000_000)
                    continue; // too much especially for RAM due to O(n*log^d(n)) memory consumption

                for (int maxCoord : MAX_COORDS) {
                    result.add(new Object[]{maxCoord, points, dims});
                }
            }
        }

        return result.toArray(new Object[0][]);
    }

    @Parameterized.Parameter(0)
    public int maxCoord;
    @Parameterized.Parameter(1)
    public int count;
    @Parameterized.Parameter(2)
    public int dims;

    @Test(timeout = 5000)
    public void test() {
        List<Point> points = randPoints(count);
        RangeTree tree = new RangeTree(points, dims);
        Rect rect = randRect();
        Set<Point> algoResult = new HashSet<>(tree.query(rect.from, rect.to));
        Set<Point> naiveResult = naive(points, rect.from, rect.to);
        if (!algoResult.equals(naiveResult)) {
            String message = "Failed on points: " + points + "\nAnd rect: " + rect;
            Assert.fail(message);
        }
    }

    private Point randPoint() {
        double[] xs = new double[dims];
        for (int i = 0; i < dims; ++i)
            xs[i] = -maxCoord + random.nextInt(maxCoord * 2);
        return new Point(xs);
    }

    private Rect randRect() {
        Point a = randPoint();
        Point b = randPoint();
        double[] from = new double[dims];
        double[] to = new double[dims];
        for (int i = 0; i < dims; i++) {
            from[i] = Math.min(a.get(i), b.get(i));
            to[i] = Math.max(a.get(i), b.get(i));
        }
        return new Rect(new Point(from), new Point(to));
    }

    private List<Point> randPoints(int count) {
        return Stream.generate(this::randPoint).limit(count).collect(Collectors.toList());
    }

    private boolean between(Point point, Point from, Point to) {
        for (int i = 0; i < dims; i++) {
            if (point.get(i) < from.get(i) || point.get(i) > to.get(i))
                return false;
        }
        return true;
    }

    private Set<Point> naive(List<Point> all, Point from, Point to) {
        return all.stream().filter(point -> between(point, from, to)).collect(Collectors.toSet());
    }

    private static class Rect {
        private final Point from;
        private final Point to;

        private Rect(Point from, Point to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public String toString() {
            return "{" + from + "->" + to + "}";
        }
    }
}
