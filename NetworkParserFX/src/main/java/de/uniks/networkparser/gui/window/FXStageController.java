package de.uniks.networkparser.gui.window;

/*
 NetworkParser
 Copyright (c) 2011 - 2014, Stefan Lindel
 All rights reserved.

 Licensed under the EUPL, Version 1.1 or (as soon they
 will be approved by the European Commission) subsequent
 versions of the EUPL (the "Licence");
 You may not use this work except in compliance with the Licence.
 You may obtain a copy of the Licence at:

 http://ec.europa.eu/idabc/eupl5

 Unless required by applicable law or agreed to in
 writing, software distributed under the Licence is
 distributed on an "AS IS" basis,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 express or implied.
 See the Licence for the specific language governing
 permissions and limitations under the Licence.
 */
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.imageio.ImageIO;

public class FXStageController implements StageEvent, WindowListener {
	private KeyListenerMap listener = new KeyListenerMap(this);
	private Object model;

	protected Stage stage;
	private Scene scene;
	private Object controller;
	protected Region pane;
	private boolean wait;

	public FXStageController(Stage newStage){
		this.withStage(newStage);
		this.withPane(new BorderPane());
		this.createScene(pane);
	}
	
	public FXStageController(){
	}


	public void createScene(Region pane) {
		this.scene = new Scene(pane, 600, 400);
		stage.setScene(scene);
	}

	public Stage getStage() {
		return stage;
	}

