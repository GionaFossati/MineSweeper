package application;

import java.util.Random;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import java.util.Timer;

public class Controller {

	@FXML
	private AnchorPane gameStatsPane;

	@FXML
	private Text openedNumber;

	@FXML
	private Text remainingNumber;

	@FXML
	private ProgressBar progressBar;

	@FXML
	private Text minutesCounter;

	@FXML
	private Text secondsCounter;

	@FXML
	private Button btnMineDetector;

	@FXML
	private Button btnNewGame;

	@FXML
	private Button btnSnapshot;

	@FXML
	private GridPane buttonField;

	@FXML
	private GridPane bombField;

	@FXML
	private Text gameStatus;

	private Integer numberOfBombs;
	private Integer openedCells;
	private String gameState = "gameover";
	public int timeCounter;

	@FXML
	void newGame(MouseEvent event) {
//		graphic adjustments 
		buttonField.setVisible(true);
		gameStatsPane.setOpacity(1);
		bombField.setVisible(true);
		bombField.setOpacity(1);

		openedNumber.setText(" 0");
		openedCells = 0;
		remainingNumber.setText("150");
		progressBar.setProgress(0);

		secondsCounter.setText("00");
		minutesCounter.setText("00");

		gameStatus.setVisible(false);

		btnMineDetector.getStyleClass().add("enabledButton");
		btnSnapshot.getStyleClass().add("enabledButton");

		btnNewGame.setDisable(true);

		// if a previous game has been played: delete last buttonField & bombField and
		// create a new one
		bombField.getChildren().removeAll(bombField.getChildren());
		buttonField.getChildren().removeAll(bombField.getChildren());

//		create stacked grids and attach the mouse clicked event to each cell
		createGrid();
		createBombField();
		addGridEvent();

//		starts match timer 
		timeCounter = 0;
		gameState = "going";
		startClockThread();

	}

// Match timer method

	void startClockThread() {

		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				while (gameState == "going") {
					timeCounter++;
					try {

						secondsCounter.setText(String.valueOf(timeCounter % 60));

						if (timeCounter % 60 == 0) {
							minutesCounter.setText("0" + String.valueOf(Integer.valueOf(minutesCounter.getText()) + 1));
						}

						Thread.sleep(1000);
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				}
			}
		});

		t.setDaemon(true);
		t.start();
	}

//	   Grid creation method
	@FXML
	public void createGrid() {

		buttonField.getChildren().clear();

		int i = 0;
		int j = 0;

		for (i = 0; i < 15; ++i)
			for (j = 0; j < 10; ++j) {

				Button btn = new Button();
				btn.getStyleClass().add("enabledButton");
				btn.setPrefSize(10, 10);
				buttonField.add(btn, i, j);

			}

	}

	@FXML
	public void createBombField() {

		bombField.getChildren().clear();
		numberOfBombs = 0;
		int i = 0;
		int j = 0;

		for (i = 0; i < 15; ++i)
			for (j = 0; j < 10; ++j) {

//				randomize the chanche to have a bomb: in this case, 1 out of 15
				boolean fate = new Random().nextInt(15) == 0;
				Button btn = new Button();

//				use css class to determine if a cell contains a mine or "water"
				if (fate == false) {
					btn.getStyleClass().add("water");
					bombField.add(btn, i, j);
				} else if (fate == true) {
					btn.getStyleClass().add("mine");
					bombField.add(btn, i, j);
					++numberOfBombs;
				}

				btn.setPrefSize(10, 10);

			}

	}

