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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
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

  private void checkHasModifications(boolean expected, String line1, String line2) {
    assertEquals("Line <" + line1 + "> and <" + line2 + "> has modifications", expected, PublishUtility.hasModifications(line1, line2));
  }

  private static String readFile(String file) throws URISyntaxException, UnsupportedEncodingException, IOException {
    URL url = PublishUtilityTest.class.getResource(file);
    Path resPath = Paths.get(url.toURI());
    return new String(java.nio.file.Files.readAllBytes(resPath), "UTF8");
  }
}
