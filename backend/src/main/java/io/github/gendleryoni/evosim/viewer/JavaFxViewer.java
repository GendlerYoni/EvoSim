package io.github.gendleryoni.evosim.viewer;

import io.github.gendleryoni.evosim.SimulationSession;
import io.github.gendleryoni.evosim.statistics.GenomeAverages;
import io.github.gendleryoni.evosim.statistics.SimulationSnapshot;
import io.github.gendleryoni.evosim.statistics.StatisticsBatch;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class JavaFxViewer extends Application {

    private static final Color BACKGROUND_COLOR = Color.web("#FFF9C4");
    private static final Color FOOD_COLOR = Color.PURPLE;
    private static final Color EGG_COLOR = Color.DARKORANGE;
    private static final double HERBIVORE_SATURATION = 0.75;
    private static final double HERBIVORE_BRIGHTNESS = 0.85;

    private static final String HERBIVORE_LINE_COLOR = "#22A06B";
    private static final String PREDATOR_LINE_COLOR = "#E07832";
    private static final String FOOD_LINE_COLOR = "#9048C9";
    private static final double CHART_WIDTH = 460;
    private static final double CHART_HEIGHT = 362;
    private static final long STATS_REFRESH_NANOS = 100_000_000L;

    private Canvas canvas;
    private GraphicsContext graphicsContext;
    private JavaFxSimulationRunner runner;
    private AnimationTimer animationTimer;

    private Button pauseResumeButton;
    private Label stateLabel;
    private Label speedLabel;

    private ComboBox<Trait> traitSelector;
    private LineChart<Number, Number> evolutionChart;
    private LineChart<Number, Number> populationChart;
    private LineChart<Number, Number> foodChart;

    private XYChart.Series<Number, Number> herbivorePopulationSeries;
    private XYChart.Series<Number, Number> predatorPopulationSeries;
    private XYChart.Series<Number, Number> foodSeries;
    private XYChart.Series<Number, Number> currentHerbivoreEvolutionSegment;
    private XYChart.Series<Number, Number> currentPredatorEvolutionSegment;

    // This is only a local display window, not a second simulation history.
    private final List<SimulationSnapshot> displayedSamples = new ArrayList<>();
    private int firstDisplayedSampleIndex = 0;
    private int nextSampleIndex = 0;
    private long lastStatisticsRefreshNanos = -1L;

    private enum Trait {
        SPEED("Speed"),
        SIZE("Size"),
        SENSE_RADIUS("Sense Radius"),
        REPRODUCTION_THRESHOLD("Reproduction Threshold"),
        EGG_HATCH_TIME("Egg Hatch Time");

        private final String displayName;

        Trait(String displayName) {
            this.displayName = displayName;
        }

        double valueOf(GenomeAverages averages) {
            return switch (this) {
                case SPEED -> averages.speed();
                case SIZE -> averages.size();
                case SENSE_RADIUS -> averages.senseRadius();
                case REPRODUCTION_THRESHOLD -> averages.reproductionThreshold();
                case EGG_HATCH_TIME -> averages.eggHatchTime();
            };
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    @Override
    public void start(Stage stage) {
        runner = new JavaFxSimulationRunner();

        canvas = new Canvas(runner.getWorldWidth(), runner.getWorldHeight());
        graphicsContext = canvas.getGraphicsContext2D();

        VBox chartsColumn = new VBox(
                12,
                createEvolutionSection(),
                createPopulationSection()
        );
        chartsColumn.setAlignment(Pos.TOP_CENTER);

        HBox content = new HBox(12, canvas, chartsColumn);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(createControls());
        root.setCenter(content);

        Scene scene = new Scene(root);
        stage.setTitle("EvoSim - JavaFX Viewer");
        stage.setScene(scene);

        runner.drawCurrentState(this);
        refreshControls();
        stage.show();

        // The secondary chart must not cover the primary chart's plot.
        foodChart.applyCss();
        Node plotBackground = foodChart.lookup(".chart-plot-background");
        if (plotBackground != null) {
            plotBackground.setStyle("-fx-background-color: transparent;");
        }

        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                long nowNanos = System.nanoTime();
                runner.update(nowNanos, JavaFxViewer.this);
                refreshStatisticsIfNeeded(nowNanos);
                refreshControls();
            }
        };
        animationTimer.start();
    }

    // ---------------------------------------------------------
    // Statistics charts
    // ---------------------------------------------------------

    private VBox createEvolutionSection() {
        traitSelector = new ComboBox<>();
        traitSelector.getItems().addAll(Trait.values());
        traitSelector.setValue(Trait.SPEED);
        traitSelector.setPrefWidth(185);
        traitSelector.setOnAction(event -> rebuildEvolutionChart());

        Label title = new Label("Genome Evolution");
        HBox header = new HBox(12, title, traitSelector);
        header.setAlignment(Pos.CENTER_LEFT);

        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Tick");
        xAxis.setForceZeroInRange(false);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Genome Average");
        yAxis.setForceZeroInRange(false);
        formatTwoDecimals(yAxis);

        evolutionChart = createLineChart(xAxis, yAxis);

        HBox legend = new HBox(
                15,
                coloredLabel("Herbivores", HERBIVORE_LINE_COLOR),
                coloredLabel("Predators", PREDATOR_LINE_COLOR)
        );
        legend.setAlignment(Pos.CENTER);

        return new VBox(5, header, evolutionChart, legend);
    }

    private VBox createPopulationSection() {
        Label title = new Label("Population & Food");

        NumberAxis populationXAxis = new NumberAxis();
        populationXAxis.setLabel("Tick");
        populationXAxis.setForceZeroInRange(false);
        NumberAxis populationYAxis = new NumberAxis();
        populationYAxis.setLabel("Creatures");
        populationYAxis.setForceZeroInRange(false);

        populationChart = createLineChart(
                populationXAxis, populationYAxis
        );

        NumberAxis foodXAxis = new NumberAxis();
        foodXAxis.setLabel("Tick");
        foodXAxis.setAutoRanging(false);
        foodXAxis.lowerBoundProperty().bind(populationXAxis.lowerBoundProperty());
        foodXAxis.upperBoundProperty().bind(populationXAxis.upperBoundProperty());
        foodXAxis.tickUnitProperty().bind(populationXAxis.tickUnitProperty());
        foodXAxis.setOpacity(0); // Keep the layout space, hide duplicated labels.

        NumberAxis foodYAxis = new NumberAxis();
        foodYAxis.setLabel("Food");
        foodYAxis.setSide(Side.RIGHT);
        foodYAxis.setForceZeroInRange(false);

        foodChart = createLineChart(foodXAxis, foodYAxis);
        foodChart.setHorizontalGridLinesVisible(false);
        foodChart.setVerticalGridLinesVisible(false);
        foodChart.setHorizontalZeroLineVisible(false);
        foodChart.setVerticalZeroLineVisible(false);
        foodChart.setMouseTransparent(true);
        foodChart.setStyle("-fx-background-color: transparent;");

        herbivorePopulationSeries = new XYChart.Series<>();
        predatorPopulationSeries = new XYChart.Series<>();
        foodSeries = new XYChart.Series<>();

        colorSeries(herbivorePopulationSeries, HERBIVORE_LINE_COLOR);
        colorSeries(predatorPopulationSeries, PREDATOR_LINE_COLOR);
        colorSeries(foodSeries, FOOD_LINE_COLOR);
        populationChart.getData().addAll(
                herbivorePopulationSeries, predatorPopulationSeries
        );
        foodChart.getData().add(foodSeries);

        // Two charts share the plot area while keeping opposite Y axes.
        StackPane overlay = new StackPane();
        overlay.setPrefSize(CHART_WIDTH, CHART_HEIGHT);
        overlay.setMinSize(CHART_WIDTH, CHART_HEIGHT);
        overlay.setMaxSize(CHART_WIDTH, CHART_HEIGHT);

        populationChart.prefWidthProperty().bind(
                overlay.widthProperty().subtract(foodYAxis.widthProperty())
        );
        foodChart.prefWidthProperty().bind(
                overlay.widthProperty().subtract(populationYAxis.widthProperty())
        );

        overlay.getChildren().addAll(
                wrapWithOppositeAxisSpacer(
                        populationChart, foodYAxis, true
                ),
                wrapWithOppositeAxisSpacer(
                        foodChart, populationYAxis, false
                )
        );

        HBox legend = new HBox(
                12,
                coloredLabel("Herbivores", HERBIVORE_LINE_COLOR),
                coloredLabel("Predators", PREDATOR_LINE_COLOR),
                coloredLabel("Food", FOOD_LINE_COLOR)
        );
        legend.setAlignment(Pos.CENTER);

        return new VBox(5, title, overlay, legend);
    }

    private HBox wrapWithOppositeAxisSpacer(
            LineChart<Number, Number> chart,
            NumberAxis oppositeYAxis,
            boolean primary
    ) {
        Pane spacer = new Pane();
        spacer.minWidthProperty().bind(oppositeYAxis.widthProperty());
        spacer.prefWidthProperty().bind(oppositeYAxis.widthProperty());
        spacer.maxWidthProperty().bind(oppositeYAxis.widthProperty());

        HBox.setHgrow(chart, Priority.ALWAYS);
        return primary ? new HBox(chart, spacer) : new HBox(spacer, chart);
    }

    private LineChart<Number, Number> createLineChart(
            NumberAxis xAxis,
            NumberAxis yAxis
    ) {
        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setAnimated(false);
        chart.setCreateSymbols(false);
        chart.setLegendVisible(false);
        chart.setPrefSize(CHART_WIDTH, CHART_HEIGHT);
        chart.setMinWidth(0);
        chart.setMaxWidth(Double.MAX_VALUE);
        return chart;
    }

    private Label coloredLabel(String label, String color) {
        Label result = new Label("● " + label);
        result.setStyle("-fx-text-fill: " + color + ";");
        return result;
    }

    private void formatTwoDecimals(NumberAxis axis) {
        DecimalFormat format = new DecimalFormat(
                "0.##",
                DecimalFormatSymbols.getInstance(Locale.US)
        );
        axis.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number value) {
                return format.format(value.doubleValue());
            }

            @Override
            public Number fromString(String value) {
                return Double.parseDouble(value);
            }
        });
    }

    private void colorSeries(
            XYChart.Series<Number, Number> series,
            String color
    ) {
        series.nodeProperty().addListener((observable, previous, node) -> {
            if (node != null) {
                node.setStyle(
                        "-fx-stroke: " + color + "; -fx-stroke-width: 2;"
                );
            }
        });
    }

    private void refreshStatisticsIfNeeded(long nowNanos) {
        if (lastStatisticsRefreshNanos >= 0
                && nowNanos - lastStatisticsRefreshNanos
                < STATS_REFRESH_NANOS) {
            return;
        }
        lastStatisticsRefreshNanos = nowNanos;

        // This also runs in PAUSED and FINISHED, to fetch pending samples.
        StatisticsBatch batch = runner.getStatisticsFromIndex(nextSampleIndex);
        if (batch.snapshots().isEmpty()) {
            return;
        }

        int returnedStartIndex =
                batch.nextSampleIndex() - batch.snapshots().size();

        if (returnedStartIndex > nextSampleIndex) {
            // JavaFX fell behind the circular buffer. Discard stale UI data.
            clearChartData();
            firstDisplayedSampleIndex = returnedStartIndex;
        }

        int interval = runner.getStatisticsIntervalTicks();
        for (int i = 0; i < batch.snapshots().size(); i++) {
            int sampleIndex = returnedStartIndex + i;
            SimulationSnapshot snapshot = batch.snapshots().get(i);

            if (displayedSamples.isEmpty()) {
                firstDisplayedSampleIndex = sampleIndex;
            }
            displayedSamples.add(snapshot);
            long tick = (long) sampleIndex * interval;

            appendEvolutionPoint(tick, snapshot);
            appendPopulationPoint(tick, snapshot);
        }
        nextSampleIndex = batch.nextSampleIndex();

        int excess = displayedSamples.size()
                - runner.getStatisticsHistoryCapacity();
        if (excess > 0) {
            displayedSamples.subList(0, excess).clear();
            firstDisplayedSampleIndex += excess;
            pruneOldChartPoints();
        }
    }

    private void appendEvolutionPoint(long tick, SimulationSnapshot snapshot) {
        currentHerbivoreEvolutionSegment = appendTraitPoint(
                currentHerbivoreEvolutionSegment,
                snapshot.herbivores(), tick, HERBIVORE_LINE_COLOR
        );
        currentPredatorEvolutionSegment = appendTraitPoint(
                currentPredatorEvolutionSegment,
                snapshot.predators(), tick, PREDATOR_LINE_COLOR
        );
    }

    private XYChart.Series<Number, Number> appendTraitPoint(
            XYChart.Series<Number, Number> activeSegment,
            GenomeAverages averages,
            long tick,
            String color
    ) {
        if (averages == null) {
            return null; // A gap, not a zero value or a connecting line.
        }
        if (activeSegment == null) {
            activeSegment = new XYChart.Series<>();
            colorSeries(activeSegment, color);
            evolutionChart.getData().add(activeSegment);
        }
        activeSegment.getData().add(
                new XYChart.Data<>(tick, traitSelector.getValue().valueOf(averages))
        );
        return activeSegment;
    }

    private void appendPopulationPoint(long tick, SimulationSnapshot snapshot) {
        herbivorePopulationSeries.getData().add(
                new XYChart.Data<>(tick, snapshot.population().herbivores())
        );
        predatorPopulationSeries.getData().add(
                new XYChart.Data<>(tick, snapshot.population().predators())
        );
        foodSeries.getData().add(
                new XYChart.Data<>(tick, snapshot.population().food())
        );
    }

    private void pruneOldChartPoints() {
        long minTick = (long) firstDisplayedSampleIndex
                * runner.getStatisticsIntervalTicks();

        evolutionChart.getData().removeIf(series -> {
            series.getData().removeIf(
                    point -> point.getXValue().longValue() < minTick
            );
            if (!series.getData().isEmpty()) {
                return false;
            }
            if (series == currentHerbivoreEvolutionSegment) {
                currentHerbivoreEvolutionSegment = null;
            }
            if (series == currentPredatorEvolutionSegment) {
                currentPredatorEvolutionSegment = null;
            }
            return true;
        });

        prunePopulationSeries(herbivorePopulationSeries, minTick);
        prunePopulationSeries(predatorPopulationSeries, minTick);
        prunePopulationSeries(foodSeries, minTick);
    }

    private void prunePopulationSeries(
            XYChart.Series<Number, Number> series,
            long minTick
    ) {
        series.getData().removeIf(
                point -> point.getXValue().longValue() < minTick
        );
    }

    private void rebuildEvolutionChart() {
        evolutionChart.getData().clear();
        currentHerbivoreEvolutionSegment = null;
        currentPredatorEvolutionSegment = null;

        int interval = runner.getStatisticsIntervalTicks();
        for (int i = 0; i < displayedSamples.size(); i++) {
            long tick = (long) (firstDisplayedSampleIndex + i) * interval;
            appendEvolutionPoint(tick, displayedSamples.get(i));
        }
    }

    private void clearChartData() {
        displayedSamples.clear();
        evolutionChart.getData().clear();
        currentHerbivoreEvolutionSegment = null;
        currentPredatorEvolutionSegment = null;
        herbivorePopulationSeries.getData().clear();
        predatorPopulationSeries.getData().clear();
        foodSeries.getData().clear();
    }

    private void resetStatisticsView() {
        clearChartData();
        nextSampleIndex = 0;
        firstDisplayedSampleIndex = 0;
        lastStatisticsRefreshNanos = -1L;
    }

    // ---------------------------------------------------------
    // Simulation controls
    // ---------------------------------------------------------

    private VBox createControls() {
        pauseResumeButton = new Button("Pause");
        pauseResumeButton.setOnAction(event -> {
            if (runner.getState() == SimulationSession.State.RUNNING) {
                runner.pause(System.nanoTime());
                runner.drawCurrentState(this);
            } else if (runner.getState() == SimulationSession.State.PAUSED) {
                runner.resume();
            }
            refreshControls();
        });

        Button resetButton = new Button("Reset");
        resetButton.setOnAction(event -> {
            runner.reset();
            resetStatisticsView();
            runner.drawCurrentState(this);
            refreshControls();
        });

        stateLabel = new Label();
        speedLabel = new Label();

        HBox statusRow = new HBox(
                15, pauseResumeButton, resetButton, stateLabel, speedLabel
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

        VBox controls = new VBox(10, statusRow, speedRow);
        controls.setPadding(new Insets(10));
        controls.setAlignment(Pos.CENTER);
        return controls;
    }

    private Button createSpeedButton(String label, int delta) {
        Button button = new Button(label);
        button.setOnAction(event -> {
            runner.changeSpeed(delta, System.nanoTime());
            runner.drawCurrentState(this);
            refreshControls();
        });
        return button;
    }

    private void refreshControls() {
        SimulationSession.State state = runner.getState();
        stateLabel.setText("State: " + state);
        speedLabel.setText("Speed: " + runner.getSpeedMultiplier() + "x");

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

    // ---------------------------------------------------------
    // Canvas drawing (unchanged)
    // ---------------------------------------------------------

    void clear() {
        graphicsContext.setFill(BACKGROUND_COLOR);
        graphicsContext.fillRect(
                0.0, 0.0, canvas.getWidth(), canvas.getHeight()
        );
    }

    void drawFood(double x, double y, double radius) {
        drawCircle(x, y, radius, FOOD_COLOR);
    }

    void drawEgg(double x, double y, double radius) {
        drawCircle(x, y, radius, EGG_COLOR);
    }

    void drawHerbivore(double x, double y, double radius, double hue) {
        Color color = Color.hsb(
                hue, HERBIVORE_SATURATION, HERBIVORE_BRIGHTNESS
        );
        drawCircle(x, y, radius, color);
    }

    void drawPredator(double x, double y, double radius, double hue) {
        Color color = Color.hsb(
                hue, HERBIVORE_SATURATION, HERBIVORE_BRIGHTNESS
        );
        drawSquare(x, y, radius, color);
    }

    private void drawCircle(
            double centerX, double centerY, double radius, Color color
    ) {
        double diameter = radius * 2.0;
        graphicsContext.setFill(color);
        graphicsContext.fillOval(
                centerX - radius, centerY - radius, diameter, diameter
        );
    }

    private void drawSquare(
            double centerX, double centerY, double radius, Color color
    ) {
        double sideLength = radius * Math.sqrt(2.0);
        graphicsContext.setFill(color);
        graphicsContext.fillRect(
                centerX - sideLength / 2.0,
                centerY - sideLength / 2.0,
                sideLength, sideLength
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}
