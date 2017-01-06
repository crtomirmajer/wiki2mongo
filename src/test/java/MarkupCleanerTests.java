import com.crtomirmajer.wiki2mongo.clean.MarkupCleaner;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Majer
 */
public class MarkupCleanerTests {
    
    private MarkupCleaner markupCleaner = new MarkupCleaner.Builder().build();
    
    @Test
    public void fixUnitConversion() {
        
        String result = markupCleaner.fixUnitConversion("{{convert|18.45|in|ft in hand cm|frac=2|1}}");
        Assert.assertEquals("18.45 in", result);
        
        result = markupCleaner.fixUnitConversion("{{convert|1,234,567|m|ft|comma=off}}");
        Assert.assertEquals("1,234,567 m", result);
    }
    
    @Test
    public void removeListPrefix() {
        String result = markupCleaner.removeListPrefix("\n* this is line:\n# this is another line\n##: woho");
        Assert.assertEquals("this is line: this is another line. woho. ", result);
        
        result = markupCleaner.removeListPrefix(
                "\n;Absheron Economic Region\n* Absheron Rayon\n* Khizi Rayon\n* Baku\n* Sumqayıt");
        Assert.assertEquals("Absheron Economic Region. Absheron Rayon. Khizi Rayon. Baku. Sumqayıt. ", result);
    }
    
    @Test
    public void removeCategories() {
        String result = markupCleaner.removeCategories("[[Category:Software development kits]]");
        Assert.assertEquals("", result);
    }
    
    @Test
    public void removeLinks() {
        String result = markupCleaner.removeLinks("[[Java version history]]");
        Assert.assertEquals("Java version history", result);
        
        result = markupCleaner.removeLinks("[[alpha|beta]]");
        Assert.assertEquals("beta", result);
        
        result = markupCleaner.removeLinks("[[:commons:Athens]]");
        Assert.assertEquals("Athens", result);
        
        result = markupCleaner.removeLinks("[[Page name#Section name|displayed text]]");
        Assert.assertEquals("displayed text", result);
        
        result = markupCleaner.removeLinks("[[commons:Boston, Massachusetts|]]");
        Assert.assertEquals("Boston, Massachusetts", result);
        
        result = markupCleaner.removeLinks("[[File:UFHistoricBuildingBuckmanHall.JPG|thumb|left|upright|alt=Exterior of Buckman Hall, a red brick and" +
                                                   " carved stone student residence hall at the University of Florida, built in 1905–06 in the " +
                                                   "then-popular collegiate gothic architectural style.  |[[Buckman Hall (Gainesville, Florida)" +
                                                   "|Buckman Hall]], one of the first two buildings on the new campus of the [[University of " +
                                                   "Florida]].  Andrew Sledd and his family lived in Buckman Hall from 1906 to 1909.]]");
        Assert.assertEquals("Buckman Hall, one of the first two buildings on the new campus of the University of Florida.  Andrew Sledd and his family lived in Buckman Hall from 1906 to 1909.", result);
    }
    
    @Test
    public void removeIndentation() {
        
        String result = markupCleaner.removeIndentation("\n;item\n: single\n::: multiple");
        Assert.assertEquals(" item single multiple", result);
    }
    
    @Test
    public void clearLangBraces() {
        
        String text = "{{lang|fr|Anatole France}} {{lang|fr|Jacob France}}";
        String result = markupCleaner.removeLang(text);
        System.out.println(result);
    }
    
