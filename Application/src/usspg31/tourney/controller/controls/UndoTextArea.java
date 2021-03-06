package usspg31.tourney.controller.controls;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TextArea;

public class UndoTextArea extends TextArea {

    private StringProperty undoText;

    public UndoTextArea() {
        this.init();
    }

    public UndoTextArea(String text) {
        super(text);
        this.init();
    }

    private void init() {
        this.focusedProperty().addListener((ov, o, n) -> {
            if (!n) {
                this.updateUndoText();
            }
        });
        this.setUndoText(this.getText());
    }

    private void updateUndoText() {
        if (!this.getUndoText().equals(this.getText())) {
            this.setUndoText(this.getText());
        }
    }

    /**
     * @return the undoText property
     */
    public StringProperty undoTextProperty() {
        if (this.undoText == null) {
            this.undoText = new SimpleStringProperty("") {
                @Override
                public void set(String newValue) {
                    super.set(newValue);
                    UndoTextArea.this.setText(newValue);
                }
            };
        }
        return this.undoText;
    }

    /**
     * @return the value of the undoText property
     */
    public String getUndoText() {
        return this.undoTextProperty().get();
    }

    /**
     * @param value
     *            sets the new value for the undoText property
     */
    private void setUndoText(String value) {
        this.undoTextProperty().set(value);
    }

}
