package org.pegdown;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.mson.MSONSerializer;
import org.parboiled.Parboiled;
import org.parboiled.common.FileUtils;
import org.parboiled.common.Preconditions;
import org.pegdown.ast.RootNode;
import org.pegdown.plugins.PegDownPlugins;
import org.pegdown.plugins.ToHtmlSerializerPlugin;

public class MSONProcessor extends PegDownProcessor {

	public static MSONParser plugins = Parboiled.createParser(MSONParser.class);
	public static final Parser myParser = Parboiled.createParser(
			Parser.class, Extensions.ALL, 1000000l, Parser.DefaultParseRunnerProvider,
			new PegDownPlugins.Builder().withInlinePluginRules(plugins.inlinePluginRules())
			.withBlockPluginRules(plugins.blockPluginRules()).build()
			);

	public MSONProcessor() {
		super(plugins);//myParser);
		plugins.setInternalParser(myParser);
	}

	public static void main(String[] args) {
		MSONProcessor processor = new MSONProcessor();
//		processor.process("- `some:location`: local (string)".toCharArray());

		File dir = new File("src/test/mson-zoo/samples/");
//		File dir = new File("src/test/mson-zoo/samples/array-fixed-inline-samples.md");
//		File dir = new File("src/test/mson-zoo/samples/array-fixed-samples.md");
//		File dir = new File("src/test/mson-zoo/samples/array-fixed-samples-complex.md");
//		File dir = new File("src/test/mson-zoo/samples/array-of-arrays.md");
//		File dir = new File("src/test/mson-zoo/samples/description.md");		
		if(dir.isDirectory()) {
			for (File file : dir.listFiles()) {
				processor.processFile(file);
			}
		} else {
			processor.processFile(dir);			
		}
	}

	public void processFile(File file) {
		char[] markdown = FileUtils.readAllText(file).toCharArray();
		process(markdown);
	}
	
	public void process(char[] markdown) {
		System.out.println(markdown);
		Preconditions.checkNotNull(markdown, "benchmark file not found");
		String json = this.markdownToJson(markdown);
		System.out.println(json);
		System.out.println("________________________\n\n");		
	}

	public String markdownToJson(char[] markdown) {
		plugins.parsingStartTimeStamp = System.currentTimeMillis();
		RootNode root = parseMarkdown(markdown);
		
		List<ToHtmlSerializerPlugin> serializePlugins = Arrays
				.asList((ToHtmlSerializerPlugin) (new MSONSerializer()));

		ToJsonSerializer json = new ToJsonSerializer(new LinkRenderer(), serializePlugins);
		return json.toJson(root);
	}

}
