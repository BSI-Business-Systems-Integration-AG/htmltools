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
package com.bsiag.geneclipsetoc.maven;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.bsiag.geneclipsetoc.internal.PublishUtility;
import com.bsiag.geneclipsetoc.internal.ZipUtility;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

@Mojo(name = "htmltools")
public class HtmltoolsMojo<V> extends AbstractMojo {

  private static final String INPUT_SOURCES = "inputSources";
  private static final String OUTPUT_FOLDER = "outputFolder";

  /**
   * Ordered list of HTLM pages.
   * If set, {@link #pagesListFile} can not be set.
   */
  @Parameter(property = INPUT_SOURCES)
  protected List<InputSource> inputSources;

  /**
   * Output folder.
   *
   * @parameter expression="${project.build.directory}/published-docs/"
   * @required
   */
  @Parameter(property = OUTPUT_FOLDER, defaultValue = "${project.build.directory}/published-docs")
  protected File outputFolder;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {

    if (outputFolder == null || outputFolder.isFile()) {
      throw new MojoFailureException("Error with the outputFolder.");
    }

    HashMap<HtmlOutput, List<File>> pageListMap = new HashMap<HtmlOutput, List<File>>();

    for (InputSource inputSource : inputSources) {
      if (inputSource.getInputFolder() == null || !inputSource.getInputFolder().isDirectory()) {
        throw new MojoFailureException("No input folder defined in a <" + InputSource.INPUT_SOURCE + ">, please add a <" + InputSource.INPUT_FOLDER + "> node in your configuration.");
      }
      if (inputSource.getHtmlOutput() == null && inputSource.getPdfOutput() == null) {
        throw new MojoFailureException("No output defined in a <" + InputSource.INPUT_SOURCE + ">, please add a <" + InputSource.HTML_OUTPUT + "> or <" + InputSource.PDF_OUTPUT + "> node in your configuration.");
      }
      if (inputSource.getHtmlOutput() != null) {
        HtmlOutput htmlOutput = inputSource.getHtmlOutput();
        if (htmlOutput.getCssReplacements() != null) {
          for (CssReplacement entry : htmlOutput.getCssReplacements()) {
            if (entry.getOriginalFileName() == null && entry.getOriginalFileName().isEmpty()) {
              throw new MojoFailureException("Wrong <" + CssReplacement.CSS_REPLACEMENT + "> entry, please add a <" + CssReplacement.ORIGINAL_FILE_NAME + "> in your configuration.");
            }
            if (entry.getReplacementFile() == null && !entry.getReplacementFile().isFile()) {
              throw new MojoFailureException("Wrong <" + CssReplacement.CSS_REPLACEMENT + "> entry: the file specified in <" + CssReplacement.REPLACEMENT_FILE + "> is not valid.");
            }
          }
        }
        if (htmlOutput.getPagesListFile() != null) {
          if (!htmlOutput.getPagesListFile().isFile() || !htmlOutput.getPagesListFile().canRead()) {
            throw new MojoFailureException("Problem with the file defined in the <" + HtmlOutput.PAGES_LIST_FILE + "> node.");
          }
          try {
            List<String> pageNames = Files.readLines(htmlOutput.getPagesListFile(), Charsets.UTF_8);
            List<File> pageFiles = new ArrayList<File>();
            for (String pageName : pageNames) {
              File pageFile = new File(inputSource.getInputFolder(), pageName);
              if (!pageFile.isFile() || !pageFile.canRead()) {
                throw new MojoFailureException("Problem with the file defined in the <" + HtmlOutput.PAGES_LIST_FILE + ">: " + pageFile.getAbsolutePath());
              }
              pageFiles.add(pageFile);
            }
            pageListMap.put(htmlOutput, pageFiles);
          }
          catch (IOException e) {
            throw new MojoFailureException("Error while reading the " + HtmlOutput.PAGES_LIST_FILE, e);
          }
        }
      }
    }

//    getLog().info("outputFolder: " + outputFolder);
//    for (int i = 0; i < inputSources.size(); i++) {
//      InputSource inputSource = inputSources.get(i);
//      getLog().info("inputSource " + (i + 1));
//      getLog().info("  - OutputSubFolder: " + inputSource.getOutputSubFolder());
//      getLog().info("  - InputFolder: " + inputSource.getInputFolder());
//      getLog().info("  - InputFolder (exists): " + inputSource.getInputFolder().exists());
//    }

    for (InputSource inputSource : inputSources) {
      File inputSourceOutputFolder = computeSubFolder(outputFolder, inputSource.getOutputSubFolder());

      if (inputSource.getHtmlOutput() != null) {
        HtmlOutput htmlOutput = inputSource.getHtmlOutput();

        File htmlOutputFolder = computeSubFolder(inputSourceOutputFolder, htmlOutput.getOutputSubFolder());

        Map<String, File> cssReplacementMap = new HashMap<String, File>();
        if (htmlOutput.getCssReplacements() != null) {
          for (CssReplacement entry : htmlOutput.getCssReplacements()) {
            cssReplacementMap.put(entry.getOriginalFileName(), entry.getReplacementFile());
          }
        }

        try {
          PublishUtility.publishHtmlFiles(inputSource.getInputFolder(), pageListMap.get(htmlOutput), htmlOutputFolder, cssReplacementMap);
          getLog().info("HTML InputSource <" + inputSource.getInputFolder().getAbsolutePath() + "> to " + htmlOutputFolder.getAbsolutePath());

          String outputZipFileName = htmlOutput.getOutputZipFileName();
          if (outputZipFileName != null && outputZipFileName.length() > 0) {
            File destZipFile = new File(htmlOutputFolder.getParentFile(), outputZipFileName);
            ZipUtility.zipFolder(htmlOutputFolder, destZipFile);
            getLog().info("Folder <" + htmlOutputFolder.getAbsolutePath() + "> zipped as " + destZipFile.getAbsolutePath());
          }
        }
        catch (IOException e) {
          new MojoFailureException("Could not publish inputSource <" + inputSource.getInputFolder().getAbsolutePath() + "> to HTML", e);
        }
      }

      if (inputSource.getPdfOutput() != null) {
        PdfOutput pdfOutput = inputSource.getPdfOutput();

        File pdfOutputFolder = computeSubFolder(inputSourceOutputFolder, pdfOutput.getOutputSubFolder());
        try {
          PublishUtility.publishPdfFiles(inputSource.getInputFolder(), pdfOutputFolder);
          getLog().info("PDF InputSource <" + inputSource.getInputFolder().getAbsolutePath() + "> to " + pdfOutputFolder.getAbsolutePath());
        }
        catch (IOException e) {
          new MojoFailureException("Could not publish inputSource <" + inputSource.getInputFolder().getAbsolutePath() + "> to PDF", e);
        }
      }
    }
  }

  private static File computeSubFolder(File folder, String subPath) {
    File inputSourceOutputFolder;
    if (subPath != null && subPath.length() > 0) {
      inputSourceOutputFolder = new File(folder, subPath);
    }
    else {
      inputSourceOutputFolder = folder;
    }
    return inputSourceOutputFolder;
  }

}
