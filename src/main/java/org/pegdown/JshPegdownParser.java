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

import org.parboiled.Rule;
import org.parboiled.common.ArrayBuilder;
import org.pegdown.plugins.PegDownPlugins;

/**
 * the pegdown-parser with extended jsh-syntax
 */
public class JshPegdownParser extends Parser{

    public JshPegdownParser(Integer options, Long maxParsingTimeInMillis, ParseRunnerProvider parseRunnerProvider, PegDownPlugins plugins) {
        super(options, maxParsingTimeInMillis, parseRunnerProvider, plugins);
    }

    public JshPegdownParser(Integer options, Long maxParsingTimeInMillis, ParseRunnerProvider parseRunnerProvider) {
        this(options, maxParsingTimeInMillis, parseRunnerProvider, PegDownPlugins.NONE);
    }

    @Override
    public Rule NonLinkInline() {
        return FirstOf(new ArrayBuilder<Rule>()
                .add(plugins.getInlinePluginRules())
                .add(JshInline(), Str(), Endline(), UlOrStarLine(), Space(), StrongOrEmph(), Image(), Code(), JshBlock(), InlineHtml(), JshBlock(),
                        Entity(), EscapedChar(), JshInline())
                .addNonNulls(ext(QUOTES) ? new Rule[]{SingleQuoted(), DoubleQuoted(), DoubleAngleQuoted()} : null)
                .addNonNulls(ext(SMARTS) ? new Rule[]{Smarts()} : null)
                .addNonNulls(ext(STRIKETHROUGH) ? new Rule[]{Strike()} : null)
                .add(Symbol())
                .get()
        );
    }

    @Override
    public Rule Block() {
        return Sequence(
                ZeroOrMore(BlankLine()),
                FirstOf(new ArrayBuilder<Rule>()
                        .add(plugins.getBlockPluginRules())
                        .add(BlockQuote(), Verbatim())
                        .addNonNulls(ext(ABBREVIATIONS) ? Abbreviation() : null)
                        .add(JshInline(), Reference(), HorizontalRule(), Heading(), OrderedList(), BulletList(), JshBlock(), HtmlBlock(), JshBlock(), JshInline())
                        .addNonNulls(ext(TABLES) ? Table() : null)
                        .addNonNulls(ext(DEFINITIONS) ? DefinitionList() : null)
                        .addNonNulls(ext(FENCED_CODE_BLOCKS) ? FencedCodeBlock() : null)
                        .add(Para(), Inlines())
                        .get()
                )
        );
    }

    /**
     * generate rule to extract jsh-blocks
     * @return     rule to extract jsh-blocks
     */
    public Rule JshBlock() {
        return NodeSequence(FirstOf(new ArrayBuilder<Rule>()
                .addNonNulls(new Rule[]{JshHtmlComment()})
                .get())
        );
    }

    /**
     * generate rule to extract jsh-comments with commands like BOX...
     * @return     rule to extract jsh-comments
     */
    public Rule JshHtmlComment() {
        Rule rule;
        rule = Sequence(
                "<!---",
                OneOrMore(TestNot(Sequence("---", ">")), ANY), // might have to restrict from ANY
                push(new JshNode(match())),
                "--->");
        return rule;
    }

    /**
     * generate rule to extract jsh-inlineblocks with commands like BOX...
     * @return     rule to extract jsh-inlineblocks
     */
    public Rule JshInline() {
        return NodeSequence(FirstOf(new ArrayBuilder<Rule>()
                .addNonNulls(new Rule[]{JshSplitter()})
                .get())
        );
    }

    /**
     * generate rule to extract jsh-splitter
     * @return     rule to extract jsh-splitter
     */
    public Rule JshSplitter() {
        // TODO - not functional
        Rule rule;
        rule = Sequence(
                Test(OneOrMore(TestNot(":|:"), ANY)),
                push(new JshNodeSplitter1(match())),
                ":|:",
                Test(OneOrMore(TestNot(":|:"), ANY)),
                push(new JshNodeSplitter2(match()))
                );
        return rule;
    }
}

