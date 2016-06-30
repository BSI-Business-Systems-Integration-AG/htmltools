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
package com.bsiag.htmltools.maven;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.bsiag.htmltools.internal.FileAction;
import com.bsiag.htmltools.internal.PublishUtility;
import com.bsiag.htmltools.internal.ZipUtility;
import com.google.common.io.Resources;

@Mojo(name = "copydocs")
public class CopyDocsMojo extends AbstractMojo {

  private static final String INPUT_FOLDER = "inputFolder";
  private static final String INPUT_ZIP_URL = "inputZipUrl";
  private static final String INPUT_SUB_FOLDER = "inputSubFolder";
  private static final String OUTPUT_FOLDER = "outputFolder";

  @Parameter(property = INPUT_ZIP_URL)
  protected String inputZipUrl;

  @Parameter(property = INPUT_FOLDER, defaultValue = "${project.build.directory}/working-docs")
  protected File inputFolder;

  @Parameter(property = INPUT_SUB_FOLDER)
  protected String inputSubFolder;

  @Parameter(property = OUTPUT_FOLDER, defaultValue = "${project.build.directory}/copied-docs")
  protected File outputFolder;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    //Output Folder:
    if (outputFolder == null) {
      throw new MojoFailureException("outputFolder is undefined.");
    }
    else if (outputFolder.exists() && !outputFolder.isDirectory()) {
      throw new MojoFailureException("outputFolder is not a directory. " + outputFolder.getAbsolutePath());
    }

    //Input Folder:
    if (inputFolder == null) {
      throw new MojoFailureException("inputFolder is undefined.");
    }
    else if (inputFolder.exists() && !inputFolder.isDirectory()) {
      throw new MojoFailureException("inputFolder is not a directory. " + inputFolder.getAbsolutePath());
    }

    //If the ZipUrl is defined, the zip file will be extracted in the input folder:
    if (inputZipUrl != null) {
      URL zipFile;
      try {
        zipFile = new URL(inputZipUrl);
        InputStream zipInputStream = Resources.asByteSource(zipFile).openBufferedStream();

        ZipUtility.unzip(zipInputStream, inputFolder);
        getLog().info("Zip '" + inputZipUrl + "' unzipped at: " + inputFolder.getAbsolutePath());
      }
      catch (MalformedURLException e) {
        new MojoFailureException("MalformedURLException in copydocs (unzip)", e);
      }
      catch (IOException e) {
        new MojoFailureException("IOExectpion in copydocs (unzip)", e);
      }
    }

    //Input work folder:
    File inputWorkFolder;
    if (inputSubFolder != null) {
      inputWorkFolder = new File(inputFolder, inputSubFolder);
      if (inputWorkFolder.exists() && !inputWorkFolder.isDirectory()) {
        throw new MojoFailureException("inputWorkFolder is not a directory. " + inputFolder.getAbsolutePath());
      }
    }
    else {
      inputWorkFolder = inputFolder;
    }
    getLog().info("Working with the input folder: " + inputWorkFolder.getAbsolutePath());

    //Execute action:
    try {
      List<FileAction> actions = PublishUtility.computeActions(inputWorkFolder, outputFolder);
      getLog().info("Found '" + actions.size() + "' actions to perform in: " + outputFolder.getAbsolutePath());
      for (FileAction a : actions) {
        getLog().debug("Action : " + a.toString());
      }
      PublishUtility.doActions(actions);
      if (actions.size() > 0) {
        Collection<FileAction> parents = PublishUtility.computeParentFolders(actions);
        for (FileAction a : parents) {
          FileAction child = a.createActionWithSubFolder("images");
          getLog().info("Sync subfolder 'images': " + child.toString());
          a.doAction();
        }
      }
    }
    catch (IOException e) {
      new MojoFailureException("IOExectpion in copydocs", e);
    }

  }
}
