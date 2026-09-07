package test;

import org.junit.Assert;
import org.junit.Test;
import qtnotes.actions.UndoActionType;
import qtnotes.actions.Undoer;


public class TestUndo {

    @Test
    public void removeTest() {
        var undoer = new Undoer();
        //wa|ter
        undoer.removeTextUndo(2, "t");
        undoer.removeTextUndo(2, "e");
        undoer.removeTextUndo(1, "a");
        undoer.removeTextUndo(1, "r");
        undoer.removeTextUndo(0, "w");
        undoer.removeTextUndo(0, " ");

        undoer.removeTextUndo(0, "t");
        undoer.removeTextUndo(0, "e");
        undoer.removeTextUndo(0, "a");
        undoer.removeTextUndo(0, "r");
        undoer.removeTextUndo(0, " ");

        undoer.removeTextUndo(0, "fancy");
        undoer.removeTextUndo(0, "boy");
        undoer.removeTextUndo(15, "fancyboy");

        undoer.removeTextUndo(0, " ");
        undoer.removeTextUndo(0, "\n");
        undoer.removeTextUndo(0, "\t");

        undoer.removeTextUndo(0, "k");
        undoer.removeTextUndo(0, " ");

        undoer.removeTextUndo(0, " ");
        undoer.removeTextUndo(0, " ");

        undoer.removeTextUndo(10, " ");
        undoer.removeTextUndo(10, " ");


        var note = undoer.getUndoAction(0);
        Assert.assertEquals("water ",note.text.toString());
        Assert.assertEquals(0, note.location);

        Assert.assertEquals("water ",note.text.toString());
        Assert.assertEquals(0, note.location);
        note = undoer.getUndoAction(1);
        Assert.assertEquals("tear ",note.text.toString());
        Assert.assertEquals(0, note.location);
        note = undoer.getUndoAction(2);
        Assert.assertEquals("fancy", note.text.toString());
        Assert.assertEquals(0, note.location);
        note = undoer.getUndoAction(3);
        Assert.assertEquals("boy", note.text.toString());
        Assert.assertEquals(0, note.location);
        note = undoer.getUndoAction(4);
        Assert.assertEquals("fancyboy",note.text.toString());
        Assert.assertEquals(15, note.location);
        note = undoer.getUndoAction(5);
        Assert.assertEquals(" \n\t",note.text.toString());
        note = undoer.getUndoAction(6);
        Assert.assertEquals("k ",note.text.toString());
        note = undoer.getUndoAction(7);
        Assert.assertEquals("  ",note.text.toString());
        note = undoer.getUndoAction(8);
        Assert.assertEquals("  ",note.text.toString());

    }


    @Test
    public void addingTest() {
        var undoer = new Undoer();

        undoer.addingTextUndo(2, "t");
        undoer.addingTextUndo(2, "e");
        undoer.addingTextUndo(1, "a");
        undoer.addingTextUndo(1, "r");
        undoer.addingTextUndo(0, "w");
        undoer.addingTextUndo(0, " ");

        undoer.addingTextUndo(0, "fancy");


        var note = undoer.getUndoAction(0);
        Assert.assertEquals("water ",note.text.toString());
        Assert.assertEquals(0, note.location);

        note = undoer.getUndoAction(1);
        Assert.assertEquals("fancy", note.text.toString());
        Assert.assertEquals(0, note.location);
    }

    @Test
    public void bothTest(){
        var undoer = new Undoer();
        undoer.addingTextUndo(2, "t");
        undoer.removeTextUndo(2, "e");

        var note = undoer.getUndoAction(0);
        Assert.assertEquals("t",note.text.toString());
        Assert.assertEquals(2, note.location);
        Assert.assertEquals(UndoActionType.ADD, note.actionType);

        note = undoer.getUndoAction(1);
        Assert.assertEquals("e", note.text.toString());
        Assert.assertEquals(UndoActionType.DELETE, note.actionType);

        for (int i = 0; i < 200; i++) {
            undoer.addingTextUndo(i * 2, "t");
        }
        Assert.assertEquals(47, undoer.undoIndex);
        undoer.addingTextUndo(0, "x");
        undoer.undoAnAction();
        note = undoer.undoAnAction();
        Assert.assertEquals("t", note.text.toString());
    }

    @Test
    public void undoRedoTest(){
        var undoer = new Undoer();
        undoer.removeTextUndo(2, "best");
        undoer.removeTextUndo(2, "rest");
        undoer.removeTextUndo(2, "lest");
        undoer.removeTextUndo(2, "jest");
        undoer.addingTextUndo(2, "test");

        var note = undoer.undoAnAction();
        Assert.assertEquals("test",note.text.toString());
        note = undoer.redoAnAction();
        Assert.assertEquals("test",note.text.toString());
        note = undoer.redoAnAction();
        Assert.assertNull(note);
        note = undoer.undoAnAction();
        Assert.assertEquals("test",note.text.toString());
        note = undoer.undoAnAction();
        Assert.assertEquals("jest",note.text.toString());
        note = undoer.undoAnAction();
        Assert.assertEquals("lest",note.text.toString());
        undoer.undoAnAction();
        undoer.undoAnAction();
        undoer.undoAnAction();
        undoer.undoAnAction();
        note = undoer.undoAnAction();
        Assert.assertNull(note);
        note = undoer.redoAnAction();
        Assert.assertEquals("best",note.text.toString());
        undoer.redoAnAction();
        undoer.redoAnAction();
        undoer.redoAnAction();
        note = undoer.redoAnAction();
        Assert.assertEquals("test",note.text.toString());
        undoer.redoAnAction();
        note = undoer.redoAnAction();
        Assert.assertNull(note);
        undoer.undoAnAction();
        undoer.undoAnAction();
        undoer.undoAnAction();
        undoer.undoAnAction();
        undoer.undoAnAction();
        undoer.addingTextUndo(2, "quest");
        note = undoer.redoAnAction();
        Assert.assertNull(note);
        note = undoer.undoAnAction();
        Assert.assertEquals("quest",note.text.toString());
        note = undoer.undoAnAction();
        Assert.assertNull(note);
        undoer.redoAnAction();
        undoer.redoAnAction();
        undoer.redoAnAction();
        undoer.redoAnAction();
        Assert.assertEquals(1, undoer.undoIndex);
    }
}
