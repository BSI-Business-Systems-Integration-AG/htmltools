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

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

/**
 * @author jbr
 */
public class IndexUtilityTest {

  @Test
  public void testToHtmlEmpty() throws Exception {
    File root = File.createTempFile("test", "dir");

    String actual = IndexUtility.toHtmlList(Collections.<DocEntry> emptyList(), root, "INDEX");
    StringBuilder sb = new StringBuilder();

    expectedStart(sb);
    expectedEnd(sb);
    assertEquals(sb.toString(), actual);
  }

  @Test
  public void testToHtmlSingleton() throws Exception {
    File root = File.createTempFile("test", "dir");
    DocEntry d1 = createDocEntry1(root);

    String actual = IndexUtility.toHtmlList(Collections.singletonList(d1), d1.getFolder(), "INDEX");
    StringBuilder sb = new StringBuilder();

    expectedStart(sb);
    sb.append("    <li>First Article (<a href=\"first/index.html\">html</a>, <a href=\"first.pdf\">pdf</a>, <a href=\"first.zip\">zip</a>)</li>\n");
    expectedEnd(sb);
    assertEquals(sb.toString(), actual);
  }

  @Test
  public void testToHtmlMultiple() throws Exception {
    File root = File.createTempFile("test", "dir");
    DocEntry d1 = createDocEntry1(root);
    DocEntry d2 = createDocEntry2(root);

    String actual = IndexUtility.toHtmlList(Arrays.asList(d1, d2), root, "INDEX");
    StringBuilder sb = new StringBuilder();

    expectedStart(sb);
    sb.append("    <li>First Article (<a href=\"first/first/index.html\">html</a>, <a href=\"first/first.pdf\">pdf</a>, <a href=\"first/first.zip\">zip</a>)</li>\n");
    sb.append("    <li>Second Article (<a href=\"second/second_article/second.html\">html</a>, <a href=\"second/second_article.pdf\">pdf</a>, <a href=\"second/second_article.zip\">zip</a>)</li>\n");
    expectedEnd(sb);
    assertEquals(sb.toString(), actual);
  }

  private DocEntry createDocEntry1(File root) {
    File folder = new File(root, "first");
    DocEntry d = new DocEntry();
    d.setName("First Article");
    d.setFolder(folder);
    d.setHtmlSubPath("first/index.html");
    d.setPdfSubPath("first.pdf");
    d.setZipSubPath("first.zip");
    return d;
  }

  private DocEntry createDocEntry2(File root) {
    File folder = new File(root, "second");
    DocEntry d = new DocEntry();
    d.setName("Second Article");
    d.setFolder(folder);
    d.setHtmlSubPath("second_article/second.html");
    d.setPdfSubPath("second_article.pdf");
    d.setZipSubPath("second_article.zip");
    return d;
  }

  private void expectedStart(StringBuilder sb) {
    sb.append("<html>\n");
    sb.append("<head>\n");
    sb.append("  <title>INDEX</title>\n");
    sb.append("</head>\n");
    sb.append("<body>\n");
    sb.append("  <h1>INDEX</h1>\n");
    sb.append("  <hr />\n");
    sb.append("  <ul>\n");
  }

  private void expectedEnd(StringBuilder sb) {
    sb.append("  </ul>\n");
    sb.append("  <hr />\n");
    sb.append("</body>\n");
    sb.append("</head>\n");
    sb.append("</html>\n");
  }

  @Test
  public void testComputeRelPath() throws Exception {
    File root = File.createTempFile("test", "dir");
    File folder = new File(root, "folder");

    assertEquals("folder/file.html", IndexUtility.computeRelPath(root, folder, "file.html"));
    assertEquals("file.pdf", IndexUtility.computeRelPath(root, root, "file.pdf"));
  }

}
