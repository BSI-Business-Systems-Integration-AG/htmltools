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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

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
  public void testIsExternalLink() throws Exception {
    assertEquals(true, PublishUtility.isExternalLink("http://eclipse.org/index.html"));
    assertEquals(true, PublishUtility.isExternalLink("http://eclipse.org/#test"));
    assertEquals(true, PublishUtility.isExternalLink("https://git.eclipse.org/"));
    assertEquals(true, PublishUtility.isExternalLink("ftp://me@site.com"));

    assertEquals(false, PublishUtility.isExternalLink("other-page.html"));
    assertEquals(false, PublishUtility.isExternalLink("../index.html"));
    assertEquals(false, PublishUtility.isExternalLink("folder/page.html"));

  }

  private static String readFile(String file) throws URISyntaxException, UnsupportedEncodingException, IOException {
    URL url = PublishUtilityTest.class.getResource(file);
    Path resPath = Paths.get(url.toURI());
    return new String(java.nio.file.Files.readAllBytes(resPath), "UTF8");
  }
}
