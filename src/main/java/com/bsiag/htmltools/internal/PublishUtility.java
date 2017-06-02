/*******************************************************************************
 * Copyright (c) 2015 Jeremie Bresson.
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
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
  private static final FilenameFilter HTML_FILENAME_FILTER = new FilenameFilter() {

    @Override
    public boolean accept(File dir, String name) {
      return name.endsWith("html") && !name.equals("index.html");
    }
  };
  private static final FileFilter DIRECTORY_FILEFILTER = new FileFilter() {

    @Override
    public boolean accept(File file) {
      return file.isDirectory();
    }
  };

  public static void publishHtmlFiles(ParamPublishHtmlFiles param) throws IOException {
    final File inFolder = param.getInFolder();
    final List<File> inFiles = param.getInFiles();
    final File outFolder = param.getOutFolder();
    final Map<String, File> cssReplacement = param.getCssReplacement();
    final boolean fixXrefLinks = param.isFixXrefLinks();
    final boolean fixExternalLinks = param.isFixExternalLinks();

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
      publishHtmlFile(inFolder, file, outFolder, cssReplacement, inFiles, root, fixXrefLinks, fixExternalLinks);
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
   * @param fixXrefLinks
   *          tells if the cross references links should be fixed as described here
   *          https://github.com/asciidoctor/asciidoctor/issues/858
   * @param fixExternalLinks
   *          add taget="_blank" on links starting with http(s):// or ftp://
   * @throws IOException
   */
  private static void publishHtmlFile(File inFolder, File inFile, File outFolder, Map<String, File> cssReplacement, List<File> pageList, RootItem root, boolean fixXrefLinks, boolean fixExternalLinks) throws IOException {
    File outFile = new File(outFolder, inFile.getName());
    String html = Files.toString(inFile, Charsets.UTF_8);

    Document doc = Jsoup.parse(html);
    doc.outputSettings().charset("ASCII");

    fixNavigation(doc, inFile, pageList, root);

    if (fixXrefLinks) {
      fixListingLink(doc);
      fixFigureLink(doc);
      fixTableLink(doc);
    }

    if (fixExternalLinks) {
      fixExternalLinks(doc);
    }

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
    fixBlockLink(doc, "listingblock", listingPattern);
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
            if (id.equals(childNode.attr("id")) && classAttributeContains(childNode, "imageblock")) {
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

  static private Pattern tablePattern = Pattern.compile("(Table [0-9]+)\\.");

  /**
   * #858 workaround for table
   * https://github.com/asciidoctor/asciidoctor/issues/858
   *
   * @param doc
   *          JSoup document (type is {@link org.jsoup.nodes.Document})
   */
  public static void fixTableLink(Document doc) {
    fixBlockLink(doc, "tableblock", tablePattern);
  }

  private static void fixBlockLink(Document doc, String classAttrValue, Pattern pattern) {
    Elements elements = doc.getElementsByTag("a");
    for (Element link : elements) {
      String href = link.attr("href");
      if (href != null && href.startsWith("#")) {
        String id = href.substring(1);
        Element idElement = doc.getElementById(id);
        if (idElement != null && classAttributeContains(idElement, classAttrValue)) {
          fixLink(link, idElement, pattern);
        }
      }
    }
  }

  private static boolean classAttributeContains(Node element, String value) {
    String classAttr = element.attr("class");
    return classAttr != null && classAttr.contains(value);
  }

  private static boolean fixLink(Element link, Element element, Pattern pattern) {
    Element titleTag = findTitleTag(element);
    if (titleTag != null) {
      Matcher matcher = pattern.matcher(titleTag.text());
      if (matcher.find()) {
        link.text(matcher.group(1));
        return true;
      }
    }
    return false;
  }

  static void fixExternalLinks(Document doc) {
    Elements elements = doc.getElementsByTag("a");
    for (Element link : elements) {
      String href = link.attr("href");
      if (href != null && (href.startsWith("http://") || href.startsWith("https://") || href.startsWith("ftp://"))) {
        link.attr("target", "_blank");
      }
    }
  }

  private static Element findTitleTag(Element element) {
    Elements elements;
    elements = element.getElementsByTag("div");
    for (Element e : elements) {
      if ("title".equals(e.attr("class"))) {
        return e;
      }
    }
    elements = element.getElementsByTag("caption");
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
  public static String readAndFindFirstHeader(File file) throws IOException {
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

  public static List<FileAction> computeActions(File inputRootFolder, File outputRootFolder, boolean deleteNonExistingFolders) throws IOException {
    List<FileAction> result = new ArrayList<>();
    List<String> inputHtmlFiles = childHtmlFileNames(inputRootFolder);
    for (String fileName : inputHtmlFiles) {
      File inputFile = new File(inputRootFolder, fileName);
      File outputFile = new File(outputRootFolder, fileName);
      if (!outputFile.exists()) {
        addCopyActions(result, inputFile, outputFile);
      }
      else {
        if (hasModifications(inputFile, outputFile)) {
          addCopyActions(result, inputFile, outputFile);
        }
      }
    }

    List<String> outputHtmlFiles = childHtmlFileNames(outputRootFolder);
    for (String fileName : outputHtmlFiles) {
      if (!inputHtmlFiles.contains(fileName)) {
        addRemoveActions(result, new File(outputRootFolder, fileName));
      }
    }

    //recursive call for child folders:
    List<File> inputSubFolders = childDirectories(inputRootFolder);
    List<String> inputSubFolderNames = new ArrayList<>();
    for (File f : inputSubFolders) {
      inputSubFolderNames.add(f.getName());
      result.addAll(computeActions(f, new File(outputRootFolder, f.getName()), deleteNonExistingFolders));
    }

    if (deleteNonExistingFolders) {
      List<File> outSubFolders = childDirectories(outputRootFolder);
      for (File outputSubFolder : outSubFolders) {
        if (!inputSubFolderNames.contains(outputSubFolder.getName())) {
          result.add(FileAction.remove(outputSubFolder));
        }
      }
    }
    return result;
  }

  private static List<File> childDirectories(File folder) {
    File[] list = folder.listFiles(DIRECTORY_FILEFILTER);
    if (list == null) {
      return Collections.emptyList();
    }
    return Arrays.asList(list);
  }

  private static List<String> childHtmlFileNames(File folder) {
    String[] list = folder.list(HTML_FILENAME_FILTER);
    if (list == null) {
      return Collections.emptyList();
    }
    return Arrays.asList(list);
  }

  private static void addCopyActions(List<FileAction> list, File inputFile, File outputFile) {
    list.add(FileAction.copy(inputFile, outputFile));
    File inputPdfFile = companionFile(inputFile, "pdf");
    if (inputPdfFile.exists()) {
      File outputPdfFile = companionFile(outputFile, "pdf");
      list.add(FileAction.copy(inputPdfFile, outputPdfFile));
    }
    File inputZipFile = companionFile(inputFile, "zip");
    if (inputZipFile.exists()) {
      File outputZipFile = companionFile(outputFile, "zip");
      list.add(FileAction.copy(inputZipFile, outputZipFile));
    }
  }

  private static void addRemoveActions(List<FileAction> list, File outputFile) {
    list.add(FileAction.remove(outputFile));
    File outputPdfFile = companionFile(outputFile, "pdf");
    if (outputPdfFile.exists()) {
      list.add(FileAction.remove(outputPdfFile));
    }
    File outputZipFile = companionFile(outputFile, "zip");
    if (outputZipFile.exists()) {
      list.add(FileAction.remove(outputZipFile));
    }
  }

  public static void doActions(List<FileAction> actions) throws IOException {
    if (actions != null) {
      for (FileAction a : actions) {
        a.doAction();
      }
    }
  }

  /**
   * @param file
   * @param ext
   * @return
   */
  private static File companionFile(File file, String ext) {
    String name = Files.getNameWithoutExtension(file.getName()) + "." + ext;
    return new File(file.getParent(), name);
  }

//  private static void copyDocEntry(DocEntry input, File outputFile) throws IOException {
//    File pdfFrom = new File(input.getFolder(), input.getPdfSubPath());
//    File pdfTo = new File(outputFile, input.getPdfSubPath());
//    Files.createParentDirs(pdfTo);
//    Files.copy(pdfFrom, pdfTo);
//
//    File zipFrom = new File(input.getFolder(), input.getZipSubPath());
//    File zipTo = new File(outputFile, input.getZipSubPath());
//    Files.createParentDirs(zipTo);
//    Files.copy(zipFrom, zipTo);
//
//    File htmlFolderFrom = new File(input.getFolder(), input.getHtmlSubPath()).getParentFile();
//    File htmlFolderTo = new File(outputFile, input.getHtmlSubPath()).getParentFile();
//    Files.createParentDirs(htmlFolderTo);
//    copyRec(htmlFolderFrom, htmlFolderTo);
//  }

  public static void copyRec(File sourceLocation, File targetLocation) throws IOException {
    if (sourceLocation.isDirectory()) {
      if (!targetLocation.exists()) {
        targetLocation.mkdir();
      }

      List<String> sourceChildren = Arrays.asList(sourceLocation.list());
      for (String child : sourceChildren) {
        copyRec(new File(sourceLocation, child), new File(targetLocation, child));
      }
      for (String child : targetLocation.list()) {
        if (!sourceChildren.contains(child)) {
          File file = new File(targetLocation, child);
          if (!file.isDirectory()) {
            file.delete();
          }
        }
      }
    }
    else {
      Files.copy(sourceLocation, targetLocation);
    }
  }

  public static void deleteFilesRec(File targetLocation) throws IOException {
    if (targetLocation.exists()) {
      for (File file : targetLocation.listFiles()) {
        if (file.isDirectory()) {
          deleteFilesRec(file);
        }
        else {
          file.delete();
        }
      }
    }
  }

  public static boolean hasModifications(File file1, File file2) throws IOException {
    List<String> lines1 = Files.readLines(file1, Charsets.UTF_8);
    List<String> lines2 = Files.readLines(file2, Charsets.UTF_8);

    return hasModifications(lines1, lines2);
  }

  public static boolean hasModifications(List<String> lines1, List<String> lines2) {
    if (lines1.size() != lines2.size()) {
      return true;
    }
    for (int i = 0; i < lines1.size(); i++) {
      String l1 = lines1.get(i);
      String l2 = lines2.get(i);
      if (hasModifications(l1, l2)) {
        return true;
      }
    }

    return false;
  }

  static boolean hasModifications(String line1, String line2) {
    String trim1 = line1.trim();
    String trim2 = line2.trim();
    if (trim1.matches("<br /> Last updated .+") && trim2.matches("<br /> Last updated .+")) {
      return false;
    }
    return !line1.equals(line2);
  }

  /**
   * @param actions
   * @return
   */
  public static Collection<FileAction> computeParentFolders(List<FileAction> actions) {
    //Find all parent of the copy target:
    Collection<File> parentOfCreatedFiles = new HashSet<>();
    for (FileAction a : actions) {
      if (a.isCreateFile()) {
        parentOfCreatedFiles.add(a.getParentFile());
      }
    }

    Collection<FileAction> result = new HashSet<>();
    for (FileAction a : actions) {
      if (a.isCreateFile() || !parentOfCreatedFiles.contains(a.getParentFile())) {
        result.add(a.createActionWithParentFolder());
      }
    }
    return result;
  }
}
