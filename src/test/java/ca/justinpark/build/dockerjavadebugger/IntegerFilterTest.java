package ca.justinpark.build.dockerjavadebugger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.swing.*;
import javax.swing.text.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntegerFilterTest {

    @ParameterizedTest
    @ValueSource(strings = {"t", "*", " "})
    void nonInteger(String input) {
        JTextField textField = new JTextField();
        PlainDocument doc = (PlainDocument) textField.getDocument();
        doc.setDocumentFilter(new IntegerFilter());
        textField.setText(input);
        assertEquals("", textField.getText());
    }

    @ParameterizedTest
    @ValueSource(strings = {"t", "*", " "})
    void addNonIntegerCharacter(String input) throws BadLocationException {
        JTextField textField = new JTextField();
        PlainDocument doc = (PlainDocument) textField.getDocument();
        IntegerFilter filter = new IntegerFilter();
        textField.setText("1");
        doc.setDocumentFilter(filter);

        filter.insertString(new FakeFilterBypass(doc), 0, input, new SimpleAttributeSet());
        assertEquals("1", textField.getText());

    }

    @Test
    void removeCharacter() throws BadLocationException {
        JTextField textField = new JTextField();
        PlainDocument doc = (PlainDocument) textField.getDocument();
        IntegerFilter filter = new IntegerFilter();
        textField.setText("12");
        doc.setDocumentFilter(filter);

        filter.remove(new FakeFilterBypass(doc), 0, 1);
        assertEquals("2", textField.getText());

    }

    class FakeFilterBypass extends DocumentFilter.FilterBypass {
        private Document doc;

        public FakeFilterBypass(Document doc) {
            this.doc = doc;
        }

        @Override
        public Document getDocument() {
            return this.doc;
        }

        @Override
        public void remove(int offset, int length) throws BadLocationException {
            doc.remove(offset, length);
        }

        @Override
        public void insertString(int offset, String string, AttributeSet attrs) throws BadLocationException {
            doc.insertString(offset, string, attrs);
        }

        @Override
        public void replace(int offset, int length, String string, AttributeSet attrs) throws BadLocationException {
            doc.remove(offset, length);
            doc.insertString(offset, string, attrs);
        }
    }
}
