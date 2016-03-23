package com.DoublesChess.gui;
import static com.DoublesChess.engine.board.BoardUtils.*;
import com.DoublesChess.engine.board.Board;
import com.DoublesChess.engine.board.Move;
import com.DoublesChess.engine.board.Tile;
import com.DoublesChess.engine.pieces.Piece;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.*;


import com.DoublesChess.engine.player.MoveTransition;

import javafx.application.Application;
import javafx.application.Platform;

import javafx.geometry.Orientation;

import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Table extends Application {

    private Scene scene;
    private BorderPane BP;
    private AnchorPane AP; // stackpane for stacking grid of draggable pieces 1x1 on top of drid of board 8x8

    private Board chessBoard;
    private BoardPanel boardPanel;

    private BoardDirection boardDirection;
//   private BoardPanel pieceLegalMoves;

    public static final int TILE_SIZE = 80;
    public static final int WIDTH = 8;
    public static final int HEIGHT = 8;

    private Group tileGroup = new Group();
    private Group pieceGroup = new Group();

    // private final Color lightTileColor = Color.valueOf("#FFFACD");
    private final String lightTileColorString = "-fx-background-color: #FFFACD;";
    private final String darkTileColorString = "-fx-background-color: #8B4513;";
    private final String selectedTileColorDarkString = "-fx-background-color: #469F27;";
    private final String selectedTileColorLightString = "-fx-background-color: #99FD80;";
    private final String selectedTileColorString = "-fx-background-color: #33FF33;";

    private final String defaultPieceImagePath = "art/clean/";

    private final static double H_TILE_GAP = 0;
    private final static double V_TILE_GAP = 0;

    private Tile sourceTile;
    private Tile hoveredTile;
    private Tile destinationTile;
    private TilePanel sourceTilePanel;
    private TilePanel hoveredTilePanel;
    private Piece humanMovedPiece;

    private boolean showLegalMoves;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.scene = new Scene(createContent());
        primaryStage.setScene(scene);
        primaryStage.setTitle("DoublesChess");
        primaryStage.show();
    }

    private Parent createContent() {
        this.chessBoard = Board.createStandardBoard();
        this.boardDirection = BoardDirection.NORMAL;
        this.showLegalMoves = true;

        this.AP = new AnchorPane();
        this.AP.setMinSize(TILE_SIZE * WIDTH, TILE_SIZE * HEIGHT);

        this.BP = new BorderPane();
        this.BP.setCenter(this.AP);
            
        this.boardPanel = new BoardPanel();

        this.BP.setMinSize(TILE_SIZE * WIDTH, TILE_SIZE * HEIGHT);

        final int vBoxHeight = 25;
        final int vBoxPadding = 2;
        final int vBoxTotalHeight = vBoxHeight + vBoxPadding * 2;
        final int vBoxSpacing = 4;

        VBox vBox = new VBox();
        vBox.setPrefSize(vBoxHeight, vBoxHeight);
        vBox.setSpacing(vBoxSpacing);
        vBox.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null, new BorderWidths(vBoxPadding))));
        final MenuBar tableMenuBar = createTableMenuBar();
        vBox.getChildren().addAll(tableMenuBar);
        this.BP.setTop(vBox);

        AP.getChildren().addAll( boardPanel, pieceGroup);

        return BP;
    }

    protected class BoardPanel extends TilePane{
        final List<TilePanel> boardTiles;

        BoardPanel() {
            this.boardTiles = new ArrayList<>();
            setPrefSize(TILE_SIZE * WIDTH, TILE_SIZE * HEIGHT);
            pieceGroup.getChildren().clear();

            for (int i = 0; i < NUM_TILES; i++){
                final TilePanel tilePanel = new TilePanel(this, boardDirection.traverseTileID(i), Orientation.HORIZONTAL, H_TILE_GAP, V_TILE_GAP);
                this.boardTiles.add(tilePanel);
                tilePanel.setOnMousePressed(e->{
                    tilePanel.assignMoveTileColor();
                });

                tilePanel.setOnMouseReleased(e->{
                    tilePanel.assignTileColor();
                });
                getChildren().add(tilePanel);
            }
        }
        protected void drawBoard(final Board board) {
            AP.getChildren().clear();
            pieceGroup.getChildren().clear();
            boardPanel.getChildren().clear();
            boardPanel = new BoardPanel();

            AP.getChildren().addAll(boardPanel, pieceGroup);

       }
    }

    private int tileIDFromCoOrdinate(int x, int y) {
        int tileID = x + y * 8;
        return tileID;
    }

    private class TilePanel extends TilePane {
        private final int tileID;
        private String originalColorString;

        TilePanel(final BoardPanel boardPanel, final int tileID, Orientation orientation, double hgap, double vgap) {
            super(orientation, hgap, vgap);
            this.tileID = tileID;
            this.setOnMousePressed(e -> {
                System.out.println("tilePanel Tile ID: "+ this.tileID);
            });
            setPrefSize(TILE_SIZE, TILE_SIZE);
            assignTileColor();

            //setPosition();
            assignTilePieceIcon(chessBoard);
            highLightLegalMoves(chessBoard);
        }

        public void drawTilePanel (Board board) {

            assignTileColor();
            assignTilePieceIcon(board);

        }

        private void highLightLegalMoves(final Board board){
            if(true){
                for (final Move move : pieceLegalMoves (board)){
                    if (move.getDestinationCoordinate() == this.tileID){
                        assignMoveTileColor();
                    }
                }
            }
        }

        private Collection<Move> pieceLegalMoves(final Board board){
            if (humanMovedPiece!= null && humanMovedPiece.getPieceAlliance() == board.getCurrentPlayer().getAlliance()){
                return humanMovedPiece.calculateLegalMoves(board);
            }
            return Collections.emptyList();
        }

        private void assignTileColor() {
            if (FIRST_ROW[this.tileID] ||
                    THIRD_ROW[this.tileID] ||
                    FIFTH_ROW[this.tileID] ||
                    SEVENTH_ROW[this.tileID]) {
                setStyle(tileID % 2 != 0 ? lightTileColorString : darkTileColorString);
                originalColorString = getStyle();
            } else if (SECOND_ROW[this.tileID] ||
                    FOURTH_ROW[this.tileID] ||
                    SIXTH_ROW[this.tileID] ||
                    EIGHTH_ROW[this.tileID]) {
                setStyle(tileID % 2 == 0 ? lightTileColorString : darkTileColorString);
                originalColorString = getStyle();
            }
        }

        private void assignMoveTileColor() {
            if (FIRST_ROW[this.tileID] ||
                    THIRD_ROW[this.tileID] ||
                    FIFTH_ROW[this.tileID] ||
                    SEVENTH_ROW[this.tileID]) {
                setStyle(tileID % 2 != 0 ? selectedTileColorLightString : selectedTileColorDarkString);
                originalColorString = getStyle();
            } else if (SECOND_ROW[this.tileID] ||
                    FOURTH_ROW[this.tileID] ||
                    SIXTH_ROW[this.tileID] ||
                    EIGHTH_ROW[this.tileID]) {
                setStyle(tileID % 2 == 0 ? selectedTileColorLightString: selectedTileColorDarkString);
                originalColorString = getStyle();
            }
        }

        public void assignTilePieceIcon(final Board board) {
            //TODO figure out why pathing not working 100%
            if (board.getTile(this.tileID).isTileOccupied()) {

                final String path = defaultPieceImagePath + board.getTile(this.tileID).getPiece().getPieceAlliance().toString().substring(0, 1) +
                        board.getTile(this.tileID).getPiece().toString() + ".png";
                final int x = getCoOrdinateX(boardDirection.traverseTileID(this.tileID));
                final int y = getCoOrdinateY(boardDirection.traverseTileID(this.tileID));

                final PieceImage pieceImage = new PieceImage(
                        chessBoard.getTile(tileID).getPiece(),
                        x,
                        y,
                        path);
                pieceGroup.getChildren().add(pieceImage);

                pieceImage.setOnMousePressed(e -> {
                    final int tileCoOrd = boardDirection.traverseTileID(tileIDFromCoOrdinate(x, y));
                    pieceImage.mouseX = e.getSceneX();
                    pieceImage.mouseY = e.getSceneY();

                    if (sourceTile == null) {
                        sourceTilePanel = boardPanel.boardTiles.get(boardDirection.traverseTileID(tileCoOrd));
                        sourceTile = chessBoard.getTile(tileCoOrd);
                        humanMovedPiece = sourceTile.getPiece();

                        if(showLegalMoves) {
                            Collection<Move> selectedPiece = pieceLegalMoves(chessBoard);
                            for (Move move : selectedPiece) {
                                TilePanel tilePanel = boardPanel.boardTiles.get(boardDirection.traverseTileID(move.getDestinationCoordinate()));
                                tilePanel.assignMoveTileColor();

                            }
                        }
                    }
                });

                pieceImage.setOnMouseEntered(e -> {
                    pieceImage.toFront(); // beautiful.
                    if (showLegalMoves) {
                        final int tileCoOrd = boardDirection.traverseTileID(tileIDFromCoOrdinate(x, y));
                        hoveredTilePanel = boardPanel.boardTiles.get(boardDirection.traverseTileID(tileCoOrd));
                        hoveredTile = chessBoard.getTile(tileCoOrd);

                        if (chessBoard.getTile(tileCoOrd).isTileOccupied() &&
                                chessBoard.getTile(tileCoOrd).getPiece().getPieceAlliance() == chessBoard.getCurrentPlayer().getAlliance()) {
                            hoveredTilePanel.setStyle(selectedTileColorString);
                        }
                    }
                });
                pieceImage.setOnMouseExited(e -> {
                    final int tileCoOrd = boardDirection.traverseTileID(tileIDFromCoOrdinate(x, y));
                    boardPanel.boardTiles.get(boardDirection.traverseTileID(tileCoOrd)).setStyle(boardPanel.boardTiles.get(tileCoOrd).originalColorString);
                });

                pieceImage.setOnMouseDragged(e -> {
                    if(showLegalMoves){
                        sourceTilePanel.setStyle(sourceTilePanel.originalColorString == darkTileColorString ? selectedTileColorDarkString : selectedTileColorLightString);
                    }
                    pieceImage.relocate(
                            Math.max(0,Math.min(TILE_SIZE * WIDTH- TILE_SIZE, e.getSceneX() - pieceImage.mouseX + pieceImage.oldX)),
                            Math.max(0,Math.min(TILE_SIZE * WIDTH- TILE_SIZE, e.getSceneY() - pieceImage.mouseY + pieceImage.oldY)));
                    //limit where the piece can be moved to so funny stuff doesnt happpen

                });

                pieceImage.setOnMouseReleased(e -> {

                    int newX = toBoard(pieceImage.getLayoutX());
                    int newY = toBoard(pieceImage.getLayoutY());
                    int x0 = (int) pieceImage.getOldX();
                    int y0 = (int) pieceImage.getOldY();

                    final int tileCoOrd = boardDirection.traverseTileID(tileIDFromCoOrdinate(newX, newY));
                    destinationTile = chessBoard.getTile(tileCoOrd);
                    final Move move = Move.MoveFactory.createMove(chessBoard, sourceTile.getTileCoordinate(), destinationTile.getTileCoordinate());
                    final MoveTransition transition = chessBoard.getCurrentPlayer().makeMove(move);

                    if (transition.getMovesStatus().isDone()) {

                        chessBoard = transition.getTransitionBoard();
                    //todo add move to movelog

                        sourceTilePanel.setStyle(sourceTilePanel.originalColorString);
                        sourceTilePanel = null;
                        sourceTile = null;
                        humanMovedPiece = null;

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                boardPanel.drawBoard(chessBoard);
                            }
                        });
                    } else {
                        //null move - relocate to old position
                        pieceImage.relocate(x0, y0);
                        sourceTilePanel = null;
                        sourceTile = null;
                        humanMovedPiece = null;
                        Collection<Move> selectedPiece = pieceLegalMoves(chessBoard);
                        for (final TilePanel tilePanel: boardPanel.boardTiles){
                            tilePanel.assignTileColor();
                        }
                    }
                });
                // TODO special thanks to https://openclipart.org/search/?query=Chess+tile+
            }
        }
    }

    private int toBoard(double pixel) {
        return (int) ((pixel + TILE_SIZE / 2) / TILE_SIZE);
    }

    private int getCoOrdinateX(int tileID) {
        int x = tileID % 8;
        return x;

    }

    private int getCoOrdinateY(int tileID) {
        int y = (int) Math.floor(tileID / 8);
        return y;
    }

    private MenuBar createTableMenuBar() {
        final MenuBar tableMenuBar = new MenuBar();
        tableMenuBar.getMenus().add(createFileMenu());
        tableMenuBar.getMenus().add(createPreferenceMenu());
        return tableMenuBar;
    }

    private Menu createFileMenu() {
        final Menu fileMenu = new Menu("File");
        final MenuItem openPGN = new MenuItem("Load PGN File");
        openPGN.setOnAction(e -> {
            System.out.println("Open that PGN file");
        });
        final MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(e -> {
            System.exit(0);
        });

        final MenuItem newGame = new MenuItem("New game");
        newGame.setOnAction(e -> {
            this.chessBoard = Board.createStandardBoard();
            boardPanel.drawBoard(chessBoard);
        });
        fileMenu.getItems().addAll(openPGN, exit, newGame);
        return fileMenu;
    }

    private Menu createPreferenceMenu() {
        final Menu preferenceMenu = new Menu("Preference");
        final MenuItem flipBoardMenu = new MenuItem("Flip Board");
        final CheckMenuItem showLegalMoves = new CheckMenuItem("Show Legal Moves");
        showLegalMoves.setSelected(true);


        flipBoardMenu.setOnAction(e -> {
            boardDirection = boardDirection.opposite();
            boardPanel.drawBoard(chessBoard);
        });

        showLegalMoves.setOnAction(e -> {
            if (showLegalMoves.isSelected()){
                this.showLegalMoves = true;
            }
            else{
                this.showLegalMoves = false;
            }
        });

        preferenceMenu.getItems().addAll(flipBoardMenu, showLegalMoves);

        return preferenceMenu;
    }

    public enum BoardDirection {
        NORMAL {
            @Override
            List<TilePanel> traverse(final List<TilePanel> boardTiles) {
                return boardTiles;
            }

            @Override
            int traverseTileID(final int tileID) {
                return tileID;
            }

            @Override
            BoardDirection opposite() {

                System.out.println("Board Flipped");
                return FLIPPED;
            }
        },
        FLIPPED {
            @Override
            List<TilePanel> traverse(final List<TilePanel> boardTiles) {
                return Lists.reverse(boardTiles);
            }

            @Override
            int traverseTileID(final int tileID) {

                return (63 - tileID);
            }

            @Override
            BoardDirection opposite() {
                System.out.println("Board Normal");
                return NORMAL;
            }
        };

        abstract List<TilePanel> traverse(final List<TilePanel> boardTiles);

        abstract int traverseTileID (final int tileID);

        abstract BoardDirection opposite();
    }
}


