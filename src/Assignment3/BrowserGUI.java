package Assignment3;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.io.*;
import java.nio.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;

import Assignment3.DownloadBar;
import javafx.scene.control.Alert.AlertType;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.concurrent.Worker.State;
import javafx.animation.FadeTransition;
import javafx.animation.*;
import javafx.util.Duration;

public class BrowserGUI extends Application {

	private HBox buttonList;
	private Button forButton, backButton;
	private TextField addressField;
	private WebEngine engine;
	private ArrayList<String> bookmarks;
	private String currentAddress;
	private Button bookmarkButton;
	private ListView<WebHistory.Entry> historyList;

	
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		MenuItem currentItem = new MenuItem("Quit");
		currentItem.setOnAction(event->{ saveSettings(primaryStage);Platform.exit();});
		BorderPane root = new BorderPane();
		bookmarks = new ArrayList<>();

		MyButtonClickHandler illDoIt = new MyButtonClickHandler();

		buttonList = new HBox();
		forButton = new Button("Forward");
		backButton = new Button("Back");
		
		historyList = new ListView<>();

		addressField = new TextField("address here");
		//Tell the address field to grow to fill empty space:
		addressField.setMaxWidth(Double.MAX_VALUE);
		HBox.setHgrow(addressField, Priority.ALWAYS);
		
		
		addressField.setOnKeyReleased(e -> {
			KeyCode kc = e.getCode();

			//when the user types ENTER:
			if (kc == KeyCode.ENTER) {
				String add = addressField.getText();
				forButton.setDisable(true);
				engine.load(add);
				bookmarkButton.setDisable(true);
				currentAddress = add;
			}
		});
		addressField.setOnMouseClicked(illDoIt);

		backButton.setOnMouseClicked(illDoIt);
		forButton.setOnMouseClicked(illDoIt);

		//set the back and forward buttons disabled by default
		backButton.setDisable(true);
		forButton.setDisable(true);
		
		
		Menu bookmarksMenu = new Menu("Bookmarks");//InitializeBookmarks();//new Menu("Bookmarks");
		InitializeBookmarks(bookmarksMenu);
		bookmarkButton = new Button("Add Bookmark");
		bookmarkButton.setOnMouseClicked(e -> {
			
			if (!bookmarks.contains(currentAddress)) {
				bookmarks.add(currentAddress);
				bookmarkButton.setDisable(true);
				MenuItem newItem = new MenuItem(currentAddress);
				String addressCopy = new String(currentAddress); 
				newItem.setOnAction(event ->  engine.load(addressCopy));
				bookmarksMenu.getItems().add(newItem);
				try{
			         FileOutputStream fos= new FileOutputStream("bookmark.bkmk");
			         ObjectOutputStream oos= new ObjectOutputStream(fos);
			         oos.writeObject(bookmarks);
			         oos.close();
			         fos.close();
			       }catch(IOException ioe){
			            ioe.printStackTrace();
			        }
			}
		});
		buttonList.getChildren().addAll(backButton, addressField, bookmarkButton, forButton);

		WebView wv = new WebView();

		Menu menu1 = new Menu("File");

		Menu helpMenu = new Menu("Help");

		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().addAll(menu1, bookmarksMenu, helpMenu);
		MenuItem javaHelp = new MenuItem("Get help for java class");
		javaHelp.setOnAction(e -> {
			TextInputDialog dialog = new TextInputDialog("Type here");
			dialog.setTitle("Find help for java class");
			dialog.setHeaderText("Search for Java Class Documentation");
			dialog.setContentText("Which Java class do you want to research?");

			// Traditional way to get the response value.
			Optional<String> result = dialog.showAndWait();
			if (result.isPresent()) {
				currentAddress = "https://www.google.ca/search?q=java+" + result.get();
				engine.load(currentAddress);
			}
		});
		
		CheckMenuItem cmi = new CheckMenuItem("Show history");
		cmi.setOnAction(e -> {
			if (cmi.isSelected()) {
				historyList.setMaxWidth(300.0f);
				ScaleTransition back = new ScaleTransition(Duration.millis(3000), historyList);
				back.setToX(1.0);
				back.setToY(1.0);
				FadeTransition ft = new FadeTransition(Duration.millis(3000), historyList);
				
				// Make sure the fade is starting from invisible:
				ft.setFromValue(0);
				ft.setToValue(1);
				ParallelTransition pt = new ParallelTransition();
				pt.getChildren().addAll(back, ft);
				pt.play();
			} else {
				ScaleTransition down = new ScaleTransition(Duration.millis(1000), historyList);
				down.setToY(0.2f);
				ScaleTransition right = new ScaleTransition(Duration.millis(1000), historyList);
				right.setToX(0.0f);
				SequentialTransition seq = new SequentialTransition();
				seq.getChildren().addAll(down, right);
				seq.play();
				seq.setOnFinished(arg -> historyList.setMaxWidth(0.0f));
			}
		});

		historyList.setMaxWidth(0);
		MenuItem about = new MenuItem("About");
		about.setOnAction(e -> {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Information Dialog");
			alert.setHeaderText("About");
			alert.setContentText("Eric's browser, v1.0. Feb. 18, 2016");
			alert.show();
		});
		menu1.getItems().addAll(currentItem);
		
		helpMenu.getItems().addAll(javaHelp, cmi, about);

		root.setTop(new VBox(menuBar, buttonList));
		root.setCenter(wv);
		root.setRight(historyList);
		Scene scene = new Scene(root, 800, 800);
		engine = wv.getEngine();
		engine.load("http://www.google.com");

