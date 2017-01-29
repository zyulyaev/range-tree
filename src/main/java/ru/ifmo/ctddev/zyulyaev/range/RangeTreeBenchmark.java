package ru.ifmo.ctddev.zyulyaev.range;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author zyulyaev
 * @since 30.01.17
 */
@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
public class RangeTreeBenchmark {
    private static final Random random = new Random(239);
    private static final int MAX_COORD = 1000;

    @Param({"10", "100", "1000", "10000"})
    private int count;
    @Param({"2", "3", "4", "5"})
    private int dims;

    private List<Point> points;
    private RangeTree tree;
    private Rect query;
    private Set<Point> valid;
    private Collection<Point> result;

    @Setup(Level.Trial)
    public void setupTree() {
        points = randPoints(count);
        tree = new RangeTree(points, dims);
    }

    @Setup(Level.Iteration)
    public void setupQueryAndResult() {
        query = randRect();
        valid = naive(points, query.from, query.to);
    }

    @Benchmark
    public void benchmark(Blackhole bh) {
        bh.consume(result = tree.query(query.from, query.to));
    }

    @Benchmark
    public void baseline(Blackhole bh) {
        bh.consume(result = naive(points, query.from, query.to));
    }

    @TearDown(Level.Iteration)
    public void assertValid() {
        Set<Point> resultSet = new HashSet<>(result);
        if (!resultSet.equals(valid))
            throw new AssertionError("Invalid result");
    }

    private Point randPoint() {
        double[] xs = new double[dims];
        for (int i = 0; i < dims; ++i)
            xs[i] = -MAX_COORD + random.nextInt(MAX_COORD * 2 + 1);
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
