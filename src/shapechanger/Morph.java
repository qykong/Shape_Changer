package shapechanger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.*;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;

/**
 * The model part of the ShapeChanger.
 * The morphing algorithm @link Morph.normalise
 * is not perfected -- for complex enough shapes
 * the results is a strongly diminished shape compared with
 * the expected one (TODO: 25/04/2016 needs fixing later)
 *
 * <p>
 * Created with IntelliJ IDEA.
 * User: abx
 * Date: 21/04/2016
 * Time: 5:46 PM
 * Created for ass2 in package shapechanger
 * @version 1.1
 * @author abx
 * @author Quyu Kong u5862608
 *
 * @see ShapeChanger
 * </p>
 */

public class Morph {

    public List<Point> points;

    public Morph(List<Point> points) {

        this.points = new ArrayList<>();
        this.points.addAll(points
                .stream()
                .map(p -> Point.makePoint(p.x, p.y))
                .collect(Collectors.toList())
        );
    }

    public Point anchorPoint() {
        return points.get(0);
    }

    public Morph roundMorph() {
        if (points.size() < 3)
            return this;

        List<Point> newPoints = new ArrayList<>();
        double mx = medX();
        double my = medY();
        double firstX = points.get(0).x - mx;
        double firstY = points.get(0).y - my;
        double theta0 = /*0.5*PI*/ -atan2(firstY, firstX);
        double secondX = points.get(1).x - mx;
        double secondY = points.get(2).y - my;
        int orient = (int) signum(firstX * secondY - firstY * secondX);
        int n = points.size();
        double r = radius();
        double theta;

        for (int i = 0; i < n; i++) {
            theta = orient * 2 * PI * i / n - theta0;
            newPoints.add(Point.makePoint(mx + r * cos(theta),
                    my + r * sin(theta)));
        }
        //System.out.printf("Morph radius %.2f%n", r);
        return new Morph(newPoints);
    }

    public static Morph normalize(Morph source, int pointLimit) {
        if (source.points.size() < pointLimit)
            throw new AssertionError("Source oneMorph has less points than pointLimit");

        int sourcePoints = source.points.size();
        int excess = sourcePoints - pointLimit;
        int gap = sourcePoints / (excess + 1);
        List<Point> filteredPoints =
                IntStream.range(0, source.points.size())
                        .filter(i -> (i+1) % gap != 0 || i >= gap * excess)
                        .mapToObj(source.points::get)
                        .collect(Collectors.toList());

//        System.out.printf("source %d, limit %d and normalised morph %d%n",
//                source.points.size(), pointLimit, filteredPoints.size());

        return new Morph(filteredPoints);
    }
    //get morph object with path
    public static Morph getMorph(Path p) {
        List<Point> points = new ArrayList<>();
        points.add(Point.makePoint(((MoveTo)p.getElements().get(0)).getX(), ((MoveTo)p.getElements().get(0)).getY()));
        System.out.println(((MoveTo)p.getElements().get(0)).getX()+" "+((MoveTo)p.getElements().get(0)).getY());
        int k = p.getElements().size()-1;
        for (int i=1;i<k;i++) {
            points.add(Point.makePoint(((LineTo)p.getElements().get(i)).getX(), ((LineTo)p.getElements().get(i)).getY()));
        }
        return new Morph(points);
    }
    //get path object with morph
    public static Path getPath(Morph m) {
        Path newPath = new Path(new MoveTo(m.points.get(0).x,m.points.get(0).y));
        for (int i=1;i<m.points.size();i++) {
            newPath.getElements().add(new LineTo(m.points.get(i).x,m.points.get(i).y));
        }
        newPath.getElements().add(new LineTo(m.points.get(0).x,m.points.get(0).y));
        return newPath;
    }

    private double medX() {
        double x = 0;
        for (Point p : points) {
            x += p.x;
        }
        return x / points.size();
    }

    private double medY() {
        double y = 0;
        for (Point p : points) {
            y += p.y;
        }
        return y / points.size();
    }

    private double area() {
        double res = 0;
        if (points.size() < 3)
            return res;
        double cx = medX();
        double cy = medY();
        double dx1, dx2, dy1, dy2;
        for (int i = 0; i < points.size() - 1; i++) {
            dx1 = points.get(i).x - cx;
            dy1 = points.get(i).y - cy;
            dx2 = points.get(i + 1).x - cx;
            dy2 = points.get(i + 1).y - cy;
            res += 0.5 * abs(dx1 * dy2 - dx2 * dy1);
        }
        dx1 = points.get(points.size() - 1).x - cx;
        dy1 = points.get(points.size() - 1).y - cy;
        dx2 = points.get(0).x - cx;
        dy2 = points.get(0).y - cy;
        res += 0.5 * abs(dx1 * dy2 - dx2 * dy1);

        return res;
    }

    private double radius() {
        return sqrt(area() / PI);
    }
}