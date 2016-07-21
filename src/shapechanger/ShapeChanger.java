package shapechanger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.shape.PathElement;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * The skeleton code for comp6700.2016 assignment 2.
 * Draws free-hand shapes and morphs one into another.
 *
 * Created with IntelliJ IDEA.
 * User: abx
 * Date: 21/04/2016
 * Time: 5:46 PM
 * Created for ass2 in package shapechanger
 *
 * @author abx
 * @author Quyu Kong u5862608
 * @version 2.0
 *
 * @see Morph
 */

public class ShapeChanger extends Application {

    private static final double useOfScreenFactor = 0.8;
    private static final int PointsLimitationForEllipse = 300;
    private static final boolean ShowCoordinates = false;

    private static Map<Predicate<KeyEvent>, Consumer<KeyEvent>> keyEventSelectors =
            new HashMap<>();
    // these are for smoother-like part, for drawing and splotching
    private final Path onePath = new Path();
    private final ArrayList<Path> Paths = new ArrayList<>();
    private Path selectedPath;
    private Path morphPath = new Path();
    private final ArrayList<Point> points = new ArrayList<>();
    private final ArrayList<Morph> Morphs = new ArrayList<>();
    private Morph selectedMorph;
    private Point2D anchorPt;
    private Point currentPoint;
    private Point lastPoint;
    private State state = State.CLEAR; // at the start there is no paths
    private boolean saved = true;
    private boolean selectionMode = false;
    private boolean allowMorph = false;
    private boolean animating = false;
    private MenuBar menuBar = new MenuBar();
    private final Menu menu1 = new Menu("File");
    private final Menu menu2 = new Menu("Edit");
    private final Menu menu3 = new Menu("Morph");

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        launch(args);
    }


    /**
     * override start method which creates the scene
     * and all nodes and shapes in it (main window only),
     * and redefines how the nodes react to user inputs
     * and other events;
     *
     * @param primaryStage Stage (the top level container)
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
//        Parent root = FXMLLoader.load(getClass().getResource("view.fxml"));

        primaryStage.setTitle("Shape Changing Objects");

        /* next two lines are needed to read command-line args
         * -- such are JavaFX's awkward ways
         */
