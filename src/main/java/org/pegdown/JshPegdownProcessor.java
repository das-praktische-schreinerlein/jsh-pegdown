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

import org.parboiled.Parboiled;
import org.pegdown.ast.RootNode;
import org.pegdown.plugins.PegDownPlugins;
import org.pegdown.plugins.ToHtmlSerializerPlugin;

import java.util.List;
import java.util.Map;

/**
 * the pegdown-processor with extended jsh-syntax
 */
public class JshPegdownProcessor extends PegDownProcessor {
    protected JshConfig config;

    /**
     * Creates a new processor instance without any enabled extensions and the default parsing timeout.
     */
    public JshPegdownProcessor() {
        this(DEFAULT_MAX_PARSING_TIME);
    }

    /**
     * Creates a new processor instance without any enabled extensions and the given parsing timeout.
     */
    public JshPegdownProcessor(long maxParsingTimeInMillis) {
        this(Extensions.NONE, maxParsingTimeInMillis);
    }

    /**
     * Creates a new processor instance with the given {@link org.pegdown.Extensions} and the default parsing timeout.
     *
     * @param options the flags of the extensions to enable as a bitmask
     */
    public JshPegdownProcessor(int options) {
        this(new JshConfig(), options, DEFAULT_MAX_PARSING_TIME, PegDownPlugins.NONE);
    }

    /**
     * Creates a new processor instance with the given {@link org.pegdown.Extensions} and parsing timeout.
     *
     * @param options the flags of the extensions to enable as a bitmask
     * @param maxParsingTimeInMillis the parsing timeout
     */
    public JshPegdownProcessor(int options, long maxParsingTimeInMillis) {
        this(new JshConfig(), options, maxParsingTimeInMillis, PegDownPlugins.NONE);
    }

    /**
     * Creates a new processor instance with the given {@link org.pegdown.Extensions} and plugins.
     *
     * @param options the flags of the extensions to enable as a bitmask
     * @param plugins the plugins to use
     */
    public JshPegdownProcessor(int options, PegDownPlugins plugins) {
        this(new JshConfig(), options, DEFAULT_MAX_PARSING_TIME, plugins);
    }

    /**
     * Creates a new processor instance with the given {@link org.pegdown.Extensions}, parsing timeout and plugins.
     *
     * @param options the flags of the extensions to enable as a bitmask
     * @param maxParsingTimeInMillis the parsing timeout
     * @param plugins the plugins to use
     */
    public JshPegdownProcessor(JshConfig config, int options, long maxParsingTimeInMillis, PegDownPlugins plugins) {
        this(config, Parboiled.createParser(JshPegdownParser.class, options, maxParsingTimeInMillis, JshPegdownParser.DefaultParseRunnerProvider, plugins));
    }

    /**
     * Creates a new processor instance using the given Parser.
     *
     * @param parser the parser instance to use
     */
    public JshPegdownProcessor(JshConfig config, Parser parser) {
        super(parser);
        this.config = config;
    }

    @Override
    public String markdownToHtml(char[] markdownSource,
                                 LinkRenderer linkRenderer,
                                 Map<String, VerbatimSerializer> verbatimSerializerMap,
                                 List<ToHtmlSerializerPlugin> plugins) {
        try {
            RootNode astRoot = parseMarkdown(markdownSource);
            return new JshPegdownToHtmlSerializer(this.config, linkRenderer, verbatimSerializerMap, plugins).toHtml(astRoot);
        } catch(ParsingTimeoutException e) {
            return null;
        }
    }
}