    @Test
    public void removeDoubleBraces() {
        
        String result = markupCleaner.removeDoubleBraces("{{this}}");
        Assert.assertEquals(" ", result);
        
        result = markupCleaner.removeDoubleBraces("{{IPA|[ˈkæ.ɹəkˌtə(ɹ)z]}}");
        Assert.assertEquals(" ", result);
        
        result = markupCleaner.removeDoubleBraces(
                "{{efn|Printed out, the characters are: {{Pre2|scroll|<nowiki> !\"#$%&'()*+,-./0123456789:;" +
                        "<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~</nowiki>}}}}");
        Assert.assertEquals(" ", result);
        
        result = markupCleaner.removeDoubleBraces(
                "{{Infobox country\n" + "| latd=40 \n" + "|latm=25 \n" + "|latNS=N \n" + "|longd=49 \n" + "|longm=50 \n" +
                        "|longEW=E\n" + "|languages_type = Other languages\n" +
                        "|languages = [[Russian language|Russian]], [[Armenian language|Armenian]] (In Nagorno-Karabakh " +
                        "only)\n" + "| leader_name2 = [[Artur Rasizade]]\n" +
                        "| conventional_long_name = Republic of Azerbaijan\n" +
                        "| native_name = {{native name|az|Azərbaycan Respublikası}}\n" +
                        "| image_flag = Flag of Azerbaijan.svg\n" + "| image_coat = Coat of arms of Azerbaijan.svg\n" +
                        "| alt_flag = Three equally sized horizontal bands of blue, red, and green, with a white crescent " +
                        "and an eight-pointed star centered in the red band\n" + "| common_name =Azerbaijan\n" +
                        "| symbol_type = Emblem}}");
        Assert.assertEquals(" ", result);
    }
    
    @Test
    public void removeHeadings() {
        
        String result = markupCleaner.removeHeadings("==heading==");
        Assert.assertEquals(" ", result);
        
        result = markupCleaner.removeHeadings("== heading ==");
        Assert.assertEquals(" ", result);
        
        result = markupCleaner.removeHeadings("===heading asf ===");
        Assert.assertEquals(" ", result);
        
        result = markupCleaner.removeHeadings("===heading asf === ==sada==");
        Assert.assertEquals("   ", result);
    }
    
    @Test
    public void removeStyle() {
        
        String result = markupCleaner.removeStyle(" ---- ");
        Assert.assertEquals("  ", result);
        
        result = markupCleaner.removeStyle("__NOTOC__ __TOC__ __FORCETOC__");
        Assert.assertEquals("  ", result);
        
        result = markupCleaner.removeStyle("''''don't'''' ''appear''");
        Assert.assertEquals("don't appear", result);
    }
    
    @Test
    public void removeExternalLinks() {
        
        String result = markupCleaner.removeExternalLinks("[//en.wikipedia.org/w/index.php?title=Help:Link no protocol]");
        Assert.assertEquals(" ", result);
        
        result = markupCleaner.removeExternalLinks("http://github.com/crtomirmajer");
        Assert.assertEquals(" ", result);
    }
    
    @Test
    public void removeInvalidSections() {
        String result = markupCleaner.removeInvalidSections("== sEE AlSO == this is unwanted");
        Assert.assertEquals("", result);
        
        result = markupCleaner.removeInvalidSections("==NoTeS== this is unwanted");
        Assert.assertEquals("", result);
    }
    
    @Test
    public void removeHtmlTags() {
        
        String result = markupCleaner.removeHtmlTags(
                "<!DOCTYPE " + "html><html><head><title></title></head><body><h1>Text</h1></body></html>");
        Assert.assertEquals("Text", result);
        
        result = markupCleaner.removeHtmlTags("<ref name=UNLANGDATA/> Russian and English");
        Assert.assertEquals("Russian and English", result);
    
        result = markupCleaner.removeHtmlTags("<!-- comment -->");
        Assert.assertEquals("", result);
    
        result = markupCleaner.removeHtmlTags("<?php php?> <%asp%> <%jsp%>");
        Assert.assertEquals("php php asp jsp", result);
    
        result = markupCleaner.removeHtmlTags("<table>this is content</table>");
        Assert.assertEquals("", result);
        
        result = markupCleaner.removeHtmlTags("<syntaxhighlight>this is code</syntaxhighlight>");
        Assert.assertEquals("", result);
    }
    
    @Test
    public void removeTables() {
        
        String result = markupCleaner.removeTables("{| class=\"wikitable\"\n" + "|-\n" + "! Header 1\n" + "! Header 2\n" +
                                                           "! Header 3\n" + "|-\n" + "| row 1, cell 1\n" +
                                                           "| row 1, cell 2\n" + "| row 1, cell 3\n" + "|-\n" +
                                                           "| row 2, cell 1\n" + "| row 2, {|\n" + "|+ caption\n" +
                                                           "  table code goes here\n" + "|}\n" + "| row 2, cell 3\n" +
                                                           "|}");
        Assert.assertEquals("", result);
    }
}
