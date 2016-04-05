package com.bsiag.htmltools.maven;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.bsiag.htmltools.internal.DocEntry;
import com.bsiag.htmltools.internal.IndexUtility;
import com.bsiag.htmltools.internal.PublishUtility;

@Mojo(name = "copydocs")
public class CopyDocsMojo extends AbstractMojo {

  private static final String INPUT_FOLDER = "inputFolder";
  private static final String INPUT_ZIP = "inputZip";
  private static final String OUTPUT_FOLDER = "outputFolder";

  @Parameter(property = INPUT_ZIP)
  protected String inputZip;

  @Parameter(property = INPUT_FOLDER, defaultValue = "${project.build.directory}/working-docs")
  protected File inputFolder;

  /**
   * Output folder.
   *
   * @parameter expression="${project.build.directory}/published-docs/"
   * @required
   */
  @Parameter(property = OUTPUT_FOLDER, defaultValue = "${project.build.directory}/copied-docs")
  protected File outputFolder;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (outputFolder == null || outputFolder.isFile()) {
      throw new MojoFailureException("Error with the outputFolder.");
    }

    if (inputFolder == null || inputFolder.isFile()) {
      throw new MojoFailureException("Error with the inputFolder.");
    }

    if (inputZip != null) {
//      URL resource = Resources.getResource(inputZip);
      getLog().info("Zip '" + inputZip + "' unzipped at: " + outputFolder.getAbsolutePath());
    }
    else {
      //TODO outputFolder should exist
    }

    try {
      List<DocEntry> input = IndexUtility.computeEntries(inputFolder);
      getLog().info("Found '" + input.size() + "' docs in: " + inputFolder.getAbsolutePath());

      List<DocEntry> output = IndexUtility.computeEntries(outputFolder);
      getLog().info("Found '" + output.size() + "' docs in: " + outputFolder.getAbsolutePath());

      PublishUtility.diffAndCopy(inputFolder, input, outputFolder, output);
    }
    catch (IOException e) {
      new MojoFailureException("IOExectpion in copydocs", e);
    }

  }
}
