package com.axonivy.ivy.toolkits.scripting.objects;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.*;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.axonivy.ivy.toolkits.scripting.objects.IvyFileBuilder;

import ch.ivyteam.ivy.scripting.objects.File;
import ch.ivyteam.ivy.scripting.objects.util.IIvyScriptObjectEnvironment;
import ch.ivyteam.ivy.scripting.objects.util.IvyScriptObjectEnvironment;

@RunWith(PowerMockRunner.class)
@PrepareForTest(IvyScriptObjectEnvironment.class)
public class IvyFileBuilderTest {

    private static final String MOCK_TEMPORARY_FILE_NAME = "temporaryNewFile.txt";
    private static final String MOCK_PERMANENT_FILE_NAME = "newFile.txt";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private java.io.File mockTemporaryFileZone;
    private java.io.File mockPermanentFileZone;

    @Before
    public void setUp() throws Exception {
    	mockTemporaryFileZone = temporaryFolder.newFolder("z", "temporary");
    	mockPermanentFileZone = temporaryFolder.newFolder("z", "permanent");
    	
        IIvyScriptObjectEnvironment mockEnvironment = mock(IIvyScriptObjectEnvironment.class);
        when(mockEnvironment.getFileArea()).thenReturn(mockPermanentFileZone);
        when(mockEnvironment.getTempFileArea()).thenReturn(mockTemporaryFileZone);

        mockStatic(IvyScriptObjectEnvironment.class);
        when(IvyScriptObjectEnvironment.getIvyScriptObjectEnvironment()).thenReturn(mockEnvironment);
        
    }

    @Test
    public void should_create_file_with_given_name() throws Exception {
        File newFile = IvyFileBuilder.forName(MOCK_PERMANENT_FILE_NAME)
                .storePermanently()
                .putDirectlyInTargetFolder()
                .getFile();
        assertEquals(mockPermanentFileZone + "\\" + MOCK_PERMANENT_FILE_NAME, newFile.getAbsolutePath());
    }

    @Test
    public void should_create_session_file_when_instructed() {
        File newFile = IvyFileBuilder.forName(MOCK_TEMPORARY_FILE_NAME)
                .thisSessionOnly()
                .putDirectlyInTargetFolder()
                .getFile();
        assertEquals(mockTemporaryFileZone + "\\" + MOCK_TEMPORARY_FILE_NAME, newFile.getAbsolutePath());
    }

    @Test
    public void should_create_empty_session_file() throws Exception {
        File newFile = IvyFileBuilder.empty()
        		.thisSessionOnly()
        		.getFile();
        
        String regexName = (mockTemporaryFileZone.getAbsolutePath()
                + "\\" + "[0-9a-f]+" + "\\" + "temp[0-9]+.temp").replace("\\", "\\\\");
        
        assertTrue(newFile.getAbsolutePath().matches(regexName));
    }
    
    
    @Test
	public void should_smartly_check_whether_a_java_file_is_actually_an_existing_session_ivy_file() throws Exception {
		java.io.File anExistingSessionJavaFile = new java.io.File(mockTemporaryFileZone, MOCK_TEMPORARY_FILE_NAME);
		anExistingSessionJavaFile.createNewFile();

		File newIvyFile = IvyFileBuilder.copyFromJavaFile(anExistingSessionJavaFile);
		assertThat(newIvyFile.getAbsolutePath(), CoreMatchers.is(anExistingSessionJavaFile.getAbsolutePath()));
	}
    
    @Test
	public void should_smartly_check_whether_a_java_file_is_actually_an_existing_permanent_ivy_file() throws Exception {
		java.io.File anExistingPermanentJavaFile = new java.io.File(mockPermanentFileZone, MOCK_PERMANENT_FILE_NAME);
		anExistingPermanentJavaFile.createNewFile();

		File newIvyFile = IvyFileBuilder.copyFromJavaFile(anExistingPermanentJavaFile);
		assertThat(newIvyFile.getAbsolutePath(), CoreMatchers.is(anExistingPermanentJavaFile.getAbsolutePath()));
	}
}
