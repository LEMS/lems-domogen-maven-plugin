package org.lemsml;

import java.io.File;
import java.text.MessageFormat;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.lemsml.model.compiler.backend.JavaDomainModelBackend;

@Mojo(name = "generateDoMoClasses")
public class DomainModelMojo extends AbstractMojo {
	@Parameter(property = "generateDoMoClasses.mlName", defaultValue = "org.MyML.model")
	private String mlName;

	@Parameter(property = "generateDoMoClasses.outputDir", defaultValue = "${project.build.directory}/generated-sources/LEMS")
	private File outputDir;

	@Parameter(property = "generateDoMoClasses.componentTypeDefs", defaultValue = "${project.basedir}/src/main/resources/lems/MyML.xml")
	private File componentTypeDefs;

	@Parameter(property = "generateDoMoClasses.project", defaultValue = "${project}")
	private MavenProject project;

	private File baseDir;
	private String pkgName;

	public void execute() throws MojoExecutionException {
		getLog().info(
				MessageFormat.format(
						"Generating domain model classes for {0},\n"
								+ "\t defined in {1},\n" + "\t onto {2}",
						mlName, componentTypeDefs, outputDir));

		pkgName = "org." + mlName + ".model";
		baseDir = new File(outputDir, pkgName.replace(".", "/"));
		try {
			JavaDomainModelBackend be = new JavaDomainModelBackend(mlName,
					componentTypeDefs, outputDir);
			be.generate();
			project.addCompileSourceRoot(baseDir.getAbsolutePath());

		} catch (Throwable e) {
			throw new MojoExecutionException(e.toString());
		}

	}
}