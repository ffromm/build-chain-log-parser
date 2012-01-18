package org.jenkinsci.plugins.buildchainlogparser;

import hudson.XmlFile;
import hudson.console.AnnotatedLargeText;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;
import org.powermock.api.mockito.PowerMockito;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: c031
 * Date: 17.01.12
 * Time: 14:36
 * To change this template use File | Settings | File Templates.
 */
public class BuildChainConsoleAnnotatorTest {

    private AbstractBuild build1;

    private AbstractProject project1;
    private AbstractProject project2;
    private AbstractProject project3;
    private AbstractProject project4;
    private AbstractProject project5;

    @Before
    public void setup() throws Throwable {
        File logFile = new File("src/test/resources/logs/log");
        final AnnotatedLargeText logText = new AnnotatedLargeText(logFile, Charset.defaultCharset(), false, null);

        build1 = PowerMockito.mock(AbstractBuild.class);
        PowerMockito.when(build1.getLogFile()).thenReturn(new File("src/test/resources/logs/log"));
        PowerMockito.when(build1.getNumber()).thenReturn(1);
        PowerMockito.when(build1.getLogText()).thenReturn(logText);

        project1 = PowerMockito.mock(AbstractProject.class);
        PowerMockito.when(project1.getName()).thenReturn("foo");
        PowerMockito.when(project1.getLastBuild()).thenReturn(build1);

        project2 = PowerMockito.mock(AbstractProject.class);
        PowerMockito.when(project2.getName()).thenReturn("bar");

        project3 = PowerMockito.mock(AbstractProject.class);
        PowerMockito.when(project3.getName()).thenReturn("baz");

        project4 = PowerMockito.mock(AbstractProject.class);
        PowerMockito.when(project4.getName()).thenReturn("m02");

        project5 = PowerMockito.mock(AbstractProject.class);
        PowerMockito.when(project5.getName()).thenReturn("job03");
    }

    @Test
    public void testCreateUsedJobNamesList() {
        assertTrue(project1.getLastBuild().getLogFile().exists());

        BuildChainConsoleAnnotator b = new BuildChainConsoleAnnotator();
        b = new BuildChainConsoleAnnotator();

        List< AbstractProject> projectsList = new ArrayList<AbstractProject>();

        projectsList.add(project1);
        projectsList.add(project2);
        projectsList.add(project3);
        projectsList.add(project4);
        projectsList.add(project5);

        ProjectCollector.setProjectsListForTest(projectsList);

        List<String> usedJobNamesList = b.getUsedJobNamesList(null);
        assertNotNull(usedJobNamesList);
        String expectedJobNames = "foo,bar,baz,m02,job03";
        assertEquals(expectedJobNames, StringUtils.join(usedJobNamesList, ","));

        b = new BuildChainConsoleAnnotator();
        usedJobNamesList = b.getUsedJobNamesList(build1);
        assertNotNull(usedJobNamesList);
        expectedJobNames = "m02,job03";
        assertEquals(expectedJobNames, StringUtils.join(usedJobNamesList, ","));
    }
    
    @Test
    public void testGetBuildNumberFromLine() {

        String line1 = "Gestartet durch vorgelagertes Projekt \"job01\", Build 4 4";
        String line2 = "Löse einen neuen Build von job03 #4 aus";
        String line3 = "Löse einen neuen Build von job03 aus";
        String line4 = "Finished: SUCCESS";

        BuildChainConsoleAnnotator b = new BuildChainConsoleAnnotator();
        assertEquals(4, b.getBuildNumberFromLine(line1, "job01"));
        assertEquals(4, b.getBuildNumberFromLine(line2, "job03"));
        assertEquals(-1, b.getBuildNumberFromLine(line3, "job03"));
        assertEquals(-1, b.getBuildNumberFromLine(line4, "xxx"));
    }

}
