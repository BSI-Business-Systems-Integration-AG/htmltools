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

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public final class IndexUtility {

  private static final String INDEX_HTML = "index.html";
  private static final FileFilter DIRECTORY_FILEFILTER = new FileFilter() {
    @Override
    public boolean accept(File f) {
      return f.isDirectory();
    }
  };

  public static void createIndexList(List<DocEntry> entries, File folder, String title) throws IOException {
    File indexFile = new File(folder, INDEX_HTML);
    String html = IndexUtility.toHtmlList(entries, folder, title);
    Files.write(html, indexFile, Charsets.UTF_8);
  }

  public static void createIndexSingle(DocEntry entry, File folder, String title) throws IOException {
    File indexFile = new File(folder, INDEX_HTML);
    String html = IndexUtility.toHtmlSingle(entry, folder, title);
    Files.write(html, indexFile, Charsets.UTF_8);
  }

  public static final List<DocEntry> computeEntries(File folder) throws IOException {
    List<DocEntry> result = new ArrayList<>();
    File[] folders = folder.listFiles(DIRECTORY_FILEFILTER);
    if (folders != null) {
      for (File f : folders) {
        DocEntry entry = computeEntry(f);
        if (entry.getHtmlSubPath() != null || entry.getPdfSubPath() != null || entry.getZipSubPath() != null) {
          result.add(entry);
        }
        else {
          result.addAll(computeEntries(f));
        }
      }
    }
    return result;
  }

  private static DocEntry computeEntry(File folder) throws IOException {
    DocEntry result = new DocEntry();
    result.setFolder(folder);

    File subFolder = findSingleFolder(folder);
    if (subFolder != null) {
      File htmlFile = findFile("html", subFolder);
      if (htmlFile != null) {
        result.setHtmlSubPath(subFolder.getName() + "/" + htmlFile.getName());
        result.setName(PublishUtility.readAndFindFirstHeader(htmlFile));
      }
      else {
        result.setHtmlSubPath(null);
        result.setName(folder.getName());
      }
    }
    else {
      result.setHtmlSubPath(null);
      result.setName(folder.getName());
    }
    result.setPdfSubPath(name(findFile("pdf", folder)));
    result.setZipSubPath(name(findFile("zip", folder)));
    return result;
  }

  private static File findSingleFolder(File folder) {
    File[] files = folder.listFiles(DIRECTORY_FILEFILTER);
    if (files.length == 1) {
      return files[0];
    }
    return null;
  }

  private static File findFile(final String suffix, File folder) {
    File[] files = folder.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.endsWith(suffix) && !INDEX_HTML.equals(name);
      }
    });
    if (files.length == 1) {
      return files[0];
    }
    else {
      return null;
    }
  }

  private static String name(File file) {
    if (file != null) {
      return file.getName();
    }
    else {
      return null;
    }
  }

  public static final String toHtmlList(List<DocEntry> entries, File outputFolder, String title) {
    StringBuilder sb = new StringBuilder();
    appendStartPage(title, sb);
    for (DocEntry e : entries) {
      sb.append("    <li>");
      sb.append(e.getName());
      sb.append(" (");
      boolean isFirst = true;
      isFirst = createLink(sb, isFirst, outputFolder, e.getFolder(), e.getHtmlSubPath(), "html");
      isFirst = createLink(sb, isFirst, outputFolder, e.getFolder(), e.getPdfSubPath(), "pdf");
      isFirst = createLink(sb, isFirst, outputFolder, e.getFolder(), e.getZipSubPath(), "zip");
      sb.append(")");
      sb.append("</li>\n");
    }
    appendEndPage(sb);

    return sb.toString();
  }

  public static final String toHtmlSingle(DocEntry entry, File outputFolder, String title) {
    StringBuilder sb = new StringBuilder();
    appendStartPage(title, sb);
    createLinkSingle(sb, outputFolder, entry, entry.getHtmlSubPath(), "View as html");
    createLinkSingle(sb, outputFolder, entry, entry.getPdfSubPath(), "View as pdf");
    createLinkSingle(sb, outputFolder, entry, entry.getZipSubPath(), "Download zipped html");
    appendEndPage(sb);

    return sb.toString();
  }

  private static void createLinkSingle(StringBuilder sb, File outputFolder, DocEntry entry, String subPath, String linkTitle) {
    if (subPath != null && !subPath.isEmpty()) {
      sb.append("    <li>");
      createLink(sb, true, outputFolder, entry.getFolder(), subPath, linkTitle);
      sb.append("</li>\n");
    }
  }

  private static boolean createLink(StringBuilder sb, boolean isFirst, File outputFolder, File linkFolder, String linkSubPath, String name) {
    if (linkSubPath == null || linkSubPath.isEmpty()) {
      return isFirst;
    }
    else {
      if (!isFirst) {
        sb.append(", ");
      }
      sb.append("<a href=\"");
      sb.append(computeRelPath(outputFolder, linkFolder, linkSubPath));
      sb.append("\">");
      sb.append(name);
      sb.append("</a>");
      return false;
    }
  }

  private static void appendStartPage(String title, StringBuilder sb) {
    sb.append("<html>\n");
    sb.append("<head>\n");
    sb.append("  <title>");
    sb.append(title);
    sb.append("</title>\n");
    sb.append("</head>\n");

    sb.append("<body>\n");
    sb.append("  <h1>");
    sb.append(title);
    sb.append("</h1>\n");
    sb.append("  <hr />\n");

    sb.append("  <ul>\n");
  }

  private static void appendEndPage(StringBuilder sb) {
    sb.append("  </ul>\n");

    sb.append("  <hr />\n");
    sb.append("</body>\n");
    sb.append("</head>\n");
    sb.append("</html>\n");
  }

  static String computeRelPath(File outputFile, File linkFolder, String href) {
    if (outputFile == null || linkFolder == null) {
      return "";
    }
    File linkFile = new File(linkFolder, href);

    Path pathAbsolute = linkFile.toPath();
    Path pathBase = outputFile.toPath();
    Path pathRelative = pathBase.relativize(pathAbsolute);
    String result = pathRelative.toString();
    if (result.isEmpty()) {
      return "";
    }
    return result.replaceAll("\\\\", "/");
  }

  private IndexUtility() {
  }
}
