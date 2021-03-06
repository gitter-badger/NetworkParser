package de.uniks.networkparser.ext.javafx.dialog;

/*
NetworkParser
The MIT License
Copyright (c) 2010-2016 Stefan Lindel https://github.com/fujaba/NetworkParser/

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class ShowTask implements Runnable{
	public DialogBox parent;
	private Window owner;

	public ShowTask(DialogBox parent, Window owner) {
		this.parent = parent;
		this.owner = owner;
	}
	@Override
	public void run() {
//	protected DialogButton call() throws Exception {
		parent.stage = new Stage(StageStyle.TRANSPARENT) {
			@Override public void showAndWait() {
				centerOnScreen();
				super.showAndWait();
			}

			@Override public void centerOnScreen() {
				Window owner = getOwner();
				if (owner != null && owner.getScene() != null) {
					Scene scene = owner.getScene();

					// scene.getY() seems to represent the y-offset from the top of the titlebar to the
					// start point of the scene, so it is the titlebar height
					final double titleBarHeight = scene.getY();

					// because Stage does not seem to centre itself over its owner, we
					// do it here.
					double x, y;

					final double dialogWidth = parent.root.prefWidth(-1);
					final double dialogHeight = parent.root.prefHeight(-1);

					if (owner.getX() < 0 || owner.getY() < 0) {
						// Fix for #165
						Screen screen = Screen.getPrimary(); // todo something more sensible
						double maxW = screen.getVisualBounds().getWidth();
						double maxH = screen.getVisualBounds().getHeight();

						x = maxW / 2.0 - dialogWidth / 2.0;
						y = maxH / 2.0 - dialogHeight / 2.0 + titleBarHeight;
					} else {
						x = owner.getX() + (scene.getWidth() / 2.0) - (dialogWidth / 2.0);
						y = owner.getY() + titleBarHeight + (scene.getHeight() / 2.0) - (dialogHeight / 2.0);
					}

					setX(x);
					setY(y);
				}
			}
		};

		if (owner != null) {
			parent.stage.initOwner(owner);
		}
		if (parent.modal) {
			if (owner != null) {
				parent.stage.initModality(Modality.WINDOW_MODAL);
			} else {
				parent.stage.initModality(Modality.APPLICATION_MODAL);
			}
		} else {
			parent.stage.initModality(Modality.NONE);
		}

		parent.createContent();
		parent.scene = new Scene(parent.root);
		parent.scene.setFill(Color.TRANSPARENT);
		parent.stage.setScene(parent.scene);
		parent.configScene();

		if(parent.modal) {
//			parent.stage.setAlwaysOnTop(parent.alwaysOnTop);
			parent.stage.showAndWait();
			return;
//			return parent.action;
		}
		parent.stage.show();
//		return null;
		}

}
