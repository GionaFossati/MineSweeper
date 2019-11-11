package application;

import java.util.Random;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
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
	private Button btnNewGame;

	@FXML
	private GridPane buttonField;

	@FXML
	private GridPane bombField;

	@FXML
	private ImageView dropOne;

	@FXML
	private ImageView dropTwo;

	@FXML
	private ImageView dropThree;

	@FXML
	private Text gameStatus;

	private Integer numberOfBombs;
	private Integer openedCells;
	private String gameState = "gameover";
	public int timeCounter;
	int[] points = new int[] { -1, -1, -1, 0, -1, 1, 0, -1, 0, 1, 1, -1, 1, 0, 1, 1 };

	@FXML
	void newGame(MouseEvent event) {

// 		if a previous game has been played: delete last buttonField & bombField and
// 		create a new one
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

		btnNewGame.setDisable(true);

		dropOne.setDisable(false);
		dropTwo.setDisable(false);
		dropThree.setDisable(false);

		dropOne.setOpacity(1.0);
		dropTwo.setOpacity(1.0);
		dropThree.setOpacity(1.0);

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

//				Randomise the chance to have a bomb: 1 out of 15
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

//	forEach statement that adds events to every grid cell 
	private void addGridEvent() {

		buttonField.getChildren().forEach(item -> {
			
			Integer[] tileCoord = new Integer[2];

//			coord[0] = row index (Y)
//			coord[1] = column index (X)

			tileCoord[0] = (Integer) item.getProperties().get("gridpane-row");
			tileCoord[1] = (Integer) item.getProperties().get("gridpane-column");
			
//			EVENT: ON MOUSE CLICKED
			item.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {

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

//			EVENTS: DRAGGING
			item.setOnDragEntered(new EventHandler<DragEvent>() {
				public void handle(DragEvent event) {
					/* the drag-and-drop gesture entered the target */
					/* show to the user that it is an actual gesture target */
					if (event.getGestureSource() != item && event.getDragboard().hasImage()) {
						item.getStyleClass().add("flagged");
					}
				}
			});

			item.setOnDragExited(new EventHandler<DragEvent>() {
				public void handle(DragEvent event) {
					/* mouse moved away, remove the graphical cues */
					item.getStyleClass().remove("flagged");
					item.getStyleClass().add("enabledbutton");
				}
			});

			item.setOnDragOver(new EventHandler<DragEvent>() {
				public void handle(DragEvent event) {
					event.acceptTransferModes(TransferMode.ANY);
					event.consume();
				}
			});

			item.setOnDragDropped(new EventHandler<DragEvent>() {
				@Override
				public void handle(DragEvent event) {

					Dragboard db = event.getDragboard();
					boolean success = false;
					if (db.hasImage()) {
						clearCellsAround(tileCoord);
						success = true;
					}

					event.setDropCompleted(success);
				}
			});

		});
	}
	
//	DROP DRAG&DROP METHODS
	@FXML
	void detectedDrag(MouseEvent event) {

		System.out.println("drag detected");
		Dragboard db = ((Node) event.getSource()).startDragAndDrop(TransferMode.ANY);
		ClipboardContent content = new ClipboardContent();
		content.putImage(((ImageView) event.getSource()).getImage());
		db.setContent(content);

		event.consume();

	}
	
//	disable drop after drag&drop done
	@FXML
	void dropDragDone(DragEvent event) {
		System.out.println("drag done");
		((Node) event.getSource()).setDisable(true);
		((Node) event.getSource()).setOpacity(0.3);
	}

	
	
//	method invoked when one of the three "drops" is dragged over a cell
	void clearCellsAround(Integer tileCoord[]) {

		Node central_tile = getNodeFromField(tileCoord[0], tileCoord[1], buttonField);
		central_tile.setVisible(false);
		setNumberOfAdjacentBombs(tileCoord);
		++openedCells;
		updateCellsCounter();

		for (int i = 0; i < points.length; i++) {
			int dx = points[i];
			int dy = points[++i];

			int newX = tileCoord[1] + dx;
			int newY = tileCoord[0] + dy;

			Node tile = getNodeFromField(newY, newX, buttonField);

			if (newX != -1 && newY != -1 && newX < 15 && newY < 10) {
				tile.setVisible(false);
				setNumberOfAdjacentBombs(new Integer[] { newY, newX });
				++openedCells;
				updateCellsCounter();
			}

		}
	}


//	retrieves the node from the getNodeFromField method and check if contains a button with the css class ".mine"
	void checkIfBomb(Integer[] tileCoord) {

		Node clickedNode = getNodeFromField(tileCoord[0], tileCoord[1], bombField);

		if (clickedNode.getStyleClass().toString().contains("mine")) {
			System.out.println("it's a mine!");
			gameOver();
		} else {
			System.out.println("it's empty!");
			setNumberOfAdjacentBombs(tileCoord);

		}
	}

//	method used to add a text corresponding to the number of bombs around a cell
	public void setNumberOfAdjacentBombs(Integer[] tileCoord) {

		int numberAdjacentOfBombs = 0;

		for (int i = 0; i < points.length; i++) {
			int dx = points[i];
			int dy = points[++i];

			int newX = tileCoord[1] + dx;
			int newY = tileCoord[0] + dy;

			Node adjacentNode = getNodeFromField(newY, newX, bombField);

//	        first if: handle edges tiles in order to avoid coordinates that are out of the grid
			if (newX != -1 && newY != -1 && newX < 15 && newY < 10) {

//		        second if: check if is a mine
				if (adjacentNode.getStyleClass().toString().contains("mine")) {
					++numberAdjacentOfBombs;
				}
			}

		}
		 
//		add the number to the corresponding cell
		
		String adjBombsNumber = String.valueOf(numberAdjacentOfBombs);
		adjBombsNumber = (adjBombsNumber.equals("0") ? " " : adjBombsNumber); /* used to add a blank space instead of a "0" */
		
		Text numberField = new Text(adjBombsNumber);
		
		bombField.add(numberField, tileCoord[1], tileCoord[0]);
		bombField.setHalignment(numberField, HPos.CENTER);

	}

//	simple method that, given a cell coordinates, returns its content
	private Node getNodeFromField(int row, int col, GridPane selectedGridPane) {
		for (Node node : selectedGridPane.getChildren()) {
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

//	method that checks if the number of n° opened cells + n° bombs is equal total number of cells
	private void checkIfWon() {

		if (numberOfBombs + openedCells == 150) {

			// you won!
			buttonField.setVisible(false);
			bombField.setVisible(false);
			btnNewGame.setDisable(false);
			gameStatus.setText("––––You won!!––––");
			gameStatus.setVisible(true);

		}
	}

	private void gameOver() {

		buttonField.setVisible(false);
		bombField.setOpacity(0.3);
		gameStatsPane.setOpacity(0.4);
		btnNewGame.setDisable(false);
		gameStatus.setText("–––Game Over––––");
		gameStatus.setVisible(true);
		dropOne.setOpacity(0.3);
		dropTwo.setOpacity(0.3);
		dropThree.setOpacity(0.3);
		gameState = "gameover";

	}
}
