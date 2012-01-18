/*
 * The MIT License
 *
 * Copyright (c) 2004-2011, Sun Microsystems, Inc., Kohsuke Kawaguchi, Frederik Fromm
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

import hudson.model.AbstractProject;
import hudson.model.Hudson;

import java.util.List;

/**
 * Provides static methods to initialize the list of projects once per view and not for every xpath config. 
 */
public class ProjectCollector {
    /**
     * list of all projects.
     */
    @SuppressWarnings("rawtypes")
    private static List< AbstractProject > projects;
    
    /**
     * Initialize the list of all projects.
     */
    public static void init() {
        Hudson instance = Hudson.getInstance();
        
        if(instance != null) {
           projects = Hudson.getInstance().getItems(AbstractProject.class);
        }
    }
    
    /**
     * Returns the list of all projects. Needs to be initialized before.
     * @return the list of all projects.
     */
    @SuppressWarnings("rawtypes")
    public static List< AbstractProject > getProjectsList() {
        return projects;
    }
    
    /**
     * Set the projects list for unit tests
     */
    public static void setProjectsListForTest(List< AbstractProject > testProjects) {
        projects = testProjects;
    }
}
