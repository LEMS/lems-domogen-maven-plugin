package org.lemsml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.lemsml.model.compiler.LEMSCompilerFrontend;
import org.lemsml.model.extended.ComponentType;
import org.lemsml.model.extended.Lems;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import org.stringtemplate.v4.StringRenderer;

import com.google.common.base.CaseFormat;

import expr_parser.utils.DirectedGraph;
import expr_parser.utils.TopologicalSort;

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

	private Lems domainDefs;
	private File baseDir;
	private STGroup typeSTG;
	private String pkgName;

	private LEMSCompilerFrontend compiler;

	public void execute() throws MojoExecutionException {
		getLog().info(
				MessageFormat.format(
						"Generating domain model classes for {0},\n"
								+ "\t defined in {1},\n" + "\t onto {2}",
						mlName, componentTypeDefs, outputDir));

		pkgName = "org." + mlName + ".model";
		baseDir = new File(outputDir, pkgName.replace(".", "/"));
		try {
			compiler = new LEMSCompilerFrontend(componentTypeDefs);
			domainDefs = compiler.generateLEMSDocument();
		} catch (Throwable e) {
			throw new MojoExecutionException(e.toString());
		}
		typeSTG = parseDomainTypeSTG();

		try {
			baseDir.mkdirs();
			createBaseDefinitions();
			generateDomainClasses();
			generateObjectFactory();
		} catch (Throwable e) {
			throw new MojoExecutionException(e.toString());
		}

		project.addCompileSourceRoot(baseDir.getAbsolutePath());

	}

	private void createBaseDefinitions() throws IOException {
		String fName = CaseFormat.LOWER_CAMEL
				.to(CaseFormat.UPPER_CAMEL, mlName) + ".java";
		getLog().info("\t" + fName);

		ST merged = mergeRootElement();
		dumpTemplateToFile(fName, merged);

	}

	private void generateObjectFactory() throws IOException {
		String fName = "ObjectFactory.java";

		URL stURL = getClass().getResource("/templates/obj_factory.stg");
		STGroup group = new STGroupFile(stURL, "UTF-8", '<', '>');
		group.registerRenderer(String.class, new StringRenderer());

		ST template = group.getInstanceOf("obj_factory");
		template.add("lems", domainDefs);
		template.add("package", pkgName);
		template.add("ml_name", mlName);
		dumpTemplateToFile(fName, template);

	}

	private void generateDomainClasses() throws IOException {
		for (ComponentType ct : domainDefs.getComponentTypes()) {
			String classFname = CaseFormat.LOWER_CAMEL.to(
					CaseFormat.UPPER_CAMEL, ct.getName()).replace(".", "_")
					+ ".java";

			getLog().info("\t" + classFname);

			ST merged = mergeCompTypeTemplate(ct);
			dumpTemplateToFile(classFname, merged);
		}
	}

	public ST mergeCompTypeTemplate(ComponentType ct) {
		ST template = typeSTG.getInstanceOf("class_file");

		DirectedGraph<ComponentType> typeGraph = TopologicalSort.reverseGraph(compiler.getSemanticAnalyser()
				.getTypeExtender().getVisitor().getTypeGraph());

		Map<String, Set<ComponentType>> typeDepsMap = new HashMap<String, Set<ComponentType>>();
		for (ComponentType node : typeGraph.getGraph().keySet()) {
			List<ComponentType> result = new ArrayList<ComponentType>();
			Set<ComponentType> visited = new HashSet<ComponentType>();
			Set<ComponentType> expanded = new HashSet<ComponentType>();
			TopologicalSort.explore(node, typeGraph, result, visited, expanded);
			typeDepsMap.put(node.getName(), new HashSet<ComponentType>(result));
		}

		template.add("type_deps", typeDepsMap);
		template.add("type", ct);
		template.add("ml_name", mlName);
		template.add("package", pkgName);

		return template;
	}

	public ST mergeRootElement() {
		URL stURL = getClass().getResource("/templates/root_element.stg");
		STGroup group = new STGroupFile(stURL, "UTF-8", '<', '>');
		group.registerRenderer(String.class, new StringRenderer());

		ST template = group.getInstanceOf("root_element");
		template.add("lems", domainDefs);
		template.add("ml_name", mlName);
		template.add("package", pkgName);

		return template;
	}

	private STGroup parseDomainTypeSTG() {
		URL stURL = getClass().getResource("/templates/domain_type.stg");
		STGroup group = new STGroupFile(stURL, "UTF-8", '<', '>');
		group.registerRenderer(String.class, new StringRenderer());
		return group;
	}

	public void dumpTemplateToFile(String fName, ST merged) throws IOException {
		FileWriter out = new FileWriter(new File(baseDir, fName));
		out.append(merged.render());
		out.flush();
		out.close();
	}

}