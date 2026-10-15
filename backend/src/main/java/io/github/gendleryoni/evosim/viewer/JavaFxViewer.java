
package io.github.gendleryoni.evosim.viewer;

import io.github.gendleryoni.evosim.SimulationSession;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class JavaFxViewer extends Application {

    private static final Color BACKGROUND_COLOR =
            Color.web("#FFF9C4");

    private static final Color FOOD_COLOR =
            Color.PURPLE;

    private static final Color EGG_COLOR =
            Color.DARKORANGE;

    private static final double HERBIVORE_SATURATION = 0.75;
    private static final double HERBIVORE_BRIGHTNESS = 0.85;

    private Canvas canvas;
    private GraphicsContext graphicsContext;

    private JavaFxSimulationRunner runner;
    private AnimationTimer animationTimer;

    private Button pauseResumeButton;
    private Label stateLabel;
    private Label speedLabel;

    @Override
    public void start(Stage stage) {
        runner = new JavaFxSimulationRunner();

        canvas = new Canvas(
                runner.getWorldWidth(),
                runner.getWorldHeight()
        );

        graphicsContext = canvas.getGraphicsContext2D();

        BorderPane root = new BorderPane();
        root.setCenter(canvas);
        root.setTop(createControls());

        Scene scene = new Scene(root);

        stage.setTitle("EvoSim - JavaFX Viewer");
        stage.setScene(scene);

        runner.drawCurrentState(this);
        refreshControls();

        stage.show();

        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Use the same clock as the button handlers.
                runner.update(
                        System.nanoTime(),
                        JavaFxViewer.this
                );

                refreshControls();
            }
        };

        animationTimer.start();
    }

    private VBox createControls() {
        pauseResumeButton = new Button("Pause");
        pauseResumeButton.setOnAction(event -> {
            if (runner.getState() == SimulationSession.State.RUNNING) {
                runner.pause(System.nanoTime());

                // Pausing may have processed additional ticks.
                runner.drawCurrentState(this);

            } else if (runner.getState()
                    == SimulationSession.State.PAUSED) {
                runner.resume();
            }

            refreshControls();
        });

        Button resetButton = new Button("Reset");
        resetButton.setOnAction(event -> {
            runner.reset();
            runner.drawCurrentState(this);
            refreshControls();
        });

        stateLabel = new Label();
        speedLabel = new Label();

        HBox statusRow = new HBox(
                15,
                pauseResumeButton,
                resetButton,
                stateLabel,
                speedLabel
        );

        statusRow.setAlignment(Pos.CENTER);

        HBox speedRow = new HBox(
                8,
                new Label("Change speed:"),
                createSpeedButton("-10", -10),
                createSpeedButton("-5", -5),
                createSpeedButton("-2", -2),
                createSpeedButton("-1", -1),
                createSpeedButton("+1", 1),
                createSpeedButton("+2", 2),
                createSpeedButton("+5", 5),
                createSpeedButton("+10", 10)
        );

        speedRow.setAlignment(Pos.CENTER);

        VBox controls = new VBox(
                10,
                statusRow,
                speedRow
        );

        controls.setPadding(new Insets(10));

        controls.setAlignment(Pos.CENTER);
        
        return controls;
    }

    private Button createSpeedButton(String text, int delta) {
        Button button = new Button(text);

        button.setOnAction(event -> {
            runner.changeSpeed(delta, System.nanoTime());

            // Changing speed may also process elapsed ticks.
            runner.drawCurrentState(this);

            refreshControls();
        });

        return button;
    }

    private void refreshControls() {
        SimulationSession.State state = runner.getState();

        stateLabel.setText("State: " + state);
        speedLabel.setText(
                "Speed: " + runner.getSpeedMultiplier() + "x"
        );

        switch (state) {
            case RUNNING -> {
                pauseResumeButton.setText("Pause");
                pauseResumeButton.setDisable(false);
            }

            case PAUSED -> {
                pauseResumeButton.setText("Resume");
                pauseResumeButton.setDisable(false);
            }

            case FINISHED -> {
                pauseResumeButton.setText("Finished");
                pauseResumeButton.setDisable(true);
            }
        }
    }

    @Override
    public void stop() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }

    void clear() {
        graphicsContext.setFill(BACKGROUND_COLOR);

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

    void drawEgg(
            double x,
            double y,
            double radius
    ) {
        drawCircle(
                x,
                y,
                radius,
                EGG_COLOR
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

    void drawPredator(
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

        drawSquare(
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
        double diameter = radius * 2.0;

        graphicsContext.setFill(color);

        graphicsContext.fillOval(
                centerX - radius,
                centerY - radius,
                diameter,
                diameter
        );
    }

    private void drawSquare(
            double centerX,
            double centerY,
            double radius,
            Color color
    ) {
        double sideLength = radius * Math.sqrt(2.0);

        graphicsContext.setFill(color);

        graphicsContext.fillRect(
                centerX - sideLength / 2.0,
                centerY - sideLength / 2.0,
                sideLength,
                sideLength
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}