	public FXStageController withStage(Stage value) {
		this.stage = value;
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent we) {
				stageClosing(we, stage, FXStageController.this);
			}
		});
		stage.setOnShowing(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent we) {
				stageShowing(we, stage, FXStageController.this);
			}
		});

		return this;
	}

	@Override
	public void stageClosing(WindowEvent event, Stage stage,
			FXStageController controller) {
		if (this.pane instanceof StageEvent) {
			((StageEvent) this.pane).stageClosing(event, stage, controller);
		}
		if (this.controller instanceof StageEvent) {
			((StageEvent) this.controller).stageClosing(event, stage,
					controller);
		}
	}

	@Override
	public void stageShowing(WindowEvent event, Stage stage,
			FXStageController controller) {
		if (this.pane instanceof StageEvent) {
			((StageEvent) this.pane).stageShowing(event, stage, controller);
		}
		if (this.controller instanceof StageEvent) {
			((StageEvent) this.controller).stageShowing(event, stage,
					controller);
		}
	}

	public Scene getScene() {
		return scene;
	}

	public Object getController() {
		return controller;
	}

	public FXStageController withController(Object value) {
		this.controller = value;
		return this;
	}

	public boolean close() {
		WindowEvent event = new WindowEvent(stage,
				WindowEvent.WINDOW_CLOSE_REQUEST);
		stageClosing(event, stage, this);
		if (!event.isConsumed()) {
			this.stage.close();
			return true;
		}
		return false;
	}

	public void show(Stage stage) {
		this.withStage(stage);
		if (this.pane != null) {
			Scene scene = new Scene(this.pane);
			stage.setScene(scene);
			showing();
		}
	}

	protected void showing() {
		if (wait && this.stage.getOwner() != null) {
			this.stage.showAndWait();
		} else {
			this.stage.show();
		}
	}

	public void show() {
		showing();
	}

	public Exception saveScreenShoot(String fullScreenFileName,
			String windowScreenFileName) {
		// Save Screenshot
		BufferedImage bi;
		try {
			if (fullScreenFileName != null) {
				bi = new Robot().createScreenCapture(new Rectangle(Toolkit
						.getDefaultToolkit().getScreenSize()));
				ImageIO.write(bi, "jpg", new File(fullScreenFileName));
			}
			if (windowScreenFileName != null) {
				bi = new Robot().createScreenCapture(new java.awt.Rectangle(
						((Double) stage.getX()).intValue(), ((Double) stage
								.getY()).intValue(),
						((Double) stage.getWidth()).intValue(), ((Double) stage
								.getHeight()).intValue()));
				ImageIO.write(bi, "jpg", new File(windowScreenFileName));
			}
		} catch (Exception e1) {
			return e1;
		}
		return null;
	}

	public void setTitle(String value) {
		this.stage.setTitle(value);
	}

	public FXStageController withIcon(String value) {
		if (this.stage != null) {
			if (value.startsWith("file")) {
				stage.getIcons().add(new Image(value));
			} else {
				stage.getIcons().add(new Image("file:" + value));
			}
		}
		return this;
	}

	public boolean isWait() {
		return wait;
	}

	public FXStageController withWait(boolean value) {
		this.wait = value;
		return this;
	}

	public BorderPane createBorderPane() {
		BorderPane value = new BorderPane();
		this.withPane(value);
		return value;
	}

	public GridPane createGridPane() {
		GridPane value = new GridPane();
		this.withPane(value);
		return value;
	}

	public Node getElementById(String id) {
		if (this.pane != null) {
			return this.pane.lookup(id);
		}
		return null;
	}
	
	public FXStageController withFXML(String fxmlFile) {
		if (this.getStage() == null) {
			this.withStage(new Stage());
		}
		create(FXStageController.class.getResource(fxmlFile), null);
		return this;
	}

	public FXStageController withFXML(URL fxmlFile, ResourceBundle resources) {
		if (this.getStage() == null) {
			this.withStage(new Stage());
		}
		create(fxmlFile, null);
		return this;
	}
	
	public Region create(String fxmlFile) {
		return create(FXStageController.class.getResource(fxmlFile), null);
	}

	public Region create(URL location, ResourceBundle resources) {
		FXMLLoader fxmlLoader;
		if (location == null) {
			System.out.println("FXML not found");
			return null;
		}
		if (resources != null) {
			fxmlLoader = new FXMLLoader(location, resources,
					new JavaFXBuilderFactory());
		} else {
			fxmlLoader = new FXMLLoader(location);
		}
		try {
			this.withPane((Region) fxmlLoader.load(location.openStream()));

		} catch (IOException e) {
			System.err.println("FXML Load Error:" + e.getMessage());
			System.err.println("FXML Load Error:" + e.getCause().toString());
			return null;
		}
		this.withController(fxmlLoader.getController());
		return pane;
	}

	public Region getPane() {
		return pane;
	}

	public FXStageController withCenter(Node value) {
		if (pane instanceof BorderPane) {
			((BorderPane) pane).setCenter(value);
		}
		return this;
	}

	public void showNewStage(String fxml, Class<?> path) {
		loadNewStage(fxml, path);
		this.show();
	}

	public Stage loadNewStage(String fxml, Class<?> path) {
		Stage oldStage = this.getStage();
		this.withStage(new Stage());
		URL location;
		if (path == null) {
			location = this.getClass().getResource(fxml);
		} else {
			location = path.getResource(fxml);
		}
		if (location != null) {
			FXMLLoader fxmlLoader = new FXMLLoader(location);
			Pane value = null;
			try {
				value = (Pane) fxmlLoader.load(location.openStream());
			} catch (IOException e) {
				System.out.println("FXML Load Error:" + e.getMessage());
				return null;
			}
			this.withPane(value);
			this.withController(fxmlLoader.getController());

			if (value instanceof StageEvent) {
				Stage myStage = getStage();
				((StageEvent) value).stageShowing(new WindowEvent(myStage,
						WindowEvent.WINDOW_SHOWING), myStage, this);
			}
			this.createScene(value);
		}
		oldStage.close();
		return this.getStage();
	}

	public void showNewStage(Node value) {
		Stage oldStage = this.getStage();

		this.withStage(new Stage());

		Pane newPane;
		if (value instanceof Pane) {
			newPane = (Pane) value;
		} else {
			newPane = new CustomPane(value);
		}

		if (value instanceof StageEvent) {
			Stage myStage = getStage();
			((StageEvent) value).stageShowing(new WindowEvent(myStage,
					WindowEvent.WINDOW_SHOWING), myStage, this);
		}

		this.createScene(newPane);
		this.show();
		this.withPane(newPane);
		oldStage.close();
	}

	public FXStageController withPane(Region value) {
		this.pane = value;
		this.pane.addEventFilter(KeyEvent.KEY_PRESSED, listener);
		return this;
	}

	public FXStageController withSize(int width, int height) {
		getStage().setWidth(width);
		getStage().setHeight(height);
		return this;
	}

	public Object getModel() {
		return model;
	}

	public void setModel(Object model) {
		this.model = model;
	}

	public FXStageController withModel(Object model) {
		this.model = model;
		return this;
	}

	public static FXStageController load(String fxml) {
		return new FXStageController().withFXML(fxml);
	}

	public static FXStageController load(String fxml, Class<?> path) {
		return new FXStageController().withFXML(path.getResource(fxml), null);
	}

	public static FXStageController show(Stage stage, String fxml,
			Class<?> path) {
		FXStageController controller = new FXStageController().withFXML(path.getResource(fxml), null);
		controller.show(stage);
		return controller;
	}
}
