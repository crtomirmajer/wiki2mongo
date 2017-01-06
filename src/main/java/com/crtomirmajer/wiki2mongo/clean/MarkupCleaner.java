package com.crtomirmajer.wiki2mongo.clean;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Majer on 3.11.2016.
 */
public class MarkupCleaner implements Cleaner{
    
    private static final List<String> DEFAULT_INVALID_SECTIONS = Arrays.asList("See also",
                                                                               "References",
                                                                               "Further reading",
                                                                               "External Links",
                                                                               "Bibliography",
                                                                               "Notes");
    
    //Wiki Markup matchers
    private Pattern unitConversionMatcher = Pattern.compile("\\{\\{convert\\|([\\d\\./\\-,+x]+)\\|([^|]+)\\|[^}]+\\}\\}");
    private Pattern listPrefixMatcher     = Pattern.compile("(?:\\n)(;|:|\\*|#)+[^\\n]+");
    private Pattern categoryMatcher       = Pattern.compile("\\[\\[Category:([^\\]]+)\\]\\]");
    private Pattern externalLinkMatcher   = Pattern.compile(
            "(\\[(\\/+))?(https?:\\/\\/)*(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+\" + \"" +
                    ".~#?&//=]*)\\]?");
    private Pattern linksMatcher          = Pattern.compile("\\[\\[((:|#)*[^\\[\\]]+(:|\\|))*([^\\[\\]\\|]+)\\|*\\]\\]");
    private Pattern indentationMatcher    = Pattern.compile("[\\n\\r](:|;)+\\s*");
    private Pattern langMatcher           = Pattern.compile("(\\{\\{lang\\|[a-z]+\\|)([^}]+)(\\}\\})");
    private Pattern doubleBracesMarcher   = Pattern.compile("\\{\\{((?!\\{\\{|\\}\\})[\\s\\S])*\\}\\}");
    private Pattern headingMatcher        = Pattern.compile("==+\\s?(.*?)\\s?==+");
    private Pattern textStyleMatcher      = Pattern.compile("('{2,}|-{4,}|__(FORCE|NO)*TOC__)");
    private Pattern invalidSectionMatcher;
    
    //HTML matchers
    private Pattern preMatcher     = Pattern.compile("<pre[^>]*>((?!<\\/*pre>)[\\s\\S])*<\\/pre>");
    private Pattern syntaxMatcher  = Pattern.compile(
            "<syntaxhighlight[^>]*>((?!<\\/*syntaxhighlight>)[\\s\\S])*<\\/syntaxhighlight>");
    private Pattern sourceMatcher  = Pattern.compile("<source[^>]*>((?!<\\/*source>)[\\s\\S])*<\\/source>");
    private Pattern commentMatcher = Pattern.compile("<!--((?!-->)[\\s\\S])*-->");
    private Pattern monoTagMatcher = Pattern.compile("<[A-z0-9]+[^<>]*\\s*\\/\\s*>");
    private Pattern fakeTagMatcher = Pattern.compile("((<(\\?|%))|((\\?|%))>)");
    
    public MarkupCleaner(Builder builder) {
        this.invalidSectionMatcher = builder.invalidSectionMatcher;
    }
    
    public String clean(String markup) {
        String clean = markup;
        
        clean = removeInvalidSections(clean);
        clean = removeCategories(clean);
        clean = removeTables(clean);
        
        clean = removeLang(clean);
        clean = removeDoubleBraces(clean);
        
        clean = removeHeadings(clean);
        clean = removeIndentation(clean);
        clean = removeListPrefix(clean);
        
        clean = removeHtmlTags(clean);
        
        clean = removeExternalLinks(clean);
        clean = removeLinks(clean);
        
        clean = fixUnitConversion(clean);
        clean = removeStyle(clean);
        
        clean = clean.replaceAll("\\([;:\\-.,!?=*+%&\"#$/'\\s]+\\)", " ");
        clean = clean.replaceAll("\\s{2,}", " ");
        
        return clean;
    }
    
    public String removeLang(String text) {
        return langMatcher.matcher(text).replaceAll("$2");
    }
    
