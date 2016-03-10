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
package org.pegdown;

import org.apache.commons.lang3.StringUtils;
import org.pegdown.ast.SuperNode;
import org.pegdown.ast.TextNode;
import org.pegdown.plugins.ToHtmlSerializerPlugin;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class JshPegdownToHtmlSerializer extends ToHtmlSerializer {

    private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(JshPegdownToHtmlSerializer.class);
    protected JshConfig config;
    protected JshRenderer jshRenderer;

    public JshPegdownToHtmlSerializer(JshConfig config, LinkRenderer linkRenderer) {
        this(config, linkRenderer, Collections.<ToHtmlSerializerPlugin>emptyList());
    }

    public JshPegdownToHtmlSerializer(JshConfig config, LinkRenderer linkRenderer, List<ToHtmlSerializerPlugin> plugins) {
        this(config, linkRenderer, Collections.<String, VerbatimSerializer>emptyMap(), plugins);
    }

    public JshPegdownToHtmlSerializer(JshConfig config, final LinkRenderer linkRenderer, final Map<String, VerbatimSerializer> verbatimSerializers) {
        this(config, linkRenderer, verbatimSerializers, Collections.<ToHtmlSerializerPlugin>emptyList());
    }

    public JshPegdownToHtmlSerializer(JshConfig config, final LinkRenderer linkRenderer, final Map<String, VerbatimSerializer> verbatimSerializers, final List<ToHtmlSerializerPlugin> plugins) {
        super(linkRenderer, verbatimSerializers, plugins);
        this.config = config;
        this.jshRenderer = new JshRenderer(this.config);
        jshRenderer.initStylesClassesForTags(this.config.getStylePrefix());
    }

    /*
     * Override visitor
     */

    @Override
    public void visit(TextNode node) {
        if (JshNode.class.isInstance(node)) {
            this.renderJshNode((JshNode)node);
            return;
        }
        if (JshNodeSplitter2.class.isInstance(node)) {
            this.renderJshNodeSplitter2((JshNodeSplitter2)node);
            return;
        }
        if (JshNodeSplitter1.class.isInstance(node)) {
            this.renderJshNodeSplitter1((JshNodeSplitter1)node);
            return;
        }
        super.visit(node);
    }

    public void visit(JshNode node) {
        renderJshNode(node);
    }

    public void visit(JshNodeSplitter2 node) {
        renderJshNodeSplitter2(node);
    }
    public void visit(JshNodeSplitter1 node) {
        renderJshNodeSplitter1(node);
    }
    /*
     * renderer
     */
    public void renderJshNode(JshNode node) {
        printer.print(jshRenderer.renderJshNode(node));
    }
    public void renderJshNodeSplitter1(JshNodeSplitter1 node) {
        printer.print(jshRenderer.renderJshNodeSplitter1(node));
    }
    public void renderJshNodeSplitter2(JshNodeSplitter2 node) {
        printer.print(jshRenderer.renderJshNodeSplitter2(node));
    }

    /*
     * Override Renderer to add class-attribute
     */

    @Override
    protected void printTag(TextNode node, String tag) {
        printer.print("<").print(tag).print(jshRenderer.genStyleClassAttrForTag(tag)).print(">");
        printer.printEncoded(node.getText());
        printer.print("<").print("/").print(tag).print(">");
    }

    @Override
    protected void printTag(SuperNode node, String tag) {
        printer.print("<").print(tag).print(jshRenderer.genStyleClassAttrForTag(tag)).print(">");
        visitChildren(node);
        printer.print("<").print("/").print(tag).print(">");
    }

    @Override
    protected void printBreakBeforeTag(SuperNode node, String tag) {
        boolean startWasNewLine = printer.endsWithNewLine();
        printer.println();
        printTag(node, tag);
        if (startWasNewLine) printer.println();
    }

    @Override
    protected void printIndentedTag(SuperNode node, String tag) {
        printer.println().print("<").print(tag).print(jshRenderer.genStyleClassAttrForTag(tag)).print(">").indent(+2);
        visitChildren(node);
        printer.indent(-2).println().print("<").print("/").print(tag).print(">");
    }

    @Override
    protected void printConditionallyIndentedTag(SuperNode node, String tag) {
        if (node.getChildren().size() > 1) {
            printer.println().print("<").print(tag).print(jshRenderer.genStyleClassAttrForTag(tag)).print(">").indent(+2);
            visitChildren(node);
            printer.indent(-2).println().print("<").print("/").print(tag).print(">");
        } else {
            boolean startWasNewLine = printer.endsWithNewLine();

            printer.println().print("<").print(tag).print(jshRenderer.genStyleClassAttrForTag(tag)).print(">");
            visitChildren(node);
            printer.print("<").print("/").print(tag).print(">").printchkln(startWasNewLine);
        }
    }

    @Override
    protected void printImageTag(LinkRenderer.Rendering rendering) {
        printer.print("<img");
        printAttribute("class", jshRenderer.genStyleClassesForTag("img"));
        printAttribute("src", rendering.href);
        // shouldn"t include the alt attribute if its empty
        if(!rendering.text.equals("")){
            printAttribute("alt", rendering.text);
        }
        for (LinkRenderer.Attribute attr : rendering.attributes) {
            printAttribute(attr.name, attr.value);
        }
        printer.print(" />");
    }

    @Override
    protected void printLink(LinkRenderer.Rendering rendering) {
        printer.print("<").print("a");
        printAttribute("class", jshRenderer.genStyleClassesForTag("a"));
        printAttribute("href", rendering.href);
        for (LinkRenderer.Attribute attr : rendering.attributes) {
            printAttribute(attr.name, attr.value);
        }
        printer.print(">").print(rendering.text).print("</a>");
    }

    @Override
    protected void printWithAbbreviations(String string) {
        Map<Integer, Map.Entry<String, String>> expansions = null;

        for (Map.Entry<String, String> entry : abbreviations.entrySet()) {
            // first check, whether we have a legal match
            String abbr = entry.getKey();

            int ix = 0;
            while (true) {
                int sx = string.indexOf(abbr, ix);
                if (sx == -1) break;

                // only allow whole word matches
                ix = sx + abbr.length();

                if (sx > 0 && Character.isLetterOrDigit(string.charAt(sx - 1))) continue;
                if (ix < string.length() && Character.isLetterOrDigit(string.charAt(ix))) {
                    continue;
                }

                // ok, legal match so save an expansions "task" for all matches
                if (expansions == null) {
                    expansions = new TreeMap<Integer, Map.Entry<String, String>>();
                }
                expansions.put(sx, entry);
            }
        }

        if (expansions != null) {
            int ix = 0;
            for (Map.Entry<Integer, Map.Entry<String, String>> entry : expansions.entrySet()) {
                int sx = entry.getKey();
                String abbr = entry.getValue().getKey();
                String expansion = entry.getValue().getValue();

                printer.printEncoded(string.substring(ix, sx));
                printer.print("<abbr");
                if (StringUtils.isNotEmpty(expansion)) {
                    printer.print(" title=\"");
                    printer.printEncoded(expansion);
                    printer.print("\"");
                }
                printer.print(">");
                printer.printEncoded(abbr);
                printer.print("</abbr>");
                ix = sx + abbr.length();
            }
            printer.print(string.substring(ix));
        } else {
            printer.print(string);
        }
    }
}
