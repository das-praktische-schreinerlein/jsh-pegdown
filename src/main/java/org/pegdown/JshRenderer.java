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

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JshRenderer {

    private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(JshRenderer.class);

    protected HashMap<String, HashMap<String,  String>> allTagStyles = new HashMap<>();
    protected int nextTocId = 1;
    protected JshConfig config;

    protected static Pattern regExpToggler = Pattern.compile("^(TOGGLER) *([-#_a-zA-Z,;0-9\\.]*?) *");
    protected static Pattern regExpTogglerAppend = Pattern.compile("^(TOGGLER\\.AFTER|TOGGLER\\.BEFORE) *([-#_a-zA-Z,;0-9\\.: ]*?) *");
    protected static Pattern regExpSplitter = Pattern.compile("([\\s\\S]*?)(:\\|:)(.*)([\\s\\S]*)");
    protected static Pattern regExpToc = Pattern.compile("^(TOC) *([-#_a-zA-Z,;0-9\\.]*?) *");
    protected static Pattern regExpRuleBoxStart = Pattern.compile("^(BOX\\.INFO|BOX\\.WARN|BOX\\.ALERT|BOX|CONTAINER|STYLE?) *([#-_a-zA-Z,;0-9\\.: ]*?) *");
    protected static Pattern regExpRuleBoxEnd = Pattern.compile("^/(BOX\\.INFO|BOX\\.WARN|BOX\\.ALERT|BOX|CONTAINER|STYLE?) *([#-_a-zA-Z,;0-9\\.: ]*?) *");

    public JshRenderer(JshConfig config) {
        this.config = config;
    }

    public String renderJshNode(JshNode node) {
        String res = "";
        String src = node.getText();

        Matcher matcher;

        matcher = regExpRuleBoxStart.matcher(src);
        if (matcher.matches() && matcher.group(1) != null) {
            res = renderExtendedMarkdownBoxStart(matcher.group(1), matcher.group(2));
            return res;
        }

        matcher = regExpRuleBoxEnd.matcher(src);
        if (matcher.matches() && matcher.group(1) != null) {
            res = renderExtendedMarkdownBoxEnd(matcher.group(1), matcher.group(2));
            return res;
        }

        matcher = regExpToggler.matcher(src);
        if (matcher.matches() && matcher.group(1) != null) {
            res = renderExtendedMarkdownToggler(matcher.group(1), matcher.group(2));
            return res;
        }
        matcher = regExpTogglerAppend.matcher(src);
        if (matcher.matches() && matcher.group(1) != null) {
            res = renderExtendedMarkdownTogglerAppend(matcher.group(1), matcher.group(2));
            return res;
        }

        matcher = regExpToc.matcher(src);
        if (matcher.matches() && matcher.group(1) != null) {
            res = renderExtendedMarkdownTOC(matcher.group(1), matcher.group(2));
            return res;
        }

        return res;
    }

    public String renderJshNodeSplitter1(JshNodeSplitter1 node) {
        return renderExtendedMarkdownSplitter1("splitter", "", node.getText());
    }

    public String renderJshNodeSplitter2(JshNodeSplitter2 node) {
        return renderExtendedMarkdownSplitter2("splitter", "", node.getText());
    }

    /*
     * services
     */
    public String genStyleClassesForTag(String tag) {
        HashMap<String, String> styles = allTagStyles.get(tag);
        if (MapUtils.isEmpty(styles)) {
            return "";
        }
        return StringUtils.join(styles.keySet(), " ");
    };

    public String genStyleClassAttrForTag(String tag) {
        String styleClasses = genStyleClassesForTag(tag);
        if (StringUtils.isEmpty(styleClasses)) {
            return "";
        }
        return " class=\"" + styleClasses + "\"";
    };


    public void initStylesClassesForTags(String prefix) {
        String[] tags = new String[]{
                "h1", "h2", "h3", "h4", "h5", "h6", "h7", "h8",
                "img", "a", "p", "table", "tr", "td", "th", "tbody", "thead", "br", "li", "ul", "ol",
                "container", "code",
                "box", "box-ue", "box-container",
                "infobox", "infobox-ue", "infobox-container",
                "warnbox", "warnbox-ue", "warnbox-container",
                "alertbox", "alertbox-ue", "alertbox-container",
                "togglerparent", "splitter1", "splitter2"
        };
        for (java.lang.String tag : tags) {
            String style = (StringUtils.isNotEmpty(prefix) ? prefix : "") + "md-" + tag;
            HashMap<String, String> tagStyles = allTagStyles.get(tag);
            if (MapUtils.isEmpty(tagStyles)) {
                tagStyles = new HashMap<String, String>();
            }
            tagStyles.put(style, style);
            allTagStyles.put(tag, tagStyles);
        }
    };

    public String renderExtendedMarkdownBoxhtmlStart(String type, String param) {
        return "<div class=\"" + genStyleClassesForTag(type + "box") + "\">" +
                "<div class=\"" + genStyleClassesForTag(type + "box-ue") + "\">" + param + "</div>" +
                "<div class=\"" + genStyleClassesForTag(type + "box-container") + "\">";
    };

    public String renderExtendedMarkdownBoxStart(String type, String param) {
        String res = "";
        if ("box".equalsIgnoreCase(type)) {
            res = "<div class=\"" + genStyleClassesForTag("box") + " " + param + "\">";
        } else if ("container".equalsIgnoreCase(type)) {
            res = "<div class=\"" + genStyleClassesForTag("container") + " md-container-" + param + "\" id=\"md-container-" + param + "\">";
        } else if ("box.info".equalsIgnoreCase(type)) {
            res = renderExtendedMarkdownBoxhtmlStart("info", param);
        } else if ("box.warn".equalsIgnoreCase(type)) {
            res = renderExtendedMarkdownBoxhtmlStart("warn", param);
        } else if ("box.alert".equalsIgnoreCase(type)) {
            res = renderExtendedMarkdownBoxhtmlStart("alert", param);
        } else if ("style".equalsIgnoreCase(type) && StringUtils.isNotEmpty(param)) {
            // do set style for next elements

            // split params elements:styles
            String[] params = param.split(":");
            String[] tags = new String[]{};
            String[] styles = new String[]{};
            if (params.length > 0) {
                tags = params[0].split(" ");
                if (params.length > 1) {
                    styles = params[1].split(" ");
                }
            }
            // set styles for all tags
            for (String tag : tags) {
                HashMap<String, String> tagStyles = allTagStyles.get(tag);
                if (MapUtils.isEmpty(tagStyles)) {
                    tagStyles = new HashMap<>();
                }
                for (String style : styles) {
                    tagStyles.put(style, style);
                }
                allTagStyles.put(tag, tagStyles);
            }
        }

        return res;
    };

    public String renderExtendedMarkdownBoxEnd(String type, String param) {
        String res = "";

        if ("box".equalsIgnoreCase(type)) {
            res = "</div>";
        } else if ("box.info".equalsIgnoreCase(type) ||
                "box.alert".equalsIgnoreCase(type) ||
                "box.warn".equalsIgnoreCase(type)) {
            res = "</div></div>";
        } else if ("container".equalsIgnoreCase(type)) {
            res = "</div>";
        } else if ("style".equalsIgnoreCase(type) && StringUtils.isNotEmpty(param)) {
            // do reset style for next elements
            // split params elements:styles
            String[] params = param.split(":");
            String[] tags = new String[]{};
            String[] styles = new String[]{};
            if (params.length > 0) {
                tags = params[0].split(" ");
                if (params.length > 1) {
                    styles = params[1].split(" ");
                }
            }
            // reset styles for all tags
            for (String tag : tags) {
                for (String style : styles) {
                    if (allTagStyles.containsKey(tag) && allTagStyles.get(tag).containsKey(style)) {
                        allTagStyles.get(tag).remove(style);
                    }
                }
            }
        }
        return res;
    };

    public String renderExtendedMarkdownToggler(String type, String attr) {
        String res = "";
        String[] params = (StringUtils.isNotEmpty(attr) ? attr  : "").split(",");
        String togglerType = "icon";
        String id = null;
        if (params.length > 0) {
            id = params[0].replaceAll(" ", "");
            if (params.length > 1) {
                togglerType = params[1];
            }
        }

        if ("toggler".equalsIgnoreCase(type) && StringUtils.isNotEmpty(id)) {
            res = "<div class=\"" + genStyleClassesForTag("togglerparent") + " md-togglerparent-" + id + "\" id=\"md-togglerparent-" + id + "\"></div>" +
                    "<script>" + config.getAppBaseVarName() + ".get(\"UIToggler\").appendToggler(\".md-togglerparent-" + id + "\", \".md-container-" + id + "\", \"" + togglerType + "\");</script>";
        }
        return res;
    };

    public String renderExtendedMarkdownTogglerAppend(String type, String attr) {
        String res = "";
        String[] params = (StringUtils.isNotEmpty(attr) ? attr  : "").split(",");
        String togglerType = "icon";
        String[] tags = new String[]{};
        String[] styles = new String[]{};
        boolean flgInsertBefore = (type.equalsIgnoreCase("TOGGLER.BEFORE"));
        if (params.length > 0) {
            if (params.length > 1) {
                togglerType = params[1];
            }

            // split params elements:styles
            String[] filter = params[0].replaceAll(" ", "").split(":");
            if (filter.length > 0) {
                tags = filter[0].split(" ");
                if (filter.length > 1) {
                    styles = filter[1].split(" ");
                }
            }
            for (String tag : tags) {
                for (String style : styles) {
                    res = "<script>" + config.getAppBaseVarName() + ".get(\"UIToggler\").appendTogglerForElements(\"" +
                            tag + "." + style + "\", \"" + togglerType + "\", " + flgInsertBefore + ");</script>";
                }
            }
        }

        return res;
    };

    public String renderExtendedMarkdownTOC(String type, String attr) {
        String res = "";
        String[] params = (StringUtils.isNotEmpty(attr) ? attr  : "").split(",");
        String togglerType = "icon";
        String id;
        if (params.length > 0) {
            id = params[0].replaceAll(" ", "");
            if (params.length > 1) {
                togglerType = params[1];
            }
        }
        if ("toc".equalsIgnoreCase(type)) {
            String tocId = "jsh-md-toc-container-" + nextTocId,
                    tocElement = config.getAppBaseVarName() + ".$(\"div."+ tocId + "\")",
                    srcElement = tocElement + ".parents(\"div\")",
                    settings = "undefined";
            res = "<div class=\"jsh-md-toc-container " + tocId + "\" id=\"" + tocId + "\"></div>" +
                    "<script>" + config.getAppBaseVarName() + ".get(\"Renderer\").addTOCForBlock(" +
                    tocElement +", " + srcElement + ", " + settings + ");</script>";
        }
        nextTocId++;
        return res;
    };

    public String renderExtendedMarkdownSplitter(String type, String attr, String first, String second) {
        return "<label class=\"" + genStyleClassesForTag("splitter1") + "\">" + first + "</label>" +
                "<span class=\"" + genStyleClassesForTag("splitter2") + "\">" + second + "</span>";
    };

    public String renderExtendedMarkdownSplitter1(String type, String attr, String first) {
        return "<label class=\"" + genStyleClassesForTag("splitter1") + "\">" + first + "</label>";
    };

    public String renderExtendedMarkdownSplitter2(String type, String attr, String second) {
        return "<span class=\"" + genStyleClassesForTag("splitter2") + "\">" + second + "</span>";
    };
}

