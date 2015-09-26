/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package com.bsiag.geneclipsetoc.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.Resources;

public class PublishUtility {
  private static final String CSS_SUB_PATH = "css/";
  private static final String IMAGES_SUB_PATH = "images/";

  private static final String IMAGE_HOME = "home.gif";
  private static final String IMAGE_NEXT = "next.gif";
  private static final String IMAGE_PREV = "prev.gif";

  public static void publishHtmlFiles(File inFolder, List<File> inFiles, File outFolder, Map<String, File> cssReplacement) throws IOException {
    if (!inFolder.exists() || !inFolder.isDirectory()) {
      throw new IllegalStateException("Folder inFolder '" + inFolder.getAbsolutePath() + "' not found.");
    }

    List<File> files;
    RootItem root;
    if (inFiles != null) {
      if (inFiles.size() > 1) {
        copyNavImg(outFolder);

        String title = readAndFindFirstHeader(inFiles.get(0));
        root = new RootItem(title, inFiles.get(0).getName());
      }
      else {
        root = null;
      }
      files = inFiles;
    }
    else {
      File[] childFiles = inFolder.listFiles(new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          return name.endsWith("html");
        }
      });
      files = Arrays.asList(childFiles);
      root = null;
    }

    for (File file : files) {
      publishHtmlFile(inFolder, file, outFolder, cssReplacement, inFiles, root);
    }
  }

  /**
   * Take a single HTML file and publish it to the outFolder.
   * Images and CSS resources are moved, HTML is formatted.
   *
   * @param inFolder
   *          root folder where the input HTML file is located.
   * @param outFolder
   *          directory where the post-processed HTML file is saved.
   * @param cssReplacement
   * @param pageList
   * @param root
   * @throws IOException
   */
  private static void publishHtmlFile(File inFolder, File inFile, File outFolder, Map<String, File> cssReplacement, List<File> pageList, RootItem root) throws IOException {
    File outFile = new File(outFolder, inFile.getName());
    String html = Files.toString(inFile, Charsets.UTF_8);

    Document doc = Jsoup.parse(html);
    doc.outputSettings().charset("ASCII");

    fixNavigation(doc, inFile, pageList, root);

    fixListingLink(doc);
    fixFigureLink(doc);

    Files.createParentDirs(outFile);
    moveAndCopyImages(doc, inFolder, outFolder, IMAGES_SUB_PATH);

    moveAndCopyCss(doc, inFolder, outFolder, CSS_SUB_PATH, cssReplacement);

    Files.write(doc.toString(), outFile, Charsets.UTF_8);
  }

  public static void publishPdfFiles(File inFolder, File outFolder) throws IOException {
    if (!inFolder.exists() || !inFolder.isDirectory()) {
      throw new IllegalStateException("Folder inFolder '" + inFolder.getAbsolutePath() + "' not found.");
    }

    File[] files = inFolder.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.endsWith("pdf");
      }
    });

    if (files.length == 0) {
      new IllegalStateException("No HTML files found in: " + inFolder.getAbsolutePath());
    }

    for (File file : files) {
      publishPdfFile(file, outFolder);
    }
  }

  private static void copyNavImg(File htmlFolder) throws IOException {
    File toFolder = new File(htmlFolder, IMAGES_SUB_PATH);
    toFolder.mkdirs();

    Resources.copy(Resources.getResource("nav_images/" + IMAGE_HOME), new FileOutputStream(new File(toFolder, IMAGE_HOME)));
    Resources.copy(Resources.getResource("nav_images/" + IMAGE_NEXT), new FileOutputStream(new File(toFolder, IMAGE_NEXT)));
    Resources.copy(Resources.getResource("nav_images/" + IMAGE_PREV), new FileOutputStream(new File(toFolder, IMAGE_PREV)));
  }

  private static void publishPdfFile(File inFile, File outFolder) throws IOException {
    File outFile = new File(outFolder, inFile.getName());

    Files.createParentDirs(outFile);
    Files.copy(inFile, outFile);
  }

  /**
   * Copy images to the outFolder
   *
   * @param doc
   *          the html content as JSoup document
   * @param inFolder
   * @param outFolder
   * @param imgSubPath
   *          image path relative to the outFolder
   * @throws IOException
   */
  public static void moveAndCopyImages(Document doc, File inFolder, File outFolder, String imgSubPath) throws IOException {
    if (imgSubPath == null) {
      throw new IllegalArgumentException("imgSubPath can not be null, use empty string if you do not want to modify the relative path of the image file");
    }
    else if (imgSubPath.length() > 0 && !imgSubPath.endsWith("/")) {
      throw new IllegalArgumentException("imgSubPath point to an other subdirectory. It should ends with '/'");
    }

    Elements elements = doc.getElementsByTag("img");
    for (Element element : elements) {
      String src = element.attr("src");
      if (src != null) {
        //consider that the src attribute is relative to the inFolder:
        File inFile = new File(inFolder, src);
        //if no file exists at this location, consider that the src attribute contains an absolute path to the image:
        if (!inFile.exists() || !inFile.isFile()) {
          inFile = new File(src);
        }
        String newSrc = imgSubPath + inFile.getName();
        element.attr("src", newSrc);
        File outFile = new File(outFolder, newSrc);
        if (inFile.exists() && inFile.isFile()) {
          Files.createParentDirs(outFile);
          Files.copy(inFile, outFile);
        }
        else if (outFile.exists() && outFile.isFile()) {
          //the in file was not found as inFile and it was not copied, but the outFile already exists. Nothing to do, no warning.
        }
        else {
          System.err.println("Image file '" + inFile.getAbsolutePath() + "' is missing");
        }
      }
    }
  }

  /**
   * Copy the CSS files to the outFolder
   *
   * @param doc
   *          the html content as JSoup document
   * @param inFolder
   * @param outFolder
   * @param cssSubPath
   * @param cssReplacement
   * @throws IOException
   */
  public static void moveAndCopyCss(Document doc, File inFolder, File outFolder, String cssSubPath, Map<String, File> cssReplacement) throws IOException {
    if (cssSubPath == null) {
      throw new IllegalArgumentException("cssSubPath can not be null, use empty string if you do not want to change the relative path of the css file");
    }
    else if (cssSubPath.length() > 0 && !cssSubPath.endsWith("/")) {
      throw new IllegalArgumentException("cssSubPath point to an other subdirectory. It should ends with '/'");
    }

    Elements elements = doc.getElementsByTag("link");
    for (Element element : elements) {
      String rel = element.attr("rel");
      if ("stylesheet".equals(rel)) {
        String href = element.attr("href");
        if (href != null && !href.startsWith("http")) {
          File inFile = new File(inFolder, href);
          inFile = replaceCssFile(inFile, cssReplacement);
          String newHref = cssSubPath + inFile.getName();
          element.attr("href", newHref);
          File outFile = new File(outFolder, newHref);
          if (inFile.exists() && inFile.isFile()) {
            Files.createParentDirs(outFile);
            Files.copy(inFile, outFile);
          }
          else if (outFile.exists() && outFile.isFile()) {
            //the in file was not found as inFile and it was not copied, but the outFile already exists. Nothing to do, no warning.
          }
          else {
            System.err.println("CSS file '" + inFile.getAbsolutePath() + "' is missing");
          }
        }
      }
    }
  }

  /**
   * @param inFile
   * @return
   */
  private static File replaceCssFile(File inFile, Map<String, File> cssReplacement) {
    String key = inFile.getName();
    if (cssReplacement.containsKey(key)) {
      return cssReplacement.get(key);
    }
    return inFile;
  }

  static private Pattern listingPattern = Pattern.compile("(Listing [0-9]+)\\.");

  /**
   * #858 workaround for listing
   * https://github.com/asciidoctor/asciidoctor/issues/858
   *
   * @param doc
   *          JSoup document (type is {@link org.jsoup.nodes.Document})
   */
  public static void fixListingLink(Document doc) {
    Elements elements = doc.getElementsByTag("a");
    for (Element link : elements) {
      String href = link.attr("href");
      if (href != null && href.startsWith("#")) {
        String id = href.substring(1);
        Element idElement = doc.getElementById(id);
        if (idElement != null && "listingblock".equals(idElement.attr("class"))) {
          fixLink(link, idElement, listingPattern);
        }
      }
    }
  }

  static private Pattern figurePattern = Pattern.compile("(Figure [0-9]+)\\.");

  /**
   * #858 workaround for figure
   * https://github.com/asciidoctor/asciidoctor/issues/858
   *
   * @param doc
   *          JSoup document (type is {@link org.jsoup.nodes.Document})
   */
  public static void fixFigureLink(Document doc) {
    Elements elements = doc.getElementsByTag("a");
    for (Element link : elements) {
      String href = link.attr("href");
      if (href != null && href.startsWith("#")) {
        String id = href.substring(1);
        Element idElement = doc.getElementById(id);
        boolean fixedText = false;
        if (idElement != null && "imageblock".equals(idElement.attr("class"))) {
          fixedText = fixLink(link, idElement, figurePattern);
        }
        //Support for the multiple Images in one figure workaround (see Issue 1287)
        if (!fixedText && idElement != null) {
          Element container = idElement.parent();
          boolean checkNext = false;
          for (int i = 0; i < container.childNodeSize(); i++) {
            Node childNode = container.childNode(i);
            if (id.equals(childNode.attr("id")) && "imageblock".equals(childNode.attr("class"))) {
              checkNext = true;
            }
            if (!fixedText && checkNext) {
              if ("imageblock".equals(childNode.attr("class")) && childNode instanceof Element) {
                fixedText = fixLink(link, (Element) childNode, figurePattern);
              }
              else if (!(childNode instanceof Element)) {
                //do nothing.
              }
              else {
                //found an other element that does not correspond.
                checkNext = false;
              }
            }
          }
        }
      }
    }
  }

  private static boolean fixLink(Element link, Element element, Pattern pattern) {
    Element titleDiv = findTitleDiv(element);
    if (titleDiv != null) {
      Matcher matcher = pattern.matcher(titleDiv.text());
      if (matcher.find()) {
        link.text(matcher.group(1));
        return true;
      }
    }
    return false;
  }

  private static Element findTitleDiv(Element element) {
    Elements elements = element.getElementsByTag("div");
    for (Element e : elements) {
      if ("title".equals(e.attr("class"))) {
        return e;
      }
    }
    return null;
  }

  /**
   * @param doc
   * @param inFile
   * @param pages
   * @param root
   * @throws IOException
   */
  private static void fixNavigation(Document doc, File inFile, List<File> pages, RootItem root) throws IOException {
    if (pages != null) {
      //Create the navigation section:
      String nextHref = null;
      String nextTitle = null;
      String prevHref = null;
      String prevTitle = null;

      int i = pages.indexOf(inFile);

      if (i < pages.size() - 1) {
        nextHref = pages.get(i + 1).getName();
        nextTitle = readAndFindFirstHeader(pages.get(i + 1));
      }
      if (i > 0 && pages.size() > 0) {
        prevHref = pages.get(i - 1).getName();
        prevTitle = readAndFindFirstHeader(pages.get(i - 1));
      }
      String baseUri = doc.baseUri();

      String title = findFirstHeader(doc);
      Element tableTop = createNavigationTable(root, title, true, nextHref, prevHref, nextTitle, prevTitle, baseUri);
      doc.body().insertChildren(0, Collections.singleton(tableTop));
      Element tableBottom = createNavigationTable(root, title, false, nextHref, prevHref, nextTitle, prevTitle, baseUri);
      insertBeforeId(doc.body(), "footer", tableBottom);
    }
  }

  /**
   * Read the file and apply {@link #findFirstHeader(Document)}
   *
   * @param file
   * @return title or null if not found
   * @throws IOException
   */
  private static String readAndFindFirstHeader(File file) throws IOException {
    String html = Files.toString(file, Charsets.ISO_8859_1);
    Document doc = Jsoup.parse(html);
    return findFirstHeader(doc);
  }

  /**
   * Find the first header (h1, h2, h3, h4, h5 or h6) and returns the text content .
   *
   * @param doc
   *          the html content as JSoup document
   * @return title or null if not found
   */
  private static String findFirstHeader(Document doc) {
    Elements elements = doc.getAllElements();
    for (Element element : elements) {
      if (isHeaderTag(element)) {
        return sanitize(element.text());
      }
    }
    return null;
  }

  private static boolean isHeaderTag(Element element) {
    return element.nodeName().matches("h[1-6]");
  }

  private static String sanitize(String text) {
    String result = text;
    result = result.replaceAll("”", "\"");
    return result;
  }

  private static void insertBeforeId(Element container, String id, Element element) {
    for (int i = 0; i < container.childNodeSize(); i++) {
      Node childNode = container.childNode(i);
      if (id.equals(childNode.attr("id"))) {
        container.insertChildren(i, Collections.singleton(element));
        return;
      }
    }
    throw new IllegalStateException("ChildNode with id '" + id + "' not found");
  }

  private static Element createNavigationTable(RootItem root, String title, boolean isTop, String nextHref, String prevHref, String nextTitle, String prevTitle, String baseUri) {
    Element table = new Element(Tag.valueOf("table"), baseUri);
    table.attr("border", "0");
    table.attr("class", "navigation");
    table.attr("style", "width: 100%;");
    table.attr("summary", "navigation");

    Element prevLinkElement = createLinkElement(prevTitle, prevHref, "Previous", IMAGE_PREV, baseUri);
    Element nextLinkElement = createLinkElement(nextTitle, nextHref, "Next", IMAGE_NEXT, baseUri);

    Element homeLinkElement;
    if (isTop) {
      homeLinkElement = null;
    }
    else {
      homeLinkElement = createLinkElement(root.getTitle(), root.getFileName(), root.getTitle(), IMAGE_HOME, table.baseUri());
    }
    appendNavigationTableTR(table, prevLinkElement, homeLinkElement, nextLinkElement);
    appendNavigationTableTR(table, prevTitle, null, nextTitle);
    return table;
  }

  /**
   * @param title
   * @param href
   * @param imgAlt
   * @param imgSrc
   * @param baseUri
   * @return
   */
  private static Element createLinkElement(String title, String href, String imgAlt, String imgSrc, String baseUri) {
    if (title == null || href == null) {
      return null;
    }
    Element a = new Element(Tag.valueOf("a"), baseUri);
    a.attr("href", href);
    a.attr("shape", "rect");
    a.attr("title", title);
    Element img = new Element(Tag.valueOf("img"), baseUri);
    img.attr("alt", imgAlt);
    img.attr("border", "0");
    img.attr("src", imgSrc);
    a.appendChild(img);
    return a;
  }

  private static void appendNavigationTableTR(Element table, Object leftContent, Object midContent, Object rigthContent) {
    Element tr = new Element(Tag.valueOf("tr"), table.baseUri());
    table.appendChild(tr);
    appendNavigationTableTD(tr, "left", "width: 30%", leftContent);
    appendNavigationTableTD(tr, "center", "width: 40%", midContent);
    appendNavigationTableTD(tr, "right", "width: 30%", rigthContent);
  }

  private static void appendNavigationTableTD(Element tr, String align, String style, Object content) {
    Element td = new Element(Tag.valueOf("td"), tr.baseUri());
    td.attr("align", align);
    td.attr("colspan", "1");
    td.attr("rowspan", "1");
    td.attr("style", style);
    tr.appendChild(td);
    if (content == null) {
      //Do nothing
    }
    else if (content instanceof String) {
      td.text((String) content);
    }
    else if (content instanceof Element) {
      td.appendChild((Node) content);
    }
    else {
      System.err.println("Unexpected content type: " + content);
    }
  }
}