    public String removeHtmlTags(String markup) {
        
        String clean = preMatcher.matcher(markup).replaceAll(" ");
        clean = sourceMatcher.matcher(clean).replaceAll(" ");
        clean = syntaxMatcher.matcher(clean).replaceAll(" ");
        clean = commentMatcher.matcher(clean).replaceAll(" ");
        clean = monoTagMatcher.matcher(clean).replaceAll(" ");
        clean = fakeTagMatcher.matcher(clean).replaceAll(" ");
        
        try {
            Document document = Jsoup.parse(clean, "", Parser.xmlParser());
            document.select("math, gallery, ref, br, ins, s, del, tt, blockqoute, table").html(" ");
            clean = document.text();
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        return clean;
    }
    
    public String removeStyle(String s) {
        return textStyleMatcher.matcher(s).replaceAll("");
    }
    
    public String removeExternalLinks(String text) {
        return externalLinkMatcher.matcher(text).replaceAll(" ");
    }
    
    public String removeInvalidSections(String text) {
        return invalidSectionMatcher.matcher(text).replaceAll("");
    }
    
    public String removeCategories(String text) {
        return categoryMatcher.matcher(text).replaceAll("");
    }
    
    public String removeIndentation(String text) {
        return indentationMatcher.matcher(text).replaceAll(" ");
    }
    
    public String fixUnitConversion(String text) {
        return unitConversionMatcher.matcher(text).replaceAll("$1 $2");
    }
    
    public String removeListPrefix(String s) {
        Matcher m = listPrefixMatcher.matcher(s);
        
        while(m.find()) {
            String clean = m.group().replaceFirst("(\\*|#|:|;)+", "").trim();
            if(!clean.matches("^.*(\\.|!|\\?|:)+$"))
                clean += ". ";
            else if(!clean.matches("^.*\\s"))
                clean += " ";
            s = s.replace(m.group(), clean);
        }
        
        return s;
    }
    
    public String removeHeadings(String text) {
        return headingMatcher.matcher(text).replaceAll(" ");
    }
    
    public String removeDoubleBraces(String text) {
        
        int len = text.length();
        while(true) {
            text = doubleBracesMarcher.matcher(text).replaceAll(" ");
            
            if(text.length() == len)
                break;
            
            len = text.length();
        }
        
        return text;
    }
    
    public String removeLinks(String text) {
        
        if(text.contains("[[File:")) {
            text = linksMatcher.matcher(text).replaceAll("$4");
        }
        
        return linksMatcher.matcher(text).replaceAll("$4");
        
    }
    
    public String removeTables(String markup) {
        for(int i = markup.indexOf("{|") ; i != -1 ; i = markup.indexOf("{|", i)) {
            byte state = 0;
            int level = 1;
            
            int cur;
            for(cur = i + 2; cur < markup.length() ; ++cur) {
                if(state == 2 && markup.charAt(cur) == 124) {
                    ++level;
                    state = 0;
                }
                
                if(state == 2) {
                    state = 0;
                }
                
                if(markup.charAt(cur) == 123) {
                    state = 2;
                }
                
                if(state == 1 && markup.charAt(cur) == 125) {
                    --level;
                    if(level == 0) {
                        break;
                    }
                    
                    state = 0;
                }
                else {
                    if(state == 1) {
                        state = 0;
                    }
                    
                    if(markup.charAt(cur) == 124) {
                        state = 1;
                    }
                }
            }
            
            if(cur == markup.length()) {
                return markup.substring(0, i);
            }
            
            markup = markup.substring(0, i) + markup.substring(cur + 1, markup.length());
        }
        
        return markup;
    }
    
    //endregion
    
    //region Builder
    
    public static final class Builder {
        private List<String> invalidSections = DEFAULT_INVALID_SECTIONS;
        
        private Pattern invalidSectionMatcher;
        
        public Builder() {}
        
        public MarkupCleaner build() {
            
            String sections = String.join("|", invalidSections);
            invalidSectionMatcher = Pattern.compile("==\\s*(" + sections + ")\\s*==[\\s\\S]*", 34);
            
            return new MarkupCleaner(this);
        }
        
        public Builder invalidSections(List<String> invalidSections) {
            
            if(invalidSections == null || invalidSections.size() < 1)
                throw new IllegalArgumentException();
            
            this.invalidSections = invalidSections;
            return this;
        }
    }
    
    //endregion
}
