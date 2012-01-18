/*
 * The MIT License
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., Kohsuke Kawaguchi,
 * Daniel Dyer, Red Hat, Inc., Tom Huybrechts, Romain Seguy, Yahoo! Inc.,
 * Darek Ostolski
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.buildchainlogparser;

import hudson.MarkupText;
import hudson.console.ConsoleAnnotator;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Build;
import hudson.util.IOUtils;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ConsoleAnnotator that parses each log row for job names, looks for a build number behind and
 * sets a link to the named build on the whole row.
 */
public class BuildChainConsoleAnnotator extends ConsoleAnnotator {

    /**
     * the logger
     */
    private static final Logger LOGGER = Logger.getLogger(Build.class.getName());

    /**
     * list of job names actually used in the log.
     */
    private List<String> usedJobNames;

    /**
     * Returns this annotator for further processing.
     * <p/>
     * Iterates over the used job names and a build number behind, if found. The whole line is tranformed into
     * a link to the build found.
     *
     * @param o          the current build the log belongs to
     * @param markupText the log text line
     * @return this annotator
     */
    @Override
    public ConsoleAnnotator annotate(Object o, MarkupText markupText) {

        for (String usedJobName : this.getUsedJobNamesList((AbstractBuild) o)) {
            String line = markupText.getText();
            if (line.contains(usedJobName)) {
                final int buildNumberFromLine = this.getBuildNumberFromLine(line, usedJobName);
                if (buildNumberFromLine >= 0) {
                    String url = "/job/" + usedJobName + "/" + this.getBuildNumberFromLine(markupText.getText(), usedJobName) + "/console";
                    markupText.hide(0, markupText.length() - 2);
                    markupText.addMarkup(markupText.length() - 2, "<a href=\"" + url + "\">" + line + "</a>");
                }
            }
        }

        return this;
    }

    /**
     * Returns the list of used jobs that occur in the build's log.
     *
     * @param build the build
     * @return the list of used jobs
     */
    public List<String> getUsedJobNamesList(AbstractBuild build) {
        if (this.usedJobNames == null) {
            this.usedJobNames = new ArrayList<String>();
            String logContent = null;

            if (build != null) {
                final StringWriter writer = new StringWriter();

                try {
                    final Reader reader = build.getLogText().readAll();

                    IOUtils.copy(reader, writer);
                    logContent = writer.toString();
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Exception getting log content.", e);
                } finally {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "Exception closing log content writer.", e);
                    }
                }
            }

            for (AbstractProject abstractProject : ProjectCollector.getProjectsList()) {
                if (logContent == null || logContent.contains(abstractProject.getName())) {
                    this.usedJobNames.add(abstractProject.getName());
                }
            }
        }

        return this.usedJobNames;
    }

    /**
     * Returns the build number that appears after the given used job name
     *
     * @param line        the log text line
     * @param usedJobName the used job name.
     * @return the build number
     */
    public int getBuildNumberFromLine(String line, String usedJobName) {
        // return -1 if usedJobName is not in line. This should not happen!
        if (line.contains(usedJobName)) {
            // get line part behind usedJobName
            String part = line.split(usedJobName)[1];

            // find first number in String
            Pattern pattern = Pattern.compile(".*(\\d).*");
            final Matcher matcher = pattern.matcher(part);
            if (matcher.find()) {
                try {
                    return Integer.parseInt(matcher.group(1));
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.SEVERE, "Exception parsing build number from " + matcher.group(1), e);
                }
            }
        }
        return -1;
    }
}
