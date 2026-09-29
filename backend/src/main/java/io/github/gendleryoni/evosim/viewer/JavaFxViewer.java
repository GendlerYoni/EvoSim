package io.github.gendleryoni.evosim.viewer;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class JavaFxViewer extends Application {

    private static final Color BACKGROUND_COLOR =
            Color.web("#FFF9C4");

    private static final Color FOOD_COLOR =
            Color.PURPLE;

    private static final double HERBIVORE_SATURATION = 0.75;
    private static final double HERBIVORE_BRIGHTNESS = 0.85;

    private Canvas canvas;
    private GraphicsContext graphicsContext;

    private JavaFxSimulationRunner runner;
    private AnimationTimer animationTimer;

    @Override
    public void start(Stage stage) {
        runner = new JavaFxSimulationRunner();

        canvas = new Canvas(
                runner.getWorldWidth(),
                runner.getWorldHeight()
        );

        graphicsContext =
                canvas.getGraphicsContext2D();

        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root);

        stage.setTitle("EvoSim - JavaFX Viewer");
        stage.setScene(scene);

        runner.drawCurrentState(this);

        stage.show();

        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                runner.update(
                        now,
                        JavaFxViewer.this
                );
            }
        };

        animationTimer.start();
    }

    @Override
    public void stop() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }

    void clear() {
        graphicsContext.setFill(
                BACKGROUND_COLOR
        );

        graphicsContext.fillRect(
                0.0,
                0.0,
                canvas.getWidth(),
                canvas.getHeight()
        );
    }

    void drawFood(
            double x,
            double y,
            double radius
    ) {
        drawCircle(
                x,
                y,
                radius,
                FOOD_COLOR
        );
    }

    void drawHerbivore(
            double x,
            double y,
            double radius,
            double hue
    ) {
        Color color = Color.hsb(
                hue,
                HERBIVORE_SATURATION,
                HERBIVORE_BRIGHTNESS
        );

        drawCircle(
                x,
                y,
                radius,
                color
        );
    }

    private void drawCircle(
            double centerX,
            double centerY,
            double radius,
            Color color
    ) {
        double diameter =
                radius * 2.0;

        graphicsContext.setFill(color);

        graphicsContext.fillOval(
                centerX - radius,
                centerY - radius,
                diameter,
                diameter
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}