		engine.getLoadWorker().stateProperty().addListener((ov, oldState, newState) -> {
			if (newState == State.SUCCEEDED) {
				final WebHistory history = engine.getHistory();
				ObservableList<WebHistory.Entry> entryList = history.getEntries();
				int currentIndex = history.getCurrentIndex();

				//update the address field with the new engine URL:
				addressField.setText(currentAddress = engine.getLocation());
				//If the ArrayList<String> of bookmarked urls contains the new address, then disable the Bookmarks button:
				bookmarkButton.setDisable( bookmarks.contains(currentAddress) );
				
				historyList.getSelectionModel().select(engine.getHistory().getCurrentIndex());
				backButton.setDisable(currentIndex == 0);
				forButton.setDisable(currentIndex == entryList.size() - 1);
			}
		});
		engine.locationProperty().addListener(new ChangeListener<String>() {
			@Override public void changed(ObservableValue<? extends String> observableValue, String oldLoc, String newLocation) {
				if(newLocation.endsWith(".exe")||newLocation.endsWith(".PDF")||newLocation.endsWith(".ZIP")||
				newLocation.endsWith(".DOC")||newLocation.endsWith(".DOCX")||newLocation.endsWith(".XLS")||newLocation.endsWith(".XLSX")
				||newLocation.endsWith(".ISO")||newLocation.endsWith(".IMG")||newLocation.endsWith(".DMG")||newLocation.endsWith(".TAR")
				||newLocation.endsWith(".TGZ")||newLocation.endsWith(".JAR"))
				{
					System.out.print("great");
					DownloadBar newDownload = new DownloadBar(newLocation);		  
				}
			}
		});	
		historyList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		historyList.setItems(engine.getHistory().getEntries());
		historyList.setOnMouseClicked(e -> {
			//previousItem is where the engine is currently in the history
			int previousItem = engine.getHistory().getCurrentIndex();
			
			//selectedItem is where the user wants to go in the history
			int selectedItem = historyList.getSelectionModel().getSelectedIndex();
			
			//change is the direction, and number of pages to go forward or backward:
			int change = selectedItem - previousItem;
			engine.getHistory().go(change);
		});

		primaryStage.setScene(scene);
		ReadSettings(primaryStage);
		primaryStage.show();
	}

	private void ReadSettings(Stage se){
		try {
			String content = new Scanner(new File("Settings.txt")).useDelimiter("\\Z").next();
			String[] parts = content.split(" ");
			se.setX(Double.parseDouble(parts[2]));
			se.setY(Double.parseDouble(parts[4]));
			se.setHeight(Double.parseDouble(parts[6]));
			se.setWidth(Double.parseDouble(parts[8]));
		} catch (FileNotFoundException e) {
			System.out.print("No File: error 102");
		}
		
	}
	
	
	protected void saveSettings(Stage primaryStage){
		File S = new File("Settings.txt");
		
		try{S.createNewFile();
		
	        BufferedWriter writer = new BufferedWriter(new FileWriter(S));
	            
	                writer.write(" ScreenX= " + primaryStage.getX() + "\n");
	                writer.write(" ScreenY= " + primaryStage.getY() + "\n");
	                writer.write(" Height= " + primaryStage.getHeight() + "\n");
	                writer.write(" Width= " + primaryStage.getWidth() + "\n");
	                /*writer.write("Width =" + primaryStage.getWidt() + "\n");
	                writer.write("Width =" + primaryStage.getWidth() + "\n");*/
	            
	            writer.close();
	        } catch (IOException e) {}
	    
	}
	
		
	protected void InitializeBookmarks(Menu bookmarksMenu){
		/*
		 * creates a new file if there does not exist one
		 */
		
		/*
		 * get the information inside of the file
		 */

		try{
            FileInputStream fis = new FileInputStream("bookmark.bkmk");
            ObjectInputStream ois = new ObjectInputStream(fis);
            bookmarks = (ArrayList)ois.readObject();
            
            ois.close();
            fis.close();
		}catch(Exception l){
			System.out.println("No Object in File: ERROR:101");	
			//l.printStackTrace();
		}
		
		/*
		 * loops through the bookmarks and adds them to the menu
		 */
		for(int i = 0; i < bookmarks.size(); i++){
			MenuItem newItem = new MenuItem(bookmarks.get(i));
			String addressCopy = new String(bookmarks.get(i));
			newItem.setOnAction(event ->  engine.load(addressCopy));
			bookmarksMenu.getItems().add(newItem);
		}
	}
	private class MyButtonClickHandler implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent event) {

			if (event.getSource() == backButton) {
				goBack();
			} else if (event.getSource() == forButton) {
				goForward();
			} else if ((event.getSource() == addressField)) {
				//If the user clicked twice, clear the text:
				if (event.getClickCount() > 1)
					addressField.setText("");
				//else the user clicked once, so select all the text:
				else
					addressField.selectAll();
			}
		}
	}

	public void goBack() {
		final WebHistory history = engine.getHistory();
		int currentIndex = history.getCurrentIndex();
		if (currentIndex > 0) {
			Platform.runLater(() -> {
				history.go(-1);
				currentAddress = history.getEntries().get(currentIndex - 1).getUrl();
				addressField.setText(currentAddress);
				backButton.setDisable(currentIndex - 1 ==0);
			});
		}
	}

	public void goForward() {
		final WebHistory history = engine.getHistory();
		ObservableList<WebHistory.Entry> entryList = history.getEntries();
		int currentIndex = history.getCurrentIndex();
		if (currentIndex + 1 < entryList.size()) {
			Platform.runLater(new Runnable() {
				public void run() {
					history.go(1);
					currentAddress = history.getEntries().get(currentIndex + 1).getUrl();
					addressField.setText(currentAddress);
					forButton.setDisable(currentIndex + 1 == entryList.size());
				}
			});
		}
	}
}
