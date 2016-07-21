package shapechanger;

/**
 * Created with IntelliJ IDEA.
 * User: abx
 * Date: 21/04/2016
 * Time: 5:42 PM
 * Created for ass2 in package shapechanger
 * @version 1.0
 * @author abx
 * @author Quyu Kong u5862608
 */

class Point {

    public final double x, y;

    private Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static Point makePoint(double x, double y) {
        return new Point(x, y);
    }
}
