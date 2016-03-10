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

/**
 * configuration for markdown-rendering
 */
public class JshConfig {
    private String stylePrefix = "jsh-";

    private String appBaseVarName = "jshAppBase";

    public String getStylePrefix() {
        return stylePrefix;
    }

    public void setStylePrefix(String stylePrefix) {
        this.stylePrefix = stylePrefix;
    }

    public String getAppBaseVarName() {
        return appBaseVarName;
    }

    public void setAppBaseVarName(String appBaseVarName) {
        this.appBaseVarName = appBaseVarName;
    }

}
