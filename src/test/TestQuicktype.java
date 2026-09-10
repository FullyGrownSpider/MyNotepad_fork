package test;

import org.junit.Test;
import qtnotes.quicktype.AddedWord;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TestQuicktype {


    public final Map<String, AddedWord> data = new HashMap<>();

    public void fillData(){
        data.put("tz", new AddedWord(new String[]{"tz", "that", "", "", "", ""}));
        data.put(".w", new AddedWord(new String[]{".w", "will", "", "", "", "would", "", "won't"}));
    }
    @Test
    public void normalTest(){
        fillData();
        var out = AddedWord.createText("zt zt zt zt zt= w.[, w. w,.", data,false, true);
        assertEquals(out, "that that that that thating wouldn't will won't");
    }
}