//	  add event to every grid cell 
	private void addGridEvent() {
		buttonField.getChildren().forEach(item -> {
			item.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {

					Integer[] tileCoord = new Integer[2];

//	        			coord[0] = row index (Y)
//	        			coord[1] = column index (X)

					tileCoord[0] = (Integer) item.getProperties().get("gridpane-row");
					tileCoord[1] = (Integer) item.getProperties().get("gridpane-column");

//					if click left button : disable visibility and check if under it there is a bomb (checkIfBomb) 
//					or if there are no more cells remaining (checkIfWon)
					if (event.getButton() == MouseButton.PRIMARY) {
						item.setVisible(false);
						++openedCells;
						updateCellsCounter();
						checkIfBomb(tileCoord);
						checkIfWon();

					}

//					change cell colour if the right button is clicked
					if (event.getButton() == MouseButton.SECONDARY) {
						System.out.println("Right button clicked");
						item.getStyleClass().add("flagged");
					}

				}
			});

		});
	}

	void checkIfBomb(Integer[] tileCoord) {
//		retrieve the node from the getNodeFromBombField method and check if contains a button with the css class ".mine"

		Node clickedNode = getNodeFromBombField(tileCoord[0], tileCoord[1]);

		if (clickedNode.getStyleClass().toString().contains("mine")) {
			System.out.println("it's a mine!");
			gameOver();
		} else {
			System.out.println("it's water!");

//			if not a mine: add a text with the NUMBER OF ADJACENT MINES
			String adjBombsNumber = String.valueOf(getNumberOfAdjacentBombs(tileCoord));
			adjBombsNumber = (adjBombsNumber.equals("0") ? " " : adjBombsNumber);
			Text numberField = new Text(adjBombsNumber);
			bombField.add(numberField, tileCoord[1], tileCoord[0]);
			bombField.setHalignment(numberField, HPos.CENTER);

		}
	}

//	method used to return a int corresponding to the number of the bombs around a cell
	public int getNumberOfAdjacentBombs(Integer[] tileCoord) {
		int[] points = new int[] { -1, -1, -1, 0, -1, 1, 0, -1, 0, 1, 1, -1, 1, 0, 1, 1 };

		int numberAdjacentOfBombs = 0;

		for (int i = 0; i < points.length; i++) {
			int dx = points[i];
			int dy = points[++i];

			int newX = tileCoord[1] + dx;
			int newY = tileCoord[0] + dy;

			Node adjacentNode = getNodeFromBombField(newY, newX);

//	        first if: handle edges tiles in order to avoid coordinates that are out of the grid
			if (newX != -1 && newY != -1 && newX < 15 && newY < 10) {

//		        second if: check if is a mine
				if (adjacentNode.getStyleClass().toString().contains("mine")) {
					++numberAdjacentOfBombs;
				}
			}

		}
		return numberAdjacentOfBombs;
	}

//	simple method that, given a cell coordinates, returns its content
	private Node getNodeFromBombField(int row, int col) {
		for (Node node : bombField.getChildren()) {
			if (GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row) {
				return node;
			}
		}
		return null;
	}

//	updates the counter of the opened cells 
	private void updateCellsCounter() {
		progressBar.setProgress(0.01 * openedCells);
		openedNumber.setText(openedCells.toString());
		remainingNumber.setText(Integer.toString(150 - openedCells));

	}

	private void checkIfWon() {
		System.out.println(numberOfBombs);
		if (numberOfBombs + openedCells == 150) {

			// you won!
			buttonField.setVisible(false);
			bombField.setVisible(false);
			btnNewGame.setDisable(false);
			btnMineDetector.getStyleClass().add("disabledButton");
			btnSnapshot.getStyleClass().add("disabledButton");
			gameStatus.setText("––––You won!!––––");
			gameStatus.setVisible(true);

		}
	}

	private void gameOver() {

		buttonField.setVisible(false);
		bombField.setOpacity(0.3);
		gameStatsPane.setOpacity(0.4);
		btnNewGame.setDisable(false);
		btnMineDetector.getStyleClass().add("disabledButton");
		btnSnapshot.getStyleClass().add("disabledButton");
		gameStatus.setText("–––Game Over––––");
		gameStatus.setVisible(true);
		gameState = "gameover";

	}
}