//        Parameters parameters = getParameters();
//        String fontFileName = parameters.getRaw().get(0);

        final Group root = new Group();
        root.getChildren().add(onePath);
        
        /* reading the screen size and using it to set up all
         * necessary dimensions, scaling factor and locations */
        Rectangle2D screenBound = Screen.getPrimary().getBounds();
        double screenWidth = screenBound.getWidth();
        double screenHeight = screenBound.getHeight();
        final Scene scene = new Scene(root, screenWidth * useOfScreenFactor,
                screenHeight * useOfScreenFactor, Color.WHEAT);
        menuBar = new MenuBar();
        
        //File part
        //save item
        MenuItem saveItem = new MenuItem("Save");
        saveItem.setOnAction(event -> {
            if (state == State.CLEAR) {
                Alert nothingToSaveError = new Alert(Alert.AlertType.INFORMATION,"You haven't drawn anything.");
                nothingToSaveError.showAndWait();
                return;
            }
            
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save shapes");
            fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Text Files", "*.txt"));
            try{
                File selectedFile = fileChooser.showSaveDialog(primaryStage);
                if (selectedFile == null) return;
                FileWriter fw = new FileWriter(selectedFile);
                fw.write(Paths.size()+" ");
                for (Path p: Paths) {
                    fw.write(pathToString(p));//call a method to convert a path to string
                }
                fw.close();
                saved = true;
            } catch (IOException e) {
                System.out.println("Failed to save shapes!");
            }
            
        });
        //open item
        MenuItem openItem = new MenuItem("Open");
        openItem.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open shapes");
            fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Text Files", "*.txt"));
            if (saved == false) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "You haven't saved your shapes yet, are you sure to open other shapes?");
                Optional<ButtonType> answer = alert.showAndWait();
                if (answer.get().getText().equals("Cancel")) {
                    return;
                }
            }
            try{
                File selectedFile = fileChooser.showOpenDialog(primaryStage);
                if (selectedFile == null) return;
                Scanner sc = new Scanner(selectedFile); 
                int count = sc.nextInt();
                state = State.START;
                for (Path p: Paths) {
                    p.getElements().clear();
                    root.getChildren().remove(p);
                }
                Paths.clear();
                Morphs.clear();
                for (int i=0;i<count;i++) {
                    points.clear();
                    Paths.add(stringToPath(sc,points));//call another method to read one path
                    root.getChildren().add(Paths.get(Paths.size()-1));
                    Paths.get(Paths.size()-1).setStrokeWidth(1);
                    Paths.get(Paths.size()-1).setFill(Color.DARKGRAY);
                    Morphs.add(new Morph(points));
                }
                sc.close();
                saved = true;
            } catch(FileNotFoundException e){
                System.out.println("No such a file.");
            }
        });
        MenuItem quitItem = new MenuItem("Quit");
        quitItem.setOnAction(event -> {
            if (saved == false) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "You haven't saved your shapes yet, are you sure to quit?");
                Optional<ButtonType> answer = alert.showAndWait();
                if (answer.get().getText().equals("OK")) {
                    Platform.exit();
                }
            } else {
                Platform.exit();
            }
        });
        menu1.getItems().addAll(saveItem,openItem,new SeparatorMenuItem(),quitItem);
        
        //Edit part
        MenuItem selectItem = new MenuItem("Select");
        selectItem.setOnAction(e -> {
            if (state == State.CLEAR) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION,"No shape is available! Please draw something.");
                alert.showAndWait();
                return;
            }
            primaryStage.setTitle("Selection mode");
            selectionMode = true;
            for (Path p: Paths) {
                p.getStrokeDashArray().addAll(2d);
                p.setFill(Color.HONEYDEW);
                // add mouse move reactions for selection mode
                p.setOnMouseEntered(event -> {
                    ((Path)event.getSource()).toFront();
                    ((Path)event.getSource()).setFill(Color.BLUE);
                });
                p.setOnMouseExited(event -> {
                    ((Path)event.getSource()).setFill(Color.HONEYDEW);
                });
                p.setOnMouseClicked(event -> {
                    primaryStage.setTitle("Shape Changing Objects");
                    turnOffSelectionMode();
                    ((Path)event.getSource()).setFill(Color.BLUE);
                    selectedPath = ((Path)event.getSource());
                    selectedMorph = Morphs.get(Paths.indexOf((Path)event.getSource()));
                });
            }
        });
        MenuItem clearItem = new MenuItem("Clear");
        clearItem.setOnAction(event -> {
            for (Path p: Paths) {
                p.getElements().clear();
                root.getChildren().remove(p);
            }
            Paths.clear();
            Morphs.clear();
            points.clear();
            morphPath.getElements().clear();
            state = State.CLEAR;
            saved = true;
            allowMorph = false;
        });
        menu2.getItems().addAll(selectItem,clearItem);
        
        //Morph part
        //Both ellipse and rectangle share one eventhandler to save codes
        EventHandler eventHandlerForEllipseAndRectangle = event -> {
            if (pathIsNull()) return;
            double[] edgePoints = new double[4];
            if (((MenuItem)event.getSource()).getText().equals("Ellipse")){
                selectedMorph = Morph.getMorph(selectedPath);
                addPoints();
            }
            Morph backupMorph = selectedMorph;
            double angle = getAngle(selectedMorph);
            getBoundingBox(selectedMorph, edgePoints);
            double midX = (edgePoints[0]+edgePoints[1])/2;
            double midY = (edgePoints[2]+edgePoints[3])/2;
            selectedMorph = rotate(selectedMorph, angle);
            getBoundingBox(selectedMorph, edgePoints);
            if (((MenuItem)event.getSource()).getText().equals("Ellipse")){
                selectedMorph = normaliseEllipse(selectedMorph, edgePoints);
            }else {
                selectedMorph = normaliseRectangle(selectedMorph, edgePoints);
            }
            selectedMorph = rotate(selectedMorph,-angle,midX,midY);
            morphPath = Morph.getPath(selectedMorph);
            showMorphPath();
            root.getChildren().add(morphPath);
            selectedMorph = backupMorph;
        };
        MenuItem triangleItem = new MenuItem("Triangle");
        triangleItem.setOnAction(event -> {
            if (pathIsNull()) return;
            Morph backupMorph = selectedMorph;
            double[] edgePoints = new double[4];
            getBoundingBox(selectedMorph, edgePoints);
            morphPath = normaliseTriangle(selectedMorph, edgePoints);
            showMorphPath();
            root.getChildren().add(morphPath);
            selectedMorph = backupMorph;
        });
        MenuItem ellipseItem = new MenuItem("Ellipse");
        ellipseItem.setOnAction(eventHandlerForEllipseAndRectangle);
        MenuItem rectangleItem = new MenuItem("Rectangle");
        rectangleItem.setOnAction(eventHandlerForEllipseAndRectangle);
        MenuItem polygonItem = new MenuItem("Polygon");
        polygonItem.setOnAction(event -> {
            if (pathIsNull()) return;
            try{
                boolean legal = false;
                while (legal == false) {
                    TextInputDialog edgeNumIput = new TextInputDialog("Please enter the number of sides:");
                    Optional<String> results = edgeNumIput.showAndWait();
                    int edgeNum = Integer.parseInt(results.get());
                    if (edgeNum < 3) {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Please enter a number which is greater than 2.");
                        alert.showAndWait();
                    } else if (edgeNum > selectedMorph.points.size()){
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Sorry, this numer is too large.");
                        alert.showAndWait();
                    } else {
                        Morph backupMorph = selectedMorph;
                        double[] edgePoints = new double[4];
                        getBoundingBox(selectedMorph, edgePoints);
                        morphPath = normalisePolygon(selectedMorph, edgePoints, edgeNum);
                        showMorphPath();
                        root.getChildren().add(morphPath);
                        selectedMorph = backupMorph;
                        legal = true;
                    }
                }
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Please enter a number!");
                alert.showAndWait();
                System.out.println(e);
            } 
        });
        menu3.getItems().addAll(triangleItem,ellipseItem,rectangleItem,polygonItem);

        menuBar.getMenus().addAll(menu1,menu2,menu3);
        root.getChildren().addAll(menuBar);
        menuBar.setPrefWidth(screenWidth);

        // starting initial path
        scene.setOnMousePressed(event ->
        {
            if (animating == true) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION,"It's morphing now, please try agian later.");
            alert.showAndWait();
            return;
            }
            if (selectionMode == true) {return;}
            saved = false;
            if (allowMorph == true) {
                allowMorph = false;
                morphPath.getElements().clear();
            }
            if (state == State.CLEAR) state = State.START;
            anchorPt = new Point2D(event.getX(), event.getY());
            // clean points which comprise a path to be drawn and start anew
            points.clear();
            onePath.setOpacity(1);
            onePath.setFill(null);
            points.add(Point.makePoint(anchorPt.getX(), anchorPt.getY()));

            onePath.getElements().clear();
            onePath.setStrokeWidth(3);
            onePath.setStrokeDashOffset(0.7);
            onePath.setStroke(Color.BLACK);
            onePath.getElements()
                    .add(new MoveTo(anchorPt.getX(), anchorPt.getY()));
        });

        // dragging creates lineTos added to the path
        scene.onMouseDraggedProperty().set(event ->
        {
            if (selectionMode == true) {return;}
            currentPoint = Point.makePoint(event.getX(), event.getY());
            points.add(currentPoint);
            onePath.getElements()
                    .add(new LineTo(currentPoint.x, currentPoint.y));
        });

        // end onePath or twoPath (depending on which
        // is being drawn) when mouse released event
        scene.onMouseReleasedProperty().set(event ->
        {
            if (selectionMode == true) {return;}
//            System.out.printf("Switching from %s -> ", state);
            lastPoint = Point.makePoint(event.getX(), event.getY());
            points.add(lastPoint);
            onePath.getElements().add(new LineTo(lastPoint.x, lastPoint.y));
            onePath.getElements().add(new LineTo(anchorPt.getX(), anchorPt.getY()));
            Paths.add(new Path(onePath.getElements()));
            root.getChildren().add(Paths.get(Paths.size()-1));
            Paths.get(Paths.size()-1).setStrokeWidth(1);
            Paths.get(Paths.size()-1).setFill(Color.DARKGRAY);
            onePath.getElements().clear();
            Morphs.add(new Morph(points));
            System.out.printf("The size of this path is %d%n", points.size());
        });

        // simple event handlers (key board inputs which initiate transitions
        scene.onKeyPressedProperty().set(keyEvent ->
        {
            if (keyEvent.isMetaDown() && keyEvent.getCode() == KeyCode.M) {
                if (allowMorph == true) {
                    if (selectedPath.getElements().size() > 0 && morphPath.getElements().size() >0){
                        final Timeline timeline = makeTimeline(selectedPath,morphPath);
                        animating = true;
                        timeline.play();
                    }
                }
            }
        });
        if (ShowCoordinates == true) {
            Label ss = new Label();
            scene.setOnMouseMoved(event->{
                ss.setText(""+event.getX()+" "+event.getY());
            });
            root.getChildren().add(ss);
        }
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> Platform.exit());

    }

    private Timeline makeTimeline(Path p1, Path p2) {
        assert p1.getElements().size() == p2.getElements().size() : "uneven paths";
        final Timeline timeline = new Timeline();
        timeline.setCycleCount(1);//(Timeline.INDEFINITE);
        timeline.setAutoReverse(false);
        int n = p1.getElements().size();
        KeyValue kvx, kvy;
        KeyFrame kf;
        MoveTo ap1, ap2;
        LineTo pe1, pe2;
        ap1 = (MoveTo) p1.getElements().get(0);
        ap2 = (MoveTo) p2.getElements().get(0);
        kvx = new KeyValue(ap1.xProperty(), ap2.getX());
        kvy = new KeyValue(ap1.yProperty(), ap2.getY());
        kf = new KeyFrame(Duration.millis(5000), kvx, kvy);
        timeline.getKeyFrames().add(kf);
        for (int i = 1; i < n; i++) {
            pe1 = (LineTo) p1.getElements().get(i);
            pe2 = (LineTo) p2.getElements().get(i);
            kvx = new KeyValue(pe1.xProperty(), pe2.getX());
            kvy = new KeyValue(pe1.yProperty(), pe2.getY());
            kf = new KeyFrame(Duration.millis(5000), kvx, kvy);
            timeline.getKeyFrames().add(kf);
        }
        timeline.setOnFinished(event -> animating = false);
        return timeline;
    }
    //convert a path into a string
    private String pathToString(Path targetPath) {
        ObservableList<PathElement> elements = targetPath.getElements();
        String stringOfThePath = ""+elements.size()+" "+((MoveTo)elements.get(0)).getX() + " " + ((MoveTo)elements.get(0)).getY()+" ";
        for (int i = 1; i < targetPath.getElements().size(); i++) {
            stringOfThePath = stringOfThePath + ((LineTo)elements.get(i)).getX() + " " + ((LineTo)elements.get(i)).getY()+" ";
        }
        return stringOfThePath;
    }
    //read from file and create a new path
    private Path stringToPath(Scanner sc, ArrayList<Point> pts) {
        Path tempPath = new Path();
        int elementsNum = sc.nextInt();
        pts.add(Point.makePoint(sc.nextDouble(),sc.nextDouble()));
        tempPath.getElements().add(new MoveTo(pts.get(0).x,pts.get(0).y));
        for (int i=1;i<elementsNum;i++){
            pts.add(Point.makePoint(sc.nextDouble(),sc.nextDouble()));
            tempPath.getElements().add(new LineTo(pts.get(i).x,pts.get(i).y));
        }
        pts.remove(pts.size()-1);
        return tempPath;
    }
    
    private void turnOffSelectionMode() {
        for (Path p: Paths) {
            p.setOnMouseEntered(null);
            p.setOnMouseExited(null);
            p.setOnMouseClicked(null);
            p.getStrokeDashArray().remove(0);
            p.setFill(Color.DARKGRAY);
        }
        selectionMode = false;
        allowMorph = false;
    }
    
    private void getBoundingBox(Morph m, double[] pts) {
        double minX,maxX,minY,maxY;
        minX = Double.MAX_VALUE;
        minY = Double.MAX_VALUE;
        maxX = Double.MIN_VALUE;
        maxY = Double.MIN_VALUE;
        for (int i=0;i<m.points.size();i++) {
            if (m.points.get(i).x<minX) {minX = m.points.get(i).x; }
            if (m.points.get(i).x>maxX) {maxX = m.points.get(i).x; }
            if (m.points.get(i).y<minY) {minY = m.points.get(i).y; }
            if (m.points.get(i).y>maxY) {maxY = m.points.get(i).y; }
        }
        pts[0] = minX;
        pts[1] = maxX;
        pts[2] = minY;
        pts[3] = maxY;

    }
    
    private Morph normaliseRectangle(Morph m, double[] pts) {
        ArrayList<Point> points = new ArrayList<>();
        points.add(Point.makePoint(pts[0],pts[2]));

        int intervalPoints = (m.points.size()-4)/4;
        double intervalY = (pts[3]-pts[2])/(intervalPoints+1);
        double intervalX = (pts[1]-pts[0])/(intervalPoints+1);
        for (int i=1;i<intervalPoints+2;i++) {
            points.add(Point.makePoint(pts[0],pts[2]+i*intervalY));
        }
        
        for (int i=1;i<intervalPoints+2;i++) {
            points.add(Point.makePoint(pts[0]+i*intervalX,pts[3]));
        }
        
        for (int i=1;i<intervalPoints+2;i++) {
            points.add(Point.makePoint(pts[1],pts[3]-i*intervalY));
        }
        intervalPoints = m.points.size()-4-intervalPoints*3;
        intervalX = (pts[1]-pts[0])/(intervalPoints+1);
        for (int i=1;i<intervalPoints+1;i++) {
            points.add(Point.makePoint(pts[1]-i*intervalX,pts[2]));
        }
        
        return new Morph(points);
    }
    
    private Path normaliseTriangle(Morph m, double[] pts){
        CoordinateGetter cg = new CoordinateGetter(Point.makePoint(pts[0], pts[3]), Point.makePoint(pts[1], pts[3]), Point.makePoint((pts[0]+pts[1])/2, pts[2]));
        Path newPath = new Path(new MoveTo(pts[0],pts[3]));
        double midX = (pts[0]+pts[1])/2d;
        int intervalPoints = (m.points.size()-3)/3;
        double intervalX = (midX-pts[0])/(intervalPoints+1);
        for (int i=1;i<(intervalPoints+1)*2+1;i++) {
            newPath.getElements().add(new LineTo(pts[0]+intervalX*i,cg.getY(pts[0]+intervalX*i).get(1)));
        }
        intervalPoints = m.points.size()-3-intervalPoints*2;
        intervalX = (pts[1]-pts[0])/(intervalPoints+1);
        for (int i=1;i<intervalPoints+2;i++) {
            newPath.getElements().add(new LineTo(pts[1]-intervalX*i,pts[3]));
        }
        return newPath;
    }
    
    private double distance(Point p1, Point p2) {
        return Math.sqrt((p2.y-p1.y)*(p2.y-p1.y)+(p2.x-p1.x)*(p2.x-p1.x));
    }
    
    //To make the ellipse smoother, I chose to add more points to the oroginal path
    private void addPoints() {
        if (selectedMorph.points.size()>=PointsLimitationForEllipse) return;
        selectedMorph.points.add(selectedMorph.points.get(0));
        int numberOfPoints = PointsLimitationForEllipse-selectedMorph.points.size();
        double totalDis = 0;
        int pointNum = selectedMorph.points.size();
        for (int i=0;i<pointNum-1;i++){
            totalDis += distance(selectedMorph.points.get(i),selectedMorph.points.get(i+1));
        }
        int k = 0;
        for (int i=0;i<pointNum-1;i++) {
            int thisNum = (int)(numberOfPoints*(distance(selectedMorph.points.get(k),selectedMorph.points.get(k+1))/totalDis));
            insertPoints(k, thisNum);
            k = k+thisNum+1;
        }
        selectedMorph.points.remove(selectedMorph.points.size()-1);
        selectedPath.getElements().clear();
        selectedPath.getElements().add(new MoveTo(selectedMorph.points.get(0).x,selectedMorph.points.get(0).y));
        for (int i=1;i<selectedMorph.points.size();i++) {
            selectedPath.getElements().add(new LineTo(selectedMorph.points.get(i).x,selectedMorph.points.get(i).y));
        }
        selectedPath.getElements().add(new LineTo(selectedMorph.points.get(0).x,selectedMorph.points.get(0).y));
    }
    
    private void insertPoints(int i, int points) {
        Point p1 = selectedMorph.points.get(i);
        Point p2 = selectedMorph.points.get(i+1);
        double dx = p2.x-p1.x;
        double dy = p2.y-p1.y;
        double interval = dx/(points+1);
        double intervalY = dy/(points+1);
        for (int j=0; j<points; j++) {
            if (p2.x-p1.x == 0) {
                Point newPoint = Point.makePoint(p1.x, p1.y+intervalY*(j+1));
                selectedMorph.points.add(i+1,newPoint);
            }else {
                double x = p2.x-interval*(j+1);
                Point newPoint = Point.makePoint(x, (p2.y-p1.y)*(x-p1.x)/(p2.x-p1.x)+p1.y);
                selectedMorph.points.add(i+1,newPoint);
            }  
        }
    }
    
    private Morph normaliseEllipse(Morph m, double[] pts) {
        CoordinateGetter cg = new CoordinateGetter(pts[0], pts[1], pts[2], pts[3]);
        ArrayList<Point> points = new ArrayList<>();
        double midY = (pts[2]+pts[3])/2;
        points.add(Point.makePoint(pts[0],midY));
        int intervalPoints = (m.points.size()-2)/2;
        double interval = (pts[1]-pts[0])/(intervalPoints+1);
        for (int i=1;i<=intervalPoints+1;i++){
            double x = pts[0]+interval*i;
            points.add(Point.makePoint(x,cg.getY(x).get(0)));
        }
        intervalPoints = m.points.size()-2-intervalPoints;
        interval = (pts[1]-pts[0])/(intervalPoints+1);
        for (int i=1;i<intervalPoints+1;i++) {
            double x = pts[1]-interval*i;
            points.add(Point.makePoint(x,cg.getY(x).get(1)));
        }
        return new Morph(points);
    }
    
    private Path normalisePolygon(Morph m, double[] pts, int edgeNum) {
        double midY = (pts[2]+pts[3])/2;
        double midX = (pts[0]+pts[1])/2;
        CoordinateGetter cg = new CoordinateGetter(pts[0], pts[1], pts[2], pts[3], edgeNum);        
        double startY = midY-cg.radius;
        Path newPath = new Path(new MoveTo(midX,startY));
        System.out.println(pts[0]+" "+midY);
        int intervalPoints = (m.points.size()-edgeNum)/edgeNum;
        double interval = 360d/(edgeNum*(intervalPoints+1));
        for (int i=1;i<=(intervalPoints+1)*(edgeNum-1);i++) {
            double angle = Math.toRadians(interval*i);
            double l = cg.getL(angle);
            newPath.getElements().add(new LineTo(midX+l*Math.sin(angle),midY-l*Math.cos(angle)));
        }
        intervalPoints = (m.points.size()-edgeNum-intervalPoints*(edgeNum-1));
        interval = 360d/(edgeNum*(intervalPoints+1));
        for (int i=1;i<=intervalPoints+1;i++) {
            double angle = Math.toRadians(360d*(edgeNum-1)/edgeNum+interval*i);
            double l = cg.getL(angle);
            newPath.getElements().add(new LineTo(midX+l*Math.sin(angle),midY-l*Math.cos(angle)));
        }
        return newPath;
    }
    
    private boolean pathIsNull() {
        if (selectedPath == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION,"No shape is selected.");
            alert.showAndWait();
            return true;
        } else if (animating == true) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION,"It's morphing now, please try agian later.");
            alert.showAndWait();
            return true;
        }
        if (morphPath != null) {
            morphPath.getElements().clear();
        }
        return false;
    }
    
    private void showMorphPath() {
        morphPath.setStroke(Color.BLACK);
        morphPath.setStrokeWidth(1);
        morphPath.getStrokeDashArray().addAll(2d);
        allowMorph = true;
    }
    
    //rotate according to its own centre
    private Morph rotate(Morph m, double angle) {
        ArrayList<Point> pts = new ArrayList<>();
        double[] pt = new double[4];
        getBoundingBox(m, pt);
        double midX = (pt[0]+pt[1])/2;
        double midY = (pt[2]+pt[3])/2;
        for (Point p:m.points) {
            pts.add(m.points.indexOf(p), Point.makePoint(((p.x-midX)*Math.cos(angle)-(p.y-midY)*Math.sin(angle))+midX, ((p.x-midX)*Math.sin(angle)+(p.y-midY)*Math.cos(angle))+midY));
        }
        return new Morph(pts);
    }
    //rotate according to a fixed centre
    private Morph rotate(Morph m, double angle, double midX, double midY) {
        ArrayList<Point> pts = new ArrayList<>();
        for (Point p:m.points) {
            
            pts.add(m.points.indexOf(p), Point.makePoint(((p.x-midX)*Math.cos(angle)-(p.y-midY)*Math.sin(angle))+midX, ((p.x-midX)*Math.sin(angle)+(p.y-midY)*Math.cos(angle))+midY));
        }
        return new Morph(pts);
    }
    
    private double getAngle(Morph m) {
        double maxDis = Double.MIN_VALUE;
        Point mp1 = null;
        Point mp2 = null;
        for (Point p1: m.points) {
            for (Point p2: m.points) {
                if (p1.equals(p2)&&p1.x>p2.x) continue;
                double dis = distance(p1,p2);
                if (dis>maxDis){
                    maxDis = dis;
                    mp1 = p1;
                    mp2 = p2;
                }
            }
        }
        if (mp1.x==mp2.x) return Math.PI/2;
        return Math.atan2(mp2.y-mp1.y, mp2.x-mp1.x);
    }
    
    enum State {CLEAR, START} /* to control the keyboard/mouse input */

}
