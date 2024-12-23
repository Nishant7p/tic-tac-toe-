package uiproject.intro;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ScreenUtils;

public class Main extends ApplicationAdapter implements InputProcessor {
    private ShapeRenderer shapeRenderer;
    private int[][] winningTriplet;
    private Label scoreLabel;
    private Texture p1wins;
    private Texture p2wins;
    private Texture drawTexture;

    private SpriteBatch batch;
    private Texture bgTexture;
    private boolean gameOver;
    private Stage stage;
    private int gamesPlayed = 0;
    private int p1Win = 0;
    private int p2Win = 0;
    private Texture xTexture;
    private Texture oTexture;
    private int[][] grid;
    private boolean isPlayerOneTurn;
    private int gridSize = 3;
    private int[][] boxCoordinates = {
        {736, 590}, {920, 590}, {1100, 590},
        {736, 380}, {920, 380}, {1100, 380},
        {736, 196}, {920, 196}, {1100, 196}
    };
    private String winner = null; // Keeps track of the winner

    @Override
    public void create() {
        stage = new Stage();
        batch = new SpriteBatch();
        bgTexture = new Texture("bg.png");
        xTexture = new Texture("X.png");
        oTexture = new Texture("O.png");
        p1wins = new Texture("p1win.png");
        p2wins = new Texture("p2win.png");
        drawTexture = new Texture("draw.png");
        shapeRenderer = new ShapeRenderer();
        Gdx.input.setInputProcessor(this);
        startNewGame();
    }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        if (gameOver) return true;
        y = Gdx.graphics.getHeight() - y;
        for (int i = 0; i < boxCoordinates.length; i++) {
            int boxX = boxCoordinates[i][0];
            int boxY = boxCoordinates[i][1];
            if (Math.abs(x - boxX) < 50 && Math.abs(y - boxY) < 50) {
                int row = i / gridSize;
                int col = i % gridSize;
                if (grid[row][col] == 0) {
                    grid[row][col] = isPlayerOneTurn ? 1 : 2;
                    isPlayerOneTurn = !isPlayerOneTurn;
                    if (checkWin(1)) {
                        winningTriplet = getWinningTriplet(1);
                        gamesPlayed++;
                        p1Win++;
                        updateScoreLabel();
                        gameOver = true;
                        winner = "p1";
                    } else if (checkWin(2)) {
                        winningTriplet = getWinningTriplet(2);
                        gamesPlayed++;
                        p2Win++;
                        updateScoreLabel();
                        gameOver = true;
                        winner = "p2";
                    } else if (checkDraw()) {
                        gamesPlayed++;
                        updateScoreLabel();
                        gameOver = true;
                        winner = "draw";
                    }
                }
                break;
            }
        }
        return true;
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 1);
        batch.begin();
        batch.draw(bgTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                int index = row * gridSize + col;
                int x = boxCoordinates[index][0] - xTexture.getWidth() / 2;
                int y = boxCoordinates[index][1] - xTexture.getHeight() / 2;
                if (grid[row][col] == 1) {
                    batch.draw(xTexture, x, y);
                } else if (grid[row][col] == 2) {
                    batch.draw(oTexture, x, y);
                }
            }
        }


        if (gameOver) {
            if ("p1".equals(winner)) {
                batch.draw(p1wins, 100, 30);
            } else if ("p2".equals(winner)) {
                batch.draw(p2wins, 100, 30);
            } else if ("draw".equals(winner)) {
                batch.draw(drawTexture, 100, 30);
            }
        }

        batch.end();

        if (gameOver && winningTriplet != null) {
            drawWinningLine();
            if (Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
                startNewGame();
            }
        }
        else if (gameOver){
            if (Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
                startNewGame();}

        }
        stage.act();
        stage.draw();
    }

    private void startNewGame() {
        grid = new int[gridSize][gridSize];
        isPlayerOneTurn = true;
        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));
        if (scoreLabel == null) {
            scoreLabel = new Label("Total games: " + gamesPlayed + "\nP1: " + p1Win + "\nP2: " + p2Win, skin);
            scoreLabel.setSize(200, 100);
            scoreLabel.setPosition(200, 150);
            stage.addActor(scoreLabel);
        } else {
            updateScoreLabel();
        }
        winner = null;
        gameOver = false;
        winningTriplet = null;
    }

    private void updateScoreLabel() {
        scoreLabel.setText("Total games: " + gamesPlayed + "\nP1: " + p1Win + "\nP2: " + p2Win);
    }

    private void drawWinningLine() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1, 0, 0, 1);
        int[] start = winningTriplet[0];
        int[] end = winningTriplet[1];
        float startX = boxCoordinates[start[0] * gridSize + start[1]][0];
        float startY = boxCoordinates[start[0] * gridSize + start[1]][1];
        float endX = boxCoordinates[end[0] * gridSize + end[1]][0];
        float endY = boxCoordinates[end[0] * gridSize + end[1]][1];
        shapeRenderer.rectLine(startX, startY, endX, endY, 10);
        shapeRenderer.end();
    }

    private int[][] getWinningTriplet(int player) {
        for (int i = 0; i < gridSize; i++) {
            if (grid[i][0] == player && grid[i][1] == player && grid[i][2] == player)
                return new int[][]{{i, 0}, {i, 2}};
            if (grid[0][i] == player && grid[1][i] == player && grid[2][i] == player)
                return new int[][]{{0, i}, {2, i}};
        }
        if (grid[0][0] == player && grid[1][1] == player && grid[2][2] == player)
            return new int[][]{{0, 0}, {2, 2}};
        if (grid[0][2] == player && grid[1][1] == player && grid[2][0] == player)
            return new int[][]{{0, 2}, {2, 0}};
        return null;
    }

    private boolean checkWin(int player) {
        for (int i = 0; i < gridSize; i++) {
            if ((grid[i][0] == player && grid[i][1] == player && grid[i][2] == player) ||
                (grid[0][i] == player && grid[1][i] == player && grid[2][i] == player)) {
                return true;
            }
        }
        return (grid[0][0] == player && grid[1][1] == player && grid[2][2] == player) ||
            (grid[0][2] == player && grid[1][1] == player && grid[2][0] == player);
    }

    private boolean checkDraw() {
        for (int[] row : grid) {
            for (int cell : row) {
                if (cell == 0) return false;
            }
        }
        return true;
    }

    @Override
    public void dispose() {
        batch.dispose();
        bgTexture.dispose();
        xTexture.dispose();
        oTexture.dispose();
        p1wins.dispose();
        p2wins.dispose();
        drawTexture.dispose();
    }

    @Override
    public boolean keyDown(int keycode) { return false; }
    @Override
    public boolean keyUp(int keycode) { return false; }
    @Override
    public boolean keyTyped(char character) { return false; }
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }

    @Override
    public boolean touchCancelled(int i, int i1, int i2, int i3) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override
    public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override
    public boolean scrolled(float amountX, float amountY) { return false; }
}
