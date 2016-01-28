package com.grusade.mson;

import java.io.File;
import java.io.FilenameFilter;

import org.parboiled.common.FileUtils;
import org.pegdown.MSONProcessor;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class MSONProcessorTest 
    extends TestCase
{
    private String json;
	private MSONProcessor processor;
	private char[] markdown;
	
	static File dir = new File("src/test/resources");

	/**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public MSONProcessorTest( String testName )
    {
        super( testName );

    	this.processor = new MSONProcessor();
    }

    @Override
	protected void setUp() throws Exception {
    	super.setUp();
		// load the files using the name
		File jsonF = new File(dir, getName()+".json");
		this.json = FileUtils.readAllText(jsonF);
		File md = new File(dir, getName() +".md");
		System.out.println(md.getAbsolutePath());
		this.markdown = FileUtils.readAllText(md).toCharArray();
	}

    @Override
    protected void tearDown() throws Exception {
    	super.tearDown();
    }
    
	/**
     * @return the suite of tests being tested
     */
    public static TestSuite suite()
    {
    	TestSuite suite = new TestSuite();
    	
    	for (String file : dir.list(new FilenameFilter(){

			public boolean accept(File d, String name) {
				// TODO Auto-generated method stub
				return name.endsWith(".md");
			}})) {
        	String name = file.substring(0, file.length() - 3);

        	//load all the .md file that have a corresponding .json file
        	File j = new File(dir, name+".json");
        	if(j.exists()) {
        		suite.addTest(TestSuite.createTest(MSONProcessorTest.class, name));
        	}			
		}
        return suite;
    }

    /**
     * Test the files
     */
    public void testProcessor()
    {
    	//get json string from processing md
    	System.out.println(this.getName());
    	//get json string from file
        assertEquals(this.json, this.processor.markdownToJson(this.markdown));//file string == processed string
    }

    //instead of testing methods we are testing files parsing
	@Override
	protected void runTest() throws Throwable {
		testProcessor();
	}

}
