/*******************************************************************************
 * Copyright (c) 2016 Jeremie Bresson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jeremie Bresson - initial API and implementation
 ******************************************************************************/
package com.bsiag.htmltools.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

public class PublishUtilityTest {

  @Test
  public void testListingLink() throws Exception {
    String html = readFile("/listing-input.html");
    String expected = readFile("/listing-expected.html");

    Document doc = Jsoup.parseBodyFragment(html);
    doc.outputSettings().prettyPrint(false);

    PublishUtility.fixListingLink(doc);

    String result = doc.body().html();
    assertEquals(expected, result);
  }

  @Test
  public void testFigureLink() throws Exception {
    String html = readFile("/figure-input.html");
    String expected = readFile("/figure-expected.html");

    Document doc = Jsoup.parseBodyFragment(html);
    doc.outputSettings().prettyPrint(false);

    PublishUtility.fixFigureLink(doc);

    String result = doc.body().html();
    assertEquals(expected, result);
  }

  @Test
  public void testTableLink() throws Exception {
    String html = readFile("/table-input.html");
    String expected = readFile("/table-expected.html");

    Document doc = Jsoup.parseBodyFragment(html);
    doc.outputSettings().prettyPrint(false);

    PublishUtility.fixTableLink(doc);

    String result = doc.body().html();
    assertEquals(expected, result);
  }

  @Test
  public void testFixExternalLink() throws Exception {
    String html = "<p>The <a href=\"http://example.com\">Link 1</a>, <a href=\"https://example.com\">Link 2</a> and <a href=\"ftp://example.com\">Link 3</a> are important. <a href=\"page.html\">Link</a> is not</p>.";
    String expected = "<p>The <a href=\"http://example.com\" target=\"_blank\">Link 1</a>, <a href=\"https://example.com\" target=\"_blank\">Link 2</a> and <a href=\"ftp://example.com\" target=\"_blank\">Link 3</a> are important. <a href=\"page.html\">Link</a> is not</p>.";

    Document doc = Jsoup.parseBodyFragment(html);
    doc.outputSettings().prettyPrint(false);

    PublishUtility.fixExternalLinks(doc);

    String result = doc.body().html();
    assertEquals(expected, result);
  }

  @Test
  public void testTrimTrailingWhitespaces() throws Exception {
    String content = "<p>Lorem<p>   \n" //
        + "<div> \n" //
        + "    <p>Ipsum<p>\t\n" //
        + "    <p>Dolor<p>\n" //
        + "</div>    \n";
    String expected = "<p>Lorem<p>\n" //
        + "<div>\n" //
        + "    <p>Ipsum<p>\n" //
        + "    <p>Dolor<p>\n" //
        + "</div>";
    String result = PublishUtility.trimTrailingWhitespaces(content);
    assertEquals(expected, result);
  }

  private static final List<String> LINES1 = Arrays.asList("Lorem", "Ipsum", "Dolor");
  private static final List<String> LINES2 = Arrays.asList("Lorem", "Ipsum", "Dolor");
  private static final List<String> LINES3 = Arrays.asList("Lorem", "Ipsum");
  private static final List<String> LINES4 = Arrays.asList("Lorem", "XXX");

  @Test
  public void testHasModificationsList() throws Exception {
    assertFalse(PublishUtility.hasModifications(LINES1, LINES2));

    assertTrue(PublishUtility.hasModifications(LINES1, LINES4));
    assertTrue(PublishUtility.hasModifications(LINES2, LINES4));
    assertTrue(PublishUtility.hasModifications(LINES3, LINES4));
  }

  @Test
  public void testHasModifications() throws Exception {
    checkHasModifications(false, "Lorem", "Lorem");
    checkHasModifications(false, "", "");
    checkHasModifications(false, "<br /> Last updated 2016-03-02 02:26:06 CET", "<br /> Last updated 2016-03-01 06:39:16 CET");

    checkHasModifications(true, "Lorem", "Ipsum");
    checkHasModifications(true, "Lorem", "<br /> Last updated 2016-03-01 06:39:16 CET");
    checkHasModifications(true, "<br /> Last updated 2016-03-02 02:26:06 CET", "Ipsum");
  }

  @Test
  public void testComputeActions01_01() throws Exception {
    File input = computeFile("/compute_actions/input_01/");
    File output = computeFile("/compute_actions/output_01/");

    List<FileAction> actions = PublishUtility.computeActions(input, output, true);
    assertEquals("actions size", 3, actions.size());
    assertEquals("actions contains a copy task for figure.html", true, actions.contains(createCopyAction(input, output, "figure.html")));
    assertEquals("actions contains a copy task for listing.html", true, actions.contains(createCopyAction(input, output, "listing.html")));
    assertEquals("actions contains a copy task for table.html", true, actions.contains(createCopyAction(input, output, "table.html")));
  }

