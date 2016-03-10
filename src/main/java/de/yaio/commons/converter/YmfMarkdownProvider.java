/**
 * markdown-converter
 *
 * @FeatureDomain                Converter
 * @author                       Michael Schreiner <michael.schreiner@your-it-fellow.de>
 * @category                     markdown-services
 * @copyright                    Copyright (c) 2016, Michael Schreiner
 * @license                      http://mozilla.org/MPL/2.0/ Mozilla Public License 2.0
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package de.yaio.commons.converter;

import de.yaio.commons.data.DataUtils;
import org.pegdown.Extensions;
import org.pegdown.JshConfig;
import org.pegdown.JshPegdownProcessor;
import org.pegdown.PegDownProcessor;
import org.pegdown.plugins.PegDownPlugins;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 
 * services to convert markdown to html
 */
public class YmfMarkdownProvider {
    private static int htmlElementId = 1;

    protected String CONST_PATTERN_SEG_CODE = "[\\p{L}\\p{M}\\{Z}\\p{S}\\p{N}\\p{P}\\p{Print}\\{Punct}\\p{Graph}\\p{Blank}\\n\\r]";

    /**
     * generate and export html from markdown
     * @param config            the jsh-configuration for style-prefix and appBaseName...
     * @param descText               the src of the markdown
     * @throws IOException           possible Exception
     */
    public String convertMarkdownToHtml(JshConfig config, String descText) throws IOException {
        // prepare descText
        String newDescText = this.prepareTextForMarkdown(descText);
        newDescText = newDescText.replaceAll("…", "...");

        newDescText = processMarkdownPegdown(config, newDescText);
        newDescText = newDescText.replaceAll("…", "...");
        newDescText = newDescText.replaceAll("&mdash;", "---");

        // add id to heading
        newDescText = replaceDiagrammPattern(newDescText,
                "<h([0-9]*)>",
                "<h$1 id=\"heading_",
                "\">").toString();

        // reescape > and replace markdown-hack "."
        newDescText = newDescText.replaceAll("&amp;gt;", "&gt;");
        newDescText = newDescText.replaceAll("\n\\.\n", "\n");

        return newDescText;
    }

    /**
     * process markdown to html
     * @param config            the jsh-configuration for style-prefix and appBaseName...
     * @param src               the string to process
     * @return                  processed markdown
     */
    public String processMarkdownPegdown(JshConfig config, String src) {
        PegDownProcessor pegdown = new JshPegdownProcessor(config,
                Extensions.SUPPRESS_ALL_HTML + Extensions.TABLES,
                PegDownProcessor.DEFAULT_MAX_PARSING_TIME,
                PegDownPlugins.NONE);
        String html = pegdown.markdownToHtml(src);

        // replace code-blocks
        html = replaceDiagrammPattern(html,
                "<code class=\"jsh-md-code\">mermaid(" + CONST_PATTERN_SEG_CODE + "*?)<\\/code>",
                "<div id=\"inlineMermaid",
                "\" class=\"mermaid\">$1</div>").toString();
        html = replaceDiagrammPattern(html,
                "<code class=\"jsh-md-code\">yaiofreemind(" + CONST_PATTERN_SEG_CODE + "*?)<\\/code>",
                "<div id=\"inlineMindmap",
                "\" class=\"yaiomindmap\">$1</div>").toString();
        html = replaceDiagrammPattern(html,
                "<code class=\"jsh-md-code\">yaiomindmap(" + CONST_PATTERN_SEG_CODE + "*?)<\\/code>",
                "<div id=\"inlineMindmap",
                "\" class=\"yaiomindmap\">$1</div>").toString();
        html = replaceDiagrammPattern(html,
                "<code class=\"jsh-md-code\">yaioplantuml(" + CONST_PATTERN_SEG_CODE + "*?)<\\/code>",
                "<div id=\"yaioplantuml",
                "\" class=\"yaioplantuml\">$1</div>").toString();
        html = replaceDiagrammPattern(html,
                "<code class=\"jsh-md-code\">ymffreemind(" + CONST_PATTERN_SEG_CODE + "*?)<\\/code>",
                "<div id=\"inlineMindmap",
                "\" class=\"ymfmindmap\">$1</div>").toString();
        html = replaceDiagrammPattern(html,
                "<code class=\"jsh-md-code\">ymfmindmap(" + CONST_PATTERN_SEG_CODE + "*?)<\\/code>",
                "<div id=\"inlineMindmap",
                "\" class=\"ymfmindmap\">$1</div>").toString();
        html = replaceDiagrammPattern(html,
                "<code class=\"jsh-md-code\">ymfplantuml(" + CONST_PATTERN_SEG_CODE + "*?)<\\/code>",
                "<div id=\"ymfplantuml",
                "\" class=\"ymfplantuml\">$1</div>").toString();
        html = replaceDiagrammPattern(html,
                "<code class=\"jsh-md-code\">(" + CONST_PATTERN_SEG_CODE + "*?)<\\/code>",
                "<pre><code id=\"inlineCode",
                "\" class=\"jsh-code txt\">$1</code></pre>").toString();
        return html;
    }

    /**
     * prepare the text to format as markdown
     * prefix empty lines inline code-segs (```) so that they will interprewted as codeline by markdown-parser
     * @param descText               the string to prepare
     * @return                       prpeared text to format as markdown
     */
    public String prepareTextForMarkdown(final String descText) {
        // prepare descText
        String newDescText = "";
        String newDescTextRest = DataUtils.htmlEscapeTextLazy(descText);
        newDescTextRest = newDescTextRest.replaceAll("\\&lt;br\\&gt;", "<br>");
        newDescTextRest = newDescTextRest.replaceAll("\\&lt;\\!---", "<!---");
        newDescTextRest = newDescTextRest.replaceAll("---\\&gt;", "--->");

        int codeStart = newDescTextRest.indexOf("```");
        while (codeStart >= 0) {
            // splice start and add to newDescText
            newDescText += newDescTextRest.substring(0, codeStart + 3);
            newDescTextRest = newDescTextRest.substring(codeStart + 3);

            int codeEnd = newDescTextRest.indexOf("```");
            if (codeEnd >= 0) {
                // splice all before ending ```
                String code = newDescTextRest.substring(0, codeEnd);
                newDescTextRest = newDescTextRest.substring(codeEnd);

                // replace empty lines in code
                code = code.replaceAll("\r\n", "\n");
                code = code.replaceAll("\n\r", "\n");
                code = code.replaceAll("\n[ \t]*\n", "\n.\n");
                code = code.replaceAll("\n\n", "\n.\n");

                // add code to newDescText
                newDescText += code;

                // extract ending ``` and add it to newDescText
                newDescText += newDescTextRest.substring(0, 3);
                newDescTextRest = newDescTextRest.substring(3);
            }
            codeStart = newDescTextRest.indexOf("```");
        }
        // add rest to newDescText
        newDescText += newDescTextRest;

        return newDescText;
    }

    /**
     * search for the pattern and replace it with the replacementhead + htmlId + replacementTail
     * @param text                   the haystack
     * @param patternString          the needle to replace
     * @param replacementHead        the head before the new htmlelement-id
     * @param replacementTail        the tail after the new htmlelement-id
     * @return                       formatted diagramm-markdown
     */
    protected StringBuffer replaceDiagrammPattern(final String text,
                                                  final String patternString,
                                                  final String replacementHead, final String replacementTail) {
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(text);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(result, replacementHead + new Integer(htmlElementId++) + replacementTail);
        }
        matcher.appendTail(result);

        return result;
    }
}