  @Test
  public void testComputeActions01_02() throws Exception {
    File input = computeFile("/compute_actions/input_01/");
    File output = computeFile("/compute_actions/output_02/");

    List<FileAction> actions = PublishUtility.computeActions(input, output, true);
    assertEquals("actions size", 2, actions.size());
    assertEquals("actions contains a copy task for figure.html", false, actions.contains(createCopyAction(input, output, "figure.html")));
    assertEquals("actions contains a copy task for listing.html", true, actions.contains(createCopyAction(input, output, "listing.html")));
    assertEquals("actions contains a copy task for table.html", false, actions.contains(createCopyAction(input, output, "table.html")));
    assertEquals("actions contains a delete task for xxx.html", true, actions.contains(createRemoveAction(output, "xxx.html")));
  }

  @Test
  public void testComputeActions02_01() throws Exception {
    File input = computeFile("/compute_actions/input_02/");
    File output = computeFile("/compute_actions/output_01/");

    List<FileAction> actions = PublishUtility.computeActions(input, output, true);
    assertEquals("actions size", 2, actions.size());
    assertEquals("actions contains a copy task for file.html", true, actions.contains(createCopyAction(input, output, "file.html")));
    assertEquals("actions contains a copy task for file2.html", true, actions.contains(createCopyAction(new File(input, "sub"), new File(output, "sub"), "file2.html")));
  }

  @Test
  public void testComputeActions01_03() throws Exception {
    File input = computeFile("/compute_actions/input_01/");
    File output = computeFile("/compute_actions/output_03/");

    List<FileAction> actions;
    actions = PublishUtility.computeActions(input, output, true);
    assertEquals("actions size", 4, actions.size());
    assertEquals("actions contains a copy task for figure.html", true, actions.contains(createCopyAction(input, output, "figure.html")));
    assertEquals("actions contains a copy task for listing.html", true, actions.contains(createCopyAction(input, output, "listing.html")));
    assertEquals("actions contains a copy task for table.html", true, actions.contains(createCopyAction(input, output, "table.html")));
    assertEquals("actions contains a delete task for sub/", true, actions.contains(FileAction.remove(new File(output, "sub"))));

    actions = PublishUtility.computeActions(input, output, false);
    assertEquals("actions size", 3, actions.size());
    assertEquals("actions contains a copy task for figure.html", true, actions.contains(createCopyAction(input, output, "figure.html")));
    assertEquals("actions contains a copy task for listing.html", true, actions.contains(createCopyAction(input, output, "listing.html")));
    assertEquals("actions contains a copy task for table.html", true, actions.contains(createCopyAction(input, output, "table.html")));
    assertEquals("actions contains a delete task for sub/", false, actions.contains(FileAction.remove(new File(output, "sub"))));
  }

  private FileAction createCopyAction(File inputFolder, File outputFolder, String filename) {
    return FileAction.copy(new File(inputFolder, filename), new File(outputFolder, filename));
  }

  private FileAction createRemoveAction(File folder, String filename) {
    return FileAction.remove(new File(folder, filename));
  }

  @Test
  public void testComputeParentFoldersWithCopy() throws Exception {
    File input = computeFile("/compute_actions/input_01/");
    File output = computeFile("/compute_actions/output_02/");

    List<FileAction> actions = new ArrayList<>();
    actions.add(createCopyAction(input, output, "figure.html"));
    actions.add(createCopyAction(input, output, "listing.html"));
    actions.add(createCopyAction(input, output, "table.html"));

    Collection<FileAction> result = PublishUtility.computeParentFolders(actions);
    assertEquals("result size", 1, result.size());
    assertEquals("result", Collections.singleton(FileAction.copy(input, output)), result);
  }

  @Test
  public void testComputeParentFoldersWithDelete() throws Exception {
    File output = computeFile("/compute_actions/output_02/");

    List<FileAction> actions = new ArrayList<>();
    actions.add(createRemoveAction(output, "listing.html"));
    actions.add(createRemoveAction(output, "xxx.html"));

    Collection<FileAction> result = PublishUtility.computeParentFolders(actions);
    assertEquals("result size", 1, result.size());
    assertEquals("result", Collections.singleton(FileAction.remove(output)), result);
  }

  @Test
  public void testComputeParentFoldersWithCopyAndDelete() throws Exception {
    File input = computeFile("/compute_actions/input_01/");
    File output = computeFile("/compute_actions/output_02/");

    List<FileAction> actions = new ArrayList<>();
    actions.add(createCopyAction(input, output, "listing.html"));
    actions.add(createRemoveAction(output, "xxx.html"));

    Collection<FileAction> result = PublishUtility.computeParentFolders(actions);
    assertEquals("result size", 1, result.size());
    assertEquals("result", Collections.singleton(FileAction.copy(input, output)), result);
  }

  private File computeFile(String name) throws URISyntaxException {
    URL url = PublishUtilityTest.class.getResource(name);
    return Paths.get(url.toURI()).toFile();
  }

  private void checkHasModifications(boolean expected, String line1, String line2) {
    assertEquals("Line <" + line1 + "> and <" + line2 + "> has modifications", expected, PublishUtility.hasModifications(line1, line2));
  }

  private static String readFile(String file) throws URISyntaxException, UnsupportedEncodingException, IOException {
    URL url = PublishUtilityTest.class.getResource(file);
    Path resPath = Paths.get(url.toURI());
    return new String(java.nio.file.Files.readAllBytes(resPath), "UTF8");
  }
}
